import { requestApi } from "@/services/api";
import { RESTAURANT } from "@/shared/constants";
import type { Driver, DriverStatus } from "@/shared/types";

interface DriverResponse {
  id: string; name: string; phone: string | null; status: "OFFLINE" | "AVAILABLE" | "BUSY";
  currentLatitude: number | null; currentLongitude: number | null; updatedAt: string;
  vehicle: { plate: string; type: "MOTORCYCLE" | "BICYCLE" | "CAR" } | null;
}
interface AssignmentResponse {
  orderId: string; driverId: string; status: "PENDING" | "ACCEPTED" | "REJECTED" | "CANCELLED" | "COMPLETED";
  completedAt: string | null;
}
interface OrderResponse { id: string; code: string; status: string }

function distanceKm(latitude: number, longitude: number): number {
  const radians = (value: number) => value * Math.PI / 180;
  const dLat = radians(RESTAURANT.lat - latitude);
  const dLng = radians(RESTAURANT.lng - longitude);
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(radians(latitude)) * Math.cos(radians(RESTAURANT.lat)) * Math.sin(dLng / 2) ** 2;
  return 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function statusOf(driver: DriverResponse, order?: OrderResponse): DriverStatus {
  if (driver.status === "OFFLINE") return "OFFLINE";
  if (driver.status === "AVAILABLE") return "AVAILABLE";
  return order?.status === "IN_TRANSIT" ? "DELIVERING" : "ASSIGNED";
}

export async function listDispatchDrivers(): Promise<Driver[]> {
  const [drivers, assignments, orders] = await Promise.all([
    requestApi<DriverResponse[]>("/drivers"), requestApi<AssignmentResponse[]>("/assignments"), requestApi<OrderResponse[]>("/orders"),
  ]);
  const orderById = new Map(orders.map((order) => [order.id, order]));
  const today = new Date().toLocaleDateString("en-CA", { timeZone: "America/Lima" });
  return drivers.map((driver) => {
    const active = assignments.find((assignment) => assignment.driverId === driver.id && (assignment.status === "PENDING" || assignment.status === "ACCEPTED"));
    const activeOrder = active ? orderById.get(active.orderId) : undefined;
    const completedToday = assignments.filter((assignment) => assignment.driverId === driver.id && assignment.status === "COMPLETED" && assignment.completedAt && new Date(assignment.completedAt).toLocaleDateString("en-CA", { timeZone: "America/Lima" }) === today).length;
    return {
      id: driver.id, name: driver.name, phone: driver.phone ?? "Sin teléfono", status: statusOf(driver, activeOrder),
      vehicleType: driver.vehicle?.type ?? "MOTORCYCLE", vehiclePlate: driver.vehicle?.plate ?? "Sin vehículo",
      deliveriesToday: completedToday, lastLocationAt: driver.updatedAt,
      activeOrderId: activeOrder?.id, activeOrderCode: activeOrder?.code,
      currentLatitude: driver.currentLatitude ?? undefined, currentLongitude: driver.currentLongitude ?? undefined,
      distanceKm: driver.currentLatitude != null && driver.currentLongitude != null ? distanceKm(driver.currentLatitude, driver.currentLongitude) : undefined,
    };
  });
}

export async function setDispatchDriverAvailability(id: string, online: boolean): Promise<void> {
  await requestApi<DriverResponse>(`/drivers/${id}/${online ? "online" : "offline"}`, { method: "POST" });
}
