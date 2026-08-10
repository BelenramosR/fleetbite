export type Page =
  | "login"
  | "dashboard"
  | "orders"
  | "order-detail"
  | "drivers"
  | "fleet"
  | "reports"
  | "users"
  | "settings"
  | "driver-assignment"
  | "driver-profile";

/** Cola de pedidos a la que se puede entrar directamente desde otra pantalla. */
export type OrdersFilter =
  | "all"
  | "active"
  | "unassigned"
  | "sla-risk"
  | "in-transit"
  | "delivered"
  | "cancelled";

export interface NavState {
  page: Page;
  selectedOrderId?: string;
  ordersFilter?: OrdersFilter;
}

export type NavExtra = Partial<Omit<NavState, "page">>;
