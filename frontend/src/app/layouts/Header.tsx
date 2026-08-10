import { useState, useEffect } from "react";
import { Menu, Plus } from "lucide-react";
import type { Page } from "@/app/router";

const PAGE_TITLES: Record<Page, string> = {
  login: "Login",
  dashboard: "Dashboard operativo",
  orders: "Pedidos",
  "order-detail": "Detalle del pedido",
  drivers: "Motorizados",
  fleet: "Flota",
  reports: "Reportes",
  users: "Usuarios",
  settings: "Configuración",
  "driver-assignment": "Mi asignación",
  "driver-profile": "Mi perfil",
};

interface HeaderProps {
  page: Page;
  onNewOrder?: () => void;
  onMenuClick?: () => void;
}

export default function Header({ page, onNewOrder, onMenuClick }: HeaderProps) {
  const [time, setTime] = useState(new Date());

  useEffect(() => {
    const id = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(id);
  }, []);

  const formatted = time.toLocaleTimeString("es-PE", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });

  const dateFormatted = time.toLocaleDateString("es-PE", {
    weekday: "short",
    day: "2-digit",
    month: "short",
  });

  const showNewOrder = Boolean(onNewOrder) && (page === "orders" || page === "dashboard");

  return (
    <header
      className="flex items-center gap-2 sm:gap-3 px-3 sm:px-5 lg:px-6 h-14 border-b shrink-0"
      style={{ background: "var(--card)", borderColor: "var(--border)" }}
    >
      <button
        type="button"
        onClick={onMenuClick}
        className="lg:hidden p-2 -ml-1 rounded cursor-pointer shrink-0"
        style={{ color: "var(--foreground)" }}
        aria-label="Abrir menú"
      >
        <Menu className="w-5 h-5" />
      </button>

      <div className="flex-1 min-w-0 flex items-center gap-2">
        <h1
          className="text-sm font-semibold truncate"
          style={{ color: "var(--foreground)" }}
        >
          {PAGE_TITLES[page]}
        </h1>
      </div>

      <div className="flex items-center gap-2 sm:gap-4 shrink-0">
        <div className="text-right hidden md:block">
          <div
            className="text-sm font-mono font-medium tabular-nums"
            style={{ color: "var(--foreground)" }}
          >
            {formatted}
          </div>
          <div
            className="text-[10px] font-mono uppercase"
            style={{ color: "var(--muted-foreground)" }}
          >
            {dateFormatted}
          </div>
        </div>

        {showNewOrder && (
          <button
            type="button"
            onClick={onNewOrder}
            className="flex items-center gap-1.5 sm:gap-2 px-2.5 sm:px-3 py-1.5 rounded text-xs font-semibold cursor-pointer"
            style={{
              background: "var(--primary)",
              color: "var(--primary-foreground)",
            }}
            onMouseEnter={(e) => ((e.currentTarget as HTMLElement).style.opacity = "0.85")}
            onMouseLeave={(e) => ((e.currentTarget as HTMLElement).style.opacity = "1")}
          >
            <Plus className="w-3.5 h-3.5" strokeWidth={2.5} />
            <span className="sm:hidden">Nuevo</span>
            <span className="hidden sm:inline">Nuevo pedido</span>
          </button>
        )}
      </div>
    </header>
  );
}
