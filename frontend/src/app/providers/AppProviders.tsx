import type { ReactNode } from "react";

interface AppProvidersProps {
  children: ReactNode;
}

/** Contenedor de providers globales (QueryClient, auth, toasts, etc.). */
export default function AppProviders({ children }: AppProvidersProps) {
  return <>{children}</>;
}
