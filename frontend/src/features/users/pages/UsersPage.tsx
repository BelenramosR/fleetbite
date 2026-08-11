import { useState, useEffect } from 'react';
import {
  Circle,
  CircleDot,
  Pencil,
  UserX,
  UserPlus,
  X,
  ArrowRight,
  User,
  Search,
} from "lucide-react";
import { TableSkeleton } from "@/shared/components/skeleton";
import { Toast } from "@/shared/components/toast";
import type { ToastTone } from "@/shared/components/toast";
import { createAdminUser, listAdminUsers, setAdminUserStatus, updateAdminUser } from "@/features/users/services/userApi";
import type { User as UserType, UserRole } from "@/shared/types";

const ROLE_CFG: Record<UserRole, { label: string; bg: string; text: string }> = {
  ADMIN:               { label: 'Admin',      bg: '#fef2f2', text: '#b91c1c' },
  DISPATCHER:          { label: 'Dispatcher', bg: '#eef2ff', text: '#4338ca' },
  RESTAURANT_OPERATOR: { label: 'Operador',   bg: '#fffbeb', text: '#b45309' },
  DRIVER:              { label: 'Motorizado', bg: '#f0fdf4', text: '#15803d' },
};

function RoleBadge({ role }: { role: UserRole }) {
  const cfg = ROLE_CFG[role];
  return (
    <span className="px-2 py-0.5 rounded text-xs font-mono font-medium" style={{ background: cfg.bg, color: cfg.text }}>
      {cfg.label}
    </span>
  );
}

const ROLE_FILTERS: { label: string; roles: UserRole[] | null }[] = [
  { label: 'Todos', roles: null },
  { label: 'Admin', roles: ['ADMIN'] },
  { label: 'Dispatcher', roles: ['DISPATCHER'] },
  { label: 'Operadores', roles: ['RESTAURANT_OPERATOR'] },
  { label: 'Motorizados', roles: ['DRIVER'] },
];

