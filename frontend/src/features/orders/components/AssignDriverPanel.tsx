import { Bike, Car, Check, ChevronLeft, MapPin, Zap } from "lucide-react";
import type { Driver, VehicleType } from "@/shared/types";
import { mockDrivers } from "@/services/api/mocks/mockData";

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
export function rankAvailableDrivers(drivers: Driver[] = mockDrivers): Driver[] {
  return drivers
    .filter((d) => d.status === "AVAILABLE" && d.distanceKm != null)
    .sort((a, b) => (a.distanceKm ?? 99) - (b.distanceKm ?? 99));
}

export function closestAvailableDriver(drivers: Driver[] = mockDrivers): Driver | null {
  return rankAvailableDrivers(drivers)[0] ?? null;
}

interface AssignDriverPanelProps {
  onBack: () => void;
  onAssign: (driver: Driver) => void;
}

export default function AssignDriverPanel({ onBack, onAssign }: AssignDriverPanelProps) {
  const ranked = rankAvailableDrivers();

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

      {ranked.length === 0 ? (
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
                  onClick={() => onAssign(driver)}
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
                    <Check className="w-4 h-4 shrink-0 text-muted-foreground" />
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
