import { useState } from 'react';
import {
  AreaChart, Area, BarChart, Bar, LineChart, Line,
  XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
  PieChart, Pie, Cell, Legend,
} from 'recharts';

const RANGE_OPTS = ['Hoy', 'Semana', 'Mes'] as const;
type Range = typeof RANGE_OPTS[number];

/* ── mock datasets ── */
const deliveriesByHour = [
  { h: '08h', ok: 2,  breach: 0 },
  { h: '09h', ok: 5,  breach: 0 },
  { h: '10h', ok: 7,  breach: 1 },
  { h: '11h', ok: 9,  breach: 1 },
  { h: '12h', ok: 14, breach: 2 },
  { h: '13h', ok: 18, breach: 1 },
  { h: '14h', ok: 16, breach: 2 },
  { h: '15h', ok: 13, breach: 1 },
  { h: '16h', ok: 11, breach: 0 },
  { h: '17h', ok: 15, breach: 1 },
  { h: '18h', ok: 8,  breach: 1 },
  { h: '19h', ok: 3,  breach: 0 },
];

const weeklyDeliveries = [
  { day: 'Lun', deliveries: 58, slaOk: 51, breach: 7 },
  { day: 'Mar', deliveries: 72, slaOk: 65, breach: 7 },
  { day: 'Mié', deliveries: 65, slaOk: 60, breach: 5 },
  { day: 'Jue', deliveries: 80, slaOk: 74, breach: 6 },
  { day: 'Vie', deliveries: 95, slaOk: 88, breach: 7 },
  { day: 'Sáb', deliveries: 112, slaOk: 101, breach: 11 },
  { day: 'Dom', deliveries: 90, slaOk: 82, breach: 8 },
];

const monthlyTrend = [
  { week: 'S1', deliveries: 340, slaOk: 310 },
  { week: 'S2', deliveries: 390, slaOk: 362 },
  { week: 'S3', deliveries: 420, slaOk: 395 },
  { week: 'S4', deliveries: 572, slaOk: 531 },
];

const driverPerf = [
  { name: 'Carlos M.', deliveries: 34, onTime: 31, avg: 28 },
  { name: 'Lucía T.',  deliveries: 28, onTime: 27, avg: 25 },
  { name: 'Diego F.',  deliveries: 22, onTime: 20, avg: 32 },
  { name: 'Angélica S.',deliveries: 30, onTime: 28, avg: 27 },
  { name: 'Rosa C.',   deliveries: 38, onTime: 35, avg: 24 },
];

const statusDist = [
  { name: 'Entregados', value: 121, color: '#16a34a' },
  { name: 'En camino',  value: 9,   color: '#4f46e5' },
  { name: 'Preparando', value: 5,   color: '#f59e0b' },
  { name: 'Cancelados', value: 6,   color: '#ef4444' },
  { name: 'Fallidos',   value: 2,   color: '#dc2626' },
];

const failReasons = [
  { reason: 'Cliente no disponible', count: 4 },
  { reason: 'Dirección incorrecta',  count: 3 },
  { reason: 'Problema de vehículo',  count: 2 },
  { reason: 'Pedido dañado',         count: 1 },
  { reason: 'Otro',                  count: 1 },
];

const Tip = ({ active, payload, label }: any) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="rounded border px-3 py-2 text-xs font-mono space-y-1 shadow-md"
      style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
      <p style={{ color: 'var(--muted-foreground)' }}>{label}</p>
      {payload.map((p: any) => (
        <p key={p.name} style={{ color: p.color ?? p.fill ?? 'var(--foreground)' }}>
          {p.name}: <strong>{p.value}</strong>
        </p>
      ))}
    </div>
  );
};

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="rounded-lg border p-5" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
      <div className="text-[10px] font-mono tracking-widest mb-4" style={{ color: 'var(--muted-foreground)' }}>
        {title}
      </div>
      {children}
    </div>
  );
}

function KPI({ label, value, sub, color }: { label: string; value: string; sub?: string; color?: string }) {
  return (
    <div className="rounded-lg border px-5 py-4" style={{ background: 'var(--card)', borderColor: 'var(--border)' }}>
      <div className="text-[10px] font-mono tracking-widest mb-2" style={{ color: 'var(--muted-foreground)' }}>{label}</div>
      <div className="text-2xl font-bold font-mono" style={{ color: color ?? 'var(--foreground)' }}>{value}</div>
      {sub && <div className="text-[11px] mt-1" style={{ color: 'var(--muted-foreground)' }}>{sub}</div>}
    </div>
  );
}

