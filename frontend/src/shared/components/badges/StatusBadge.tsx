import { AlertTriangle, X } from "lucide-react";
import type { OrderStatus, DriverStatus, SlaStatus, OrderPriority } from "@/shared/types";

const ORDER_STATUS_CONFIG: Record<
  OrderStatus,
  { label: string; bg: string; text: string; dot: string }
> = {
  CREATED: { label: "Creado", bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  CONFIRMED: { label: "Confirmado", bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  PREPARING: { label: "Preparando", bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  READY: { label: "Listo", bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  WAITING_FOR_DRIVER: { label: "Sin repartidor", bg: "#fff7ed", text: "#c2410c", dot: "#f97316" },
  ASSIGNED: { label: "Asignado", bg: "#f1f5f9", text: "#64748b", dot: "#94a3b8" },
  PICKED_UP: { label: "Recogido", bg: "#f1f5f9", text: "#64748b", dot: "#94a3b8" },
  IN_TRANSIT: { label: "En camino", bg: "#f1f5f9", text: "#64748b", dot: "#94a3b8" },
  DELIVERED: { label: "Entregado", bg: "#f0fdf4", text: "#15803d", dot: "#16a34a" },
  CANCELLED: { label: "Cancelado", bg: "#f1f5f9", text: "#64748b", dot: "#94a3b8" },
  FAILED_DELIVERY: { label: "Fallido", bg: "#f1f5f9", text: "#64748b", dot: "#94a3b8" },
};

/** Paleta para tablas densas: ops azulino · driver gris · sin repartidor naranja · entregado verde. */
const ORDER_STATUS_MUTED: Record<
  OrderStatus,
  { label: string; bg: string; text: string; dot: string }
> = {
  CREATED: { label: "Creado", bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  CONFIRMED: { label: "Confirmado", bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  PREPARING: { label: "Preparando", bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  READY: { label: "Listo", bg: "#eff6ff", text: "#1d4ed8", dot: "#3b82f6" },
  WAITING_FOR_DRIVER: { label: "Sin repartidor", bg: "#fff7ed", text: "#9a3412", dot: "#ea580c" },
  ASSIGNED: { label: "Asignado", bg: "#f8fafc", text: "#64748b", dot: "#94a3b8" },
  PICKED_UP: { label: "Recogido", bg: "#f8fafc", text: "#64748b", dot: "#94a3b8" },
  IN_TRANSIT: { label: "En camino", bg: "#f8fafc", text: "#64748b", dot: "#94a3b8" },
  DELIVERED: { label: "Entregado", bg: "#f0fdf4", text: "#166534", dot: "#16a34a" },
  CANCELLED: { label: "Cancelado", bg: "#f8fafc", text: "#64748b", dot: "#94a3b8" },
  FAILED_DELIVERY: { label: "Fallido", bg: "#f8fafc", text: "#64748b", dot: "#94a3b8" },
};

const DRIVER_STATUS_CONFIG: Record<
  DriverStatus,
  { label: string; bg: string; text: string; dot: string }
> = {
  AVAILABLE: { label: "Disponible", bg: "#f0fdf4", text: "#15803d", dot: "#16a34a" },
  ASSIGNED: { label: "Asignado", bg: "#eef2ff", text: "#4338ca", dot: "#6366f1" },
  DELIVERING: { label: "En entrega", bg: "#fffbeb", text: "#b45309", dot: "#f59e0b" },
  OFFLINE: { label: "Fuera de línea", bg: "#f1f5f9", text: "#64748b", dot: "#94a3b8" },
  SUSPENDED: { label: "Suspendido", bg: "#fef2f2", text: "#b91c1c", dot: "#ef4444" },
};

const SLA_TONE: Record<
  "green" | "yellow" | "orange" | "red" | "neutral",
  { bg: string; text: string }
> = {
  green: { bg: "#f0fdf4", text: "#15803d" },
  yellow: { bg: "#fefce8", text: "#a16207" },
  orange: { bg: "#fff7ed", text: "#c2410c" },
  red: { bg: "#fef2f2", text: "#b91c1c" },
  neutral: { bg: "#f1f5f9", text: "#64748b" },
};

const SLA_FROM_STATUS: Record<SlaStatus, keyof typeof SLA_TONE> = {
  ON_TIME: "green",
  AT_RISK: "yellow",
  BREACHED: "red",
};

const PRIORITY_CONFIG: Record<OrderPriority, { label: string; text: string }> = {
  NORMAL: { label: "NORMAL", text: "#9ca3af" },
  HIGH: { label: "HIGH", text: "#ca8a04" },
  CRITICAL: { label: "CRITICAL", text: "#dc2626" },
};

export function OrderStatusBadge({
  status,
  muted = false,
}: {
  status: OrderStatus;
  /** Variante calmada para tablas densas (Pedidos). */
  muted?: boolean;
}) {
  const cfg = muted ? ORDER_STATUS_MUTED[status] : ORDER_STATUS_CONFIG[status];
  return (
    <span
      className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-xs font-mono font-medium tracking-wide"
      style={{ background: cfg.bg, color: cfg.text }}
    >
      <span className="w-1.5 h-1.5 rounded-full shrink-0" style={{ background: cfg.dot }} />
      {cfg.label}
    </span>
  );
}

export function DriverStatusBadge({ status }: { status: DriverStatus }) {
  const cfg = DRIVER_STATUS_CONFIG[status];
  return (
    <span
      className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-xs font-mono font-medium"
      style={{ background: cfg.bg, color: cfg.text }}
    >
      <span className="w-1.5 h-1.5 rounded-full shrink-0" style={{ background: cfg.dot }} />
      {cfg.label}
    </span>
  );
}

export function SlaBadge({
  status,
  minutesRemaining,
  label,
  tone,
}: {
  status: SlaStatus;
  minutesRemaining?: number;
  /** Texto ya resuelto (p.ej. desde calculateSla). */
  label?: string;
  tone?: keyof typeof SLA_TONE;
}) {
  const resolvedTone = tone ?? SLA_FROM_STATUS[status];
  const cfg = SLA_TONE[resolvedTone];
  const display =
    label ??
    (status === "BREACHED"
      ? `+${Math.abs(minutesRemaining ?? 0)}m tarde`
      : `${minutesRemaining ?? 0}m`);

  return (
    <span
      className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-mono font-medium"
      style={{ background: cfg.bg, color: cfg.text }}
    >
      {resolvedTone === "orange" || resolvedTone === "yellow" ? (
        <AlertTriangle className="w-3 h-3" strokeWidth={2.5} />
      ) : null}
      {resolvedTone === "red" && !display.startsWith("Entrega") ? (
        <X className="w-3 h-3" strokeWidth={2.5} />
      ) : null}
      {display}
    </span>
  );
}

export function PriorityBadge({ priority }: { priority: OrderPriority }) {
  const cfg = PRIORITY_CONFIG[priority];
  return (
    <span
      className="text-[10px] font-mono font-semibold tracking-widest"
      style={{ color: cfg.text }}
    >
      {cfg.label}
    </span>
  );
}
