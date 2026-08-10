/**
 * Cliente HTTP centralizado.
 * Preparado para Axios (base URL, headers, token, interceptores).
 * Por ahora solo define la configuración base.
 */
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1";

export const httpClientConfig = {
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
} as const;

export function getApiBaseUrl(): string {
  return httpClientConfig.baseURL;
}
