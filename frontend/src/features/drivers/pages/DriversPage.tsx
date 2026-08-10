import { useState, useEffect } from "react";
import { MapContainer, TileLayer, Marker, Popup, useMap } from "react-leaflet";
import L from "leaflet";
import { List, Map, Bike, ArrowRight, Store } from "lucide-react";
import { DriverStatusBadge } from "@/shared/components/badges";
import { TableSkeleton } from "@/shared/components/skeleton";
import { mockDrivers } from "@/services/api/mocks/mockData";
import { RESTAURANT } from "@/shared/constants";
import type { Driver, DriverStatus } from "@/shared/types";

delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

const RESTAURANT_LOCATION: [number, number] = [RESTAURANT.lat, RESTAURANT.lng];

/** Posiciones demo alrededor del local Surco (Olguín). */
const DRIVER_POSITIONS: Record<string, [number, number]> = {
  "drv-001": [RESTAURANT.lat - 0.0042, RESTAURANT.lng - 0.0058],
  "drv-002": [RESTAURANT.lat + 0.0031, RESTAURANT.lng + 0.0024],
  "drv-003": [RESTAURANT.lat - 0.0065, RESTAURANT.lng + 0.0041],
  "drv-004": [RESTAURANT.lat + 0.0052, RESTAURANT.lng - 0.0035],
  "drv-005": [RESTAURANT.lat - 0.0088, RESTAURANT.lng - 0.0022],
  "drv-006": [RESTAURANT.lat + 0.0018, RESTAURANT.lng + 0.0062],
};

const STATUS_DOT: Record<DriverStatus, string> = {
  AVAILABLE: "#16a34a",
  ASSIGNED: "#6366f1",
  DELIVERING: "#f59e0b",
  OFFLINE: "#9ca3af",
  SUSPENDED: "#ef4444",
};

const SVG_STORE = `<svg xmlns="http://www.w3.org/2000/svg" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"><path d="m2 7 4.41-4.41A2 2 0 0 1 7.83 2h8.34a2 2 0 0 1 1.42.59L22 7"/><path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/><path d="M15 22v-4a2 2 0 0 0-2-2h-2a2 2 0 0 0-2 2v4"/><path d="M2 7h20"/><path d="M22 7v3a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V7z"/></svg>`;

const SVG_BIKE = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round"><circle cx="18.5" cy="17.5" r="3.5"/><circle cx="5.5" cy="17.5" r="3.5"/><circle cx="15" cy="5" r="1"/><path d="M12 17.5V14l-3-3 4-3 2 3h2"/></svg>`;

function makeStoreIcon() {
  return L.divIcon({
    html: `<div style="
      width:34px;height:34px;background:#d97706;border:2px solid #fff;
      border-radius:8px;display:flex;align-items:center;justify-content:center;
      box-shadow:0 2px 8px rgba(0,0,0,.28);">${SVG_STORE}</div>`,
    className: "",
    iconSize: [34, 34],
    iconAnchor: [17, 17],
    popupAnchor: [0, -20],
  });
}

function makeDriverIcon(status: DriverStatus) {
  const color = STATUS_DOT[status];
  return L.divIcon({
    html: `<div style="
      width:34px;height:34px;background:${color};border:2px solid #fff;
      border-radius:9999px;display:flex;align-items:center;justify-content:center;
      box-shadow:0 2px 8px rgba(0,0,0,.28);">${SVG_BIKE}</div>`,
    className: "",
    iconSize: [34, 34],
    iconAnchor: [17, 17],
    popupAnchor: [0, -20],
  });
}

const restaurantIcon = makeStoreIcon();

function MapFlyTo({ position }: { position: [number, number] }) {
  const map = useMap();
  useEffect(() => { map.flyTo(position, 15, { duration: 0.8 }); }, [position, map]);
  return null;
}

