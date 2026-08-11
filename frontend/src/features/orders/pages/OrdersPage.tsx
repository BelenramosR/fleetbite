import { useState, useEffect } from "react";
import { Package, Search } from "lucide-react";
import { OrderStatusBadge, SlaBadge, PriorityBadge } from "@/shared/components/badges";
import { TableSkeleton } from "@/shared/components/skeleton";
import { listOrders } from "@/features/orders/services/orderApi";
import type { OrderStatus, Order } from "@/shared/types";
import type { OrdersFilter } from "@/app/router";
import {
  calculateSla,
  isSlaAtRisk,
  slaTone,
} from "@/features/orders/lib/calculateSla";

const ACTIVE_STATUSES: OrderStatus[] = [
  "CREATED",
  "CONFIRMED",
  "PREPARING",
  "READY",
  "WAITING_FOR_DRIVER",
  "ASSIGNED",
  "PICKED_UP",
  "IN_TRANSIT",
];

/** Ciclo de vida: Creado → … → Entregado; cancelados/fallidos al final. */
const STATUS_SORT_ORDER: OrderStatus[] = [
  "CREATED",
  "CONFIRMED",
  "PREPARING",
  "READY",
  "WAITING_FOR_DRIVER",
  "ASSIGNED",
  "PICKED_UP",
  "IN_TRANSIT",
  "DELIVERED",
  "CANCELLED",
  "FAILED_DELIVERY",
];

function statusRank(status: OrderStatus): number {
  const i = STATUS_SORT_ORDER.indexOf(status);
  return i === -1 ? STATUS_SORT_ORDER.length : i;
}

function sortOrdersByLifecycle(a: Order, b: Order): number {
  const byStatus = statusRank(a.status) - statusRank(b.status);
  if (byStatus !== 0) return byStatus;
  return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
}

/* Cliente y repartidor comparten el ancho sobrante para llenar la fila. */
const COLS = "96px minmax(160px,1.6fr) 56px 76px 136px 92px minmax(110px,1fr) 100px";

