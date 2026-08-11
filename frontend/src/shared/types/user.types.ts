export type UserRole = "ADMIN" | "RESTAURANT_OPERATOR" | "DISPATCHER" | "DRIVER";

export interface User {
  id: string;
  fullName: string;
  email: string;
  role: UserRole;
  status: "ACTIVE" | "INACTIVE";
  createdAt: string;
  phone?: string;
}
