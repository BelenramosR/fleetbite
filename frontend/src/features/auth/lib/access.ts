import type { Page } from "@/app/router";
import type { User, UserRole } from "@/shared/types";
import { mockUsers } from "@/services/api/mocks/mockData";

export interface SessionUser {
  id: string;
  fullName: string;
  email: string;
  role: UserRole;
  /** Solo rol DRIVER: vínculo a mockDrivers. */
  driverId?: string;
}

const DRIVER_EMAIL_TO_ID: Record<string, string> = {
  "driver1@fleetbite.local": "drv-001",
  "driver2@fleetbite.local": "drv-002",
  "driver3@fleetbite.local": "drv-003",
  "driver5@fleetbite.local": "drv-005",
  "driver6@fleetbite.local": "drv-006",
};

/** Páginas permitidas por rol (sin “Vista motorizado”). */
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

export function toSessionUser(user: User): SessionUser {
  return {
    id: user.id,
    fullName: user.fullName,
    email: user.email,
    role: user.role,
    driverId: user.role === "DRIVER" ? DRIVER_EMAIL_TO_ID[user.email] : undefined,
  };
}

export function resolveSessionByEmail(email: string): SessionUser | null {
  const normalized = email.trim().toLowerCase();
  const user = mockUsers.find((u) => u.email.toLowerCase() === normalized && u.status === "ACTIVE");
  if (!user) return null;
  return toSessionUser(user);
}
