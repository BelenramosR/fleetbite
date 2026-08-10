import { useState, useEffect } from "react";
import {
  X,
  Check,
  ArrowRight,
  Pencil,
  Car,
  UserPlus,
  UserMinus,
  Bike,
} from "lucide-react";
import { TableSkeleton } from "@/shared/components/skeleton";
import { Toast } from "@/shared/components/toast";
import { mockVehicles, mockDrivers } from "@/services/api/mocks/mockData";
import type { Vehicle, VehicleStatus, VehicleType, Driver } from "@/shared/types";

const STATUS_CFG: Record<VehicleStatus, { label: string; bg: string; text: string; dot: string }> = {
  AVAILABLE:   { label: 'Disponible',   bg: '#f0fdf4', text: '#15803d', dot: '#16a34a' },
  IN_USE:      { label: 'En uso',       bg: '#eef2ff', text: '#4338ca', dot: '#6366f1' },
  MAINTENANCE: { label: 'Mantenimiento',bg: '#fffbeb', text: '#b45309', dot: '#f59e0b' },
  INACTIVE:    { label: 'Inactivo',     bg: '#f1f5f9', text: '#64748b', dot: '#94a3b8' },
};

const TYPE_ICON: Record<VehicleType, string> = {
  MOTORCYCLE: 'MOTO',
  BICYCLE: 'BICI',
  CAR: 'AUTO',
};

const TYPE_LABEL: Record<VehicleType, string> = {
  MOTORCYCLE: 'Moto',
  BICYCLE: 'Bicicleta',
  CAR: 'Auto',
};

