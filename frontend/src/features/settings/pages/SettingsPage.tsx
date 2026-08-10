import { useState, type ReactNode } from "react";
import type { LucideIcon } from "lucide-react";
import {
  Store,
  Timer,
  Cpu,
  SlidersHorizontal,
  Plus,
  Minus,
} from "lucide-react";
import { Toast } from "@/shared/components/toast";

interface NumberInputProps {
  label: string;
  value: number;
  unit?: string;
  min?: number;
  max?: number;
  step?: number;
  onChange: (v: number) => void;
  description?: string;
}

function NumberInput({
  label,
  value,
  unit,
  min = 0,
  max = 999,
  step = 1,
  onChange,
  description,
}: NumberInputProps) {
  return (
    <div
      className="flex items-start justify-between gap-4 py-3.5 border-b last:border-0"
      style={{ borderColor: "var(--border)" }}
    >
      <div className="flex-1">
        <div className="text-sm font-medium" style={{ color: "var(--foreground)" }}>
          {label}
        </div>
        {description && (
          <div className="text-xs mt-0.5" style={{ color: "var(--muted-foreground)" }}>
            {description}
          </div>
        )}
      </div>
      <div className="flex items-center gap-2 shrink-0">
        <button
          onClick={() => onChange(Math.max(min, value - step))}
          className="w-7 h-7 rounded border flex items-center justify-center cursor-pointer"
          style={{ borderColor: "var(--border)", color: "var(--muted-foreground)" }}
          onMouseEnter={(e) =>
            ((e.currentTarget as HTMLElement).style.background = "var(--muted)")
          }
          onMouseLeave={(e) =>
            ((e.currentTarget as HTMLElement).style.background = "transparent")
          }
          aria-label="Disminuir"
        >
          <Minus className="w-3.5 h-3.5" />
        </button>
        <div className="flex items-center gap-1">
          <span
            className="text-sm font-mono font-semibold w-10 text-center"
            style={{ color: "var(--foreground)" }}
          >
            {value}
          </span>
          {unit && (
            <span className="text-xs" style={{ color: "var(--muted-foreground)" }}>
              {unit}
            </span>
          )}
        </div>
        <button
          onClick={() => onChange(Math.min(max, value + step))}
          className="w-7 h-7 rounded border flex items-center justify-center cursor-pointer"
          style={{ borderColor: "var(--border)", color: "var(--muted-foreground)" }}
          onMouseEnter={(e) =>
            ((e.currentTarget as HTMLElement).style.background = "var(--muted)")
          }
          onMouseLeave={(e) =>
            ((e.currentTarget as HTMLElement).style.background = "transparent")
          }
          aria-label="Aumentar"
        >
          <Plus className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
}

function ToggleInput({
  label,
  description,
  value,
  onChange,
}: {
  label: string;
  description?: string;
  value: boolean;
  onChange: (v: boolean) => void;
}) {
  return (
    <div
      className="flex items-start justify-between gap-4 py-3.5 border-b last:border-0"
      style={{ borderColor: "var(--border)" }}
    >
      <div className="flex-1">
        <div className="text-sm font-medium" style={{ color: "var(--foreground)" }}>
          {label}
        </div>
        {description && (
          <div className="text-xs mt-0.5" style={{ color: "var(--muted-foreground)" }}>
            {description}
          </div>
        )}
      </div>
      <button
        role="switch"
        aria-checked={value}
        onClick={() => onChange(!value)}
        className="relative w-10 h-5 rounded-full flex-shrink-0 cursor-pointer transition-colors"
        style={{ background: value ? "var(--primary)" : "#d1d5db" }}
      >
        <span
          className="absolute top-0.5 w-4 h-4 rounded-full shadow-sm transition-transform"
          style={{ background: "#fff", left: value ? "22px" : "2px" }}
        />
      </button>
    </div>
  );
}

function WeightSlider({
  label,
  value,
  onChange,
  description,
}: {
  label: string;
  value: number;
  onChange: (v: number) => void;
  description?: string;
}) {
  return (
    <div className="py-3.5 border-b last:border-0" style={{ borderColor: "var(--border)" }}>
      <div className="flex items-center justify-between mb-2">
        <div>
          <div className="text-sm font-medium" style={{ color: "var(--foreground)" }}>
            {label}
          </div>
          {description && (
            <div className="text-xs" style={{ color: "var(--muted-foreground)" }}>
              {description}
            </div>
          )}
        </div>
        <span className="text-sm font-mono font-bold" style={{ color: "var(--primary)" }}>
          {value}%
        </span>
      </div>
      <input
        type="range"
        min={0}
        max={100}
        step={5}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full cursor-pointer"
        style={{ accentColor: "var(--primary)" }}
      />
    </div>
  );
}

function Card({
  title,
  icon: Icon,
  children,
}: {
  title: string;
  icon: LucideIcon;
  children: ReactNode;
}) {
  return (
    <div
      className="rounded-lg border"
      style={{ background: "var(--card)", borderColor: "var(--border)" }}
    >
      <div
        className="flex items-center gap-2.5 px-5 py-4 border-b"
        style={{ borderColor: "var(--border)" }}
      >
        <Icon className="w-4 h-4" style={{ color: "var(--primary)" }} />
        <span
          className="text-[10px] font-mono tracking-widest"
          style={{ color: "var(--muted-foreground)" }}
        >
          {title}
        </span>
      </div>
      <div className="px-5">{children}</div>
    </div>
  );
}

export default function SettingsPage() {
  const [toast, setToast] = useState("");

  const [slaNormal, setSlaNormal] = useState(45);
  const [slaHigh, setSlaHigh] = useState(35);
  const [slaCritical, setSlaCritical] = useState(25);
  const [slaWarning, setSlaWarning] = useState(10);

  const [wDistance, setWDistance] = useState(50);
  const [wWorkload, setWWorkload] = useState(20);
  const [wDelay, setWDelay] = useState(30);

  const [acceptTimeout, setAcceptTimeout] = useState(60);
  const [maxRetries, setMaxRetries] = useState(3);
  const [maxActiveOrders, setMaxActiveOrders] = useState(2);

  const [autoAssign, setAutoAssign] = useState(true);
  const [autoPriority, setAutoPriority] = useState(true);
  const [slaAlerts, setSlaAlerts] = useState(true);
  const [driverSim, setDriverSim] = useState(false);
  const [pollingEnabled, setPolling] = useState(true);
  const [pollingInterval, setPollingInterval] = useState(15);

  const [restName, setRestName] = useState("FleetBite — Lima");
  const [restLat, setRestLat] = useState("-12.1191");
  const [restLng, setRestLng] = useState("-77.0292");
  const [restPhone, setRestPhone] = useState("+51 1 234 5678");

  const totalWeight = wDistance + wWorkload + wDelay;

  function save() {
    setToast("Configuración guardada correctamente");
    setTimeout(() => setToast(""), 3000);
  }

  return (
    <div className="space-y-5 max-w-3xl">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-base font-semibold" style={{ color: "var(--foreground)" }}>
            Configuración del sistema
          </h2>
          <p className="text-xs mt-0.5" style={{ color: "var(--muted-foreground)" }}>
            Parámetros operativos, SLA y motor de asignación
          </p>
        </div>
        <button
          onClick={save}
          className="px-4 py-2 rounded text-sm font-semibold cursor-pointer"
          style={{ background: "var(--primary)", color: "var(--primary-foreground)" }}
          onMouseEnter={(e) => ((e.currentTarget as HTMLElement).style.opacity = "0.85")}
          onMouseLeave={(e) => ((e.currentTarget as HTMLElement).style.opacity = "1")}
        >
          Guardar cambios
        </button>
      </div>

      <Card title="INFORMACIÓN DEL LOCAL" icon={Store}>
        <div className="grid grid-cols-2 gap-4 py-4">
          {(
            [
              {
                label: "Nombre del local",
                key: "name",
                value: restName,
                set: setRestName,
                wide: true,
              },
              {
                label: "Teléfono",
                key: "phone",
                value: restPhone,
                set: setRestPhone,
                wide: false,
              },
            ] as {
              label: string;
              key: string;
              value: string;
              set: (v: string) => void;
              wide: boolean;
            }[]
          ).map((f) => (
            <div key={f.key} className={f.wide ? "col-span-2" : ""}>
              <label
                className="text-[10px] font-mono tracking-widest block mb-1.5"
                style={{ color: "var(--muted-foreground)" }}
              >
                {f.label.toUpperCase()}
              </label>
              <input
                type="text"
                value={f.value}
                onChange={(e) => f.set(e.target.value)}
                className="w-full px-3 py-2 rounded border text-sm outline-none"
                style={{
                  background: "var(--muted)",
                  borderColor: "var(--border)",
                  color: "var(--foreground)",
                }}
                onFocus={(e) => (e.currentTarget.style.borderColor = "var(--primary)")}
                onBlur={(e) => (e.currentTarget.style.borderColor = "var(--border)")}
              />
            </div>
          ))}
          <div>
            <label
              className="text-[10px] font-mono tracking-widest block mb-1.5"
              style={{ color: "var(--muted-foreground)" }}
            >
              LATITUD
            </label>
            <input
              type="text"
              value={restLat}
              onChange={(e) => setRestLat(e.target.value)}
              className="w-full px-3 py-2 rounded border text-sm outline-none font-mono"
              style={{
                background: "var(--muted)",
                borderColor: "var(--border)",
                color: "var(--foreground)",
              }}
              onFocus={(e) => (e.currentTarget.style.borderColor = "var(--primary)")}
              onBlur={(e) => (e.currentTarget.style.borderColor = "var(--border)")}
            />
          </div>
          <div>
            <label
              className="text-[10px] font-mono tracking-widest block mb-1.5"
              style={{ color: "var(--muted-foreground)" }}
            >
              LONGITUD
            </label>
            <input
              type="text"
              value={restLng}
              onChange={(e) => setRestLng(e.target.value)}
              className="w-full px-3 py-2 rounded border text-sm outline-none font-mono"
              style={{
                background: "var(--muted)",
                borderColor: "var(--border)",
                color: "var(--foreground)",
              }}
              onFocus={(e) => (e.currentTarget.style.borderColor = "var(--primary)")}
              onBlur={(e) => (e.currentTarget.style.borderColor = "var(--border)")}
            />
          </div>
        </div>
      </Card>

      <Card title="REGLAS DE SLA" icon={Timer}>
        <NumberInput
          label="SLA — Prioridad NORMAL"
          value={slaNormal}
          unit="min"
          min={10}
          max={120}
          onChange={setSlaNormal}
          description="Tiempo máximo de entrega para pedidos normales"
        />
        <NumberInput
          label="SLA — Prioridad HIGH"
          value={slaHigh}
          unit="min"
          min={10}
          max={90}
          onChange={setSlaHigh}
          description="Tiempo máximo para pedidos de alta prioridad"
        />
        <NumberInput
          label="SLA — Prioridad CRITICAL"
          value={slaCritical}
          unit="min"
          min={5}
          max={60}
          onChange={setSlaCritical}
          description="Tiempo máximo para pedidos críticos"
        />
        <NumberInput
          label="Umbral de alerta AT_RISK"
          value={slaWarning}
          unit="min"
          min={3}
          max={30}
          onChange={setSlaWarning}
          description="Minutos restantes para marcar un pedido como en riesgo"
        />
      </Card>

      <Card title="MOTOR DE ASIGNACIÓN" icon={Cpu}>
        <div className="py-2">
          <p className="text-xs pb-3" style={{ color: "var(--muted-foreground)" }}>
            Los pesos determinan la importancia relativa de cada factor al calcular el score
            de asignación. Total actual:{" "}
            <span
              className="font-mono font-bold"
              style={{ color: totalWeight === 100 ? "#15803d" : "#b91c1c" }}
            >
              {totalWeight}%
            </span>
            {totalWeight !== 100 && (
              <span style={{ color: "#b91c1c" }}> (debe sumar 100%)</span>
            )}
          </p>
          <WeightSlider
            label="Peso — Distancia"
            value={wDistance}
            onChange={setWDistance}
            description="Proximidad del motorizado al punto de recojo"
          />
          <WeightSlider
            label="Peso — Carga de trabajo"
            value={wWorkload}
            onChange={setWWorkload}
            description="Pedidos activos que ya tiene el motorizado"
          />
          <WeightSlider
            label="Peso — Riesgo de retraso"
            value={wDelay}
            onChange={setWDelay}
            description="Tiempo restante del SLA del pedido"
          />
        </div>
        <NumberInput
          label="Timeout de aceptación"
          value={acceptTimeout}
          unit="seg"
          min={15}
          max={300}
          step={15}
          onChange={setAcceptTimeout}
          description="Segundos que tiene el motorizado para aceptar antes de reasignar"
        />
        <NumberInput
          label="Máx. reintentos de asignación"
          value={maxRetries}
          min={1}
          max={10}
          onChange={setMaxRetries}
          description="Intentos antes de pasar a WAITING_FOR_DRIVER"
        />
        <NumberInput
          label="Máx. pedidos activos por motorizado"
          value={maxActiveOrders}
          min={1}
          max={5}
          onChange={setMaxActiveOrders}
          description="Límite de pedidos simultáneos permitidos"
        />
      </Card>

      <Card title="OPCIONES OPERATIVAS" icon={SlidersHorizontal}>
        <ToggleInput
          label="Asignación automática"
          description="Asignar automáticamente al recibir ORDER_READY"
          value={autoAssign}
          onChange={setAutoAssign}
        />
        <ToggleInput
          label="Prioridad automática"
          description="Elevar prioridad de pedidos cercanos al SLA"
          value={autoPriority}
          onChange={setAutoPriority}
        />
        <ToggleInput
          label="Alertas de SLA"
          description="Mostrar alertas en dashboard cuando un pedido está AT_RISK"
          value={slaAlerts}
          onChange={setSlaAlerts}
        />
        <ToggleInput
          label="Simulador de motorizados"
          description="Habilitar driver-simulator para demo (mueve posiciones automáticamente)"
          value={driverSim}
          onChange={setDriverSim}
        />
        <ToggleInput
          label="Polling automático"
          description="Refrescar datos operativos periódicamente"
          value={pollingEnabled}
          onChange={setPolling}
        />
        {pollingEnabled && (
          <NumberInput
            label="Intervalo de polling"
            value={pollingInterval}
            unit="seg"
            min={5}
            max={60}
            step={5}
            onChange={setPollingInterval}
            description="Frecuencia de actualización del dashboard y despacho"
          />
        )}
      </Card>

      <div className="flex justify-end pb-4">
        <button
          onClick={save}
          className="px-6 py-2.5 rounded text-sm font-semibold cursor-pointer"
          style={{ background: "var(--primary)", color: "var(--primary-foreground)" }}
          onMouseEnter={(e) => ((e.currentTarget as HTMLElement).style.opacity = "0.85")}
          onMouseLeave={(e) => ((e.currentTarget as HTMLElement).style.opacity = "1")}
        >
          Guardar cambios
        </button>
      </div>

      {toast && <Toast message={toast} onClose={() => setToast("")} />}
    </div>
  );
}
