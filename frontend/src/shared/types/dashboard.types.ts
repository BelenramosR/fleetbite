export interface DashboardMetrics {
  activeOrders: number;
  waitingForDriver: number;
  inTransit: number;
  slaAtRisk: number;
  slaBreach: number;
  availableDrivers: number;
  busyDrivers: number;
  avgAssignmentMinutes: number;
  avgDeliveryMinutes: number;
  slaCompliancePercent: number;
  fleetUtilizationPercent: number;
}