function VehicleStatusBadge({ status }: { status: VehicleStatus }) {
  const cfg = STATUS_CFG[status];
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

const FILTER_TABS: { label: string; statuses: VehicleStatus[] | null }[] = [
  { label: 'Todos', statuses: null },
  { label: 'Disponibles', statuses: ['AVAILABLE'] },
  { label: 'En uso', statuses: ['IN_USE'] },
  { label: 'Mantenimiento', statuses: ['MAINTENANCE'] },
  { label: 'Inactivos', statuses: ['INACTIVE'] },
];

const EMPTY_VEHICLE: Omit<Vehicle, 'id'> = {
  plate: '', type: 'MOTORCYCLE', brand: '', model: '', status: 'AVAILABLE',
};

export default function FleetPage() {
  const [loading, setLoading] = useState(true);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [activeTab, setActiveTab] = useState(0);
  const [selected, setSelected] = useState<Vehicle | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState<Omit<Vehicle, 'id'>>(EMPTY_VEHICLE);
  const [editId, setEditId] = useState<string | null>(null);
  const [toast, setToast] = useState("");
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null);
  const [assignTarget, setAssignTarget] = useState<Vehicle | null>(null);
  const [selectedDriverId, setSelectedDriverId] = useState<string>("");

  useEffect(() => {
    const t = setTimeout(() => {
      setVehicles(mockVehicles.map((v) => ({ ...v })));
      setLoading(false);
    }, 700);
    return () => clearTimeout(t);
  }, []);

  function showToast(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(""), 3000);
  }

  function openAssign(vehicle: Vehicle) {
    if (vehicle.status === "MAINTENANCE" || vehicle.status === "INACTIVE") {
      showToast("No se puede asignar un vehículo en mantenimiento o inactivo");
      return;
    }
    setAssignTarget(vehicle);
    setSelectedDriverId(vehicle.driverId ?? "");
  }

  function handleAssignDriver() {
    if (!assignTarget || !selectedDriverId) return;
    const driver = mockDrivers.find((d) => d.id === selectedDriverId);
    if (!driver) return;

    setVehicles((prev) =>
      prev.map((v) => {
        // Liberar el vehículo si el motorizado ya tenía otro asignado
        if (v.driverId === driver.id && v.id !== assignTarget.id) {
          return {
            ...v,
            driverId: undefined,
            driverName: undefined,
            status: v.status === "IN_USE" ? "AVAILABLE" : v.status,
          };
        }
        if (v.id === assignTarget.id) {
          return {
            ...v,
            driverId: driver.id,
            driverName: driver.name,
            status: "IN_USE",
          };
        }
        return v;
      }),
    );

    const updated: Vehicle = {
      ...assignTarget,
      driverId: driver.id,
      driverName: driver.name,
      status: "IN_USE",
    };
    setSelected((prev) => (prev?.id === assignTarget.id ? updated : prev));
    setAssignTarget(null);
    setSelectedDriverId("");
    showToast(`Vehículo ${assignTarget.plate} asignado a ${driver.name}`);
  }

  function handleUnassignDriver(vehicle: Vehicle) {
    if (!vehicle.driverId) return;
    const name = vehicle.driverName;
    setVehicles((prev) =>
      prev.map((v) =>
        v.id === vehicle.id
          ? {
              ...v,
              driverId: undefined,
              driverName: undefined,
              status: v.status === "IN_USE" ? "AVAILABLE" : v.status,
            }
          : v,
      ),
    );
    setSelected((prev) =>
      prev?.id === vehicle.id
        ? {
            ...prev,
            driverId: undefined,
            driverName: undefined,
            status: prev.status === "IN_USE" ? "AVAILABLE" : prev.status,
          }
        : prev,
    );
    showToast(
      name
        ? `Motorizado ${name} desasignado de ${vehicle.plate}`
        : `Vehículo ${vehicle.plate} sin motorizado`,
    );
  }

  /** Drivers elegibles: no suspendidos; prioriza sin vehículo o el ya asignado a este. */
  function driversForAssign(vehicle: Vehicle): Driver[] {
    const assignedDriverIds = new Set(
      vehicles.filter((v) => v.driverId && v.id !== vehicle.id).map((v) => v.driverId!),
    );
    return mockDrivers
      .filter((d) => d.status !== "SUSPENDED")
      .slice()
      .sort((a, b) => {
        const aBusy = assignedDriverIds.has(a.id) ? 1 : 0;
        const bBusy = assignedDriverIds.has(b.id) ? 1 : 0;
        if (aBusy !== bBusy) return aBusy - bBusy;
        return a.name.localeCompare(b.name);
      });
  }

  function openCreate() {
    setForm(EMPTY_VEHICLE);
    setEditId(null);
    setShowModal(true);
  }

  function openEdit(v: Vehicle) {
    setForm({ plate: v.plate, type: v.type, brand: v.brand, model: v.model, status: v.status, driverName: v.driverName, driverId: v.driverId });
    setEditId(v.id);
    setShowModal(true);
  }

  function handleSave() {
    if (!form.plate || !form.brand || !form.model) return;
    if (editId) {
      setVehicles((prev) => prev.map((v) => v.id === editId ? { ...v, ...form } : v));
      if (selected?.id === editId) setSelected((prev) => prev ? { ...prev, ...form } : null);
      showToast('Vehículo actualizado');
    } else {
      const newV: Vehicle = { id: `veh-${Date.now()}`, ...form };
      setVehicles((prev) => [newV, ...prev]);
      showToast('Vehículo registrado');
    }
    setShowModal(false);
  }

  function handleDelete(id: string) {
    setVehicles((prev) => prev.filter((v) => v.id !== id));
    if (selected?.id === id) setSelected(null);
    setConfirmDelete(null);
    showToast('Vehículo eliminado');
  }

  const filtered = vehicles.filter((v) => {
    const tab = FILTER_TABS[activeTab];
    return tab.statuses === null || tab.statuses.includes(v.status);
  });

  const stats = {
    total: vehicles.length,
    available: vehicles.filter((v) => v.status === 'AVAILABLE').length,
    inUse: vehicles.filter((v) => v.status === 'IN_USE').length,
    maintenance: vehicles.filter((v) => v.status === 'MAINTENANCE').length,
  };

  return (
    <div className="space-y-4">
      {/* Stats strip */}
      <div className="grid grid-cols-4 gap-3">
        {[
          { label: 'TOTAL', value: stats.total },
          { label: 'EN USO', value: stats.inUse, color: '#4338ca' },
          { label: 'DISPONIBLES', value: stats.available, color: '#15803d' },
          { label: 'MANTENIMIENTO', value: stats.maintenance, color: '#b45309' },
        ].map((s) => (
          <div
            key={s.label}
            className="rounded-lg border px-4 py-3"
            style={{ background: 'var(--card)', borderColor: 'var(--border)' }}
          >
            <div className="text-[10px] font-mono tracking-widest mb-1" style={{ color: 'var(--muted-foreground)' }}>{s.label}</div>
            <div className="text-2xl font-bold font-mono" style={{ color: s.color ?? 'var(--foreground)' }}>{s.value}</div>
          </div>
        ))}
      </div>

      {/* Toolbar */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-0.5 p-1 rounded-md" style={{ background: 'var(--muted)' }}>
          {FILTER_TABS.map((tab, i) => (
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
                <span className="ml-1 font-mono text-[10px]" style={{ color: 'var(--muted-foreground)' }}>
                  {vehicles.filter((v) => tab.statuses!.includes(v.status)).length}
                </span>
              )}
            </button>
          ))}
        </div>
        <button
          onClick={openCreate}
          className="ml-auto flex items-center gap-1.5 px-3 py-1.5 rounded text-xs font-semibold cursor-pointer"
          style={{ background: 'var(--primary)', color: 'var(--primary-foreground)' }}
          onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.opacity = '0.85'}
          onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.opacity = '1'}
        >
          + Registrar vehículo
        </button>
      </div>

      {/* Table + detail */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="lg:col-span-2 rounded-lg border overflow-hidden" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
          <div
            className="grid text-[10px] font-mono tracking-widest px-4 py-2.5 border-b"
            style={{ color: 'var(--muted-foreground)', borderColor: 'var(--border)', gridTemplateColumns: '80px 80px 1fr 120px 100px 80px' }}
          >
            <span>PLACA</span>
            <span>TIPO</span>
            <span>VEHÍCULO</span>
            <span>ESTADO</span>
            <span>MOTORIZADO</span>
            <span></span>
          </div>

          {loading ? (
            <TableSkeleton rows={6} />
          ) : filtered.length === 0 ? (
            <div className="py-16 text-center">
              <p className="text-sm" style={{ color: 'var(--muted-foreground)' }}>Sin vehículos en esta categoría.</p>
            </div>
          ) : (
            filtered.map((veh, i) => (
              <div
                key={veh.id}
                className="grid items-center px-4 py-3 border-b"
                style={{
                  gridTemplateColumns: '80px 80px 1fr 120px 100px 80px',
                  borderColor: 'var(--border)',
                  borderBottomWidth: i === filtered.length - 1 ? '0' : '1px',
                  background: selected?.id === veh.id ? 'rgba(217 119 6 / 0.04)' : 'transparent',
                  cursor: 'pointer',
                }}
                onClick={() => setSelected((prev) => prev?.id === veh.id ? null : veh)}
                onMouseEnter={(e) => { if (selected?.id !== veh.id) (e.currentTarget as HTMLElement).style.background = 'var(--muted)'; }}
                onMouseLeave={(e) => { if (selected?.id !== veh.id) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
              >
                <span className="text-xs font-mono font-semibold" style={{ color: 'var(--primary)' }}>{veh.plate}</span>
                <span className="text-xs" style={{ color: 'var(--muted-foreground)' }}>
                  {TYPE_ICON[veh.type]} {TYPE_LABEL[veh.type]}
                </span>
                <span className="text-xs" style={{ color: 'var(--foreground)' }}>
                  {veh.brand} {veh.model}
                </span>
                <VehicleStatusBadge status={veh.status} />
                <span className="text-xs truncate" style={{ color: veh.driverName ? 'var(--foreground)' : 'var(--muted-foreground)' }}>
                  {veh.driverName ?? '—'}
                </span>
                <div className="flex items-center gap-1">
                  {!veh.driverId &&
                    veh.status !== "MAINTENANCE" &&
                    veh.status !== "INACTIVE" && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          openAssign(veh);
                        }}
                        className="p-1.5 rounded cursor-pointer"
                        style={{ color: "var(--muted-foreground)" }}
                        onMouseEnter={(e) => {
                          (e.currentTarget as HTMLElement).style.background =
                            "rgba(217 119 6 / 0.1)";
                          (e.currentTarget as HTMLElement).style.color = "var(--primary)";
                        }}
                        onMouseLeave={(e) => {
                          (e.currentTarget as HTMLElement).style.background = "transparent";
                          (e.currentTarget as HTMLElement).style.color =
                            "var(--muted-foreground)";
                        }}
                        title="Asignar motorizado"
                      >
                        <UserPlus className="w-3.5 h-3.5" />
                      </button>
                    )}
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      openEdit(veh);
                    }}
                    className="p-1.5 rounded cursor-pointer"
                    style={{ color: "var(--muted-foreground)" }}
                    onMouseEnter={(e) => {
                      (e.currentTarget as HTMLElement).style.background = "var(--muted)";
                      (e.currentTarget as HTMLElement).style.color = "var(--foreground)";
                    }}
                    onMouseLeave={(e) => {
                      (e.currentTarget as HTMLElement).style.background = "transparent";
                      (e.currentTarget as HTMLElement).style.color =
                        "var(--muted-foreground)";
                    }}
                    title="Editar"
                  >
                    <Pencil className="w-3.5 h-3.5" />
                  </button>
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      setConfirmDelete(veh.id);
                    }}
                    className="p-1.5 rounded cursor-pointer"
                    style={{ color: "var(--muted-foreground)" }}
                    onMouseEnter={(e) => {
                      (e.currentTarget as HTMLElement).style.background = "#fef2f2";
                      (e.currentTarget as HTMLElement).style.color = "#b91c1c";
                    }}
                    onMouseLeave={(e) => {
                      (e.currentTarget as HTMLElement).style.background = "transparent";
                      (e.currentTarget as HTMLElement).style.color =
                        "var(--muted-foreground)";
                    }}
                    title="Eliminar"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Detail */}
        <div className="rounded-lg border" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
          {selected ? (
            <div className="p-5 space-y-4">
              <div className="flex items-start justify-between">
                <div>
                  <div className="text-2xl mb-1">{TYPE_ICON[selected.type]}</div>
                  <div className="text-base font-bold font-mono" style={{ color: 'var(--foreground)' }}>{selected.plate}</div>
                  <div className="text-xs" style={{ color: 'var(--muted-foreground)' }}>{selected.brand} {selected.model}</div>
                </div>
                <VehicleStatusBadge status={selected.status} />
              </div>

              <div className="space-y-3 pt-3 border-t" style={{ borderColor: "var(--border)" }}>
                {[
                  {
                    label: "TIPO",
                    value: `${TYPE_ICON[selected.type]} ${TYPE_LABEL[selected.type]}`,
                  },
                  {
                    label: "MARCA / MODELO",
                    value: `${selected.brand} ${selected.model}`,
                  },
                  { label: "PLACA", value: selected.plate },
                  {
                    label: "MOTORIZADO",
                    value: selected.driverName ?? "Sin asignar",
                  },
                  { label: "ESTADO", value: STATUS_CFG[selected.status].label },
                ].map((f) => (
                  <div key={f.label}>
                    <div
                      className="text-[9px] font-mono tracking-widest"
                      style={{ color: "var(--muted-foreground)" }}
                    >
                      {f.label}
                    </div>
                    <div
                      className="text-xs mt-0.5 font-medium"
                      style={{ color: "var(--foreground)" }}
                    >
                      {f.value}
                    </div>
                  </div>
                ))}
              </div>

              <div
                className="pt-3 border-t flex flex-col gap-2"
                style={{ borderColor: "var(--border)" }}
              >
                {selected.status !== "MAINTENANCE" &&
                  selected.status !== "INACTIVE" && (
                    <button
                      type="button"
                      onClick={() => openAssign(selected)}
                      className="w-full py-2 rounded text-xs font-semibold cursor-pointer inline-flex items-center justify-center gap-1.5"
                      style={{
                        background: "var(--primary)",
                        color: "var(--primary-foreground)",
                      }}
                      onMouseEnter={(e) =>
                        ((e.currentTarget as HTMLElement).style.opacity = "0.85")
                      }
                      onMouseLeave={(e) =>
                        ((e.currentTarget as HTMLElement).style.opacity = "1")
                      }
                    >
                      <UserPlus className="w-3.5 h-3.5" />
                      {selected.driverId ? "Reasignar motorizado" : "Asignar motorizado"}
                    </button>
                  )}

                {selected.driverId && (
                  <button
                    type="button"
                    onClick={() => handleUnassignDriver(selected)}
                    className="w-full py-2 rounded border text-xs font-medium cursor-pointer inline-flex items-center justify-center gap-1.5"
                    style={{
                      borderColor: "#fecaca",
                      color: "#b91c1c",
                      background: "#fef2f2",
                    }}
                    onMouseEnter={(e) =>
                      ((e.currentTarget as HTMLElement).style.background = "#fee2e2")
                    }
                    onMouseLeave={(e) =>
                      ((e.currentTarget as HTMLElement).style.background = "#fef2f2")
                    }
                  >
                    <UserMinus className="w-3.5 h-3.5" />
                    Desasignar motorizado
                  </button>
                )}

                <button
                  type="button"
                  onClick={() => openEdit(selected)}
                  className="w-full py-2 rounded border text-xs font-medium cursor-pointer"
                  style={{ borderColor: "var(--border)", color: "var(--foreground)" }}
                  onMouseEnter={(e) =>
                    ((e.currentTarget as HTMLElement).style.background = "var(--muted)")
                  }
                  onMouseLeave={(e) =>
                    ((e.currentTarget as HTMLElement).style.background = "transparent")
                  }
                >
                  <span className="inline-flex items-center justify-center gap-1.5">
                    Editar vehículo <ArrowRight className="w-3 h-3" />
                  </span>
                </button>
                {selected.status !== "MAINTENANCE" && (
                  <button
                    type="button"
                    onClick={() => {
                      setVehicles((prev) =>
                        prev.map((v) =>
                          v.id === selected.id
                            ? {
                                ...v,
                                status: "MAINTENANCE",
                                driverId: undefined,
                                driverName: undefined,
                              }
                            : v,
                        ),
                      );
                      setSelected((prev) =>
                        prev
                          ? {
                              ...prev,
                              status: "MAINTENANCE",
                              driverId: undefined,
                              driverName: undefined,
                            }
                          : null,
                      );
                      showToast("Vehículo enviado a mantenimiento");
                    }}
                    className="w-full py-2 rounded border text-xs font-medium cursor-pointer"
                    style={{
                      borderColor: "#fde68a",
                      color: "#b45309",
                      background: "#fffbeb",
                    }}
                    onMouseEnter={(e) =>
                      ((e.currentTarget as HTMLElement).style.background = "#fef3c7")
                    }
                    onMouseLeave={(e) =>
                      ((e.currentTarget as HTMLElement).style.background = "#fffbeb")
                    }
                  >
                    <span className="inline-flex items-center justify-center gap-1.5">
                      Enviar a mantenimiento <ArrowRight className="w-3 h-3" />
                    </span>
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-full py-16 px-6 text-center">
              <Car className="w-8 h-8 mb-3" style={{ color: 'var(--muted-foreground)' }} />
              <p className="text-xs" style={{ color: 'var(--muted-foreground)' }}>Selecciona un vehículo para ver su detalle</p>
            </div>
          )}
        </div>
      </div>

      {/* Create/Edit modal */}
      {showModal && (
        <div
          className="fixed inset-0 flex items-center justify-center z-40"
          style={{ background: 'rgba(0 0 0 / 0.4)' }}
          onClick={() => setShowModal(false)}
        >
          <div
            className="rounded-lg border p-6 w-full max-w-md space-y-4"
            style={{ background: 'var(--card)', borderColor: 'var(--border)' }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-semibold" style={{ color: 'var(--foreground)' }}>
                {editId ? 'Editar vehículo' : 'Registrar vehículo'}
              </h3>
              <button onClick={() => setShowModal(false)} className="cursor-pointer" style={{ color: 'var(--muted-foreground)' }}>
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="grid grid-cols-2 gap-4">
              {([
                { key: 'plate', label: 'PLACA', placeholder: 'MF-1234', span: 1 },
                { key: 'brand', label: 'MARCA', placeholder: 'Honda', span: 1 },
                { key: 'model', label: 'MODELO', placeholder: 'Wave 110', span: 2 },
              ] as { key: keyof typeof form; label: string; placeholder: string; span: number }[]).map((f) => (
                <div key={f.key} className={f.span === 2 ? 'col-span-2' : ''}>
                  <label className="text-[10px] font-mono tracking-widest block mb-1" style={{ color: 'var(--muted-foreground)' }}>{f.label}</label>
                  <input
                    type="text"
                    value={form[f.key] as string ?? ''}
                    onChange={(e) => setForm((prev) => ({ ...prev, [f.key]: e.target.value }))}
                    placeholder={f.placeholder}
                    className="w-full px-3 py-2 rounded border text-sm outline-none"
                    style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }}
                    onFocus={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
                    onBlur={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
                  />
                </div>
              ))}

              <div>
                <label className="text-[10px] font-mono tracking-widest block mb-1" style={{ color: 'var(--muted-foreground)' }}>TIPO</label>
                <select
                  value={form.type}
                  onChange={(e) => setForm((prev) => ({ ...prev, type: e.target.value as VehicleType }))}
                  className="w-full px-3 py-2 rounded border text-sm outline-none cursor-pointer"
                  style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }}
                  onFocus={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
                  onBlur={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
                >
                  <option value="MOTORCYCLE">Moto</option>
                  <option value="BICYCLE">Bicicleta</option>
                  <option value="CAR">Auto</option>
                </select>
              </div>

              <div>
                <label className="text-[10px] font-mono tracking-widest block mb-1" style={{ color: 'var(--muted-foreground)' }}>ESTADO</label>
                <select
                  value={form.status}
                  onChange={(e) => setForm((prev) => ({ ...prev, status: e.target.value as VehicleStatus }))}
                  className="w-full px-3 py-2 rounded border text-sm outline-none cursor-pointer"
                  style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }}
                  onFocus={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
                  onBlur={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
                >
                  <option value="AVAILABLE">Disponible</option>
                  <option value="IN_USE">En uso</option>
                  <option value="MAINTENANCE">Mantenimiento</option>
                  <option value="INACTIVE">Inactivo</option>
                </select>
              </div>
            </div>

            <div className="flex gap-2 pt-1">
              <button
                onClick={() => setShowModal(false)}
                className="flex-1 py-2 rounded border text-sm cursor-pointer"
                style={{ borderColor: 'var(--border)', color: 'var(--muted-foreground)' }}
              >
                Cancelar
              </button>
              <button
                onClick={handleSave}
                className="flex-1 py-2 rounded text-sm font-semibold cursor-pointer"
                style={{ background: 'var(--primary)', color: 'var(--primary-foreground)' }}
                onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.opacity = '0.85'}
                onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.opacity = '1'}
              >
                {editId ? 'Guardar cambios' : 'Registrar'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Assign driver modal */}
      {assignTarget && (
        <div
          className="fixed inset-0 flex items-center justify-center z-40 p-4"
          style={{ background: "rgba(0 0 0 / 0.4)" }}
          onClick={() => setAssignTarget(null)}
        >
          <div
            className="rounded-lg border p-5 sm:p-6 w-full max-w-md space-y-4 max-h-[90vh] overflow-y-auto"
            style={{ background: "var(--card)", borderColor: "var(--border)" }}
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between gap-3">
              <div>
                <h3 className="text-sm font-semibold" style={{ color: "var(--foreground)" }}>
                  Asignar motorizado
                </h3>
                <p className="text-xs mt-0.5" style={{ color: "var(--muted-foreground)" }}>
                  Vehículo{" "}
                  <span className="font-mono font-semibold" style={{ color: "var(--primary)" }}>
                    {assignTarget.plate}
                  </span>
                  {" — "}
                  {assignTarget.brand} {assignTarget.model}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setAssignTarget(null)}
                className="cursor-pointer p-1"
                style={{ color: "var(--muted-foreground)" }}
                aria-label="Cerrar"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-2">
              {driversForAssign(assignTarget).map((driver) => {
                const otherVehicle = vehicles.find(
                  (v) => v.driverId === driver.id && v.id !== assignTarget.id,
                );
                const isSelected = selectedDriverId === driver.id;
                return (
                  <button
                    key={driver.id}
                    type="button"
                    onClick={() => setSelectedDriverId(driver.id)}
                    className="w-full text-left px-3 py-3 rounded-lg border cursor-pointer flex items-center gap-3"
                    style={{
                      borderColor: isSelected ? "var(--primary)" : "var(--border)",
                      background: isSelected
                        ? "rgba(217 119 6 / 0.06)"
                        : "transparent",
                    }}
                  >
                    <div
                      className="w-8 h-8 rounded-full flex items-center justify-center shrink-0"
                      style={{
                        background: isSelected
                          ? "rgba(217 119 6 / 0.15)"
                          : "var(--muted)",
                        color: isSelected ? "var(--primary)" : "var(--muted-foreground)",
                      }}
                    >
                      <Bike className="w-4 h-4" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <div
                        className="text-xs font-semibold truncate"
                        style={{ color: "var(--foreground)" }}
                      >
                        {driver.name}
                      </div>
                      <div
                        className="text-[10px] font-mono mt-0.5"
                        style={{ color: "var(--muted-foreground)" }}
                      >
                        {driver.status}
                        {otherVehicle
                          ? ` · ahora en ${otherVehicle.plate}`
                          : driver.vehiclePlate
                            ? ` · placa actual ${driver.vehiclePlate}`
                            : ""}
                      </div>
                    </div>
                    {isSelected && (
                      <Check className="w-4 h-4 shrink-0" style={{ color: "var(--primary)" }} />
                    )}
                  </button>
                );
              })}
            </div>

            {selectedDriverId &&
              vehicles.some(
                (v) => v.driverId === selectedDriverId && v.id !== assignTarget.id,
              ) && (
                <p className="text-[11px]" style={{ color: "#b45309" }}>
                  Este motorizado ya tiene otro vehículo. Al confirmar, se liberará el
                  anterior.
                </p>
              )}

            <div className="flex gap-2 pt-1">
              <button
                type="button"
                onClick={() => setAssignTarget(null)}
                className="flex-1 py-2 rounded border text-sm cursor-pointer"
                style={{ borderColor: "var(--border)", color: "var(--muted-foreground)" }}
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleAssignDriver}
                disabled={!selectedDriverId}
                className="flex-1 py-2 rounded text-sm font-semibold cursor-pointer inline-flex items-center justify-center gap-1.5"
                style={{
                  background: "var(--primary)",
                  color: "var(--primary-foreground)",
                  opacity: selectedDriverId ? 1 : 0.5,
                }}
              >
                <UserPlus className="w-3.5 h-3.5" />
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Confirm delete */}
      {confirmDelete && (
        <div
          className="fixed inset-0 flex items-center justify-center z-50"
          style={{ background: 'rgba(0 0 0 / 0.4)' }}
        >
          <div
            className="rounded-lg border p-6 w-full max-w-sm space-y-4"
            style={{ background: 'var(--card)', borderColor: 'var(--border)' }}
          >
            <h3 className="text-sm font-semibold" style={{ color: 'var(--foreground)' }}>¿Eliminar vehículo?</h3>
            <p className="text-xs" style={{ color: 'var(--muted-foreground)' }}>Esta acción no se puede deshacer.</p>
            <div className="flex gap-2">
              <button onClick={() => setConfirmDelete(null)} className="flex-1 py-2 rounded border text-sm cursor-pointer" style={{ borderColor: 'var(--border)', color: 'var(--muted-foreground)' }}>
                Cancelar
              </button>
              <button
                onClick={() => handleDelete(confirmDelete)}
                className="flex-1 py-2 rounded text-sm font-semibold cursor-pointer"
                style={{ background: '#dc2626', color: '#fff' }}
                onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.opacity = '0.85'}
                onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.opacity = '1'}
              >
                Eliminar
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && <Toast message={toast} onClose={() => setToast("")} />}
    </div>
  );
}
