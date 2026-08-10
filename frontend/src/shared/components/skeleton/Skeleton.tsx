export function SkeletonBlock({ className = "" }: { className?: string }) {
  return <div className={`skeleton ${className}`} />;
}

export function MetricCardSkeleton() {
  return (
    <div
      className="rounded-lg border p-3.5 sm:p-5 min-w-0"
      style={{ background: "var(--card)", borderColor: "var(--border)" }}
    >
      <SkeletonBlock className="h-3 w-20 sm:w-24 mb-3 sm:mb-4" />
      <SkeletonBlock className="h-7 sm:h-8 w-12 sm:w-16 mb-2" />
      <SkeletonBlock className="h-2 w-16 sm:w-20" />
    </div>
  );
}

export function TableSkeleton({ rows = 5 }: { rows?: number }) {
  return (
    <div className="space-y-0 overflow-x-auto">
      {Array.from({ length: rows }).map((_, i) => (
        <div
          key={i}
          className="flex items-center gap-3 sm:gap-4 px-3 sm:px-4 py-3 border-b min-w-[480px]"
          style={{ borderColor: "var(--border)" }}
        >
          <SkeletonBlock className="h-3 w-24 sm:w-28" />
          <SkeletonBlock className="h-3 w-28 sm:w-32 ml-2" />
          <SkeletonBlock className="h-5 w-16 sm:w-20 ml-auto" />
          <SkeletonBlock className="h-5 w-14 sm:w-16" />
          <SkeletonBlock className="h-3 w-10 sm:w-12" />
        </div>
      ))}
    </div>
  );
}

export function DetailSkeleton() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-12 gap-3">
      <div className="col-span-1 md:col-span-2 xl:col-span-12 rounded-xl border border-border bg-card p-5">
        <SkeletonBlock className="h-3 w-16 mb-3" />
        <SkeletonBlock className="h-7 w-48" />
      </div>
      <div className="col-span-1 md:col-span-2 xl:col-span-12 rounded-xl border border-border bg-card p-4">
        <SkeletonBlock className="h-8 w-full" />
      </div>
      <div className="col-span-1 xl:col-span-4 rounded-xl border border-border bg-card p-5 space-y-4">
        <SkeletonBlock className="h-3 w-28" />
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="space-y-1.5">
            <SkeletonBlock className="h-2 w-16" />
            <SkeletonBlock className="h-4 w-32" />
          </div>
        ))}
      </div>
      <div className="col-span-1 md:col-span-2 xl:col-span-5 xl:row-span-2 rounded-xl border border-border bg-card min-h-[280px] p-4">
        <SkeletonBlock className="h-3 w-28 mb-4" />
        <SkeletonBlock className="h-48 w-full" />
      </div>
      <div className="col-span-1 xl:col-span-3 xl:row-span-2 rounded-xl border border-border bg-card p-5 space-y-3">
        <SkeletonBlock className="h-3 w-32" />
        <SkeletonBlock className="h-11 w-full" />
        <SkeletonBlock className="h-11 w-full" />
      </div>
      <div className="col-span-1 md:col-span-2 xl:col-span-4 rounded-xl border border-border bg-card p-5 space-y-3">
        <SkeletonBlock className="h-3 w-32 mb-2" />
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="flex gap-3">
            <SkeletonBlock className="w-2 h-2 rounded-full mt-1 shrink-0" />
            <div className="space-y-1 flex-1">
              <SkeletonBlock className="h-3 w-36" />
              <SkeletonBlock className="h-2 w-20" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export function DashboardSkeleton() {
  return (
    <div className="space-y-4 sm:space-y-5">
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-2.5 sm:gap-3">
        {Array.from({ length: 4 }).map((_, i) => (
          <MetricCardSkeleton key={i} />
        ))}
      </div>

      <div
        className="rounded-lg border overflow-hidden"
        style={{ background: "var(--card)", borderColor: "var(--border)" }}
      >
        <div className="px-4 sm:px-5 py-3 border-b" style={{ borderColor: "var(--border)" }}>
          <SkeletonBlock className="h-3 w-40" />
        </div>
        {Array.from({ length: 3 }).map((_, i) => (
          <div
            key={i}
            className="flex items-center gap-4 px-4 sm:px-5 py-3.5 border-b last:border-b-0"
            style={{ borderColor: "var(--border)" }}
          >
            <SkeletonBlock className="h-6 w-6 shrink-0" />
            <div className="flex-1 space-y-1.5">
              <SkeletonBlock className="h-3 w-32 sm:w-40" />
              <SkeletonBlock className="h-2 w-40 sm:w-56" />
            </div>
            <SkeletonBlock className="h-3 w-20 hidden sm:block" />
          </div>
        ))}
      </div>

      <div className="grid grid-cols-2 xl:grid-cols-4 gap-2.5 sm:gap-3">
        {Array.from({ length: 4 }).map((_, i) => (
          <MetricCardSkeleton key={`s-${i}`} />
        ))}
      </div>
    </div>
  );
}
