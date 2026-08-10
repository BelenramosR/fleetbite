export type DriverAssignmentStatus =
  | "PENDING"
  | "ACCEPTED"
  | "REJECTED"
  | "PICKED_UP"
  | "IN_TRANSIT"
  | "COMPLETED"
  | "FAILED";

export interface DriverAssignmentOrderSummary {
  id: string;
  code: string;
  customerName: string;
  customerPhone: string;
  deliveryAddress: string;
  totalAmount: number;
  priority: "NORMAL" | "HIGH" | "CRITICAL";
  slaMinutesRemaining: number;
}

export interface DriverAssignment {
  id: string;
  driverId: string;
  status: DriverAssignmentStatus;
  distanceToStoreKm: number;
  createdAt: string;
  order: DriverAssignmentOrderSummary;
}

export interface DriverDayStats {
  deliveriesCompleted: number;
  deliveriesFailed: number;
  activeMinutes: number;
  earningsEstimate: number;
  acceptanceRate: number;
  isAvailable: boolean;
}
