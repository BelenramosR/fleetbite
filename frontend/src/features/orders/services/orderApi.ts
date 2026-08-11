import { requestApi } from "@/services/api";
import type { Driver, Order, OrderStatus, SlaStatus, TimelineEvent } from "@/shared/types";
import { RESTAURANT } from "@/shared/constants";

interface OrderResponse {
  id: string; code: string; customerName: string; customerPhone: string;
  deliveryAddress: string; deliveryLatitude: number; deliveryLongitude: number;
  totalAmount: number; priority: "NORMAL" | "HIGH"; status: OrderStatus;
  promisedDeliveryAt: string; createdAt: string; deliveredAt: string | null;
}
interface HistoryResponse {
  id: string; eventType: string; previousStatus: OrderStatus | null;
  newStatus: OrderStatus; description: string; createdAt: string;
}
interface AssignmentResponse { orderId: string; driverId: string; status: "PENDING" | "ACCEPTED" | string }
interface DriverResponse { id: string; name: string }
interface DriverListResponse extends DriverResponse {
  phone: string | null; status: "OFFLINE" | "AVAILABLE" | "BUSY";
  currentLatitude: number | null; currentLongitude: number | null; updatedAt: string;
  vehicle: { plate: string; type: "MOTORCYCLE" | "BICYCLE" | "CAR" } | null;
}
interface AutoAssignmentResponse { assigned: boolean; driverId: string | null; reason: string | null }
export interface CreateOrderInput {
  customerName: string; customerPhone: string; deliveryAddress: string;
  deliveryLatitude: number; deliveryLongitude: number; totalAmount: number;
}

function mapOrder(o: OrderResponse): Order {
  const minutes = Math.ceil((new Date(o.promisedDeliveryAt).getTime() - Date.now()) / 60_000);
  const slaStatus: SlaStatus = minutes < 0 ? "BREACHED" : minutes <= 20 ? "AT_RISK" : "ON_TIME";
  return {
    id: o.id, code: o.code, customerName: o.customerName, customerPhone: o.customerPhone,
    deliveryAddress: o.deliveryAddress, totalAmount: Number(o.totalAmount), priority: o.priority,
    status: o.status, slaStatus, slaMinutesRemaining: minutes,
    promisedDeliveryAt: o.promisedDeliveryAt, createdAt: o.createdAt,
    deliveredAt: o.deliveredAt ?? undefined,
  };
}

export async function listOrders(): Promise<Order[]> {
  return (await requestApi<OrderResponse[]>("/orders")).map(mapOrder);
}
export async function getOrder(id: string): Promise<Order> {
  return mapOrder(await requestApi<OrderResponse>(`/orders/${id}`));
}
export async function createOrder(input: CreateOrderInput): Promise<Order> {
  return mapOrder(await requestApi<OrderResponse>("/orders", {
    method: "POST", body: JSON.stringify(input),
  }));
}
export async function getOrderHistory(id: string): Promise<TimelineEvent[]> {
  return (await requestApi<HistoryResponse[]>(`/orders/${id}/history`)).map((e) => ({
    id: e.id, eventType: e.eventType, previousStatus: e.previousStatus ?? undefined,
    newStatus: e.newStatus, performedBy: "Sistema", createdAt: e.createdAt,
    description: e.description || historyLabel(e.eventType, e.newStatus),
  }));
}

function historyLabel(eventType: string, status: OrderStatus): string {
  const labels: Record<string, string> = {
    ORDER_CREATED: "Pedido creado", ORDER_CONFIRMED: "Pedido confirmado",
    ORDER_PREPARING: "Inicio de preparación", ORDER_READY: "Pedido listo para entrega",
    ORDER_WAITING_FOR_DRIVER: "Esperando repartidor",
    DRIVER_ASSIGNED: "Asignación pendiente de aceptación",
    ASSIGNMENT_ACCEPTED: "Pedido aceptado por el repartidor",
    ASSIGNMENT_REJECTED: "Asignación rechazada por el repartidor",
    ORDER_PICKED_UP: "Pedido recogido por el repartidor", ORDER_IN_TRANSIT: "Reparto iniciado",
    ORDER_DELIVERED: "Pedido entregado", ORDER_CANCELLED: "Pedido cancelado",
  };
  return labels[eventType] ?? status.replaceAll("_", " ");
}

export async function getAssignedDriverLabel(orderId: string): Promise<string | undefined> {
  try {
    const assignments = await requestApi<AssignmentResponse[]>("/assignments");
    const assignment = assignments.find((item) =>
      item.orderId === orderId && (item.status === "PENDING" || item.status === "ACCEPTED"));
    if (!assignment) return undefined;
    const driver = await requestApi<DriverResponse>(`/drivers/${assignment.driverId}`);
    return assignment.status === "PENDING" ? `Pendiente - ${driver.name}` : driver.name;
  } catch {
    return undefined;
  }
}
export async function transitionOrder(id: string, next: OrderStatus): Promise<Order> {
  const action: Partial<Record<OrderStatus, string>> = {
    CONFIRMED: "confirm", PREPARING: "start-preparation", READY: "ready",
  };
  const path = action[next];
  if (!path) throw new Error(`Transición no soportada: ${next}`);
  return mapOrder(await requestApi<OrderResponse>(`/orders/${id}/${path}`, { method: "POST" }));
}

function distanceKm(lat: number, lng: number): number {
  const radians = (value: number) => value * Math.PI / 180;
  const dLat = radians(RESTAURANT.lat - lat);
  const dLng = radians(RESTAURANT.lng - lng);
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(radians(lat)) * Math.cos(radians(RESTAURANT.lat)) * Math.sin(dLng / 2) ** 2;
  return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

export async function listAvailableDrivers(): Promise<Driver[]> {
  const drivers = await requestApi<DriverListResponse[]>("/drivers");
  return drivers.filter((driver) => driver.status === "AVAILABLE" && driver.currentLatitude != null && driver.currentLongitude != null && driver.vehicle != null)
    .map((driver) => ({
      id: driver.id, name: driver.name, phone: driver.phone ?? "Sin teléfono", status: "AVAILABLE",
      vehicleType: driver.vehicle!.type, vehiclePlate: driver.vehicle!.plate,
      deliveriesToday: 0, lastLocationAt: driver.updatedAt,
      currentLatitude: driver.currentLatitude!, currentLongitude: driver.currentLongitude!,
      distanceKm: distanceKm(driver.currentLatitude!, driver.currentLongitude!),
    }));
}

export async function assignOrderManually(orderId: string, driverId: string): Promise<void> {
  await requestApi<AssignmentResponse>(`/orders/${orderId}/assign`, {
    method: "POST", body: JSON.stringify({ driverId }),
  });
}

export async function assignOrderAutomatically(orderId: string): Promise<{ driverId: string; driverName: string }> {
  const result = await requestApi<AutoAssignmentResponse>(`/orders/${orderId}/auto-assign`, { method: "POST" });
  if (!result.assigned || !result.driverId) throw new Error("No hay repartidores disponibles para asignación automática.");
  const driver = await requestApi<DriverResponse>(`/drivers/${result.driverId}`);
  return { driverId: result.driverId, driverName: driver.name };
}
