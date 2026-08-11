import { useState, type ComponentType } from "react";
import {
  LayoutDashboard,
  ClipboardList,
  Bike,
  Truck,
  BarChart3,
  Users,
  LogOut,
  X,
} from "lucide-react";
import type { Page } from "@/app/router";
import type { SessionUser } from "@/features/auth/lib/access";
import { PAGES_BY_ROLE } from "@/features/auth/lib/access";
import { APP_NAME } from "@/shared/constants";

interface SidebarProps {
  currentPage: Page;
  onNavigate: (page: Page) => void;
  onLogout: () => void;
  mobileOpen: boolean;
  onMobileClose: () => void;
  user: SessionUser;
}

const ALL_NAV: {
  page: Page;
  label: string;
  icon: ComponentType<{ className?: string; strokeWidth?: number }>;
  dividerBefore?: boolean;
}[] = [
  { page: "dashboard", label: "Dashboard", icon: LayoutDashboard },
  { page: "orders", label: "Pedidos", icon: ClipboardList },
  { page: "drivers", label: "Motorizados", icon: Bike },
  { page: "reports", label: "Reportes", icon: BarChart3 },
  { page: "fleet", label: "Flota", icon: Truck},
  { page: "users", label: "Usuarios", icon: Users },
];

const ROLE_SUBTITLE: Record<string, string> = {
  DISPATCHER: "Despacho",
  ADMIN: "Administración",
  RESTAURANT_OPERATOR: "Local",
  DRIVER: "Driver",
};

function SidebarContent({
  currentPage,
  onNavigate,
  onLogoutRequest,
  onCloseMobile,
  showClose,
  user,
}: {
  currentPage: Page;
  onNavigate: (page: Page) => void;
  onLogoutRequest: () => void;
  onCloseMobile?: () => void;
  showClose?: boolean;
  user: SessionUser;
}) {
  const allowed = new Set(PAGES_BY_ROLE[user.role]);
  const navItems = ALL_NAV.filter((item) => allowed.has(item.page));
  const initials = user.fullName
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <>
      <div
        className="flex items-center gap-2.5 px-4 sm:px-5 py-4 sm:py-5 border-b"
        style={{ borderColor: "var(--border)" }}
      >
        <div
          className="w-7 h-7 rounded flex items-center justify-center shrink-0"
          style={{ background: "var(--primary)", color: "var(--primary-foreground)" }}
        >
          <Bike className="w-4 h-4" strokeWidth={2.5} />
        </div>
        <div className="min-w-0 flex-1">
          <div
            className="text-sm font-semibold tracking-tight truncate"
            style={{ color: "var(--foreground)" }}
          >
            {APP_NAME}
          </div>
          <div className="text-[10px] font-mono" style={{ color: "var(--muted-foreground)" }}>
            {ROLE_SUBTITLE[user.role] ?? "Operaciones"}
          </div>
        </div>
        {showClose && (
          <button
            type="button"
            onClick={onCloseMobile}
            className="p-1.5 rounded cursor-pointer lg:hidden"
            style={{ color: "var(--muted-foreground)" }}
            aria-label="Cerrar menú"
          >
            <X className="w-5 h-5" />
          </button>
        )}
      </div>

      <nav className="flex-1 py-3 px-2 space-y-0.5 overflow-y-auto">
        {navItems.map((item) => {
          const active =
            currentPage === item.page ||
            (item.page === "orders" && currentPage === "order-detail");
          const Icon = item.icon;
          return (
            <div key={item.page}>
              {item.dividerBefore && (
                <div className="mx-2 my-1.5 border-t" style={{ borderColor: "var(--border)" }} />
              )}
              <button
                type="button"
                onClick={() => onNavigate(item.page)}
                className="w-full flex items-center gap-3 px-3 py-2.5 rounded text-sm text-left cursor-pointer"
                style={{
                  background: active ? "rgba(217 119 6 / 0.08)" : "transparent",
                  color: active ? "var(--primary)" : "var(--secondary-foreground)",
                  fontWeight: active ? 600 : 400,
                }}
                aria-current={active ? "page" : undefined}
              >
                <Icon className="w-4 h-4 shrink-0" strokeWidth={active ? 2.25 : 1.75} />
                <span className="truncate">{item.label}</span>
                {active && (
                  <span
                    className="ml-auto w-1 h-4 rounded-full shrink-0"
                    style={{ background: "var(--primary)" }}
                  />
                )}
              </button>
            </div>
          );
        })}
      </nav>

      <div className="border-t px-3 py-3 space-y-2" style={{ borderColor: "var(--border)" }}>
        <div className="flex items-center gap-2.5 px-1">
          <div
            className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0"
            style={{ background: "rgba(217 119 6 / 0.12)", color: "var(--primary)" }}
          >
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <div className="text-xs font-medium truncate" style={{ color: "var(--foreground)" }}>
              {user.fullName}
            </div>
            <div
              className="text-[10px] font-mono truncate"
              style={{ color: "var(--muted-foreground)" }}
            >
              {user.role}
            </div>
          </div>
        </div>
        <button
          type="button"
          onClick={onLogoutRequest}
          className="w-full flex items-center gap-2.5 px-3 py-2 rounded text-xs cursor-pointer"
          style={{ color: "var(--muted-foreground)" }}
        >
          <LogOut className="w-3.5 h-3.5 shrink-0" />
          Cerrar sesión
        </button>
      </div>
    </>
  );
}

