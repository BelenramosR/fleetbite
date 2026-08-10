import { useState, useEffect, useMemo } from "react";
import type { LucideIcon } from "lucide-react";
import {
  AlertTriangle,
  ArrowRight,
  Bike,
  CheckCircle2,
  Clock,
  Package,
  Radio,
  Timer,
  UserX,
} from "lucide-react";
import { DashboardSkeleton } from "@/shared/components/skeleton";
import { OrderStatusBadge } from "@/shared/components/badges";
import { mockMetrics, mockOrders } from "@/services/api/mocks/mockData";
import { calculateSla, slaTone } from "@/features/orders/lib/calculateSla";
import type { NavExtra, Page } from "@/app/router";
import type { Order } from "@/shared/types";

type Tone = "neutral" | "accent" | "danger";

const VALUE_COLOR: Record<Tone, string> = {
  neutral: "var(--foreground)",
  accent: "var(--primary)",
  danger: "#b91c1c",
};

function MetricCard({
  label,
  value,
  sub,
  tone = "neutral",
  actionLabel,
  onClick,
}: {
  label: string;
  value: string | number;
  sub?: string;
  tone?: Tone;
  actionLabel?: string;
  onClick?: () => void;
}) {
  const body = (
    <>
      <div className="text-[9px] sm:text-[10px] font-mono tracking-widest mb-2 sm:mb-3 truncate text-muted-foreground">
        {label}
      </div>
      <div
        className="text-2xl sm:text-3xl font-bold font-mono tabular-nums"
        style={{ color: VALUE_COLOR[tone] }}
      >
        {value}
      </div>
      {sub && (
        <div className="text-[10px] sm:text-[11px] mt-1 sm:mt-1.5 leading-snug text-muted-foreground">
          {sub}
        </div>
      )}
      {actionLabel && (
        <div className="mt-2.5 sm:mt-3 flex items-center gap-1 text-[9px] sm:text-[10px] font-mono tracking-widest text-primary">
          {actionLabel}
          <ArrowRight className="w-3 h-3" />
        </div>
      )}
    </>
  );

  const shell = `rounded-lg border p-3.5 sm:p-5 min-w-0 text-left bg-card ${
    tone === "danger"
      ? "border-red-700/25"
      : tone === "accent"
        ? "border-primary/25"
        : "border-border"
  }`;

  if (!onClick) return <div className={shell}>{body}</div>;

  return (
    <button
      type="button"
      onClick={onClick}
      className={`${shell} w-full cursor-pointer hover:border-primary/50 hover:shadow-sm`}
    >
      {body}
    </button>
  );
}

interface Alert {
  key: string;
  icon: LucideIcon;
  count: number;
  title: string;
  detail: string;
  action: string;
  critical: boolean;
  go: () => void;
}

function dispatchPriority(order: Order, now: Date): number {
  const sla = calculateSla(order, now);
  if (order.status === "WAITING_FOR_DRIVER") {
    if (sla.status === "BREACHED") return 0;
    if (sla.status === "AT_RISK" || sla.priority === "CRITICAL") return 1;
    return 2;
  }
  if (order.status === "READY") return 3;
  if (sla.status === "BREACHED") return 4;
  if (sla.status === "AT_RISK") return 5;
  return 9;
}

interface DashboardPageProps {
  onNavigate: (page: Page, extra?: NavExtra) => void;
}