const EMPTY_FORM = { fullName: '', email: '', password: '', phone: '', role: 'DRIVER' as UserRole, status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE' };

export default function UsersPage() {
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState<UserType[]>([]);
  const [activeTab, setActiveTab] = useState(0);
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState<UserType | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState<string | null>(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [toast, setToast] = useState('');
  const [toastTone, setToastTone] = useState<ToastTone>('success');
  const [confirmToggle, setConfirmToggle] = useState<UserType | null>(null);

  useEffect(() => {
    let active = true;
    async function refresh() {
      try { const result = await listAdminUsers(); if (active) { setUsers(result); setSelected((current) => current ? result.find((user) => user.id === current.id) ?? null : null); } }
      catch (cause) { if (active) showToast(cause instanceof Error ? cause.message : 'No se pudieron cargar los usuarios.', 'error'); }
      finally { if (active) setLoading(false); }
    }
    void refresh();
    const id = window.setInterval(() => void refresh(), 15_000);
    return () => { active = false; window.clearInterval(id); };
  }, []);

  function showToast(msg: string, tone: ToastTone = 'success') { setToastTone(tone); setToast(msg); setTimeout(() => setToast(''), 3000); }

  function openCreate() { setForm(EMPTY_FORM); setEditId(null); setShowModal(true); }

  function openEdit(u: UserType) {
    setForm({ fullName: u.fullName, email: u.email, password: '', phone: u.phone ?? '', role: u.role, status: u.status });
    setEditId(u.id);
    setShowModal(true);
  }

  async function handleSave() {
    if (!form.fullName || !form.email) return;
    if (!editId && form.password.length < 8) { showToast('La contraseña debe tener al menos 8 caracteres.', 'error'); return; }
    try {
      if (editId) await updateAdminUser(editId, form);
      else await createAdminUser(form);
      const refreshed = await listAdminUsers(); setUsers(refreshed);
      if (editId) setSelected(refreshed.find((user) => user.id === editId) ?? null);
      showToast(editId ? 'Usuario actualizado' : 'Usuario creado'); setShowModal(false);
    } catch (cause) { showToast(cause instanceof Error ? cause.message : 'No se pudo guardar el usuario.', 'error'); }
  }

  async function handleToggleStatus(user: UserType) {
    const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
      await setAdminUserStatus(user.id, newStatus === 'ACTIVE');
      const refreshed = await listAdminUsers(); setUsers(refreshed);
      if (selected?.id === user.id) setSelected(refreshed.find((item) => item.id === user.id) ?? null);
      setConfirmToggle(null); showToast(newStatus === 'ACTIVE' ? 'Usuario activado' : 'Usuario desactivado');
    } catch (cause) { showToast(cause instanceof Error ? cause.message : 'No se pudo cambiar el estado.', 'error'); }
  }

  const filtered = users.filter((u) => {
    const tab = ROLE_FILTERS[activeTab];
    const roleMatch = tab.roles === null || tab.roles.includes(u.role);
    const q = search.toLowerCase();
    const searchMatch = !q || u.fullName.toLowerCase().includes(q) || u.email.toLowerCase().includes(q);
    return roleMatch && searchMatch;
  });

  const stats = {
    total: users.length,
    active: users.filter((u) => u.status === 'ACTIVE').length,
    inactive: users.filter((u) => u.status === 'INACTIVE').length,
  };

  return (
    <div className="space-y-4">
      {/* Stats */}
      <div className="grid grid-cols-4 gap-3">
        {[
          { label: 'TOTAL USUARIOS', value: stats.total },
          { label: 'ACTIVOS', value: stats.active, color: '#15803d' },
          { label: 'INACTIVOS', value: stats.inactive, color: '#b91c1c' },
          { label: 'MOTORIZADOS', value: users.filter((u) => u.role === 'DRIVER').length, color: '#4338ca' },
        ].map((s) => (
          <div key={s.label} className="rounded-lg border px-4 py-3" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
            <div className="text-[10px] font-mono tracking-widest mb-1" style={{ color: 'var(--muted-foreground)' }}>{s.label}</div>
            <div className="text-2xl font-bold font-mono" style={{ color: s.color ?? 'var(--foreground)' }}>{s.value}</div>
          </div>
        ))}
      </div>

      {/* Toolbar */}
      <div className="flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-0.5 p-1 rounded-md" style={{ background: 'var(--muted)' }}>
          {ROLE_FILTERS.map((tab, i) => (
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
              {!loading && tab.roles && (
                <span className="ml-1 font-mono text-[10px]" style={{ color: 'var(--muted-foreground)' }}>
                  {users.filter((u) => tab.roles!.includes(u.role)).length}
                </span>
              )}
            </button>
          ))}
        </div>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5" style={{ color: 'var(--muted-foreground)' }} />
          <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar nombre o correo…"
            className="pl-8 pr-3 py-2 rounded border text-xs outline-none w-52"
            style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }}
            onFocus={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
            onBlur={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
          />
        </div>
        <button
          onClick={openCreate}
          className="ml-auto flex items-center gap-1.5 px-3 py-1.5 rounded text-xs font-semibold cursor-pointer"
          style={{ background: 'var(--primary)', color: 'var(--primary-foreground)' }}
          onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.opacity = '0.85'}
          onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.opacity = '1'}
        >
          + Nuevo usuario
        </button>
      </div>

      {/* Table + detail */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="lg:col-span-2 rounded-lg border overflow-hidden" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
          <div
            className="grid text-[10px] font-mono tracking-widest px-4 py-2.5 border-b"
            style={{ color: 'var(--muted-foreground)', borderColor: 'var(--border)', gridTemplateColumns: '1fr 110px 80px 80px' }}
          >
            <span>USUARIO</span>
            <span>ROL</span>
            <span>ESTADO</span>
            <span></span>
          </div>

          {loading ? (
            <TableSkeleton rows={6} />
          ) : filtered.length === 0 ? (
            <div className="py-16 text-center">
              <p className="text-sm" style={{ color: 'var(--muted-foreground)' }}>Sin usuarios que coincidan.</p>
            </div>
          ) : (
            filtered.map((user, i) => (
              <div
                key={user.id}
                className="grid items-center px-4 py-3 border-b cursor-pointer"
                style={{
                  gridTemplateColumns: '1fr 110px 80px 80px',
                  borderColor: 'var(--border)',
                  borderBottomWidth: i === filtered.length - 1 ? '0' : '1px',
                  background: selected?.id === user.id ? 'rgba(217 119 6 / 0.04)' : 'transparent',
                  opacity: user.status === 'INACTIVE' ? 0.6 : 1,
                }}
                onClick={() => setSelected((prev) => prev?.id === user.id ? null : user)}
                onMouseEnter={(e) => { if (selected?.id !== user.id) (e.currentTarget as HTMLElement).style.background = 'var(--muted)'; }}
                onMouseLeave={(e) => { if (selected?.id !== user.id) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
              >
                <div className="flex items-center gap-3">
                  <div
                    className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0"
                    style={{ background: ROLE_CFG[user.role].bg, color: ROLE_CFG[user.role].text }}
                  >
                    {user.fullName.split(' ').map((n) => n[0]).join('').slice(0, 2)}
                  </div>
                  <div>
                    <div className="text-xs font-medium" style={{ color: 'var(--foreground)' }}>{user.fullName}</div>
                    <div className="text-[11px]" style={{ color: 'var(--muted-foreground)' }}>{user.email}</div>
                  </div>
                </div>
                <RoleBadge role={user.role} />
                <span
                  className="text-[11px] font-mono font-medium inline-flex items-center gap-1"
                  style={{ color: user.status === 'ACTIVE' ? '#15803d' : '#9ca3af' }}
                >
                  {user.status === 'ACTIVE' ? (
                    <><CircleDot className="w-3 h-3" /> Activo</>
                  ) : (
                    <><Circle className="w-3 h-3" /> Inactivo</>
                  )}
                </span>
                <div className="flex items-center gap-1">
                  <button
                    onClick={(e) => { e.stopPropagation(); openEdit(user); }}
                    className="p-1.5 rounded cursor-pointer"
                    style={{ color: 'var(--muted-foreground)' }}
                    onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--muted)'; (e.currentTarget as HTMLElement).style.color = 'var(--foreground)'; }}
                    onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; (e.currentTarget as HTMLElement).style.color = 'var(--muted-foreground)'; }}
                    title="Editar"
                  >
                    <Pencil className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={(e) => { e.stopPropagation(); setConfirmToggle(user); }}
                    className="p-1.5 rounded cursor-pointer"
                    style={{ color: 'var(--muted-foreground)' }}
                    onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = user.status === 'ACTIVE' ? '#fef2f2' : '#f0fdf4'; (e.currentTarget as HTMLElement).style.color = user.status === 'ACTIVE' ? '#b91c1c' : '#15803d'; }}
                    onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; (e.currentTarget as HTMLElement).style.color = 'var(--muted-foreground)'; }}
                    title={user.status === 'ACTIVE' ? 'Desactivar' : 'Activar'}
                  >
                    {user.status === 'ACTIVE' ? <UserX className="w-3.5 h-3.5" /> : <UserPlus className="w-3.5 h-3.5" />}
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
              <div className="flex items-center gap-3">
                <div
                  className="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold"
                  style={{ background: ROLE_CFG[selected.role].bg, color: ROLE_CFG[selected.role].text }}
                >
                  {selected.fullName.split(' ').map((n) => n[0]).join('').slice(0, 2)}
                </div>
                <div>
                  <div className="text-sm font-semibold" style={{ color: 'var(--foreground)' }}>{selected.fullName}</div>
                  <RoleBadge role={selected.role} />
                </div>
              </div>

              <div className="space-y-3 pt-3 border-t" style={{ borderColor: 'var(--border)' }}>
                {[
                  { label: 'CORREO', value: selected.email },
                  { label: 'ROL', value: ROLE_CFG[selected.role].label },
                  { label: 'ESTADO', value: selected.status === 'ACTIVE' ? 'Activo' : 'Inactivo' },
                  { label: 'CREADO', value: new Date(selected.createdAt).toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' }) },
                ].map((f) => (
                  <div key={f.label}>
                    <div className="text-[9px] font-mono tracking-widest" style={{ color: 'var(--muted-foreground)' }}>{f.label}</div>
                    <div className="text-xs mt-0.5 font-medium" style={{ color: 'var(--foreground)' }}>{f.value}</div>
                  </div>
                ))}
              </div>

              <div className="pt-3 border-t flex flex-col gap-2" style={{ borderColor: 'var(--border)' }}>
                <button
                  onClick={() => openEdit(selected)}
                  className="w-full py-2 rounded border text-xs font-medium cursor-pointer"
                  style={{ borderColor: 'var(--border)', color: 'var(--foreground)' }}
                  onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.background = 'var(--muted)'}
                  onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.background = 'transparent'}
                >
                  <span className="inline-flex items-center justify-center gap-1.5">
                    Editar usuario <ArrowRight className="w-3 h-3" />
                  </span>
                </button>
                <button
                  onClick={() => setConfirmToggle(selected)}
                  className="w-full py-2 rounded border text-xs font-medium cursor-pointer"
                  style={{
                    borderColor: selected.status === 'ACTIVE' ? '#fecaca' : '#bbf7d0',
                    color: selected.status === 'ACTIVE' ? '#b91c1c' : '#15803d',
                    background: selected.status === 'ACTIVE' ? '#fef2f2' : '#f0fdf4',
                  }}
                  onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.opacity = '0.8'}
                  onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.opacity = '1'}
                >
                  <span className="inline-flex items-center justify-center gap-1.5">
                    {selected.status === 'ACTIVE' ? 'Desactivar usuario' : 'Activar usuario'}
                    <ArrowRight className="w-3 h-3" />
                  </span>
                </button>
              </div>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center h-full py-16 px-6 text-center">
              <User className="w-8 h-8 mb-3" style={{ color: 'var(--muted-foreground)' }} />
              <p className="text-xs" style={{ color: 'var(--muted-foreground)' }}>Selecciona un usuario para ver su detalle</p>
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
                {editId ? 'Editar usuario' : 'Nuevo usuario'}
              </h3>
              <button onClick={() => setShowModal(false)} className="cursor-pointer" style={{ color: 'var(--muted-foreground)' }}>
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3">
              {([
                { key: 'fullName', label: 'NOMBRE COMPLETO', placeholder: 'Andrea Salinas', type: 'text' },
                { key: 'email', label: 'CORREO', placeholder: 'usuario@fleetbite.local', type: 'email' },
              ] as { key: keyof typeof form; label: string; placeholder: string; type: string }[]).map((f) => (
                <div key={f.key}>
                  <label className="text-[10px] font-mono tracking-widest block mb-1" style={{ color: 'var(--muted-foreground)' }}>{f.label}</label>
                  <input
                    type={f.type}
                    value={form[f.key] as string}
                    onChange={(e) => setForm((prev) => ({ ...prev, [f.key]: e.target.value }))}
                    placeholder={f.placeholder}
                    className="w-full px-3 py-2 rounded border text-sm outline-none"
                    style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }}
                    onFocus={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
                    onBlur={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
                  />
                </div>
              ))}

              {!editId && (
                <div>
                  <label className="text-[10px] font-mono tracking-widest block mb-1" style={{ color: 'var(--muted-foreground)' }}>CONTRASEÑA INICIAL</label>
                  <input
                    type="password"
                    value={form.password}
                    onChange={(e) => setForm((prev) => ({ ...prev, password: e.target.value }))}
                    placeholder="••••••••"
                    className="w-full px-3 py-2 rounded border text-sm outline-none"
                    style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }}
                    onFocus={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
                    onBlur={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
                  />
                </div>
              )}

              {form.role === 'DRIVER' && (
                <div>
                  <label className="text-[10px] font-mono tracking-widest block mb-1" style={{ color: 'var(--muted-foreground)' }}>TELÉFONO DEL MOTORIZADO</label>
                  <input type="tel" value={form.phone} onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))}
                    placeholder="+51 999 000 111" className="w-full px-3 py-2 rounded border text-sm outline-none"
                    style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }} />
                  <p className="mt-1 text-[10px] text-muted-foreground">Obligatorio para que el driver pueda ponerse disponible.</p>
                </div>
              )}

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-[10px] font-mono tracking-widest block mb-1" style={{ color: 'var(--muted-foreground)' }}>ROL</label>
                  <select
                    value={form.role}
                    disabled={Boolean(editId)}
                    onChange={(e) => setForm((prev) => ({ ...prev, role: e.target.value as UserRole }))}
                    className="w-full px-3 py-2 rounded border text-sm outline-none cursor-pointer disabled:cursor-not-allowed disabled:opacity-60"
                    style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }}
                    onFocus={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
                    onBlur={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
                  >
                    <option value="ADMIN">Admin</option>
                    <option value="DISPATCHER">Dispatcher</option>
                    <option value="RESTAURANT_OPERATOR">Operador</option>
                    <option value="DRIVER">Motorizado</option>
                  </select>
                </div>
                <div>
                  <label className="text-[10px] font-mono tracking-widest block mb-1" style={{ color: 'var(--muted-foreground)' }}>ESTADO</label>
                  <select
                    value={form.status}
                    onChange={(e) => setForm((prev) => ({ ...prev, status: e.target.value as 'ACTIVE' | 'INACTIVE' }))}
                    className="w-full px-3 py-2 rounded border text-sm outline-none cursor-pointer"
                    style={{ background: 'var(--muted)', borderColor: 'var(--border)', color: 'var(--foreground)' }}
                    onFocus={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
                    onBlur={(e) => (e.currentTarget.style.borderColor = 'var(--border)')}
                  >
                    <option value="ACTIVE">Activo</option>
                    <option value="INACTIVE">Inactivo</option>
                  </select>
                </div>
              </div>
            </div>

            <div className="flex gap-2 pt-1">
              <button onClick={() => setShowModal(false)} className="flex-1 py-2 rounded border text-sm cursor-pointer" style={{ borderColor: 'var(--border)', color: 'var(--muted-foreground)' }}>
                Cancelar
              </button>
              <button
                onClick={() => void handleSave()}
                className="flex-1 py-2 rounded text-sm font-semibold cursor-pointer"
                style={{ background: 'var(--primary)', color: 'var(--primary-foreground)' }}
                onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.opacity = '0.85'}
                onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.opacity = '1'}
              >
                {editId ? 'Guardar cambios' : 'Crear usuario'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Confirm toggle status */}
      {confirmToggle && (
        <div className="fixed inset-0 flex items-center justify-center z-50" style={{ background: 'rgba(0 0 0 / 0.4)' }}>
          <div className="rounded-lg border p-6 w-full max-w-sm space-y-4" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
            <h3 className="text-sm font-semibold" style={{ color: 'var(--foreground)' }}>
              {confirmToggle.status === 'ACTIVE' ? 'Desactivar usuario' : 'Activar usuario'}
            </h3>
            <p className="text-xs" style={{ color: 'var(--muted-foreground)' }}>
              {confirmToggle.status === 'ACTIVE'
                ? `${confirmToggle.fullName} perderá acceso al sistema.`
                : `${confirmToggle.fullName} recuperará su acceso.`}
            </p>
            <div className="flex gap-2">
              <button onClick={() => setConfirmToggle(null)} className="flex-1 py-2 rounded border text-sm cursor-pointer" style={{ borderColor: 'var(--border)', color: 'var(--muted-foreground)' }}>
                Cancelar
              </button>
              <button
                onClick={() => void handleToggleStatus(confirmToggle)}
                className="flex-1 py-2 rounded text-sm font-semibold cursor-pointer"
                style={{ background: confirmToggle.status === 'ACTIVE' ? '#dc2626' : '#15803d', color: '#fff' }}
                onMouseEnter={(e) => (e.currentTarget as HTMLElement).style.opacity = '0.85'}
                onMouseLeave={(e) => (e.currentTarget as HTMLElement).style.opacity = '1'}
              >
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && <Toast message={toast} tone={toastTone} onClose={() => setToast("")} />}
    </div>
  );
}
