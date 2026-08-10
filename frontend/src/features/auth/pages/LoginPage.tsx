import { useState } from "react";
import { ArrowRight, Bike } from "lucide-react";
import { resolveSessionByEmail, type SessionUser } from "@/features/auth/lib/access";

interface LoginPageProps {
  onLogin: (user: SessionUser) => void;
}

export default function LoginPage({ onLogin }: LoginPageProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    if (!email || !password) {
      setError("Ingresa tu correo y contraseña.");
      return;
    }
    setLoading(true);
    setTimeout(() => {
      const user = resolveSessionByEmail(email);
      setLoading(false);
      if (!user) {
        setError("Usuario no encontrado o inactivo.");
        return;
      }
      onLogin(user);
    }, 700);
  };

  return (
    <div className="login-page min-h-dvh bg-background">
      <div
        className="pointer-events-none fixed inset-0"
        style={{
          background:
            "radial-gradient(90% 55% at 50% -10%, rgba(217 119 6 / 0.12), transparent 55%), var(--background)",
        }}
      />

      <div className="relative mx-auto flex min-h-dvh w-full max-w-lg flex-col px-5 py-8 sm:px-6 sm:py-10 lg:max-w-6xl lg:flex-row lg:items-center lg:gap-16 lg:px-10 xl:gap-24">
        {/* Marca */}
        <section className="login-brand shrink-0 lg:w-[46%]">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary text-primary-foreground sm:h-11 sm:w-11">
              <Bike className="h-5 w-5" strokeWidth={2.4} />
            </div>
            <div>
              <div className="text-base font-bold tracking-tight text-foreground sm:text-lg">
                FleetBite
              </div>
              <div className="mt-0.5 text-[11px] font-mono text-muted-foreground">última milla</div>
            </div>
          </div>

          <h1 className="login-display mt-8 text-[1.85rem] font-bold tracking-tight leading-[1.12] text-foreground sm:mt-10 sm:text-4xl lg:text-[2.6rem]">
            FleetBite
            <span className="mt-1 block text-primary">operaciones a tiempo</span>
          </h1>

          <p className="mt-3 max-w-md text-sm leading-relaxed text-muted-foreground sm:mt-4 sm:text-[15px]">
            Entrá con tu cuenta para despachar pedidos, administrar flota o completar entregas
            desde la app del motorizado.
          </p>

          <ul className="mt-6 hidden gap-6 text-sm text-muted-foreground sm:flex lg:mt-8">
            <li>
              <span className="block font-semibold text-foreground">Dispatcher</span>
              Cola y SLA
            </li>
            <li>
              <span className="block font-semibold text-foreground">Admin</span>
              Flota y reportes
            </li>
            <li>
              <span className="block font-semibold text-foreground">Driver</span>
              Tu asignación
            </li>
          </ul>
        </section>

        {/* Formulario */}
        <section className="login-form mt-8 w-full flex-1 sm:mt-10 lg:mt-0 lg:max-w-md lg:justify-self-end">
          <div className="rounded-2xl border border-border bg-card p-5 shadow-sm sm:p-7">
            <h2 className="text-xl font-bold tracking-tight text-foreground sm:text-2xl">
              Iniciar sesión
            </h2>
            <p className="mt-1.5 text-sm text-muted-foreground">
              Correo y contraseña de tu usuario FleetBite.
            </p>

            <form onSubmit={handleSubmit} className="mt-6 space-y-4">
              <div className="space-y-1.5">
                <label htmlFor="email" className="text-xs font-medium text-muted-foreground">
                  Correo
                </label>
                <input
                  id="email"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                  className="w-full rounded-xl border border-border bg-background px-3.5 py-3 text-sm text-foreground outline-none transition-[border-color,box-shadow] placeholder:text-muted-foreground/70 focus:border-primary focus:shadow-[0_0_0_3px_rgba(217,119,6,0.14)]"
                  placeholder="usuario@fleetbite.local"
                />
              </div>

              <div className="space-y-1.5">
                <label htmlFor="password" className="text-xs font-medium text-muted-foreground">
                  Contraseña
                </label>
                <input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                  className="w-full rounded-xl border border-border bg-background px-3.5 py-3 text-sm text-foreground outline-none transition-[border-color,box-shadow] placeholder:text-muted-foreground/70 focus:border-primary focus:shadow-[0_0_0_3px_rgba(217,119,6,0.14)]"
                  placeholder="••••••••"
                />
              </div>

              {error && (
                <p className="rounded-lg bg-red-50 px-3 py-2 text-xs text-red-700">{error}</p>
              )}

              <button
                type="submit"
                disabled={loading}
                className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-primary py-3.5 text-sm font-semibold text-primary-foreground cursor-pointer transition-opacity hover:opacity-95 disabled:opacity-70"
              >
                {loading ? "Autenticando…" : "Ingresar"}
                {!loading && <ArrowRight className="h-4 w-4" />}
              </button>
            </form>
          </div>

          <p className="mt-4 text-center text-[11px] text-muted-foreground lg:text-left">
            Si no puedes entrar, pide acceso a tu administrador.
          </p>
        </section>
      </div>
    </div>
  );
}
