export interface AuthUser {
  id: string;
  name: string;
  surname: string;
  email: string;
  role: "ADMIN" | "EVALUATOR" | string;
}

interface AuthSession {
  accessToken: string;
  refreshToken: string;
  user: AuthUser;
}

export const API_BASE_URL = (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").replace(/\/$/, "");
const SESSION_KEY = "mnr.auth.session";
export const AUTH_EXPIRED_EVENT = "mnr:auth-expired";

let refreshInFlight: Promise<AuthSession | null> | null = null;

function isAuthSession(value: unknown): value is AuthSession {
  if (!value || typeof value !== "object") return false;
  const session = value as Partial<AuthSession>;
  const user = session.user;
  return (
    typeof session.accessToken === "string" &&
    typeof session.refreshToken === "string" &&
    !!user &&
    typeof user.id === "string" &&
    typeof user.name === "string" &&
    typeof user.surname === "string" &&
    typeof user.email === "string" &&
    typeof user.role === "string"
  );
}

export function readAuthSession(): AuthSession | null {
  if (typeof window === "undefined") return null;
  const value = window.sessionStorage.getItem(SESSION_KEY);
  if (!value) return null;

  try {
    const parsed: unknown = JSON.parse(value);
    if (isAuthSession(parsed)) return parsed;
  } catch {
    clearAuthSession();
    return null;
  }

  clearAuthSession();
  return null;
}

function saveAuthSession(session: AuthSession) {
  window.sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

export function clearAuthSession() {
  if (typeof window === "undefined") return;
  window.sessionStorage.removeItem(SESSION_KEY);
}

function notifyAuthExpired() {
  clearAuthSession();
  window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
}

function getErrorMessage(status: number) {
  if (status === 401) return "E-mail ou senha inválidos.";
  if (status === 400) return "Informe um e-mail e uma senha válidos.";
  return `Não foi possível concluir a autenticação (HTTP ${status}).`;
}

async function fetchAuth(path: string, body: object): Promise<AuthSession> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
  } catch (cause) {
    throw new Error(
      "Não foi possível conectar ao servidor. Verifique se o backend está em execução e tente novamente.",
      { cause },
    );
  }

  if (!response.ok) throw new Error(getErrorMessage(response.status));

  const result: unknown = await response.json();
  if (!isAuthSession(result)) {
    throw new Error("O servidor retornou uma resposta de autenticação inválida.");
  }
  return result;
}

export async function signIn(email: string, password: string): Promise<AuthSession> {
  const session = await fetchAuth("/auth/login", { email, password });
  saveAuthSession(session);
  return session;
}

async function refreshSession(): Promise<AuthSession | null> {
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      const current = readAuthSession();
      if (!current) return null;

      let response: Response;
      try {
        response = await fetch(`${API_BASE_URL}/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken: current.refreshToken }),
        });
      } catch (cause) {
        throw new Error("Não foi possível renovar a sessão porque o servidor está indisponível.", {
          cause,
        });
      }

      if (!response.ok) {
        if (response.status === 400 || response.status === 401) notifyAuthExpired();
        else throw new Error(`Não foi possível renovar a sessão (HTTP ${response.status}).`);
        return null;
      }

      const result: unknown = await response.json();
      if (!isAuthSession(result)) {
        notifyAuthExpired();
        throw new Error("O servidor retornou uma resposta de renovação inválida.");
      }
      saveAuthSession(result);
      return result;
    })().finally(() => {
      refreshInFlight = null;
    });
  }

  return refreshInFlight;
}

function withAccessToken(init: RequestInit, token: string): RequestInit {
  const headers = new Headers(init.headers);
  headers.set("Authorization", `Bearer ${token}`);
  return { ...init, headers };
}

export async function apiFetch(path: string, init: RequestInit = {}): Promise<Response> {
  const url = `${API_BASE_URL}${path}`;
  const session = readAuthSession();
  const response = await fetch(
    url,
    session ? withAccessToken(init, session.accessToken) : init,
  );
  if (response.status !== 401) return response;

  const refreshed = await refreshSession();
  if (!refreshed) {
    if (session) notifyAuthExpired();
    return response;
  }

  const retryResponse = await fetch(url, withAccessToken(init, refreshed.accessToken));
  if (retryResponse.status === 401) notifyAuthExpired();
  return retryResponse;
}

export async function signOut(): Promise<void> {
  const session = readAuthSession();
  try {
    if (!session) return;
    const response = await fetch(`${API_BASE_URL}/auth/logout`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken: session.refreshToken }),
    });
    if (!response.ok) {
      throw new Error(`Não foi possível confirmar o encerramento da sessão (HTTP ${response.status}).`);
    }
  } finally {
    clearAuthSession();
  }
}

export function getHomeForRole(role: string): string {
  return role === "EVALUATOR" ? "/avaliador" : "/";
}
