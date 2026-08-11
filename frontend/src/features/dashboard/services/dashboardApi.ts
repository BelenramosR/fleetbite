import { requestApi } from "@/services/api";
import { listOrders } from "@/features/orders/services/orderApi";
import { listDispatchDrivers } from "@/features/drivers/services/dispatchDriverApi";
import { calculateSla } from "@/features/orders/lib/calculateSla";
import type { DashboardMetrics, Order, OrderStatus } from "@/shared/types";

interface AssignmentResponse {
  status: string; assignedAt: string; acceptedAt: string | null; completedAt: string | null;
}

const ACTIVE: OrderStatus[] = ["CREATED", "CONFIRMED", "PREPARING", "READY", "WAITING_FOR_DRIVER", "ASSIGNED", "PICKED_UP", "IN_TRANSIT"];

function average(values: number[]): number {
  return values.length ? Math.round(values.reduce((sum, value) => sum + value, 0) / values.length * 10) / 10 : 0;
}

export async function getDashboardData(): Promise<{ metrics: DashboardMetrics; orders: Order[] }> {
  const [orders, drivers, assignments] = await Promise.all([
    listOrders(), listDispatchDrivers(), requestApi<AssignmentResponse[]>("/assignments"),
  ]);
  const now = new Date();
  const today = now.toLocaleDateString("en-CA", { timeZone: "America/Lima" });
  const isToday = (iso: string | null | undefined) => Boolean(iso) && new Date(iso!).toLocaleDateString("en-CA", { timeZone: "America/Lima" }) === today;
  const activeOrders = orders.filter((order) => ACTIVE.includes(order.status));
  const sla = activeOrders.map((order) => calculateSla(order, now));
  const completedToday = assignments.filter((assignment) => assignment.status === "COMPLETED" && isToday(assignment.completedAt));
  const assignmentMinutes = assignments.filter((assignment) => assignment.acceptedAt && isToday(assignment.acceptedAt))
    .map((assignment) => (new Date(assignment.acceptedAt!).getTime() - new Date(assignment.assignedAt).getTime()) / 60_000);
  const deliveryMinutes = completedToday.filter((assignment) => assignment.acceptedAt)
    .map((assignment) => (new Date(assignment.completedAt!).getTime() - new Date(assignment.acceptedAt!).getTime()) / 60_000);
  const deliveredToday = orders.filter((order) => order.status === "DELIVERED" && isToday(order.deliveredAt));
  const onTime = deliveredToday.filter((order) => new Date(order.deliveredAt!).getTime() <= new Date(order.promisedDeliveryAt).getTime()).length;
  const availableDrivers = drivers.filter((driver) => driver.status === "AVAILABLE").length;
  const busyDrivers = drivers.filter((driver) => driver.status === "ASSIGNED" || driver.status === "DELIVERING").length;
  return {
    orders,
    metrics: {
      activeOrders: activeOrders.length,
      waitingForDriver: activeOrders.filter((order) => order.status === "WAITING_FOR_DRIVER" || order.status === "READY").length,
      inTransit: activeOrders.filter((order) => order.status === "IN_TRANSIT").length,
      slaAtRisk: sla.filter((item) => item.status === "AT_RISK").length,
      slaBreach: sla.filter((item) => item.status === "BREACHED").length,
      availableDrivers, busyDrivers,
      avgAssignmentMinutes: average(assignmentMinutes), avgDeliveryMinutes: average(deliveryMinutes),
      slaCompliancePercent: deliveredToday.length ? Math.round(onTime * 100 / deliveredToday.length) : 100,
      fleetUtilizationPercent: availableDrivers + busyDrivers ? Math.round(busyDrivers * 100 / (availableDrivers + busyDrivers)) : 0,
    },
  };
}