const AXIS = { fill: '#9ca3af', fontSize: 10, fontFamily: 'JetBrains Mono' };

export default function ReportsPage() {
  const [range, setRange] = useState<Range>('Hoy');

  const chartData: { h: string; ok: number; breach: number }[] =
    range === 'Hoy'
      ? deliveriesByHour
      : range === 'Semana'
      ? weeklyDeliveries.map((d) => ({ h: d.day, ok: d.slaOk, breach: d.breach }))
      : monthlyTrend.map((d) => ({ h: d.week, ok: d.slaOk, breach: d.deliveries - d.slaOk }));

  const totalDeliveries = chartData.reduce((a, d) => a + d.ok + d.breach, 0);
  const totalOk = chartData.reduce((a, d) => a + d.ok, 0);
  const slaPercent = totalDeliveries > 0 ? Math.round((totalOk / totalDeliveries) * 100) : 0;

  return (
    <div className="space-y-5">
      {/* Header + range selector */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-base font-semibold" style={{ color: 'var(--foreground)' }}>Reportes operativos</h2>
          <p className="text-xs mt-0.5" style={{ color: 'var(--muted-foreground)' }}>Métricas de entregas, SLA y rendimiento de flota</p>
        </div>
        <div className="flex items-center gap-0.5 p-1 rounded-md" style={{ background: 'var(--muted)' }}>
          {RANGE_OPTS.map((r) => (
            <button
              key={r}
              onClick={() => setRange(r)}
              className="px-3 py-1 rounded text-xs font-medium cursor-pointer"
              style={{
                background: range === r ? 'var(--card)' : 'transparent',
                color: range === r ? 'var(--foreground)' : 'var(--muted-foreground)',
                boxShadow: range === r ? '0 1px 3px rgba(0 0 0 / 0.1)' : 'none',
              }}
            >
              {r}
            </button>
          ))}
        </div>
      </div>

      {/* KPIs */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <KPI label="TOTAL ENTREGAS"    value={String(totalDeliveries)} sub={`rango: ${range}`} />
        <KPI label="CUMPLIMIENTO SLA"  value={`${slaPercent}%`}       sub={`${totalOk} a tiempo`} color={slaPercent >= 85 ? '#15803d' : '#b45309'} />
        <KPI label="TIEMPO PROM. ENTREGA" value="28.4m"              sub="todos los estados" color="#4338ca" />
        <KPI label="TASA DE FALLO"     value="2.1%"                   sub={`${failReasons.reduce((a, f) => a + f.count, 0)} fallidos`} color="#b91c1c" />
      </div>

      {/* Main chart: deliveries over time */}
      <Section title={`ENTREGAS — SLA CUMPLIDO VS INCUMPLIDO (${range.toUpperCase()})`}>
        <ResponsiveContainer width="100%" height={220}>
          <BarChart data={chartData} barSize={14} barCategoryGap="30%">
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(0 0 0 / 0.06)" vertical={false} />
            <XAxis dataKey="h" tick={AXIS} axisLine={false} tickLine={false} />
            <YAxis tick={AXIS} axisLine={false} tickLine={false} width={28} />
            <Tooltip content={<Tip />} cursor={{ fill: 'rgba(0 0 0 / 0.03)' }} />
            <Bar dataKey="ok"     name="A tiempo"   fill="#16a34a" radius={[3, 3, 0, 0]} stackId="a" />
            <Bar dataKey="breach" name="Incumplido" fill="#ef4444" radius={[3, 3, 0, 0]} stackId="a" />
          </BarChart>
        </ResponsiveContainer>
      </Section>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Distribución de estados */}
        <Section title="DISTRIBUCIÓN DE ESTADOS">
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={statusDist} cx="50%" cy="45%" innerRadius={48} outerRadius={72} paddingAngle={2} dataKey="value">
                {statusDist.map((e, i) => <Cell key={i} fill={e.color} />)}
              </Pie>
              <Legend iconType="circle" iconSize={6}
                wrapperStyle={{ fontSize: 10, fontFamily: 'JetBrains Mono', color: '#6b7280' }} />
              <Tooltip content={<Tip />} />
            </PieChart>
          </ResponsiveContainer>
        </Section>

        {/* Motivos de fallo */}
        <Section title="MOTIVOS DE ENTREGA FALLIDA">
          <div className="space-y-2.5 pt-1">
            {failReasons.map((f) => {
              const total = failReasons.reduce((a, r) => a + r.count, 0);
              const pct = Math.round((f.count / total) * 100);
              return (
                <div key={f.reason}>
                  <div className="flex justify-between text-xs mb-1">
                    <span style={{ color: 'var(--foreground)' }}>{f.reason}</span>
                    <span className="font-mono" style={{ color: 'var(--muted-foreground)' }}>{f.count}</span>
                  </div>
                  <div className="h-1.5 rounded-full overflow-hidden" style={{ background: 'var(--muted)' }}>
                    <div className="h-full rounded-full" style={{ width: `${pct}%`, background: '#ef4444' }} />
                  </div>
                </div>
              );
            })}
          </div>
        </Section>

        {/* Tendencia mensual */}
        <Section title="TENDENCIA MENSUAL">
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={monthlyTrend}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(0 0 0 / 0.06)" vertical={false} />
              <XAxis dataKey="week" tick={AXIS} axisLine={false} tickLine={false} />
              <YAxis tick={AXIS} axisLine={false} tickLine={false} width={32} />
              <Tooltip content={<Tip />} />
              <Line type="monotone" dataKey="deliveries" name="Total" stroke="#4f46e5" strokeWidth={2} dot={{ r: 3, fill: '#4f46e5' }} />
              <Line type="monotone" dataKey="slaOk" name="A tiempo" stroke="#16a34a" strokeWidth={2} dot={{ r: 3, fill: '#16a34a' }} strokeDasharray="4 2" />
            </LineChart>
          </ResponsiveContainer>
        </Section>
      </div>

      {/* Driver performance table */}
      <Section title="RENDIMIENTO DE MOTORIZADOS">
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="border-b" style={{ borderColor: 'var(--border)' }}>
                {['MOTORIZADO', 'ENTREGAS', 'A TIEMPO', 'SLA %', 'TIEMPO PROM.'].map((h) => (
                  <th key={h} className="text-left pb-2.5 font-mono text-[10px] tracking-widest pr-4"
                    style={{ color: 'var(--muted-foreground)' }}>{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y" style={{ borderColor: 'var(--border)' }}>
              {driverPerf.map((d) => {
                const pct = Math.round((d.onTime / d.deliveries) * 100);
                return (
                  <tr key={d.name} className="border-b" style={{ borderColor: 'var(--border)' }}>
                    <td className="py-3 pr-4 font-medium" style={{ color: 'var(--foreground)' }}>{d.name}</td>
                    <td className="py-3 pr-4 font-mono" style={{ color: 'var(--foreground)' }}>{d.deliveries}</td>
                    <td className="py-3 pr-4 font-mono" style={{ color: '#15803d' }}>{d.onTime}</td>
                    <td className="py-3 pr-4">
                      <div className="flex items-center gap-2">
                        <div className="h-1.5 w-16 rounded-full overflow-hidden" style={{ background: 'var(--muted)' }}>
                          <div className="h-full rounded-full" style={{ width: `${pct}%`, background: pct >= 90 ? '#16a34a' : pct >= 80 ? '#f59e0b' : '#ef4444' }} />
                        </div>
                        <span className="font-mono" style={{ color: pct >= 90 ? '#15803d' : pct >= 80 ? '#b45309' : '#b91c1c' }}>{pct}%</span>
                      </div>
                    </td>
                    <td className="py-3 font-mono" style={{ color: 'var(--foreground)' }}>{d.avg}m</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </Section>

      {/* Area chart: throughput trend */}
      <Section title="VOLUMEN DE PEDIDOS — TENDENCIA SEMANAL">
        <ResponsiveContainer width="100%" height={180}>
          <AreaChart data={weeklyDeliveries}>
            <defs>
              <linearGradient id="gradOk" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%"  stopColor="#16a34a" stopOpacity={0.15} />
                <stop offset="95%" stopColor="#16a34a" stopOpacity={0} />
              </linearGradient>
              <linearGradient id="gradTotal" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%"  stopColor="#4f46e5" stopOpacity={0.12} />
                <stop offset="95%" stopColor="#4f46e5" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(0 0 0 / 0.06)" vertical={false} />
            <XAxis dataKey="day" tick={AXIS} axisLine={false} tickLine={false} />
            <YAxis tick={AXIS} axisLine={false} tickLine={false} width={28} />
            <Tooltip content={<Tip />} />
            <Area type="monotone" dataKey="deliveries" name="Total pedidos" stroke="#4f46e5" strokeWidth={2} fill="url(#gradTotal)" dot={false} />
            <Area type="monotone" dataKey="slaOk" name="A tiempo"     stroke="#16a34a" strokeWidth={2} fill="url(#gradOk)"    dot={false} />
          </AreaChart>
        </ResponsiveContainer>
      </Section>
    </div>
  );
}
