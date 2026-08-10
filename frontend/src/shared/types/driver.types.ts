import type { VehicleType } from "./vehicle.types";

export type DriverStatus =
  | "AVAILABLE"
  | "ASSIGNED"
  | "DELIVERING"
  | "OFFLINE"
  | "SUSPENDED";

export interface Driver {
  id: string;
  name: string;
  phone: string;
  status: DriverStatus;
  vehicleType: VehicleType;
  vehiclePlate: string;
  activeOrderId?: string;
  activeOrderCode?: string;
  distanceKm?: number;
  deliveriesToday: number;
  lastLocationAt: string;
}
