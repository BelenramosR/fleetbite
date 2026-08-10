import type {
  DriverAssignment,
  DriverAssignmentStatus,
  DriverDayStats,
} from "@/shared/types/assignment.types";
import { RESTAURANT, coordsForAddress, haversineKm } from "@/shared/constants";
import { mockDrivers, mockOrders } from "@/services/api/mocks/mockData";

function delay(ms = 350) {
  return new Promise((r) => setTimeout(r, ms));
}

function distanceToStore(deliveryAddress: string): number {
  const [lat, lng] = coordsForAddress(deliveryAddress);
  return Number(
    haversineKm({ lat: RESTAURANT.lat, lng: RESTAURANT.lng }, { lat, lng }).toFixed(1),
  );
}

function buildFromOrder(
  assignmentId: string,
  driverId: string,
  orderId: string,
  status: DriverAssignmentStatus,
): DriverAssignment | null {
  const order = mockOrders.find((o) => o.id === orderId);
  if (!order) return null;
  return {
    id: assignmentId,
    driverId,
    status,
    distanceToStoreKm: distanceToStore(order.deliveryAddress),
    createdAt: order.createdAt,
    order: {
      id: order.id,
      code: order.code,
      customerName: order.customerName,
      customerPhone: order.customerPhone,
      deliveryAddress: order.deliveryAddress,
      totalAmount: order.totalAmount,
      priority: order.priority,
      slaMinutesRemaining: order.slaMinutesRemaining,
    },
  };
}

/** Estado mutable de demo (simula backend). */
const store: { byDriver: Record<string, DriverAssignment | null> } = {
  byDriver: {
    // Carlos — ya en camino (pasó el PENDING)
    "drv-001": buildFromOrder("asg-001", "drv-001", "ord-001", "IN_TRANSIT"),
    // Lucía — asignación PENDING por aceptar/rechazar
    "drv-002": buildFromOrder("asg-002", "drv-002", "ord-005", "PENDING"),
    "drv-003": null,
    "drv-005": null,
    "drv-006": null,
  },
};

/**
 * GET /api/v1/driver/assignments/active
 * Solo devuelve la asignación del driver autenticado.
 */
export async function fetchActiveAssignment(
  driverId: string,
): Promise<DriverAssignment | null> {
  await delay(280);
  const a = store.byDriver[driverId] ?? null;
  if (!a) return null;
  if (a.driverId !== driverId) return null;
  if (a.status === "REJECTED" || a.status === "COMPLETED" || a.status === "FAILED") {
    return null;
  }
  return { ...a, order: { ...a.order } };
}

/** POST /api/v1/driver/assignments/{id}/accept */
export async function acceptAssignment(
  driverId: string,
  assignmentId: string,
): Promise<DriverAssignment> {
  await delay(400);
  const a = store.byDriver[driverId];
  if (!a || a.id !== assignmentId || a.driverId !== driverId) {
    throw new Error("Asignación no encontrada o no te pertenece");
  }
  if (a.status !== "PENDING") {
    throw new Error("La asignación ya no está pendiente");
  }
  a.status = "ACCEPTED";
  return { ...a, order: { ...a.order } };
}

/** POST /api/v1/driver/assignments/{id}/reject */
export async function rejectAssignment(
  driverId: string,
  assignmentId: string,
): Promise<void> {
  await delay(400);
  const a = store.byDriver[driverId];
  if (!a || a.id !== assignmentId || a.driverId !== driverId) {
    throw new Error("Asignación no encontrada o no te pertenece");
  }
  if (a.status !== "PENDING") {
    throw new Error("La asignación ya no está pendiente");
  }
  a.status = "REJECTED";
  store.byDriver[driverId] = null;
}

export async function advanceAssignment(
  driverId: string,
  next: Extract<DriverAssignmentStatus, "PICKED_UP" | "IN_TRANSIT" | "COMPLETED" | "FAILED">,
): Promise<DriverAssignment | null> {
  await delay(350);
  const a = store.byDriver[driverId];
  if (!a || a.driverId !== driverId) {
    throw new Error("Asignación no encontrada o no te pertenece");
  }
  a.status = next;
  if (next === "COMPLETED" || next === "FAILED") {
    store.byDriver[driverId] = null;
    return null;
  }
  return { ...a, order: { ...a.order } };
}

export async function fetchDriverDayStats(driverId: string): Promise<DriverDayStats> {
  await delay(200);
  const driver = mockDrivers.find((d) => d.id === driverId);
  const active = store.byDriver[driverId];
  const completed = driver?.deliveriesToday ?? 0;
  return {
    deliveriesCompleted: completed,
    deliveriesFailed: driverId === "drv-001" ? 0 : 0,
    activeMinutes: active ? 42 : 18,
    earningsEstimate: Number((completed * 8.5 + (active ? 4 : 0)).toFixed(1)),
    acceptanceRate: 94,
    isAvailable: driver?.status === "AVAILABLE" || Boolean(active),
  };
}

/** Demo: inyecta una PENDING si el driver está libre (para probar el aviso). */
export async function simulateNewPending(driverId: string): Promise<DriverAssignment | null> {
  await delay(200);
  const current = store.byDriver[driverId];
  if (current && current.status !== "REJECTED") return current;

  const freeOrder = mockOrders.find(
    (o) =>
      (o.status === "WAITING_FOR_DRIVER" || o.status === "READY") && !o.driverId,
  );
  if (!freeOrder) return null;

  const assignment = buildFromOrder(
    `asg-sim-${Date.now()}`,
    driverId,
    freeOrder.id,
    "PENDING",
  );
  store.byDriver[driverId] = assignment;
  return assignment ? { ...assignment, order: { ...assignment.order } } : null;
}
