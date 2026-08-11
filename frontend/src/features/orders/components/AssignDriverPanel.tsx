import { useEffect, useState } from "react";
import { Bike, Car, Check, ChevronLeft, LoaderCircle, MapPin, Zap } from "lucide-react";
import type { Driver, VehicleType } from "@/shared/types";
import { listAvailableDrivers } from "@/features/orders/services/orderApi";

const VEHICLE_LABEL: Record<VehicleType, string> = {
  MOTORCYCLE: "Moto",
  BICYCLE: "Bici",
  CAR: "Auto",
};

function VehicleIcon({ type }: { type: VehicleType }) {
  if (type === "CAR") return <Car className="w-4 h-4" />;
  return <Bike className="w-4 h-4" />;
}

/** Disponibles rankeados por cercanía al local (menor distanceKm primero). */
export function rankAvailableDrivers(drivers: Driver[]): Driver[] {
  return drivers
    .filter((d) => d.status === "AVAILABLE" && d.distanceKm != null)
    .sort((a, b) => (a.distanceKm ?? 99) - (b.distanceKm ?? 99));
}

interface AssignDriverPanelProps {
  onBack: () => void;
  onAssign: (driver: Driver) => Promise<void>;
}

export default function AssignDriverPanel({ onBack, onAssign }: AssignDriverPanelProps) {
  const [ranked, setRanked] = useState<Driver[]>([]);
  const [loading, setLoading] = useState(true);
  const [assigningId, setAssigningId] = useState<string | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    void listAvailableDrivers().then((drivers) => { if (active) setRanked(rankAvailableDrivers(drivers)); })
      .catch((cause) => { if (active) setError(cause instanceof Error ? cause.message : "No se pudieron cargar los repartidores."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  async function select(driver: Driver) {
    setAssigningId(driver.id); setError("");
    try { await onAssign(driver); }
    catch (cause) { setError(cause instanceof Error ? cause.message : "No se pudo crear la asignación."); setAssigningId(null); }
  }

  return (
    <div className="flex flex-col flex-1 min-h-0 gap-3">
      <div className="flex items-start gap-2 shrink-0">
        <button
          type="button"
          onClick={onBack}
          className="mt-0.5 p-1 rounded-md text-muted-foreground hover:text-foreground hover:bg-muted cursor-pointer"
          aria-label="Volver"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        <div className="min-w-0">
          <div className="text-sm font-medium text-foreground">Asignación manual</div>
          <p className="text-[11px] text-muted-foreground mt-0.5 leading-relaxed">
            Motorizados disponibles, ordenados por cercanía al local.
          </p>
        </div>
      </div>

      {loading ? (
        <p className="my-auto flex items-center justify-center gap-2 py-6 text-xs text-muted-foreground"><LoaderCircle className="h-4 w-4 animate-spin" />Cargando repartidores…</p>
      ) : error ? (
        <p className="my-auto py-6 text-center text-xs text-red-600">{error}</p>
      ) : ranked.length === 0 ? (
        <p className="text-xs text-muted-foreground my-auto text-center py-6">
          No hay repartidores disponibles ahora.
        </p>
      ) : (
        <ul className="flex flex-col gap-2 overflow-y-auto flex-1 min-h-0 pr-0.5">
          {ranked.map((driver, index) => {
            const isTop = index === 0;
            return (
              <li key={driver.id}>
                <button
                  type="button"
                  onClick={() => void select(driver)}
                  disabled={assigningId !== null}
                  className={`w-full text-left rounded-xl border px-3 py-2.5 cursor-pointer transition-colors ${
                    isTop
                      ? "border-primary/40 bg-primary/5 hover:bg-primary/10"
                      : "border-border hover:bg-muted/60"
                  }`}
                >
                  <div className="flex items-center gap-2.5">
                    <div
                      className={`w-7 h-7 rounded-full flex items-center justify-center text-[11px] font-mono font-semibold shrink-0 ${
                        isTop
                          ? "bg-primary text-primary-foreground"
                          : "bg-muted text-muted-foreground"
                      }`}
                    >
                      {index + 1}
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-1.5 min-w-0">
                        <span className="text-sm font-medium text-foreground truncate">
                          {driver.name}
                        </span>
                        {isTop && (
                          <span className="inline-flex items-center gap-0.5 text-[9px] font-mono tracking-wide text-primary shrink-0">
                            <Zap className="w-2.5 h-2.5" />
                            MÁS CERCA
                          </span>
                        )}
                      </div>
                      <div className="flex flex-wrap items-center gap-x-2 gap-y-0.5 mt-0.5 text-[11px] text-muted-foreground">
                        <span className="inline-flex items-center gap-1">
                          <VehicleIcon type={driver.vehicleType} />
                          {VEHICLE_LABEL[driver.vehicleType]} · {driver.vehiclePlate}
                        </span>
                        <span className="inline-flex items-center gap-1 font-mono tabular-nums">
                          <MapPin className="w-3 h-3" />
                          {driver.distanceKm!.toFixed(1)} km
                        </span>
                        <span className="font-mono tabular-nums">
                          {driver.deliveriesToday} hoy
                        </span>
                      </div>
                    </div>
                    {assigningId === driver.id ? <LoaderCircle className="w-4 h-4 shrink-0 animate-spin text-primary" /> : <Check className="w-4 h-4 shrink-0 text-muted-foreground" />}
                  </div>
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
