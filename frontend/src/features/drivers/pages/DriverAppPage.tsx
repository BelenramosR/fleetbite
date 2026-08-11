import { useEffect, useState } from "react";
import {
  AlertTriangle,
  Bell,
  Bike,
  Clock,
  ExternalLink,
  LogOut,
  MapPin,
  Navigation,
  Package,
  Phone,
  Store,
  UserRound,
  X,
} from "lucide-react";
import { Toast, type ToastTone } from "@/shared/components/toast";
import {
  RESTAURANT,
  googleDirectionsUrl,
  googleMapsNavigateUrl,
} from "@/shared/constants";
import { getMyDriverProfile, setMyAvailability, updateMyLocation } from "@/features/drivers/services/driverApi";
import { getMyAssignmentSummary } from "@/features/drivers/services/driverApi";
import type { Driver, DriverDayStats, DriverStatus } from "@/shared/types";
import type { SessionUser } from "@/features/auth/lib/access";
import type { Page } from "@/app/router";
import { useDriverAssignmentPoll } from "@/features/drivers/hooks/useDriverAssignmentPoll";

type DriverTab = "assignment" | "profile";

const FAIL_REASONS = [
  "Cliente no disponible",
  "Dirección incorrecta",
  "Problema con el vehículo",
  "Pedido dañado",
  "Otro",
];

function pageToTab(page: Page): DriverTab {
  if (page === "driver-profile") return "profile";
  return "assignment";
}

function tabToPage(tab: DriverTab): Page {
  if (tab === "profile") return "driver-profile";
  return "driver-assignment";
}

interface DriverAppPageProps {
  user: SessionUser;
  page: Page;
  onNavigate: (page: Page) => void;
  onLogout: () => void;
}

