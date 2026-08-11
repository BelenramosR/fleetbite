import { useState, useEffect } from "react";
import { ArrowLeft, ArrowRight, Bike, Check, LoaderCircle, Smartphone, Sparkles, UserRoundSearch, X } from "lucide-react";
import { OrderStatusBadge, SlaBadge, PriorityBadge } from "@/shared/components/badges";
import { DetailSkeleton } from "@/shared/components/skeleton";
import { Toast } from "@/shared/components/toast";
import type { ToastTone } from "@/shared/components/toast";
import type { Driver, Order, OrderStatus, TimelineEvent } from "@/shared/types";
import { assignOrderAutomatically, assignOrderManually, getAssignedDriverLabel, getOrder, getOrderHistory, transitionOrder } from "@/features/orders/services/orderApi";
import DeliveryMapPreview from "@/features/orders/components/DeliveryMapPreview";
import AssignDriverPanel from "@/features/orders/components/AssignDriverPanel";

/** Acciones del panel ops/admin (sin asignación; esa tiene flujo propio). */
const ADMIN_ACTIONS: Partial<Record<OrderStatus, { label: string; next: OrderStatus }[]>> = {
  CREATED: [{ label: "Confirmar pedido", next: "CONFIRMED" }],
  CONFIRMED: [{ label: "Iniciar preparación", next: "PREPARING" }],
  PREPARING: [{ label: "Marcar como listo", next: "READY" }],
};

const NEEDS_ASSIGNMENT: OrderStatus[] = ["READY", "WAITING_FOR_DRIVER"];

const DRIVER_PHASE: Partial<
  Record<OrderStatus, { title: string; actions: string[]; hint: string }>
> = {
  ASSIGNED: {
    title: "En manos del repartidor",
    actions: ["Aceptar pedido", "Confirmar recogida"],
    hint: "El repartidor asignado gestiona la recogida desde su sesión.",
  },
  PICKED_UP: {
    title: "En manos del repartidor",
    actions: ["Iniciar reparto"],
    hint: "Solo el repartidor puede iniciar el trayecto hacia el cliente.",
  },
  IN_TRANSIT: {
    title: "En manos del repartidor",
    actions: ["Confirmar entrega", "Entrega fallida"],
    hint: "La confirmación de entrega se registra en la app del driver.",
  },
};

const STATUS_SEQUENCE: OrderStatus[] = [
  "CREATED",
  "CONFIRMED",
  "PREPARING",
  "READY",
  "ASSIGNED",
  "PICKED_UP",
  "IN_TRANSIT",
  "DELIVERED",
];

const STATUS_LABEL: Partial<Record<OrderStatus, string>> = {
  PREPARING: "Preparando",
  READY: "Listo",
  ASSIGNED: "Asignado",
  PICKED_UP: "Recogido",
  IN_TRANSIT: "En camino",
  DELIVERED: "Entregado",
  FAILED_DELIVERY: "Entrega fallida",
};

const CARD =
  "rounded-xl border border-border bg-card overflow-hidden min-w-0";

const EMPTY_ORDER: Order = {
  id: "", code: "", customerName: "", customerPhone: "", deliveryAddress: "",
  totalAmount: 0, priority: "NORMAL", status: "CREATED", slaStatus: "ON_TIME",
  slaMinutesRemaining: 0, promisedDeliveryAt: "", createdAt: "",
};

interface Props {
  orderId: string;
  onBack: () => void;
}

