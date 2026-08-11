import { useState } from "react";
import type { Page, NavState, NavExtra } from "@/app/router";
import type { SessionUser } from "@/features/auth/lib/access";
import { canAccessPage, homePageForRole } from "@/features/auth/lib/access";
import AppLayout from "@/app/layouts/AppLayout";
import LoginPage from "@/features/auth/pages/LoginPage";
import DashboardPage from "@/features/dashboard/pages/DashboardPage";
import OrdersPage from "@/features/orders/pages/OrdersPage";
import OrderDetailPage from "@/features/orders/pages/OrderDetailPage";
import NewOrderModal from "@/features/orders/components/NewOrderModal";
import DriversPage from "@/features/drivers/pages/DriversPage";
import DriverAppPage from "@/features/drivers/pages/DriverAppPage";
import FleetPage from "@/features/fleet/pages/FleetPage";
import UsersPage from "@/features/users/pages/UsersPage";
import ReportsPage from "@/features/reports/pages/ReportsPage";
import SettingsPage from "@/features/settings/pages/SettingsPage";
import { logout, restoreSession } from "@/features/auth/services/authApi";

export default function App() {
  const [session, setSession] = useState<SessionUser | null>(() => restoreSession());
  const [nav, setNav] = useState<NavState>(() => {
    const restored = restoreSession();
    return { page: restored ? homePageForRole(restored.role) : "dashboard" };
  });
  const [showNewOrder, setShowNewOrder] = useState(false);
  const [ordersRefreshKey, setOrdersRefreshKey] = useState(0);

  function handleLogin(user: SessionUser) {
    setSession(user);
    setNav({ page: homePageForRole(user.role) });
  }

  function handleLogout() {
    void logout();
    setSession(null);
    setNav({ page: "dashboard" });
    setShowNewOrder(false);
  }

  const navigate = (page: Page, extra?: NavExtra) => {
    if (!session) return;
    if (!canAccessPage(session.role, page)) {
      setNav({ page: homePageForRole(session.role) });
      return;
    }
    setNav({ page, ...extra });
  };

  if (!session) {
    return <LoginPage onLogin={handleLogin} />;
  }

  if (session.role === "DRIVER") {
    return (
      <DriverAppPage
        user={session}
        page={nav.page}
        onNavigate={(page) => navigate(page)}
        onLogout={handleLogout}
      />
    );
  }

  const canCreateOrder = session.role === "DISPATCHER" || session.role === "RESTAURANT_OPERATOR";

  return (
    <>
      <AppLayout
        currentPage={nav.page}
        onNavigate={(page) => navigate(page)}
        onLogout={handleLogout}
        onNewOrder={canCreateOrder ? () => setShowNewOrder(true) : undefined}
        user={session}
      >
        {nav.page === "dashboard" && <DashboardPage onNavigate={navigate} />}

        {nav.page === "orders" && (
          <OrdersPage
            refreshKey={ordersRefreshKey}
            onSelectOrder={(id) =>
              navigate("order-detail", {
                selectedOrderId: id,
                ordersFilter: nav.ordersFilter,
              })
            }
            initialFilter={nav.ordersFilter}
          />
        )}

        {nav.page === "order-detail" && nav.selectedOrderId && (
          <OrderDetailPage
            orderId={nav.selectedOrderId}
            onBack={() => navigate("orders", { ordersFilter: nav.ordersFilter })}
          />
        )}

        {nav.page === "drivers" && <DriversPage />}
        {nav.page === "fleet" && <FleetPage />}
        {nav.page === "reports" && <ReportsPage />}
        {nav.page === "users" && <UsersPage />}
        {nav.page === "settings" && <SettingsPage />}
      </AppLayout>

      {showNewOrder && canCreateOrder && (
        <NewOrderModal
          onClose={() => setShowNewOrder(false)}
          onCreated={() => {
            setShowNewOrder(false);
            setOrdersRefreshKey((value) => value + 1);
            navigate("orders");
          }}
        />
      )}
    </>
  );
}
