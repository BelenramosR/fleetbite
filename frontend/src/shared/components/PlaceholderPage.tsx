import type { LucideIcon } from "lucide-react";
import { LayoutGrid } from "lucide-react";

interface PlaceholderPageProps {
  title: string;
  description: string;
  icon?: LucideIcon;
}

export default function PlaceholderPage({
  title,
  description,
  icon: Icon = LayoutGrid,
}: PlaceholderPageProps) {
  return (
    <div className="flex flex-col items-center justify-center h-full min-h-[400px] text-center">
      <Icon className="w-10 h-10 mb-4" style={{ color: "var(--muted-foreground)" }} />
      <h2 className="text-base font-semibold mb-2" style={{ color: "var(--foreground)" }}>
        {title}
      </h2>
      <p className="text-sm max-w-xs" style={{ color: "var(--muted-foreground)" }}>
        {description}
      </p>
      <div
        className="mt-6 px-4 py-2 rounded border text-xs font-mono"
        style={{ borderColor: "var(--border)", color: "var(--muted-foreground)" }}
      >
        MVP — pendiente de implementación
      </div>
    </div>
  );
}
