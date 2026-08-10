import type { Order, OrderPriority, SlaStatus } from "@/shared/types";

/** Resultado visual/operativo del SLA. Independiente de la fuente de datos (mock o HTTP). */
export interface SlaResult {
  label: string;
  priority: OrderPriority;
  status: SlaStatus;
}

type SlaOrder = Pick<Order, "status" | "promisedDeliveryAt"> & {
  deliveredAt?: string;
};

function minutesBetween(fromMs: number, toMs: number): number {
  return Math.ceil((toMs - fromMs) / 60_000);
}

/**
 * Calcula el SLA de un pedido respecto a `currentTime`.
 * Usa solo `promisedDeliveryAt` (+ `deliveredAt` si ya se entregó).
 */
export function calculateSla(order: SlaOrder, currentTime: Date): SlaResult {
  if (order.status === "CANCELLED") {
    return { label: "Cancelado", priority: "NORMAL", status: "ON_TIME" };
  }

  if (order.status === "FAILED_DELIVERY") {
    return { label: "Entrega fallida", priority: "CRITICAL", status: "BREACHED" };
  }

  const promisedMs = new Date(order.promisedDeliveryAt).getTime();

  if (order.status === "DELIVERED") {
    const deliveredMs = order.deliveredAt
      ? new Date(order.deliveredAt).getTime()
      : promisedMs;
    if (deliveredMs <= promisedMs) {
      return { label: "A tiempo", priority: "NORMAL", status: "ON_TIME" };
    }
    const late = minutesBetween(promisedMs, deliveredMs);
    return {
      label: `+${late}m tarde`,
      priority: "CRITICAL",
      status: "BREACHED",
    };
  }

  const nowMs = currentTime.getTime();
  const remaining = minutesBetween(nowMs, promisedMs);

  if (remaining <= 0) {
    const late = minutesBetween(promisedMs, nowMs);
    return {
      label: `+${Math.max(1, late)}m tarde`,
      priority: "CRITICAL",
      status: "BREACHED",
    };
  }

  if (remaining > 30) {
    return { label: `${remaining}m`, priority: "NORMAL", status: "ON_TIME" };
  }

  if (remaining >= 20) {
    return { label: `${remaining}m`, priority: "HIGH", status: "AT_RISK" };
  }

  return { label: `${remaining}m`, priority: "CRITICAL", status: "AT_RISK" };
}

/** Tono visual del badge SLA a partir del pedido y del resultado de calculateSla. */
export type SlaTone = "green" | "yellow" | "orange" | "red" | "neutral";

export function slaTone(order: Pick<Order, "status">, sla: SlaResult): SlaTone {
  if (order.status === "CANCELLED") return "neutral";
  if (order.status === "FAILED_DELIVERY") return "red";
  if (sla.status === "BREACHED") return "red";
  if (sla.priority === "CRITICAL") return "orange";
  if (sla.priority === "HIGH") return "yellow";
  return "green";
}

/** Pedido activo cuyo SLA pide atención (amarillo/naranja/rojo). */
export function isSlaAtRisk(order: SlaOrder, currentTime: Date): boolean {
  if (
    order.status === "DELIVERED" ||
    order.status === "CANCELLED" ||
    order.status === "FAILED_DELIVERY"
  ) {
    return false;
  }
  const sla = calculateSla(order, currentTime);
  return sla.status === "AT_RISK" || sla.status === "BREACHED";
}
