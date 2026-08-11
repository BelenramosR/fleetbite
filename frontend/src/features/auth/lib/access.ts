import type { Page } from "@/app/router";
import type { UserRole } from "@/shared/types";

export interface SessionUser {
  id: string;
  fullName: string;
  email: string;
  role: UserRole;
  /** Solo rol DRIVER: identificador resuelto por /drivers/me. */
  driverId?: string;
}

export const PAGES_BY_ROLE: Record<UserRole, Page[]> = {
  DISPATCHER: ["dashboard", "orders", "order-detail", "drivers"],
  ADMIN: ["reports", "fleet", "users"],
  DRIVER: ["driver-assignment", "driver-profile"],
  RESTAURANT_OPERATOR: ["orders", "order-detail"],
};

export function homePageForRole(role: UserRole): Page {
  switch (role) {
    case "ADMIN":
      return "reports";
    case "DRIVER":
      return "driver-assignment";
    case "RESTAURANT_OPERATOR":
      return "orders";
    default:
      return "dashboard";
  }
}

export function canAccessPage(role: UserRole, page: Page): boolean {
  return PAGES_BY_ROLE[role].includes(page);
}
