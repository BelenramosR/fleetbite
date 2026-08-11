import { ApiClientError, requestApi } from "@/services/api";
import type { Driver, DriverAssignment, DriverAssignmentStatus, DriverDayStats } from "@/shared/types";

interface DriverProfileResponse {
  id: string; userId: string; name: string; phone: string | null;
  status: "OFFLINE" | "AVAILABLE" | "BUSY";
  currentLatitude: number | null; currentLongitude: number | null;
  vehicleId: string | null;
  vehicle: { id: string; plate: string; type: "MOTORCYCLE" | "CAR" | "BICYCLE" } | null;
  updatedAt: string;
}

interface AssignmentResponse {
  id: string; orderId: string; driverId: string;
  status: "PENDING" | "ACCEPTED" | "REJECTED" | "CANCELLED" | "COMPLETED";
  pickedUpAt: string | null; assignmentScore: number | null; createdAt: string;
}

interface OrderResponse {
  id: string; code: string; customerName: string; customerPhone: string;
  deliveryAddress: string; totalAmount: number; priority: "NORMAL" | "HIGH";
  status: string; promisedDeliveryAt: string;
}

interface ActiveAssignmentResponse { assignment: AssignmentResponse; order: OrderResponse }

function mapDriver(profile: DriverProfileResponse): Driver {
  return {
    id: profile.id, name: profile.name, phone: profile.phone ?? "Sin teléfono",
    status: profile.status === "BUSY" ? "ASSIGNED" : profile.status,
    vehicleType: profile.vehicle?.type ?? "MOTORCYCLE",
    vehiclePlate: profile.vehicle?.plate ?? "Sin vehículo",
    deliveriesToday: 0, lastLocationAt: profile.updatedAt,
    currentLatitude: profile.currentLatitude ?? undefined,
    currentLongitude: profile.currentLongitude ?? undefined,
  };
}

function mapStatus(a: AssignmentResponse, o: OrderResponse): DriverAssignmentStatus {
  if (a.status === "COMPLETED") return "COMPLETED";
  if (a.status === "REJECTED" || a.status === "CANCELLED") return "REJECTED";
  if (a.status === "PENDING") return "PENDING";
  if (o.status === "IN_TRANSIT") return "IN_TRANSIT";
  return a.pickedUpAt ? "PICKED_UP" : "ACCEPTED";
}

function mapAssignment({ assignment, order }: ActiveAssignmentResponse): DriverAssignment {
  return {
    id: assignment.id, driverId: assignment.driverId, status: mapStatus(assignment, order),
    distanceToStoreKm: assignment.assignmentScore ?? 0, createdAt: assignment.createdAt,
    order: {
      id: order.id, code: order.code, customerName: order.customerName,
      customerPhone: order.customerPhone, deliveryAddress: order.deliveryAddress,
      totalAmount: Number(order.totalAmount), priority: order.priority,
      slaMinutesRemaining: Math.ceil((new Date(order.promisedDeliveryAt).getTime() - Date.now()) / 60_000),
    },
  };
}

export async function getMyDriverProfile(): Promise<Driver> {
  return mapDriver(await requestApi<DriverProfileResponse>("/drivers/me"));
}
export async function setMyAvailability(online: boolean): Promise<Driver> {
  return mapDriver(await requestApi<DriverProfileResponse>(`/drivers/me/${online ? "online" : "offline"}`, { method: "POST" }));
}
export async function updateMyLocation(latitude: number, longitude: number): Promise<Driver> {
  return mapDriver(await requestApi<DriverProfileResponse>("/drivers/me/location", {
    method: "PATCH", body: JSON.stringify({ latitude, longitude }),
  }));
}
export async function getMyActiveAssignment(): Promise<DriverAssignment | null> {
  try { return mapAssignment(await requestApi<ActiveAssignmentResponse>("/driver/assignments/active")); }
  catch (cause) {
    if (cause instanceof ApiClientError && cause.status === 404) return null;
    throw cause;
  }
}
async function action(id: string, name: string, body?: object): Promise<void> {
  await requestApi<AssignmentResponse>(`/driver/assignments/${id}/${name}`, {
    method: "POST", body: body ? JSON.stringify(body) : undefined,
  });
}
export const acceptMyAssignment = (id: string) => action(id, "accept");
export const rejectMyAssignment = (id: string, reason: string) => action(id, "reject", { reason });
export const pickupMyAssignment = (id: string) => action(id, "pickup");
export const startMyDelivery = (id: string) => action(id, "start-delivery");
export const completeMyAssignment = (id: string) => action(id, "complete");

interface AssignmentSummaryResponse {
  deliveriesCompletedToday: number;
  assignmentsAccepted: number;
  assignmentsRejected: number;
  acceptanceRate: number;
}

export async function getMyAssignmentSummary(): Promise<DriverDayStats> {
  const summary = await requestApi<AssignmentSummaryResponse>("/driver/assignments/summary");
  return {
    deliveriesCompleted: summary.deliveriesCompletedToday,
    deliveriesFailed: 0,
    activeMinutes: 0,
    earningsEstimate: 0,
    acceptanceRate: summary.acceptanceRate,
    isAvailable: false,
  };
}
