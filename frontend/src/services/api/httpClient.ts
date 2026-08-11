const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "/api/v1";

const ACCESS_TOKEN_KEY = "fleetbite.accessToken";
const REFRESH_TOKEN_KEY = "fleetbite.refreshToken";

export interface ApiErrorItem {
  field: string | null;
  message: string;
}

export interface ApiResponse<T> {
  code: string;
  success: boolean;
  data: T | null;
  errors: ApiErrorItem[];
}

export class ApiClientError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    public readonly errors: ApiErrorItem[],
  ) {
    super(errors[0]?.message ?? "No se pudo completar la solicitud.");
    this.name = "ApiClientError";
  }
}

interface StoredTokens {
  accessToken: string;
  refreshToken: string;
}

interface RequestOptions extends RequestInit {
  authenticated?: boolean;
  retryOnUnauthorized?: boolean;
}

export const httpClientConfig = {
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
} as const;

export function getApiBaseUrl(): string {
  return httpClientConfig.baseURL;
}

export function storeTokens(tokens: StoredTokens): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

async function renewTokens(): Promise<boolean> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;

  try {
    const response = await requestApi<StoredTokens>("/auth/refresh", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
      authenticated: false,
      retryOnUnauthorized: false,
    });
    storeTokens(response);
    return true;
  } catch {
    clearTokens();
    return false;
  }
}

export async function requestApi<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const {
    authenticated = true,
    retryOnUnauthorized = true,
    headers: customHeaders,
    ...fetchOptions
  } = options;
  const headers = new Headers(customHeaders);
  if (fetchOptions.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  const accessToken = getAccessToken();
  if (authenticated && accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

  const response = await fetch(`${API_BASE_URL}${path}`, { ...fetchOptions, headers });
  if (response.status === 401 && authenticated && retryOnUnauthorized && (await renewTokens())) {
    return requestApi<T>(path, { ...options, retryOnUnauthorized: false });
  }

  if (response.status === 204) return undefined as T;

  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !body.success || body.data === null) {
    throw new ApiClientError(response.status, body.code, body.errors ?? []);
  }
  return body.data;
}
