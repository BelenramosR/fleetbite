import { X } from "lucide-react";

interface NewOrderModalProps {
  onClose: () => void;
  onCreated: () => void;
}

export default function NewOrderModal({ onClose, onCreated }: NewOrderModalProps) {
  return (
    <div
      className="fixed inset-0 flex items-center justify-center z-40"
      style={{ background: "rgba(0 0 0 / 0.6)" }}
      onClick={onClose}
    >
      <div
        className="rounded-lg border p-6 w-full max-w-md space-y-4"
        style={{ background: "var(--card)", borderColor: "var(--border)" }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold" style={{ color: "var(--foreground)" }}>
            Nuevo pedido
          </h3>
          <button
            onClick={onClose}
            className="cursor-pointer p-0.5"
            style={{ color: "var(--muted-foreground)" }}
            aria-label="Cerrar"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
        {[
          { id: "cn", label: "NOMBRE DEL CLIENTE", placeholder: "Valentina Morales", type: "text" },
          { id: "cp", label: "TELÉFONO", placeholder: "+51 999 000 111", type: "tel" },
          { id: "addr", label: "DIRECCIÓN DE ENTREGA", placeholder: "Av. Larco 1150, Miraflores", type: "text" },
          { id: "amt", label: "MONTO TOTAL (S/)", placeholder: "45.00", type: "number" },
        ].map((f) => (
          <div key={f.id} className="space-y-1.5">
            <label
              htmlFor={f.id}
              className="text-[10px] font-mono tracking-widest"
              style={{ color: "var(--muted-foreground)" }}
            >
              {f.label}
            </label>
            <input
              id={f.id}
              type={f.type}
              placeholder={f.placeholder}
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
        <div className="space-y-1.5">
          <label
            className="text-[10px] font-mono tracking-widest"
            style={{ color: "var(--muted-foreground)" }}
          >
            PRIORIDAD
          </label>
          <select
            className="w-full px-3 py-2 rounded border text-sm outline-none cursor-pointer"
            style={{
              background: "var(--muted)",
              borderColor: "var(--border)",
              color: "var(--foreground)",
            }}
            onFocus={(e) => (e.currentTarget.style.borderColor = "var(--primary)")}
            onBlur={(e) => (e.currentTarget.style.borderColor = "var(--border)")}
          >
            <option value="NORMAL">NORMAL</option>
            <option value="HIGH">HIGH</option>
            <option value="CRITICAL">CRITICAL</option>
          </select>
        </div>
        <div className="flex gap-2 pt-2">
          <button
            onClick={onClose}
            className="flex-1 py-2 rounded border text-sm cursor-pointer"
            style={{ borderColor: "var(--border)", color: "var(--muted-foreground)" }}
          >
            Cancelar
          </button>
          <button
            onClick={onCreated}
            className="flex-1 py-2 rounded text-sm font-semibold cursor-pointer"
            style={{ background: "var(--primary)", color: "var(--primary-foreground)" }}
          >
            Crear pedido
          </button>
        </div>
      </div>
    </div>
  );
}