export default function DriverAppPage({ user, page, onNavigate, onLogout }: DriverAppPageProps) {
  const tab = pageToTab(page);
  const driverId = user.driverId ?? "";

  const {
    assignment,
    loading,
    error: assignmentError,
    offerOpen,
    unreadPending,
    accept,
    reject,
    advance,
    openOffer,
    closeOffer,
  } = useDriverAssignmentPoll(driverId || undefined);

  const [driver, setDriver] = useState<Driver | null>(null);
  const [stats, setStats] = useState<DriverDayStats | null>(null);
  const [toast, setToast] = useState("");
  const [toastTone, setToastTone] = useState<ToastTone>("success");
  const [busy, setBusy] = useState(false);
  const [showFail, setShowFail] = useState(false);
  const [failReason, setFailReason] = useState("");
  const [lat, setLat] = useState(RESTAURANT.lat - 0.004);
  const [lng, setLng] = useState(RESTAURANT.lng - 0.003);
  const [locUpdatedAt, setLocUpdatedAt] = useState<string | null>(null);

  useEffect(() => {
    if (!driverId) return;
    void getMyDriverProfile().then((profile) => {
      setDriver(profile);
      if (profile.currentLatitude !== undefined) setLat(profile.currentLatitude);
      if (profile.currentLongitude !== undefined) setLng(profile.currentLongitude);
      setLocUpdatedAt(profile.lastLocationAt);
    }).catch(() => setToast("No se pudo cargar tu perfil"));
    void getMyAssignmentSummary().then(setStats);
  }, [driverId, assignment?.status]);

  function flash(msg: string, tone: ToastTone = "success") {
    setToast(msg);
    setToastTone(tone);
    setTimeout(() => setToast(""), 2800);
  }

  async function handleAccept() {
    setBusy(true);
    try {
      await accept();
      flash("Pedido aceptado — ve al local");
      onNavigate("driver-assignment");
    } catch (e) {
      flash(e instanceof Error ? e.message : "No se pudo aceptar", "error");
    } finally {
      setBusy(false);
    }
  }

  async function handleReject() {
    setBusy(true);
    try {
      await reject(failReason || "Rechazado por el driver");
      setDriver((d) => (d ? { ...d, status: "AVAILABLE", activeOrderId: undefined, activeOrderCode: undefined } : d));
      flash("Asignación rechazada");
    } catch (e) {
      flash(e instanceof Error ? e.message : "No se pudo rechazar", "error");
    } finally {
      setBusy(false);
    }
  }

  async function handleAdvance(next: "PICKED_UP" | "IN_TRANSIT" | "COMPLETED" | "FAILED") {
    setBusy(true);
    try {
      await advance(next);
      const labels = {
        PICKED_UP: "Pedido recogido",
        IN_TRANSIT: "Reparto iniciado",
        COMPLETED: "¡Entrega confirmada!",
        FAILED: "Entrega fallida registrada",
      } as const;
      flash(labels[next]);
      if (next === "COMPLETED" || next === "FAILED") {
        setDriver((d) =>
          d
            ? {
                ...d,
                status: "AVAILABLE",
                activeOrderId: undefined,
                activeOrderCode: undefined,
                deliveriesToday: d.deliveriesToday + (next === "COMPLETED" ? 1 : 0),
              }
            : d,
        );
        void getMyAssignmentSummary().then(setStats);
      }
    } catch (e) {
      flash(e instanceof Error ? e.message : "Acción no permitida", "error");
    } finally {
      setBusy(false);
      setShowFail(false);
    }
  }

  async function toggleAvailability() {
    if (!driver) return;
    if (assignment) {
      flash("No puedes cambiar disponibilidad con un pedido activo");
      return;
    }
    const nextStatus: DriverStatus = driver.status === "AVAILABLE" ? "OFFLINE" : "AVAILABLE";
    try {
      setDriver(await setMyAvailability(nextStatus === "AVAILABLE"));
    } catch (cause) {
      flash(cause instanceof Error ? cause.message : "No se pudo cambiar la disponibilidad", "error");
      return;
    }
    flash(nextStatus === "AVAILABLE" ? "Ahora estás disponible" : "Pasaste a fuera de línea");
  }

  async function updateLocation() {
    const jitter = () => (Math.random() - 0.5) * 0.008;
    const nextLat = RESTAURANT.lat + jitter();
    const nextLng = RESTAURANT.lng + jitter();
    try {
      setDriver(await updateMyLocation(nextLat, nextLng));
    } catch (cause) {
      flash(cause instanceof Error ? cause.message : "No se pudo actualizar la ubicación", "error");
      return;
    }
    setLat(nextLat);
    setLng(nextLng);
    setLocUpdatedAt(new Date().toISOString());
    flash("Ubicación actualizada");
  }

  const status = assignment?.status;
  const mapsTarget =
    status === "ACCEPTED" || status === "PICKED_UP"
      ? { label: "Ir al local", address: RESTAURANT.address, url: googleMapsNavigateUrl(RESTAURANT.address) }
      : status === "IN_TRANSIT"
        ? {
            label: "Ir al cliente",
            address: assignment!.order.deliveryAddress,
            url: googleMapsNavigateUrl(assignment!.order.deliveryAddress),
          }
        : null;

  const nextAction =
    status === "ACCEPTED"
      ? { label: "Confirmar recogida", next: "PICKED_UP" as const }
      : status === "PICKED_UP"
        ? { label: "Iniciar reparto", next: "IN_TRANSIT" as const }
        : status === "IN_TRANSIT"
          ? { label: "Confirmar entrega", next: "COMPLETED" as const }
          : null;

  return (
    <div className="min-h-dvh flex flex-col bg-background">
      <header className="shrink-0 border-b border-border bg-card px-4 py-3 flex items-center justify-between gap-3 sticky top-0 z-30">
        <div className="min-w-0">
          <div className="text-[10px] font-mono tracking-widest text-primary">FLEETBITE DRIVER</div>
          <div className="text-sm font-semibold text-foreground truncate">{user.fullName}</div>
        </div>
        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={() => {
              if (assignment?.status === "PENDING") openOffer();
              else flash("No hay asignaciones pendientes nuevas");
            }}
            className="relative p-2 rounded-lg text-muted-foreground hover:text-foreground hover:bg-muted cursor-pointer"
            aria-label="Notificaciones de asignación"
          >
            <Bell className="w-5 h-5" />
            {unreadPending && (
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-red-600 ring-2 ring-card" />
            )}
          </button>
          <button
            type="button"
            onClick={onLogout}
            className="inline-flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground cursor-pointer px-2 py-1.5 rounded-md hover:bg-muted"
          >
            <LogOut className="w-3.5 h-3.5" />
            Salir
          </button>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto pb-24">
        <div className="max-w-md mx-auto w-full p-4 sm:p-5 space-y-4">
          {tab === "assignment" && (
            <>
              {!assignment && !loading ? (
                <div className="space-y-4">
                  <div
                    className="relative overflow-hidden rounded-2xl border border-border px-5 pt-8 pb-6"
                    style={{
                      background:
                        "radial-gradient(120% 80% at 50% 0%, rgba(217 119 6 / 0.10), transparent 55%), var(--card)",
                    }}
                  >
                    <div className="relative mx-auto mb-6 flex h-28 w-28 items-center justify-center">
                      <span className="driver-radar-ring absolute inset-0 rounded-full border border-primary/35" />
                      <span className="driver-radar-ring driver-radar-ring-delay absolute inset-2 rounded-full border border-primary/25" />
                      <div className="relative z-10 flex h-16 w-16 items-center justify-center rounded-2xl bg-primary text-primary-foreground shadow-md shadow-primary/25">
                        <Bike className="h-7 w-7" strokeWidth={2.25} />
                      </div>
                    </div>

                    <div className="text-center space-y-2">
                      <h1 className="text-xl font-semibold tracking-tight text-foreground">
                        Esperando pedido
                      </h1>
                      <p className="text-sm text-muted-foreground leading-relaxed max-w-[16.5rem] mx-auto">
                        Estás en línea. Revisamos tu asignación cada pocos segundos y te avisamos
                        con la campana.
                      </p>
                    </div>

                    <button
                      type="button"
                      onClick={toggleAvailability}
                      className={`mt-6 w-full flex items-center justify-between gap-3 rounded-xl border px-4 py-3.5 text-left cursor-pointer transition-colors ${
                        driver?.status === "AVAILABLE"
                          ? "border-emerald-200/80 bg-emerald-50/90"
                          : "border-border bg-muted/60"
                      }`}
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <span
                          className={`relative flex h-2.5 w-2.5 shrink-0 rounded-full ${
                            driver?.status === "AVAILABLE" ? "bg-emerald-600" : "bg-slate-400"
                          }`}
                        >
                          {driver?.status === "AVAILABLE" && (
                            <span className="absolute inset-0 rounded-full bg-emerald-500 animate-ping opacity-40" />
                          )}
                        </span>
                        <div className="min-w-0">
                          <div
                            className={`text-sm font-semibold ${
                              driver?.status === "AVAILABLE"
                                ? "text-emerald-900"
                                : "text-foreground"
                            }`}
                          >
                            {driver?.status === "AVAILABLE" ? "Disponible" : "Pausado"}
                          </div>
                          <div
                            className={`text-[11px] ${
                              driver?.status === "AVAILABLE"
                                ? "text-emerald-800/75"
                                : "text-muted-foreground"
                            }`}
                          >
                            {driver?.status === "AVAILABLE"
                              ? "Listo para recibir asignaciones"
                              : "No recibirás pedidos nuevos"}
                          </div>
                        </div>
                      </div>
                      <span
                        className={`text-[10px] font-mono tracking-wide shrink-0 ${
                          driver?.status === "AVAILABLE"
                            ? "text-emerald-700"
                            : "text-muted-foreground"
                        }`}
                      >
                        Cambiar
                      </span>
                    </button>
                  </div>

                  <div className="grid grid-cols-2 gap-2.5">
                    <div className="rounded-xl border border-border bg-card p-3.5">
                      <div className="text-[9px] font-mono tracking-widest text-muted-foreground">
                        ENTREGAS
                      </div>
                      <div className="text-xl font-bold font-mono tabular-nums text-foreground mt-1">
                        {stats?.deliveriesCompleted ?? "—"}
                      </div>
                      <div className="text-[11px] text-muted-foreground mt-0.5">
                        completadas hoy
                      </div>
                    </div>
                    <div className="rounded-xl border border-border bg-card p-3.5">
                      <div className="text-[9px] font-mono tracking-widest text-muted-foreground">
                        ACEPTACIÓN
                      </div>
                      <div className="text-xl font-bold font-mono tabular-nums text-foreground mt-1">
                        {stats ? `${stats.acceptanceRate}%` : "—"}
                      </div>
                      <div className="text-[11px] text-muted-foreground mt-0.5">
                        histórico reciente
                      </div>
                    </div>
                  </div>

                  <div className="rounded-xl border border-border bg-card px-4 py-3.5 flex items-start gap-3">
                    <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-muted">
                      <Package className="h-4 w-4 text-muted-foreground" />
                    </div>
                    <div className="min-w-0">
                      <div className="text-sm font-medium text-foreground">Tu cola está vacía</div>
                      <p className="text-xs text-muted-foreground mt-0.5 leading-relaxed">
                        Solo verás pedidos asignados a ti. Nada de otros motorizados aparece aquí.
                      </p>
                    </div>
                  </div>
                </div>
              ) : (
                <>
                  <div>
                    <h1 className="text-lg font-semibold text-foreground">Mi asignación</h1>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      Solo ves y operas tu propio pedido
                    </p>
                  </div>

                  {loading && !assignment && (
                    <div className="rounded-xl border border-border bg-card px-4 py-8 text-center">
                      <p className="text-sm text-muted-foreground">Buscando asignación activa…</p>
                    </div>
                  )}

                  {assignmentError && !assignment && (
                    <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                      {assignmentError}. Verifica que el backend esté actualizado.
                    </div>
                  )}

              {assignment?.status === "PENDING" && (
                <div className="rounded-xl border border-primary/35 bg-primary/5 p-4 space-y-3">
                  <div className="text-[10px] font-mono tracking-widest text-primary">
                    ASIGNACIÓN PENDIENTE
                  </div>
                  <p className="text-sm font-medium text-foreground">Tienes un nuevo pedido</p>
                  <AssignmentSummaryCard
                    code={assignment.order.code}
                    customer={assignment.order.customerName}
                    address={assignment.order.deliveryAddress}
                    amount={assignment.order.totalAmount}
                    priority={assignment.order.priority}
                    sla={assignment.order.slaMinutesRemaining}
                    distanceKm={assignment.distanceToStoreKm}
                  />
                  <div className="grid grid-cols-2 gap-2 pt-1">
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => void handleReject()}
                      className="py-3 rounded-xl border border-red-200 bg-red-50 text-red-700 text-sm font-semibold cursor-pointer disabled:opacity-60"
                    >
                      Rechazar
                    </button>
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => void handleAccept()}
                      className="py-3 rounded-xl bg-primary text-primary-foreground text-sm font-semibold cursor-pointer disabled:opacity-60"
                    >
                      Aceptar
                    </button>
                  </div>
                </div>
              )}

              {assignment && assignment.status !== "PENDING" && (
                <div className="space-y-3">
                  <div
                    className="rounded-xl px-4 py-3 flex items-center gap-2"
                    style={{
                      background:
                        status === "IN_TRANSIT"
                          ? "#f8fafc"
                          : status === "ACCEPTED"
                            ? "#fffbeb"
                            : "#f8fafc",
                    }}
                  >
                    <Bike className="w-4 h-4 text-foreground" />
                    <span className="text-sm font-semibold text-foreground">
                      {status === "ACCEPTED" && "Aceptado — ve al local"}
                      {status === "PICKED_UP" && "Recogido — listo para salir"}
                      {status === "IN_TRANSIT" && "En camino al cliente"}
                    </span>
                  </div>

                  <AssignmentSummaryCard
                    code={assignment.order.code}
                    customer={assignment.order.customerName}
                    address={assignment.order.deliveryAddress}
                    amount={assignment.order.totalAmount}
                    priority={assignment.order.priority}
                    sla={assignment.order.slaMinutesRemaining}
                    distanceKm={assignment.distanceToStoreKm}
                    phone={assignment.order.customerPhone}
                  />

                  {mapsTarget && (
                    <a
                      href={mapsTarget.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex items-start gap-3 rounded-xl border border-primary/35 bg-primary/5 p-4 hover:bg-primary/10"
                    >
                      <MapPin className="w-5 h-5 text-primary shrink-0 mt-0.5" />
                      <div className="min-w-0 flex-1">
                        <div className="text-sm font-semibold text-primary">{mapsTarget.label}</div>
                        <div className="text-xs text-foreground mt-0.5 leading-snug">
                          {mapsTarget.address}
                        </div>
                        <div className="inline-flex items-center gap-1 text-[11px] font-mono text-muted-foreground mt-2">
                          Abrir en Google Maps
                          <ExternalLink className="w-3 h-3" />
                        </div>
                      </div>
                    </a>
                  )}

                  {status === "ACCEPTED" && (
                    <div className="flex items-start gap-2 text-xs text-muted-foreground rounded-lg bg-muted/50 px-3 py-2">
                      <Store className="w-3.5 h-3.5 mt-0.5 shrink-0" />
                      Recoge en {RESTAURANT.name}
                    </div>
                  )}

                  {status === "IN_TRANSIT" && (
                    <a
                      href={googleDirectionsUrl(assignment.order.deliveryAddress)}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-[11px] text-muted-foreground underline underline-offset-2"
                    >
                      Ver ruta completa local → cliente
                    </a>
                  )}

                  <div className="space-y-2 pt-1">
                    {nextAction && (
                      <button
                        type="button"
                        disabled={busy}
                        onClick={() => void handleAdvance(nextAction.next)}
                        className="w-full py-4 rounded-2xl text-base font-bold cursor-pointer bg-primary text-primary-foreground shadow-md disabled:opacity-60"
                      >
                        {nextAction.label}
                      </button>
                    )}
                    {status === "IN_TRANSIT" && (
                      <button
                        type="button"
                        onClick={() => setShowFail(true)}
                        className="w-full py-3 rounded-2xl text-sm font-medium cursor-pointer border border-red-200 text-red-700"
                      >
                        Reportar problema
                      </button>
                    )}
                  </div>
                </div>
              )}
                </>
              )}
            </>
          )}

          {tab === "profile" && (
            <>
              <div>
                <h1 className="text-lg font-semibold text-foreground">Mi perfil</h1>
                <p className="text-xs text-muted-foreground mt-0.5">Datos, disponibilidad y GPS</p>
              </div>

              <div className="rounded-xl border border-border bg-card p-4 space-y-3">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-full bg-primary/10 text-primary flex items-center justify-center">
                    <Bike className="w-6 h-6" />
                  </div>
                  <div className="min-w-0">
                    <div className="text-sm font-semibold text-foreground">{user.fullName}</div>
                    <div className="text-xs text-muted-foreground truncate">{user.email}</div>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-3 pt-2 border-t border-border">
                  <div>
                    <div className="text-[9px] font-mono tracking-widest text-muted-foreground">
                      TELÉFONO
                    </div>
                    <div className="text-xs mt-0.5 text-foreground">{driver?.phone ?? "—"}</div>
                  </div>
                  <div>
                    <div className="text-[9px] font-mono tracking-widest text-muted-foreground">
                      PLACA
                    </div>
                    <div className="text-xs mt-0.5 font-mono text-foreground">
                      {driver?.vehiclePlate ?? "—"}
                    </div>
                  </div>
                </div>
              </div>

              <div className="rounded-xl border border-border bg-card p-4 space-y-3">
                <div className="text-[10px] font-mono tracking-widest text-muted-foreground">
                  DISPONIBILIDAD
                </div>
                <button
                  type="button"
                  onClick={toggleAvailability}
                  className={`w-full py-3 rounded-xl text-sm font-semibold cursor-pointer border ${
                    driver?.status === "AVAILABLE"
                      ? "border-green-200 bg-green-50 text-green-800"
                      : "border-border bg-muted text-foreground"
                  }`}
                >
                  {driver?.status === "AVAILABLE"
                    ? "Disponible — tocar para pausar"
                    : "Fuera de línea — tocar para activar"}
                </button>
              </div>

              <div className="rounded-xl border border-border bg-card p-4 space-y-3">
                <div className="text-[10px] font-mono tracking-widest text-muted-foreground">
                  UBICACIÓN
                </div>
                <div className="flex items-start gap-2 text-xs text-foreground">
                  <MapPin className="w-4 h-4 mt-0.5 shrink-0 text-primary" />
                  <div>
                    <div className="font-mono tabular-nums">
                      {lat.toFixed(5)}, {lng.toFixed(5)}
                    </div>
                    {locUpdatedAt && (
                      <div className="text-muted-foreground mt-0.5">
                        Actualizada{" "}
                        {new Date(locUpdatedAt).toLocaleTimeString("es-PE", {
                          hour: "2-digit",
                          minute: "2-digit",
                          second: "2-digit",
                        })}
                      </div>
                    )}
                  </div>
                </div>
                <button
                  type="button"
                  onClick={updateLocation}
                  className="w-full py-3 rounded-xl text-sm font-medium cursor-pointer border border-primary/35 text-primary inline-flex items-center justify-center gap-2 hover:bg-primary/5"
                >
                  <Navigation className="w-4 h-4" />
                  Actualizar mi ubicación
                </button>
              </div>
            </>
          )}
        </div>
      </main>

      <nav className="fixed bottom-0 inset-x-0 border-t border-border bg-card z-40 safe-pb">
        <div className="max-w-md mx-auto grid grid-cols-2">
          {(
            [
              { id: "assignment" as const, label: "Pedido", icon: Package },
              { id: "profile" as const, label: "Perfil", icon: UserRound },
            ] as const
          ).map((item) => {
            const active = tab === item.id;
            const Icon = item.icon;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => onNavigate(tabToPage(item.id))}
                className={`relative flex flex-col items-center gap-1 py-3 text-[11px] font-medium cursor-pointer ${
                  active ? "text-primary" : "text-muted-foreground"
                }`}
              >
                <Icon className="w-5 h-5" />
                {item.label}
                {item.id === "assignment" && unreadPending && (
                  <span className="absolute top-2 right-[32%] w-1.5 h-1.5 rounded-full bg-red-600" />
                )}
              </button>
            );
          })}
        </div>
      </nav>

      {/* Modal oferta PENDING */}
      {offerOpen && assignment?.status === "PENDING" && (
        <div
          className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4"
          style={{ background: "rgba(0 0 0 / 0.45)" }}
        >
          <div className="w-full max-w-md rounded-t-2xl sm:rounded-2xl border border-border bg-card p-5 space-y-4 shadow-xl">
            <div className="flex items-start justify-between gap-3">
              <div>
                <div className="text-[10px] font-mono tracking-widest text-primary">NUEVO PEDIDO</div>
                <h2 className="text-base font-semibold text-foreground mt-1">
                  Tienes un nuevo pedido
                </h2>
              </div>
              <button
                type="button"
                onClick={closeOffer}
                className="p-1.5 rounded-md text-muted-foreground hover:bg-muted cursor-pointer"
                aria-label="Cerrar"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-amber-50 text-amber-900 text-xs">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              SLA: {assignment.order.slaMinutesRemaining} min · {assignment.distanceToStoreKm} km al
              local
            </div>

            <AssignmentSummaryCard
              code={assignment.order.code}
              customer={assignment.order.customerName}
              address={assignment.order.deliveryAddress}
              amount={assignment.order.totalAmount}
              priority={assignment.order.priority}
              sla={assignment.order.slaMinutesRemaining}
              distanceKm={assignment.distanceToStoreKm}
            />

            <div className="grid grid-cols-2 gap-2">
              <button
                type="button"
                disabled={busy}
                onClick={() => void handleReject()}
                className="py-3.5 rounded-xl border border-red-200 bg-red-50 text-red-700 text-sm font-semibold cursor-pointer disabled:opacity-60"
              >
                Rechazar
              </button>
              <button
                type="button"
                disabled={busy}
                onClick={() => void handleAccept()}
                className="py-3.5 rounded-xl bg-primary text-primary-foreground text-sm font-semibold cursor-pointer disabled:opacity-60"
              >
                Aceptar
              </button>
            </div>
          </div>
        </div>
      )}

      {showFail && (
        <div
          className="fixed inset-0 flex items-end justify-center z-50"
          style={{ background: "rgba(0 0 0 / 0.4)" }}
        >
          <div className="w-full max-w-md rounded-t-2xl border-t border-x border-border bg-card p-6 space-y-4">
            <div className="w-8 h-1 rounded-full mx-auto bg-border" />
            <h3 className="text-sm font-semibold text-foreground">Motivo del problema</h3>
            <div className="space-y-2">
              {FAIL_REASONS.map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setFailReason(r)}
                  className={`w-full text-left px-4 py-3 rounded-xl border text-sm cursor-pointer ${
                    failReason === r
                      ? "border-primary bg-primary/5 text-primary font-semibold"
                      : "border-border text-foreground"
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setShowFail(false)}
                className="flex-1 py-3 rounded-xl border border-border text-sm text-muted-foreground cursor-pointer"
              >
                Cancelar
              </button>
              <button
                type="button"
                disabled={!failReason || busy}
                onClick={() => void handleAdvance("FAILED")}
                className="flex-1 py-3 rounded-xl text-sm font-semibold cursor-pointer bg-red-600 text-white disabled:opacity-50"
              >
                Reportar
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && <Toast message={toast} tone={toastTone} onClose={() => setToast("")} />}
    </div>
  );
}

function AssignmentSummaryCard({
  code,
  customer,
  address,
  amount,
  priority,
  sla,
  distanceKm,
  phone,
}: {
  code: string;
  customer: string;
  address: string;
  amount: number;
  priority: string;
  sla: number;
  distanceKm: number;
  phone?: string;
}) {
  return (
    <div className="rounded-xl border border-border bg-card p-4 space-y-3">
      <div className="flex items-center justify-between gap-2">
        <span className="text-[10px] font-mono font-bold tracking-widest text-red-600">
          {priority}
        </span>
        <span className="text-xs font-mono text-primary">{code}</span>
      </div>
      <div>
        <div className="text-[9px] font-mono tracking-widest text-muted-foreground">CLIENTE</div>
        <div className="text-sm font-semibold text-foreground">{customer}</div>
        {phone && (
          <div className="text-xs flex items-center gap-1 text-primary mt-0.5">
            <Phone className="w-3.5 h-3.5" /> {phone}
          </div>
        )}
      </div>
      <div className="border-t border-border pt-3">
        <div className="text-[9px] font-mono tracking-widest text-muted-foreground">ENTREGA</div>
        <div className="text-sm text-foreground leading-snug">{address}</div>
      </div>
      <div className="border-t border-border pt-3 flex items-center justify-between gap-3 text-xs">
        <span className="inline-flex items-center gap-1 text-muted-foreground">
          <MapPin className="w-3.5 h-3.5" />
          {distanceKm} km al local
        </span>
        <span className="inline-flex items-center gap-1 text-muted-foreground">
          <Clock className="w-3.5 h-3.5" />
          {sla < 0 ? "SLA vencido" : `SLA ${sla}m`}
        </span>
        <span className="font-mono font-semibold text-foreground">S/ {amount.toFixed(2)}</span>
      </div>
    </div>
  );
}
