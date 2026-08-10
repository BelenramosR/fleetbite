export type OrderStatus =
  | "CREATED"
  | "CONFIRMED"
  | "PREPARING"
  | "READY"
  | "WAITING_FOR_DRIVER"
  | "ASSIGNED"
  | "PICKED_UP"
  | "IN_TRANSIT"
  | "DELIVERED"
  | "CANCELLED"
  | "FAILED_DELIVERY";

export type OrderPriority = "NORMAL" | "HIGH" | "CRITICAL";

export type SlaStatus = "ON_TIME" | "AT_RISK" | "BREACHED";

export interface Order {
  id: string;
  code: string;
  customerName: string;
  customerPhone: string;
  deliveryAddress: string;
  totalAmount: number;
  /** Legacy / otras pantallas; en Pedidos la prioridad se deriva del SLA. */
  priority: OrderPriority;
  status: OrderStatus;
  /** Legacy / otras pantallas; en Pedidos el SLA se calcula en vivo. */
  slaStatus: SlaStatus;
  slaMinutesRemaining: number;
  /** ISO 8601 con offset, p.ej. 2026-08-10T19:30:00-05:00 */
  promisedDeliveryAt: string;
  createdAt: string;
  /** ISO 8601; solo pedidos DELIVERED. */
  deliveredAt?: string;
  driverName?: string;
  driverId?: string;
}

export interface TimelineEvent {
  id: string;
  eventType: string;
  previousStatus?: OrderStatus;
  newStatus: OrderStatus;
  performedBy: string;
  createdAt: string;
  description: string;
}
