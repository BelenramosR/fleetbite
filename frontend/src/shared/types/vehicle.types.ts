export type VehicleType = "MOTORCYCLE" | "BICYCLE" | "CAR";

export type VehicleStatus = "AVAILABLE" | "IN_USE" | "MAINTENANCE" | "INACTIVE";

export interface Vehicle {
  id: string;
  plate: string;
  type: VehicleType;
  brand: string;
  model: string;
  status: VehicleStatus;
  driverName?: string;
  driverId?: string;
}
