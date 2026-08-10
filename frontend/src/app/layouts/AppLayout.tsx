import { useEffect, useState, type ReactNode } from "react";
import type { Page } from "@/app/router";
import type { SessionUser } from "@/features/auth/lib/access";
import Sidebar from "./Sidebar";
import Header from "./Header";

interface AppLayoutProps {
  currentPage: Page;
  onNavigate: (page: Page) => void;
  onLogout: () => void;
  onNewOrder?: () => void;
  user: SessionUser;
  children: ReactNode;
}

export default function AppLayout({
  currentPage,
  onNavigate,
  onLogout,
  onNewOrder,
  user,
  children,
}: AppLayoutProps) {
  const [mobileNavOpen, setMobileNavOpen] = useState(false);

  useEffect(() => {
    setMobileNavOpen(false);
  }, [currentPage]);

  useEffect(() => {
    if (!mobileNavOpen) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") setMobileNavOpen(false);
    };
    document.addEventListener("keydown", onKey);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKey);
      document.body.style.overflow = "";
    };
  }, [mobileNavOpen]);

  function handleNavigate(page: Page) {
    onNavigate(page);
    setMobileNavOpen(false);
  }

  return (
    <div className="flex h-dvh overflow-hidden" style={{ background: "var(--background)" }}>
      <Sidebar
        currentPage={currentPage}
        onNavigate={handleNavigate}
        onLogout={onLogout}
        mobileOpen={mobileNavOpen}
        onMobileClose={() => setMobileNavOpen(false)}
        user={user}
      />

      <div className="flex flex-col flex-1 min-w-0 overflow-hidden">
        <Header
          page={currentPage}
          onNewOrder={onNewOrder}
          onMenuClick={() => setMobileNavOpen(true)}
        />
        <main className="flex-1 overflow-y-auto overflow-x-hidden p-4 sm:p-5 lg:p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
