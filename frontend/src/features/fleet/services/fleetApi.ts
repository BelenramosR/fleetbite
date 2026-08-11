import { requestApi } from "@/services/api";
import type { Driver, Vehicle, VehicleStatus, VehicleType } from "@/shared/types";

interface VehicleResponse { id: string; plate: string; type: VehicleType; status: VehicleStatus }
interface DriverResponse {
  id: string; name: string; phone: string | null; status: "OFFLINE" | "AVAILABLE" | "BUSY";
  currentLatitude: number | null; currentLongitude: number | null; updatedAt: string;
  vehicleId: string | null; vehicle: { id: string; plate: string; type: VehicleType } | null;
}

export async function listFleetData(): Promise<{ vehicles: Vehicle[]; drivers: Driver[] }> {
  const [vehicleResponses, driverResponses] = await Promise.all([
    requestApi<VehicleResponse[]>("/vehicles"), requestApi<DriverResponse[]>("/drivers"),
  ]);
  const driverByVehicle = new Map(driverResponses.filter((driver) => driver.vehicleId).map((driver) => [driver.vehicleId!, driver]));
  const vehicles = vehicleResponses.map((vehicle) => {
    const driver = driverByVehicle.get(vehicle.id);
    const typeLabel = vehicle.type === "MOTORCYCLE" ? "Moto" : vehicle.type === "BICYCLE" ? "Bicicleta" : "Auto";
    return { ...vehicle, brand: typeLabel, model: "", driverId: driver?.id, driverName: driver?.name };
  });
  const drivers = driverResponses.map((driver) => ({
    id: driver.id, name: driver.name, phone: driver.phone ?? "Sin teléfono",
    status: driver.status === "BUSY" ? "ASSIGNED" as const : driver.status,
    vehicleType: driver.vehicle?.type ?? "MOTORCYCLE", vehiclePlate: driver.vehicle?.plate ?? "Sin vehículo",
    deliveriesToday: 0, lastLocationAt: driver.updatedAt, assignedVehicleId: driver.vehicleId ?? undefined,
  }));
  return { vehicles, drivers };
}

export async function saveVehicle(id: string | null, plate: string, type: VehicleType): Promise<void> {
  await requestApi<VehicleResponse>(id ? `/vehicles/${id}` : "/vehicles", {
    method: id ? "PUT" : "POST", body: JSON.stringify({ plate, type }),
  });
}
export async function deleteVehicle(id: string): Promise<void> {
  await requestApi<void>(`/vehicles/${id}`, { method: "DELETE" });
}
export async function setVehicleLifecycle(id: string, action: "maintenance" | "activate" | "deactivate"): Promise<void> {
  await requestApi<VehicleResponse>(`/vehicles/${id}/${action}`, { method: "POST" });
}
export async function assignVehicle(driverId: string, vehicleId: string): Promise<void> {
  await requestApi<DriverResponse>(`/drivers/${driverId}/vehicle`, { method: "PUT", body: JSON.stringify({ vehicleId }) });
}
export async function unassignVehicle(driverId: string): Promise<void> {
  await requestApi<DriverResponse>(`/drivers/${driverId}/vehicle`, { method: "DELETE" });
}