export default function DashboardPage({ onNavigate }: DashboardPageProps) {
  const [loading, setLoading] = useState(true);
  const [now] = useState(() => new Date());

  useEffect(() => {
    const t = setTimeout(() => setLoading(false), 1100);
    return () => clearTimeout(t);
  }, []);

  const quickQueue = useMemo(() => {
    return mockOrders
      .filter(
        (o) =>
          o.status === "WAITING_FOR_DRIVER" ||
          o.status === "READY" ||
          o.slaStatus === "AT_RISK" ||
          o.slaStatus === "BREACHED",
      )
      .sort((a, b) => dispatchPriority(a, now) - dispatchPriority(b, now))
      .slice(0, 5);
  }, [now]);

  if (loading) return <DashboardSkeleton />;

  const m = mockMetrics;
  const goToOrders = (ordersFilter: NavExtra["ordersFilter"]) =>
    onNavigate("orders", { ordersFilter });

  const readyCount = mockOrders.filter((o) => o.status === "READY").length;

  const alerts: Alert[] = [];

  if (m.slaBreach > 0) {
    alerts.push({
      key: "breach",
      icon: AlertTriangle,
      count: m.slaBreach,
      title: `SLA incumplido`,
      detail: "El pedido pasó su hora prometida",
      action: "Ver pedidos",
      critical: true,
      go: () => goToOrders("sla-risk"),
    });
  }

  if (m.slaAtRisk > 0) {
    alerts.push({
      key: "risk",
      icon: Timer,
      count: m.slaAtRisk,
      title: `En riesgo de SLA`,
      detail: "Quedan pocos minutos para entregar",
      action: "Ver pedidos",
      critical: false,
      go: () => goToOrders("sla-risk"),
    });
  }

  if (m.waitingForDriver > 0) {
    alerts.push({
      key: "waiting",
      icon: UserX,
      count: m.waitingForDriver,
      title: `Sin repartidor`,
      detail: "Esperando asignación desde el detalle del pedido",
      action: "Ver sin repartidor",
      critical: true,
      go: () => onNavigate("orders", { ordersFilter: "unassigned" }),
    });
  }

  if (m.availableDrivers === 0) {
    alerts.push({
      key: "no-drivers",
      icon: Radio,
      count: 0,
      title: `Sin motorizados disponibles`,
      detail: "Toda la flota está ocupada o fuera de línea",
      action: "Ver motorizados",
      critical: true,
      go: () => onNavigate("drivers"),
    });
  }

  return (
    <div className="space-y-4 sm:space-y-5">
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-2.5 sm:gap-3">
        <MetricCard
          label="PEDIDOS ACTIVOS"
          value={m.activeOrders}
          sub={`${m.inTransit} en camino`}
          tone="accent"
          actionLabel="Ver cola"
          onClick={() => goToOrders("active")}
        />
        <MetricCard
          label="SIN REPARTIDOR"
          value={m.waitingForDriver}
          sub="requieren asignación"
          tone={m.waitingForDriver > 0 ? "danger" : "neutral"}
          actionLabel="Ver pedidos"
          onClick={() => goToOrders("unassigned")}
        />
        <MetricCard
          label="SLA EN RIESGO"
          value={m.slaAtRisk}
          sub={`${m.slaBreach} incumplido`}
          tone={m.slaAtRisk > 0 || m.slaBreach > 0 ? "danger" : "neutral"}
          actionLabel="Ver pedidos"
          onClick={() => goToOrders("sla-risk")}
        />
        <MetricCard
          label="DISPONIBLES"
          value={m.availableDrivers}
          sub={`${m.busyDrivers} ocupados`}
          tone={m.availableDrivers === 0 ? "danger" : "neutral"}
          actionLabel="Ver motorizados"
          onClick={() => onNavigate("drivers")}
        />
      </div>

      <div className="rounded-lg border border-border bg-card overflow-hidden">
        <div className="flex items-center gap-2 px-4 sm:px-5 py-3 border-b border-border">
          <span className="text-[10px] font-mono tracking-widest text-muted-foreground">
            REQUIERE ATENCIÓN AHORA
          </span>
          {alerts.length > 0 && (
            <span className="ml-auto text-[10px] font-mono tabular-nums text-muted-foreground">
              {alerts.length} {alerts.length === 1 ? "aviso" : "avisos"}
            </span>
          )}
        </div>

        {alerts.length === 0 ? (
          <div className="px-4 sm:px-5 py-8 flex flex-col items-center text-center gap-2">
            <CheckCircle2 className="w-6 h-6" style={{ color: "#15803d" }} />
            <p className="text-sm text-foreground">Nada pendiente de atender.</p>
            <p className="text-xs text-muted-foreground">
              Todos los pedidos tienen repartidor y están dentro del SLA.
            </p>
          </div>
        ) : (
          alerts.map((a) => {
            const Icon = a.icon;
            return (
              <button
                key={a.key}
                type="button"
                onClick={a.go}
                className="w-full flex items-center gap-3 sm:gap-4 px-4 sm:px-5 py-3.5 border-b border-border last:border-b-0 text-left cursor-pointer hover:bg-muted"
              >
                <Icon
                  className="w-4 h-4 shrink-0"
                  style={{ color: a.critical ? "#b91c1c" : "#b45309" }}
                />

                {a.count > 0 && (
                  <span
                    className="text-lg sm:text-xl font-bold font-mono tabular-nums w-7 sm:w-8 shrink-0"
                    style={{ color: a.critical ? "#b91c1c" : "#b45309" }}
                  >
                    {a.count}
                  </span>
                )}

                <div className="min-w-0 flex-1">
                  <div className="text-xs sm:text-sm font-medium text-foreground truncate">
                    {a.title}
                  </div>
                  <div className="text-[11px] text-muted-foreground truncate">{a.detail}</div>
                </div>

                <span className="hidden sm:flex items-center gap-1 text-[10px] font-mono tracking-widest shrink-0 text-primary">
                  {a.action}
                  <ArrowRight className="w-3.5 h-3.5" />
                </span>
                <ArrowRight className="w-4 h-4 sm:hidden shrink-0 text-primary" />
              </button>
            );
          })
        )}
      </div>

      <div className="grid grid-cols-2 xl:grid-cols-4 gap-2.5 sm:gap-3">
        <MetricCard
          label="TIEMPO ASIGNACIÓN"
          value={`${m.avgAssignmentMinutes}m`}
          sub="promedio hoy"
        />
        <MetricCard
          label="TIEMPO ENTREGA"
          value={`${m.avgDeliveryMinutes}m`}
          sub="promedio hoy"
        />
        <MetricCard
          label="CUMPLIMIENTO SLA"
          value={`${m.slaCompliancePercent}%`}
          sub="pedidos dentro del tiempo"
          tone={m.slaCompliancePercent >= 85 ? "accent" : "danger"}
        />
        <MetricCard
          label="EN CAMINO"
          value={m.inTransit}
          sub={`${readyCount} listos en cocina`}
          tone="accent"
          actionLabel="Ver en camino"
          onClick={() => goToOrders("in-transit")}
        />
      </div>

      <div className="rounded-lg border border-border bg-card overflow-hidden">
        <div className="flex items-center gap-2 px-4 sm:px-5 py-3 border-b border-border">
          <Package className="w-3.5 h-3.5 text-muted-foreground" />
          <span className="text-[10px] font-mono tracking-widest text-muted-foreground">
            COLA RÁPIDA — ATENDER YA
          </span>
          <span className="ml-auto text-[10px] font-mono tabular-nums text-muted-foreground">
            {quickQueue.length} pedidos
          </span>
        </div>

        {quickQueue.length === 0 ? (
          <div className="px-4 sm:px-5 py-6 text-center text-xs text-muted-foreground">
            No hay pedidos urgentes en cola.
          </div>
        ) : (
          quickQueue.map((order, i) => {
            const sla = calculateSla(order, now);
            const tone = slaTone(order, sla);
            const toneColor =
              tone === "red"
                ? "#b91c1c"
                : tone === "orange" || tone === "yellow"
                  ? "#b45309"
                  : "var(--muted-foreground)";
            return (
              <button
                key={order.id}
                type="button"
                onClick={() =>
                  onNavigate("order-detail", { selectedOrderId: order.id })
                }
                className="w-full flex items-center gap-3 px-4 sm:px-5 py-3 border-b border-border last:border-b-0 text-left cursor-pointer hover:bg-muted"
              >
                <span className="text-[11px] font-mono tabular-nums text-muted-foreground w-4 shrink-0">
                  {i + 1}
                </span>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2 min-w-0">
                    <span className="text-xs font-mono text-primary shrink-0">
                      {order.code.slice(-6)}
                    </span>
                    <span className="text-xs font-medium text-foreground truncate">
                      {order.customerName}
                    </span>
                  </div>
                  <div className="text-[11px] text-muted-foreground truncate mt-0.5">
                    {order.deliveryAddress}
                    {order.driverName ? ` · ${order.driverName}` : ""}
                  </div>
                </div>
                <OrderStatusBadge status={order.status} muted />
                <span
                  className="hidden sm:inline text-[11px] font-mono tabular-nums shrink-0"
                  style={{ color: toneColor }}
                >
                  {sla.label}
                </span>
                <ArrowRight className="w-4 h-4 shrink-0 text-primary" />
              </button>
            );
          })
        )}

        <div className="px-4 sm:px-5 py-2.5 border-t border-border flex flex-wrap items-center gap-x-4 gap-y-1 text-[11px] text-muted-foreground">
          <span className="inline-flex items-center gap-1.5">
            <Bike className="w-3.5 h-3.5" />
            {m.availableDrivers} motos libres
          </span>
          <span className="inline-flex items-center gap-1.5">
            <UserX className="w-3.5 h-3.5" />
            {m.waitingForDriver} sin asignar
          </span>
          <button
            type="button"
            onClick={() => goToOrders("unassigned")}
            className="ml-auto inline-flex items-center gap-1 text-[10px] font-mono tracking-widest text-primary cursor-pointer"
          >
            Ir a sin repartidor
            <ArrowRight className="w-3 h-3" />
          </button>
        </div>
      </div>

      <div className="flex items-center gap-1.5 text-[10px] font-mono text-muted-foreground">
        <Clock className="w-3 h-3" />
        Datos operativos en vivo — se refrescan automáticamente
      </div>
    </div>
  );
}
