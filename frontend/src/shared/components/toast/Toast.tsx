import { Check, AlertTriangle, Info, X } from "lucide-react";

export type ToastTone = "success" | "error" | "warning" | "info";

interface ToastProps {
  message: string;
  tone?: ToastTone;
  onClose?: () => void;
}

const TONE = {
  success: {
    border: "rgba(22 163 74 / 0.35)",
    bg: "#f0fdf4",
    text: "#166534",
    icon: Check,
  },
  error: {
    border: "rgba(185 28 28 / 0.35)",
    bg: "#fef2f2",
    text: "#991b1b",
    icon: X,
  },
  warning: {
    border: "rgba(180 83 9 / 0.35)",
    bg: "#fffbeb",
    text: "#92400e",
    icon: AlertTriangle,
  },
  info: {
    border: "rgba(37 99 235 / 0.3)",
    bg: "#eff6ff",
    text: "#1e40af",
    icon: Info,
  },
} as const;

/** Toast fijo arriba a la derecha, visible al instante al cambiar de estado. */
export default function Toast({ message, tone = "success", onClose }: ToastProps) {
  const cfg = TONE[tone];
  const Icon = cfg.icon;

  return (
    <div
      role="status"
      aria-live="polite"
      className="fixed top-4 right-4 sm:top-5 sm:right-5 z-[60] flex items-start gap-3 max-w-[calc(100vw-2rem)] sm:max-w-sm px-4 py-3 rounded-lg border shadow-lg"
      style={{
        background: cfg.bg,
        borderColor: cfg.border,
        color: cfg.text,
      }}
    >
      <span
        className="mt-0.5 w-6 h-6 rounded-full flex items-center justify-center shrink-0"
        style={{ background: cfg.text, color: "#fff" }}
      >
        <Icon className="w-3.5 h-3.5" strokeWidth={2.5} />
      </span>
      <div className="min-w-0 flex-1 pt-0.5">
        <div className="text-[10px] font-mono tracking-widest uppercase opacity-70 mb-0.5">
          {tone === "success"
            ? "Listo"
            : tone === "error"
              ? "Error"
              : tone === "warning"
                ? "Atención"
                : "Info"}
        </div>
        <p className="text-sm font-medium leading-snug">{message}</p>
      </div>
      {onClose && (
        <button
          type="button"
          onClick={onClose}
          aria-label="Cerrar notificación"
          className="p-0.5 rounded cursor-pointer opacity-60 hover:opacity-100"
        >
          <X className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  );
}
