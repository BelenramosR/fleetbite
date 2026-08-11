import type { SessionUser } from "@/features/auth/lib/access";
import type { UserRole } from "@/shared/types";
import {
  clearTokens,
  getAccessToken,
  getRefreshToken,
  requestApi,
  storeTokens,
} from "@/services/api/httpClient";

interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshToken: string;
}

interface JwtClaims {
  sub: string;
  email: string;
  role: UserRole;
  exp: number;
}

interface DriverProfileResponse {
  id: string;
}

const SESSION_KEY = "fleetbite.session";

function decodeClaims(token: string): JwtClaims {
  const payload = token.split(".")[1];
  if (!payload) throw new Error("Token inválido.");
  const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
  const normalized = base64.padEnd(Math.ceil(base64.length / 4) * 4, "=");
  const decoded = decodeURIComponent(
    atob(normalized)
      .split("")
      .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, "0")}`)
      .join(""),
  );
  return JSON.parse(decoded) as JwtClaims;
}

function sessionFromToken(token: string): SessionUser {
  const claims = decodeClaims(token);
  return {
    id: claims.sub,
    email: claims.email,
    fullName: claims.email.split("@")[0],
    role: claims.role,
  };
}

export async function login(email: string, password: string): Promise<SessionUser> {
  const tokens = await requestApi<TokenResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email: email.trim().toLowerCase(), password }),
    authenticated: false,
    retryOnUnauthorized: false,
  });
  storeTokens(tokens);
  const session = sessionFromToken(tokens.accessToken);
  if (session.role === "DRIVER") {
    const profile = await requestApi<DriverProfileResponse>("/drivers/me");
    session.driverId = profile.id;
  }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  return session;
}

export function restoreSession(): SessionUser | null {
  const token = getAccessToken();
  if (!token) return null;
  try {
    const claims = decodeClaims(token);
    if (claims.exp * 1000 <= Date.now() && !getRefreshToken()) {
      clearTokens();
      return null;
    }
    const current = sessionFromToken(token);
    const stored = localStorage.getItem(SESSION_KEY);
    if (!stored) return current;
    const session = JSON.parse(stored) as SessionUser;
    return session.id === current.id ? session : current;
  } catch {
    clearTokens();
    localStorage.removeItem(SESSION_KEY);
    return null;
  }
}

export async function logout(): Promise<void> {
  const refreshToken = getRefreshToken();
  try {
    if (refreshToken) {
      await requestApi<void>("/auth/logout", {
        method: "POST",
        body: JSON.stringify({ refreshToken }),
        authenticated: false,
        retryOnUnauthorized: false,
      });
    }
  } finally {
    clearTokens();
    localStorage.removeItem(SESSION_KEY);
  }
}