export default function Sidebar({
  currentPage,
  onNavigate,
  onLogout,
  mobileOpen,
  onMobileClose,
  user,
}: SidebarProps) {
  const [confirmLogout, setConfirmLogout] = useState(false);

  return (
    <>
      <aside
        className="hidden lg:flex flex-col h-full w-56 xl:w-60 shrink-0 border-r"
        style={{ background: "var(--card)", borderColor: "var(--border)" }}
      >
        <SidebarContent
          currentPage={currentPage}
          onNavigate={onNavigate}
          onLogoutRequest={() => setConfirmLogout(true)}
          user={user}
        />
      </aside>

      <div
        className={`fixed inset-0 z-40 lg:hidden transition-opacity duration-200 ${
          mobileOpen ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
        }`}
        aria-hidden={!mobileOpen}
      >
        <button
          type="button"
          className="absolute inset-0 cursor-pointer"
          style={{ background: "rgba(0 0 0 / 0.4)" }}
          onClick={onMobileClose}
          aria-label="Cerrar menú"
        />
        <aside
          className={`absolute inset-y-0 left-0 flex flex-col w-[min(18rem,85vw)] border-r shadow-xl transition-transform duration-200 ease-out ${
            mobileOpen ? "translate-x-0" : "-translate-x-full"
          }`}
          style={{ background: "var(--card)", borderColor: "var(--border)" }}
          role="dialog"
          aria-modal="true"
          aria-label="Navegación"
        >
          <SidebarContent
            currentPage={currentPage}
            onNavigate={onNavigate}
            onLogoutRequest={() => setConfirmLogout(true)}
            onCloseMobile={onMobileClose}
            showClose
            user={user}
          />
        </aside>
      </div>

      {confirmLogout && (
        <div
          className="fixed inset-0 z-[2000] flex items-center justify-center p-4"
          style={{ background: "rgba(0 0 0 / 0.35)" }}
        >
          <div
            className="rounded-lg border p-5 sm:p-6 w-full max-w-sm space-y-4 shadow-xl"
            style={{ background: "var(--card)", borderColor: "var(--border)" }}
          >
            <div className="flex items-start gap-3">
              <div
                className="w-9 h-9 rounded-full flex items-center justify-center shrink-0"
                style={{ background: "#fef2f2", color: "#b91c1c" }}
              >
                <LogOut className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-sm font-semibold" style={{ color: "var(--foreground)" }}>
                  ¿Cerrar sesión?
                </h3>
                <p className="text-xs mt-1" style={{ color: "var(--muted-foreground)" }}>
                  Volverás a la pantalla de inicio de sesión.
                </p>
              </div>
            </div>
            <div className="flex flex-col-reverse sm:flex-row gap-2">
              <button
                type="button"
                onClick={() => setConfirmLogout(false)}
                className="flex-1 py-2 rounded border text-sm cursor-pointer"
                style={{ borderColor: "var(--border)", color: "var(--muted-foreground)" }}
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={() => {
                  setConfirmLogout(false);
                  onMobileClose();
                  onLogout();
                }}
                className="flex-1 py-2 rounded text-sm font-semibold cursor-pointer"
                style={{ background: "#dc2626", color: "#fff" }}
              >
                Cerrar sesión
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