const STATUS_FILTERS: { label: string; statuses: DriverStatus[] | null }[] = [
  { label: 'Todos', statuses: null },
  { label: 'Disponibles', statuses: ['AVAILABLE'] },
  { label: 'Ocupados', statuses: ['ASSIGNED', 'DELIVERING'] },
  { label: 'Fuera de línea', statuses: ['OFFLINE', 'SUSPENDED'] },
];

const VEHICLE_LABEL: Record<string, string> = {
  MOTORCYCLE: 'Moto',
  BICYCLE: 'Bici',
  CAR: 'Auto',
};

export default function DriversPage() {
  const [loading, setLoading] = useState(true);
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [activeTab, setActiveTab] = useState(0);
  const [selected, setSelected] = useState<Driver | null>(null);
  const [flyTo, setFlyTo] = useState<[number, number] | null>(null);
  const [view, setView] = useState<'table' | 'map'>('table');

  useEffect(() => {
    const t = setTimeout(() => { setDrivers(mockDrivers); setLoading(false); }, 750);
    return () => clearTimeout(t);
  }, []);

  const filtered = drivers.filter((d) => {
    const tab = STATUS_FILTERS[activeTab];
    return tab.statuses === null || tab.statuses.includes(d.status);
  });

  function selectDriver(driver: Driver) {
    setSelected((prev) => (prev?.id === driver.id ? null : driver));
    const pos = DRIVER_POSITIONS[driver.id];
    if (pos) setFlyTo(pos);
  }

  return (
    <div className="space-y-4">
      {/* Toolbar */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-0.5 p-1 rounded-md" style={{ background: 'var(--muted)' }}>
          {STATUS_FILTERS.map((tab, i) => (
            <button
              key={i}
              onClick={() => setActiveTab(i)}
              className="px-3 py-1 rounded text-xs font-medium cursor-pointer"
              style={{
                background: activeTab === i ? 'var(--card)' : 'transparent',
                color: activeTab === i ? 'var(--foreground)' : 'var(--muted-foreground)',
                boxShadow: activeTab === i ? '0 1px 3px rgba(0 0 0 / 0.1)' : 'none',
              }}
            >
              {tab.label}
              {!loading && tab.statuses && (
                <span className="ml-1.5 font-mono text-[10px]" style={{ color: 'var(--muted-foreground)' }}>
                  {drivers.filter((d) => tab.statuses!.includes(d.status)).length}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* View toggle */}
        <div className="ml-auto flex items-center gap-0.5 p-1 rounded-md" style={{ background: 'var(--muted)' }}>
          {(['table', 'map'] as const).map((v) => (
            <button
              key={v}
              onClick={() => setView(v)}
              className="px-3 py-1 rounded text-xs font-medium cursor-pointer"
              style={{
                background: view === v ? 'var(--card)' : 'transparent',
                color: view === v ? 'var(--foreground)' : 'var(--muted-foreground)',
                boxShadow: view === v ? '0 1px 3px rgba(0 0 0 / 0.1)' : 'none',
              }}
            >
              <span className="inline-flex items-center gap-1.5">
                {v === 'table' ? <List className="w-3.5 h-3.5" /> : <Map className="w-3.5 h-3.5" />}
                {v === 'table' ? 'Lista' : 'Mapa'}
              </span>
            </button>
          ))}
        </div>
      </div>

      {view === 'map' ? (
        /* ───── MAP VIEW ───── */
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4" style={{ height: '600px' }}>
          {/* Map */}
          <div className="lg:col-span-2 rounded-lg border overflow-hidden" style={{ borderColor: 'var(--border)' }}>
            <MapContainer
              center={RESTAURANT_LOCATION}
              zoom={14}
              style={{ height: '100%', width: '100%' }}
              zoomControl={true}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />

              {/* Restaurant marker */}
              <Marker position={RESTAURANT_LOCATION} icon={restaurantIcon}>
                <Popup>
                  <div className="text-xs" style={{ fontFamily: "DM Sans, sans-serif", minWidth: 160 }}>
                    <strong>{RESTAURANT.name}</strong>
                    <br />
                    {RESTAURANT.address}
                  </div>
                </Popup>
              </Marker>

              {/* Driver markers */}
              {drivers.map((driver) => {
                const pos = DRIVER_POSITIONS[driver.id];
                if (!pos) return null;
                return (
                  <Marker
                    key={driver.id}
                    position={pos}
                    icon={makeDriverIcon(driver.status)}
                    eventHandlers={{ click: () => selectDriver(driver) }}
                  >
                    <Popup>
                      <div className="text-xs" style={{ fontFamily: "DM Sans, sans-serif", minWidth: 160 }}>
                        <strong>{driver.name}</strong>
                        <br />
                        <span style={{ color: STATUS_DOT[driver.status] }}>{driver.status}</span>
                        <br />
                        {driver.vehiclePlate} · {VEHICLE_LABEL[driver.vehicleType]}
                        <br />
                        {driver.activeOrderCode && (
                          <>
                            Pedido: <strong>{driver.activeOrderCode}</strong>
                            <br />
                          </>
                        )}
                        {driver.distanceKm != null && `${driver.distanceKm} km del local`}
                      </div>
                    </Popup>
                  </Marker>
                );
              })}

              {flyTo && <MapFlyTo position={flyTo} />}
            </MapContainer>
          </div>

          {/* Driver list sidebar */}
          <div
            className="rounded-lg border flex flex-col overflow-hidden"
            style={{ background: 'var(--card)', borderColor: 'var(--border)' }}
          >
            <div className="px-4 py-3 border-b text-[10px] font-mono tracking-widest" style={{ color: 'var(--muted-foreground)', borderColor: 'var(--border)' }}>
              MOTORIZADOS — {drivers.length}
            </div>
            <div className="flex-1 overflow-y-auto divide-y" style={{ borderColor: 'var(--border)' }}>
              {drivers.map((driver) => (
                <button
                  key={driver.id}
                  onClick={() => selectDriver(driver)}
                  className="w-full flex items-center gap-3 px-4 py-3 text-left cursor-pointer"
                  style={{
                    background: selected?.id === driver.id ? 'rgba(217 119 6 / 0.05)' : 'transparent',
                    borderColor: 'var(--border)',
                  }}
                  onMouseEnter={(e) => { if (selected?.id !== driver.id) (e.currentTarget as HTMLElement).style.background = 'var(--muted)'; }}
                  onMouseLeave={(e) => { if (selected?.id !== driver.id) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                >
                  <div
                    className="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shrink-0"
                    style={{ background: STATUS_DOT[driver.status] + '20', color: STATUS_DOT[driver.status] }}
                  >
                    {driver.name.split(' ').map((n) => n[0]).join('').slice(0, 2)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="text-xs font-medium truncate" style={{ color: 'var(--foreground)' }}>{driver.name}</div>
                    <div className="text-[11px] font-mono" style={{ color: 'var(--muted-foreground)' }}>{driver.vehiclePlate}</div>
                  </div>
                  <DriverStatusBadge status={driver.status} />
                </button>
              ))}
            </div>

            {/* Legend */}
            <div className="px-4 py-3 border-t space-y-1.5" style={{ borderColor: "var(--border)" }}>
              <div
                className="text-[9px] font-mono tracking-widest mb-2"
                style={{ color: "var(--muted-foreground)" }}
              >
                LEYENDA
              </div>
              {[
                { color: "#16a34a", label: "Disponible" },
                { color: "#f59e0b", label: "En entrega" },
                { color: "#6366f1", label: "Asignado" },
                { color: "#9ca3af", label: "Fuera de línea" },
              ].map((l) => (
                <div
                  key={l.label}
                  className="flex items-center gap-2 text-[11px]"
                  style={{ color: "var(--muted-foreground)" }}
                >
                  <div
                    className="w-5 h-5 rounded-full flex items-center justify-center shrink-0"
                    style={{ background: l.color }}
                  >
                    <Bike className="w-3 h-3 text-white" strokeWidth={2.5} />
                  </div>
                  {l.label}
                </div>
              ))}
              <div
                className="flex items-center gap-2 text-[11px]"
                style={{ color: "var(--muted-foreground)" }}
              >
                <div className="w-5 h-5 rounded-md flex items-center justify-center shrink-0 bg-primary">
                  <Store className="w-3 h-3 text-primary-foreground" strokeWidth={2.5} />
                </div>
                Local / Restaurante
              </div>
            </div>
          </div>
        </div>
      ) : (
        /* ───── TABLE VIEW ───── */
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="lg:col-span-2 rounded-lg border overflow-hidden" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
            {/* Header */}
            <div
              className="grid text-[10px] font-mono tracking-widest px-4 py-2.5 border-b"
              style={{ color: 'var(--muted-foreground)', borderColor: 'var(--border)', gridTemplateColumns: '1fr 100px 120px 90px 70px' }}
            >
              <span>MOTORIZADO</span>
              <span>PLACA</span>
              <span>ESTADO</span>
              <span>PEDIDO</span>
              <span>HOY</span>
            </div>

            {loading ? (
              <TableSkeleton rows={6} />
            ) : filtered.length === 0 ? (
              <div className="py-16 text-center">
                <p className="text-sm" style={{ color: 'var(--muted-foreground)' }}>Sin resultados.</p>
              </div>
            ) : (
              filtered.map((driver, i) => (
                <button
                  key={driver.id}
                  onClick={() => selectDriver(driver)}
                  className="w-full grid items-center px-4 py-3 border-b text-left cursor-pointer"
                  style={{
                    gridTemplateColumns: '1fr 100px 120px 90px 70px',
                    borderColor: 'var(--border)',
                    borderBottomWidth: i === filtered.length - 1 ? '0' : '1px',
                    background: selected?.id === driver.id ? 'rgba(217 119 6 / 0.04)' : 'transparent',
                  }}
                  onMouseEnter={(e) => { if (selected?.id !== driver.id) (e.currentTarget as HTMLElement).style.background = 'var(--muted)'; }}
                  onMouseLeave={(e) => { if (selected?.id !== driver.id) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                >
                  <div className="flex items-center gap-3">
                    <div
                      className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0"
                      style={{ background: STATUS_DOT[driver.status] + '20', color: STATUS_DOT[driver.status] }}
                    >
                      {driver.name.split(' ').map((n) => n[0]).join('').slice(0, 2)}
                    </div>
                    <div>
                      <div className="text-xs font-medium" style={{ color: 'var(--foreground)' }}>{driver.name}</div>
                      <div className="text-[11px]" style={{ color: 'var(--muted-foreground)' }}>{driver.phone}</div>
                    </div>
                  </div>
                  <span className="text-xs font-mono font-medium" style={{ color: 'var(--foreground)' }}>
                    {driver.vehiclePlate}
                  </span>
                  <span><DriverStatusBadge status={driver.status} /></span>
                  <span className="text-[11px] font-mono truncate" style={{ color: driver.activeOrderCode ? '#d97706' : 'var(--muted-foreground)' }}>
                    {driver.activeOrderCode ?? '—'}
                  </span>
                  <span className="text-xs font-mono font-semibold" style={{ color: 'var(--foreground)' }}>
                    {driver.deliveriesToday}
                  </span>
                </button>
              ))
            )}
          </div>

          {/* Detail panel */}
          <div className="rounded-lg border" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
            {selected ? (
              <div className="p-5 space-y-4">
                <div className="flex items-center gap-3">
                  <div
                    className="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold"
                    style={{ background: STATUS_DOT[selected.status] + '20', color: STATUS_DOT[selected.status] }}
                  >
                    {selected.name.split(' ').map((n) => n[0]).join('').slice(0, 2)}
                  </div>
                  <div>
                    <div className="text-sm font-semibold" style={{ color: 'var(--foreground)' }}>{selected.name}</div>
                    <DriverStatusBadge status={selected.status} />
                  </div>
                </div>

                <div className="space-y-3 pt-2 border-t" style={{ borderColor: 'var(--border)' }}>
                  {[
                    { label: 'TELÉFONO', value: selected.phone },
                    { label: 'VEHÍCULO', value: `${VEHICLE_LABEL[selected.vehicleType]}` },
                    { label: 'PLACA', value: selected.vehiclePlate },
                    { label: 'ENTREGAS HOY', value: `${selected.deliveriesToday} pedidos` },
                    { label: 'PEDIDO ACTIVO', value: selected.activeOrderCode ?? 'Ninguno' },
                    { label: 'DISTANCIA', value: selected.distanceKm ? `${selected.distanceKm} km del local` : '—' },
                    {
                      label: 'ÚLTIMA ACTUALIZ.',
                      value: new Date(selected.lastLocationAt).toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }),
                    },
                  ].map((f) => (
                    <div key={f.label}>
                      <div className="text-[9px] font-mono tracking-widest" style={{ color: 'var(--muted-foreground)' }}>{f.label}</div>
                      <div className="text-xs mt-0.5" style={{ color: 'var(--foreground)' }}>{f.value}</div>
                    </div>
                  ))}
                </div>

                <div className="pt-3 border-t space-y-2" style={{ borderColor: 'var(--border)' }}>
                  <div className="text-[9px] font-mono tracking-widest" style={{ color: 'var(--muted-foreground)' }}>ACCIONES</div>
                  {selected.status === 'OFFLINE' && (
                    <button
                      className="w-full py-2 rounded border text-xs font-medium cursor-pointer"
                      style={{ borderColor: '#bbf7d0', color: '#15803d', background: '#f0fdf4' }}
                      onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.background = '#dcfce7'}
                      onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.background = '#f0fdf4'}
                      onClick={() => {
                        setDrivers((prev) => prev.map((d) => d.id === selected.id ? { ...d, status: 'AVAILABLE' } : d));
                        setSelected((prev) => prev ? { ...prev, status: 'AVAILABLE' } : null);
                      }}
                    >
                      <span className="inline-flex items-center justify-center gap-1.5">
                        Activar motorizado <ArrowRight className="w-3 h-3" />
                      </span>
                    </button>
                  )}
                  {selected.status === 'AVAILABLE' && (
                    <button
                      className="w-full py-2 rounded border text-xs font-medium cursor-pointer"
                      style={{ borderColor: '#fecaca', color: '#b91c1c', background: '#fef2f2' }}
                      onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.background = '#fee2e2'}
                      onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.background = '#fef2f2'}
                      onClick={() => {
                        setDrivers((prev) => prev.map((d) => d.id === selected.id ? { ...d, status: 'OFFLINE' } : d));
                        setSelected((prev) => prev ? { ...prev, status: 'OFFLINE' } : null);
                      }}
                    >
                      <span className="inline-flex items-center justify-center gap-1.5">
                        Desactivar <ArrowRight className="w-3 h-3" />
                      </span>
                    </button>
                  )}
                  <button
                    className="w-full py-2 rounded border text-xs font-medium cursor-pointer"
                    style={{ borderColor: 'var(--border)', color: 'var(--muted-foreground)' }}
                    onClick={() => setView('map')}
                    onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.background = 'var(--muted)'}
                    onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.background = 'transparent'}
                  >
                    <span className="inline-flex items-center justify-center gap-1.5">
                      Ver en mapa <ArrowRight className="w-3 h-3" />
                    </span>
                  </button>
                </div>
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center h-full py-16 px-6 text-center">
                <Bike className="w-8 h-8 mb-3" style={{ color: 'var(--muted-foreground)' }} />
                <p className="text-xs" style={{ color: 'var(--muted-foreground)' }}>Selecciona un motorizado para ver su detalle</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