export default function OrderDetailPage({ orderId, onBack }: Props) {
  const [loading, setLoading] = useState(true);
  const [order, setOrder] = useState<Order>(EMPTY_ORDER);
  const [timeline, setTimeline] = useState<TimelineEvent[]>([]);
  const [confirming, setConfirming] = useState<OrderStatus | null>(null);
  const [assignMode, setAssignMode] = useState<"menu" | "manual" | "auto">("menu");
  const [autoDriver, setAutoDriver] = useState<Driver | null>(null);
  const [toast, setToast] = useState("");
  const [toastTone, setToastTone] = useState<ToastTone>("success");
  const [assigning, setAssigning] = useState(false);

  useEffect(() => {
    setLoading(true);
    setAssignMode("menu");
    setAutoDriver(null);
    setConfirming(null);
    let active = true;
    async function refresh() {
      try {
        const [result, history, driverName] = await Promise.all([getOrder(orderId), getOrderHistory(orderId), getAssignedDriverLabel(orderId)]);
        if (active) { setOrder({ ...result, driverName }); setTimeline(history); }
      } catch (cause) {
        if (active) { setToastTone("error"); setToast(cause instanceof Error ? cause.message : "No se pudo cargar el pedido"); }
      } finally { if (active) setLoading(false); }
    }
    void refresh();
    const id = window.setInterval(() => void refresh(), 10_000);
    return () => { active = false; window.clearInterval(id); };
  }, [orderId]);

  const actions = ADMIN_ACTIONS[order.status] ?? [];
  const needsAssignment = NEEDS_ASSIGNMENT.includes(order.status);
  const driverPhase = DRIVER_PHASE[order.status];
  const confirmingAction = actions.find((a) => a.next === confirming);

  async function refreshAssignment(driverName: string, driverId: string) {
    const [updatedOrder, history] = await Promise.all([getOrder(order.id), getOrderHistory(order.id)]);
    setOrder({ ...updatedOrder, driverName, driverId });
    setTimeline(history);
    setAssignMode("menu");
    setAutoDriver(null);
    setConfirming(null);
  }

  async function assignDriver(driver: Driver) {
    await assignOrderManually(order.id, driver.id);
    await refreshAssignment(driver.name, driver.id);
    setToast(`Asignado manualmente a ${driver.name}`);
    setToastTone("success");
    setTimeout(() => setToast(""), 3200);
  }

  async function handleAction(next: OrderStatus) {
    try {
      setOrder(await transitionOrder(order.id, next));
      setTimeline(await getOrderHistory(order.id));
      setConfirming(null);
      setToast(`Estado actualizado a ${STATUS_LABEL[next] ?? next}`);
      setToastTone("success");
      setTimeout(() => setToast(""), 3200);
    } catch (cause) {
      setToastTone("error"); setToast(cause instanceof Error ? cause.message : "No se pudo actualizar el estado");
    }
  }

  async function openAutoAssign() {
    setAssigning(true);
    try {
      const result = await assignOrderAutomatically(order.id);
      await refreshAssignment(result.driverName, result.driverId);
      setToast(`Asignado automáticamente a ${result.driverName}`);
      setToastTone("success");
      setTimeout(() => setToast(""), 3200);
    } catch (cause) {
      setToastTone("error"); setToast(cause instanceof Error ? cause.message : "No se pudo asignar automáticamente.");
    } finally { setAssigning(false); }
  }

  const currentStep = STATUS_SEQUENCE.indexOf(order.status);

  return (
    <div className="w-full max-w-6xl mx-auto space-y-3 sm:space-y-4">
      <button
        type="button"
        onClick={onBack}
        className="flex items-center gap-2 text-xs cursor-pointer text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="w-3.5 h-3.5" />
        Volver a pedidos
      </button>

      {loading ? (
        <DetailSkeleton />
      ) : (
        /*
          Bento:
          - móvil: 1 col, acciones arriba (cerca del dedo)
          - md: 2 col
          - xl: mapa + acciones ocupan 2 filas; info e historial se encajan a la izquierda
        */
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-12 gap-3 auto-rows-auto">
          {/* Cabecera */}
          <div
            className={`${CARD} col-span-1 md:col-span-2 xl:col-span-12 px-4 sm:px-5 py-4 flex flex-col sm:flex-row sm:items-end gap-3 sm:gap-4`}
          >
            <div className="min-w-0 flex-1">
              <div className="text-[10px] font-mono tracking-widest mb-1 text-muted-foreground">
                PEDIDO
              </div>
              <h2 className="text-xl sm:text-2xl font-bold font-mono text-foreground truncate">
                {order.code}
              </h2>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              <OrderStatusBadge status={order.status} />
              <PriorityBadge priority={order.priority} />
              {!(["DELIVERED", "CANCELLED", "FAILED_DELIVERY"] as OrderStatus[]).includes(order.status) && (
                <SlaBadge status={order.slaStatus} minutesRemaining={order.slaMinutesRemaining} />
              )}
            </div>
          </div>

          {/* Progreso */}
          <div className={`${CARD} col-span-1 md:col-span-2 xl:col-span-12 p-3 sm:p-4 overflow-x-auto`}>
            <div className="flex items-center gap-0 min-w-[560px]">
              {STATUS_SEQUENCE.map((s, i) => {
                const done = i < currentStep;
                const active = i === currentStep;
                const last = i === STATUS_SEQUENCE.length - 1;
                return (
                  <div key={s} className="flex items-center flex-1">
                    <div className="flex flex-col items-center gap-1">
                      <div
                        className="w-2.5 h-2.5 rounded-full border shrink-0"
                        style={{
                          background: done
                            ? "#16a34a"
                            : active
                              ? "var(--primary)"
                              : "var(--muted)",
                          borderColor: done
                            ? "#16a34a"
                            : active
                              ? "var(--primary)"
                              : "var(--border)",
                        }}
                      />
                      <span
                        className="text-[9px] font-mono text-center leading-tight hidden md:block"
                        style={{
                          color: active
                            ? "var(--primary)"
                            : done
                              ? "#15803d"
                              : "var(--muted-foreground)",
                          width: "48px",
                        }}
                      >
                        {s.replace("_", " ")}
                      </span>
                    </div>
                    {!last && (
                      <div
                        className="flex-1 h-px mx-0.5"
                        style={{ background: done ? "#16a34a" : "var(--border)" }}
                      />
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          {/* Info — izquierda arriba */}
          <div
            className={`${CARD} col-span-1 md:col-span-1 xl:col-span-4 order-1 flex flex-col`}
          >
            <div className="px-4 sm:px-5 py-3 border-b border-border">
              <div className="text-[10px] font-mono tracking-widest text-muted-foreground">
                INFORMACIÓN DEL PEDIDO
              </div>
            </div>
            <div className="p-4 sm:p-5 grid grid-cols-2 gap-x-3 gap-y-4 flex-1">
              {[
                { label: "CLIENTE", value: order.customerName },
                { label: "TELÉFONO", value: order.customerPhone },
                { label: "DIRECCIÓN", value: order.deliveryAddress, wide: true },
                { label: "MONTO", value: `S/ ${order.totalAmount.toFixed(2)}` },
                { label: "ENTREGA PROM.", value: order.promisedDeliveryAt },
                { label: "REPARTIDOR", value: order.driverName ?? "Sin asignar" },
              ].map((f) => (
                <div key={f.label} className={f.wide ? "col-span-2" : "min-w-0"}>
                  <div className="text-[9px] font-mono tracking-widest mb-0.5 text-muted-foreground">
                    {f.label}
                  </div>
                  <div className="text-sm text-foreground break-words">{f.value}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Acciones — móvil pronto; xl derecha a 2 filas */}
          <div
            className={`${CARD} col-span-1 md:col-span-1 xl:col-span-3 xl:row-span-2 order-2 md:order-2 xl:order-3 flex flex-col min-h-[220px] ${
              needsAssignment && assignMode === "manual" ? "min-h-[420px]" : ""
            }`}
          >
            <div className="px-4 sm:px-5 py-3 border-b border-border flex items-center justify-between gap-2 shrink-0">
              <div className="text-[10px] font-mono tracking-widest text-muted-foreground">
                ACCIONES DISPONIBLES
              </div>
              <OrderStatusBadge status={order.status} />
            </div>

            <div className="p-4 sm:p-5 flex flex-col gap-3 flex-1 min-h-0">
              {driverPhase ? (
                <div className="rounded-xl border border-border bg-muted/40 p-4 flex flex-col gap-3 flex-1">
                  <div className="flex items-start gap-2.5">
                    <div className="w-8 h-8 rounded-lg bg-blue-50 border border-blue-100 flex items-center justify-center shrink-0">
                      <Bike className="w-4 h-4 text-blue-700" />
                    </div>
                    <div className="min-w-0">
                      <div className="text-sm font-medium text-foreground">{driverPhase.title}</div>
                      <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
                        {driverPhase.hint}
                      </p>
                    </div>
                  </div>
                  <div className="space-y-1.5">
                    <div className="text-[10px] font-mono tracking-widest text-muted-foreground">
                      ACCIONES DEL DRIVER
                    </div>
                    {driverPhase.actions.map((label) => (
                      <div
                        key={label}
                        className="flex items-center gap-2 px-3 py-2 rounded-lg border border-dashed border-border text-xs text-muted-foreground"
                      >
                        <Smartphone className="w-3.5 h-3.5 shrink-0" />
                        <span className="flex-1">{label}</span>
                        <span className="text-[10px] font-mono tracking-wide text-muted-foreground/80">
                          App driver
                        </span>
                      </div>
                    ))}
                  </div>
                  {order.driverName && (
                    <p className="text-[11px] text-muted-foreground mt-auto leading-relaxed">
                      Asignado a <span className="font-medium text-foreground">{order.driverName}</span>.
                    </p>
                  )}
                </div>
              ) : needsAssignment && assignMode === "manual" ? (
                <AssignDriverPanel
                  onBack={() => setAssignMode("menu")}
                  onAssign={assignDriver}
                />
              ) : needsAssignment && assignMode === "auto" ? (
                <div
                  className="rounded-xl border p-4 space-y-3 flex-1 flex flex-col border-primary/35 bg-primary/5"
                >
                  <div className="flex-1">
                    <div className="text-[10px] font-mono tracking-widest text-muted-foreground mb-1">
                      ASIGNACIÓN AUTOMÁTICA
                    </div>
                    {autoDriver ? (
                      <>
                        <p className="text-sm font-medium text-foreground">
                          Asignar a {autoDriver.name}
                        </p>
                        <p className="text-xs text-muted-foreground mt-1.5 leading-relaxed">
                          El más cercano al local (
                          <span className="font-mono tabular-nums">
                            {autoDriver.distanceKm?.toFixed(1)} km
                          </span>
                          ). Quedará en estado{" "}
                          <strong className="font-mono">Asignado</strong>.
                        </p>
                      </>
                    ) : (
                      <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
                        No hay repartidores disponibles para asignación automática.
                      </p>
                    )}
                  </div>
                  <div className="flex flex-col-reverse sm:flex-row gap-2 mt-auto">
                    <button
                      type="button"
                      onClick={() => {
                        setAssignMode("menu");
                        setAutoDriver(null);
                      }}
                      className="flex-1 py-2.5 rounded-lg border border-border text-xs font-medium cursor-pointer inline-flex items-center justify-center gap-1.5 text-muted-foreground hover:text-foreground hover:bg-muted"
                    >
                      <X className="w-3.5 h-3.5" />
                      Cancelar
                    </button>
                    {autoDriver && (
                      <button
                        type="button"
                        onClick={() => void openAutoAssign()}
                        className="flex-1 py-2.5 rounded-lg text-xs font-semibold cursor-pointer inline-flex items-center justify-center gap-1.5 bg-primary text-primary-foreground"
                      >
                        <Check className="w-3.5 h-3.5" />
                        Sí, asignar
                      </button>
                    )}
                  </div>
                </div>
              ) : needsAssignment ? (
                <>
                  <div className="space-y-2.5 flex-1">
                    <button
                      type="button"
                      onClick={() => void openAutoAssign()}
                      disabled={assigning}
                      className="w-full flex items-center justify-between gap-2 px-3.5 py-3 rounded-xl border text-left text-sm font-medium cursor-pointer transition-colors border-primary/35 text-primary hover:bg-primary/10"
                    >
                      <span className="inline-flex items-center gap-2">
                        {assigning ? <LoaderCircle className="w-4 h-4 shrink-0 animate-spin" /> : <Sparkles className="w-4 h-4 shrink-0" />}
                        {assigning ? "Asignando…" : "Asignar automáticamente"}
                      </span>
                      <ArrowRight className="w-4 h-4 shrink-0" />
                    </button>
                    <button
                      type="button"
                      onClick={() => setAssignMode("manual")}
                      className="w-full flex items-center justify-between gap-2 px-3.5 py-3 rounded-xl border text-left text-sm font-medium cursor-pointer transition-colors border-border text-foreground hover:bg-muted/60"
                    >
                      <span className="inline-flex items-center gap-2">
                        <UserRoundSearch className="w-4 h-4 shrink-0 text-muted-foreground" />
                        Asignar manualmente
                      </span>
                      <ArrowRight className="w-4 h-4 shrink-0" />
                    </button>
                  </div>
                  <p className="text-[11px] text-muted-foreground leading-relaxed mt-auto pt-1">
                    Automático elige al más cercano. Manual te deja comparar la flota disponible.
                  </p>
                </>
              ) : actions.length === 0 ? (
                <p className="text-xs text-muted-foreground leading-relaxed my-auto">
                  No hay acciones pendientes para este estado.
                </p>
              ) : confirming && confirmingAction ? (
                <div
                  className="rounded-xl border p-4 space-y-3 flex-1 flex flex-col"
                  style={{
                    borderColor:
                      confirming === "FAILED_DELIVERY"
                        ? "rgba(185 28 28 / 0.3)"
                        : "rgba(217 119 6 / 0.35)",
                    background:
                      confirming === "FAILED_DELIVERY"
                        ? "rgba(185 28 28 / 0.04)"
                        : "rgba(217 119 6 / 0.06)",
                  }}
                >
                  <div className="flex-1">
                    <div className="text-[10px] font-mono tracking-widest text-muted-foreground mb-1">
                      CONFIRMAR ACCIÓN
                    </div>
                    <p className="text-sm font-medium text-foreground">{confirmingAction.label}</p>
                    <p className="text-xs text-muted-foreground mt-1.5 leading-relaxed">
                      El pedido pasará a{" "}
                      <strong className="font-mono">
                        {STATUS_LABEL[confirming] ?? confirming}
                      </strong>
                      . Queda registrado en el historial.
                    </p>
                  </div>
                  <div className="flex flex-col-reverse sm:flex-row gap-2 mt-auto">
                    <button
                      type="button"
                      onClick={() => setConfirming(null)}
                      className="flex-1 py-2.5 rounded-lg border border-border text-xs font-medium cursor-pointer inline-flex items-center justify-center gap-1.5 text-muted-foreground hover:text-foreground hover:bg-muted"
                    >
                      <X className="w-3.5 h-3.5" />
                      Cancelar
                    </button>
                    <button
                      type="button"
                      onClick={() => void handleAction(confirming)}
                      className="flex-1 py-2.5 rounded-lg text-xs font-semibold cursor-pointer inline-flex items-center justify-center gap-1.5"
                      style={
                        confirming === "FAILED_DELIVERY"
                          ? { background: "#b91c1c", color: "#fff" }
                          : {
                              background: "var(--primary)",
                              color: "var(--primary-foreground)",
                            }
                      }
                    >
                      <Check className="w-3.5 h-3.5" />
                      Sí, confirmar
                    </button>
                  </div>
                </div>
              ) : (
                <>
                  <div className="space-y-2.5 flex-1">
                    {actions.map((a) => {
                      const destructive = a.next === "FAILED_DELIVERY";
                      return (
                        <button
                          key={a.next}
                          type="button"
                          onClick={() => setConfirming(a.next)}
                          className={`w-full flex items-center justify-between gap-2 px-3.5 py-3 rounded-xl border text-left text-sm font-medium cursor-pointer transition-colors ${
                            destructive
                              ? "border-red-700/30 text-red-700 hover:bg-red-50"
                              : "border-primary/35 text-primary hover:bg-primary/10"
                          }`}
                        >
                          {a.label}
                          <ArrowRight className="w-4 h-4 shrink-0" />
                        </button>
                      );
                    })}
                  </div>
                  <p className="text-[11px] text-muted-foreground leading-relaxed mt-auto pt-1">
                    Te pediremos confirmación antes de cambiar el estado.
                  </p>
                </>
              )}
            </div>
          </div>

          {/* Mapa — centro / xl 2 filas */}
          <div className="col-span-1 md:col-span-2 xl:col-span-5 xl:row-span-2 order-4 md:order-4 xl:order-2 min-h-[320px] xl:min-h-0">
            <DeliveryMapPreview
              deliveryAddress={order.deliveryAddress}
              status={order.status}
              driverName={order.driverName}
              fill
              className="h-full"
            />
          </div>

          {/* Historial — bajo info en xl */}
          <div
            className={`${CARD} col-span-1 md:col-span-2 xl:col-span-4 order-5 xl:order-4 flex flex-col max-h-[360px] xl:max-h-none`}
          >
            <div className="px-4 sm:px-5 py-3 border-b border-border shrink-0">
              <div className="text-[10px] font-mono tracking-widest text-muted-foreground">
                HISTORIAL DEL PEDIDO
              </div>
            </div>
            <div className="p-4 sm:p-5 overflow-y-auto flex-1 min-h-0">
              {timeline.map((evt, i) => {
                const isLast = i === timeline.length - 1;
                const time = new Date(evt.createdAt).toLocaleTimeString("es-PE", {
                  hour: "2-digit",
                  minute: "2-digit",
                });
                return (
                  <div key={evt.id} className="flex gap-3">
                    <div className="flex flex-col items-center">
                      <div
                        className="w-2 h-2 rounded-full shrink-0 mt-1"
                        style={{ background: isLast ? "var(--primary)" : "#16a34a" }}
                      />
                      {!isLast && (
                        <div className="w-px flex-1 my-1 bg-border" />
                      )}
                    </div>
                    <div className={isLast ? "min-w-0" : "pb-4 min-w-0"}>
                      <div className="text-xs font-medium text-foreground">{evt.description}</div>
                      <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5 mt-0.5">
                        <span className="text-[10px] font-mono text-muted-foreground">{time}</span>
                        <span className="text-[10px] text-muted-foreground">· {evt.performedBy}</span>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {toast && <Toast message={toast} tone={toastTone} onClose={() => setToast("")} />}
    </div>
  );
}
