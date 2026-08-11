import { requestApi } from "@/services/api";
import type { User, UserRole } from "@/shared/types";

interface UserResponse extends User { updatedAt: string }
interface DriverResponse { id: string; userId: string; phone: string | null }

export interface SaveUserInput {
  fullName: string; email: string; role: UserRole; password?: string; phone?: string; status?: "ACTIVE" | "INACTIVE";
}

export async function listAdminUsers(): Promise<User[]> {
  const [users, drivers] = await Promise.all([
    requestApi<UserResponse[]>("/users"), requestApi<DriverResponse[]>("/drivers"),
  ]);
  const driverByUser = new Map(drivers.map((driver) => [driver.userId, driver]));
  return users.map((user) => ({ ...user, phone: driverByUser.get(user.id)?.phone ?? undefined }));
}

export async function createAdminUser(input: SaveUserInput): Promise<void> {
  if (input.role === "DRIVER" && !input.phone?.trim()) throw new Error("El teléfono es obligatorio para un motorizado.");
  const user = await requestApi<UserResponse>("/users", {
    method: "POST", body: JSON.stringify({ email: input.email, password: input.password, fullName: input.fullName, role: input.role }),
  });
  if (input.role === "DRIVER" && input.phone?.trim()) {
    const drivers = await requestApi<DriverResponse[]>("/drivers");
    const driver = drivers.find((item) => item.userId === user.id);
    if (!driver) throw new Error("El usuario fue creado, pero no se encontró su perfil de motorizado.");
    await requestApi<DriverResponse>(`/drivers/${driver.id}`, {
      method: "PUT", body: JSON.stringify({ phone: input.phone.trim() }),
    });
  }
  if (input.status === "INACTIVE") {
    await requestApi<UserResponse>(`/users/${user.id}/deactivate`, { method: "POST" });
  }
}

export async function updateAdminUser(id: string, input: SaveUserInput): Promise<void> {
  await requestApi<UserResponse>(`/users/${id}`, {
    method: "PUT", body: JSON.stringify({ fullName: input.fullName, role: input.role }),
  });
  if (input.role === "DRIVER") {
    if (!input.phone?.trim()) throw new Error("El teléfono es obligatorio para un motorizado.");
    const drivers = await requestApi<DriverResponse[]>("/drivers");
    const driver = drivers.find((item) => item.userId === id);
    if (!driver) throw new Error("El usuario DRIVER no tiene un perfil de motorizado asociado.");
    await requestApi<DriverResponse>(`/drivers/${driver.id}`, { method: "PUT", body: JSON.stringify({ phone: input.phone.trim() }) });
  }
}

export async function setAdminUserStatus(id: string, active: boolean): Promise<void> {
  await requestApi<UserResponse>(`/users/${id}/${active ? "activate" : "deactivate"}`, { method: "POST" });
}