function formatOrderTime(iso: string): string {
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "—";
  return d.toLocaleTimeString("es-PE", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

interface OrdersPageProps {
  onSelectOrder: (id: string) => void;
  initialFilter?: OrdersFilter;
  refreshKey?: number;
}

export default function OrdersPage({ onSelectOrder, initialFilter, refreshKey = 0 }: OrdersPageProps) {
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState(0);
  const [search, setSearch] = useState("");
  const [orders, setOrders] = useState<Order[]>([]);
  /** Un solo reloj para toda la tabla; el SLA se recalcula con este valor. */
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    let active = true;
    async function refresh() {
      try { const data = await listOrders(); if (active) setOrders(data); }
      finally { if (active) setLoading(false); }
    }
    void refresh();
    const id = window.setInterval(() => void refresh(), 15_000);
    return () => { active = false; window.clearInterval(id); };
  }, [refreshKey]);

  useEffect(() => {
    if (!initialFilter) {
      setActiveTab(0);
      return;
    }
    const keys: OrdersFilter[] = [
      "all",
      "active",
      "unassigned",
      "sla-risk",
      "in-transit",
      "delivered",
      "cancelled",
    ];
    const i = keys.indexOf(initialFilter);
    setActiveTab(i === -1 ? 0 : i);
  }, [initialFilter]);

  useEffect(() => {
    const id = window.setInterval(() => setNow(new Date()), 60_000);
    return () => window.clearInterval(id);
  }, []);

  const filterTabs: { key: OrdersFilter; label: string; match: ((o: Order) => boolean) | null }[] =
    [
      { key: "all", label: "Todos", match: null },
      { key: "active", label: "Activos", match: (o) => ACTIVE_STATUSES.includes(o.status) },
      {
        key: "unassigned",
        label: "Sin repartidor",
        match: (o) => o.status === "WAITING_FOR_DRIVER",
      },
      {
        key: "sla-risk",
        label: "SLA en riesgo",
        match: (o) => isSlaAtRisk(o, now),
      },
      {
        key: "in-transit",
        label: "En camino",
        match: (o) => o.status === "IN_TRANSIT" || o.status === "PICKED_UP",
      },
      { key: "delivered", label: "Entregados", match: (o) => o.status === "DELIVERED" },
      {
        key: "cancelled",
        label: "Cancelados",
        match: (o) => o.status === "CANCELLED" || o.status === "FAILED_DELIVERY",
      },
    ];

  const filtered = orders
    .filter((o) => {
      const tab = filterTabs[activeTab];
      const statusMatch = tab.match === null || tab.match(o);
      const q = search.toLowerCase();
      const searchMatch =
        !q ||
        o.code.toLowerCase().includes(q) ||
        o.customerName.toLowerCase().includes(q) ||
        o.deliveryAddress.toLowerCase().includes(q);
      return statusMatch && searchMatch;
    })
    .sort(sortOrdersByLifecycle);

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row gap-3 items-start sm:items-center">
        <div className="flex flex-wrap items-center gap-0.5 p-1 rounded-md bg-muted">
          {filterTabs.map((tab, i) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(i)}
              className={`px-3 py-1 rounded text-xs font-medium cursor-pointer ${
                activeTab === i
                  ? "bg-card text-foreground shadow-sm"
                  : "text-muted-foreground hover:text-foreground"
              }`}
            >
              {tab.label}
              {tab.match !== null && !loading && (
                <span className="ml-1.5 font-mono text-[10px] tabular-nums text-muted-foreground">
                  {orders.filter(tab.match).length}
                </span>
              )}
            </button>
          ))}
        </div>

        <div className="sm:ml-auto relative w-full sm:w-auto">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-muted-foreground" />
          <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por ID, cliente, dirección…"
            aria-label="Buscar pedidos"
            className="pl-8 pr-3 py-2 rounded border border-border bg-muted text-xs text-foreground outline-none w-full sm:w-64 focus:border-primary"
          />
        </div>
      </div>

      <div className="rounded-lg border overflow-hidden overflow-x-auto bg-card border-border">
        <div
          className="grid gap-x-3 text-[10px] font-mono tracking-widest px-4 py-2.5 border-b border-border text-muted-foreground min-w-[900px]"
          style={{ gridTemplateColumns: COLS }}
        >
          <span>CÓDIGO</span>
          <span>CLIENTE / DIRECCIÓN</span>
          <span>HORA</span>
          <span>TOTAL</span>
          <span>ESTADO</span>
          <span>PRIORIDAD</span>
          <span>REPARTIDOR</span>
          <span>SLA</span>
        </div>

        {loading ? (
          <TableSkeleton rows={6} />
        ) : filtered.length === 0 ? (
          <div className="py-16 text-center flex flex-col items-center">
            <Package className="w-8 h-8 mb-3 text-muted-foreground" />
            <p className="text-sm text-muted-foreground">No hay pedidos que coincidan.</p>
          </div>
        ) : (
          filtered.map((order, i) => {
            const sla = calculateSla(order, now);
            const tone = slaTone(order, sla);
            return (
              <button
                key={order.id}
                type="button"
                onClick={() => onSelectOrder(order.id)}
                className="w-full grid items-center gap-x-3 px-4 py-3 border-b text-left cursor-pointer min-w-[900px] hover:bg-muted/60"
                style={{
                  gridTemplateColumns: COLS,
                  borderColor: "var(--border)",
                  borderBottomWidth: i === filtered.length - 1 ? "0px" : "1px",
                }}
              >
                <span className="text-xs font-mono text-primary">{order.code.slice(-6)}</span>
                <div className="min-w-0">
                  <div className="text-xs font-medium truncate text-foreground">
                    {order.customerName}
                  </div>
                  <div className="text-[11px] truncate text-muted-foreground">
                    {order.deliveryAddress}
                  </div>
                </div>
                <span className="text-xs font-mono tabular-nums text-muted-foreground">
                  {formatOrderTime(order.createdAt)}
                </span>
                <span className="text-xs font-mono tabular-nums text-foreground">
                  S/ {order.totalAmount.toFixed(2)}
                </span>
                <span>
                  <OrderStatusBadge status={order.status} muted />
                </span>
                <span>
                  <PriorityBadge priority={sla.priority} />
                </span>
                <span
                  className={`text-xs truncate ${
                    order.driverName ? "text-foreground" : "text-muted-foreground"
                  }`}
                >
                  {order.driverName ?? "—"}
                </span>
                <span>
                  <SlaBadge status={sla.status} label={sla.label} tone={tone} />
                </span>
              </button>
            );
          })
        )}
      </div>

      {!loading && (
        <div className="text-[11px] font-mono text-muted-foreground">
          Mostrando {filtered.length} de {orders.length} pedidos
        </div>
      )}
    </div>
  );
}
