// src/services/api.ts
export const API_URL = ""; // empty - nginx will proxy /api to backend

function getToken() {
  return localStorage.getItem("token");
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers || {});
  headers.set("Content-Type", "application/json");

  const token = getToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const res = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  });

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new Error(data?.message || `Request failed (${res.status})`);
  }

  return data as T;
}

export async function healthCheck() {
  return request(`/actuator/health`, { method: "GET" });
}

export async function postJSON<T>(path: string, body: any) {
  return request<T>(path, { method: "POST", body: JSON.stringify(body) });
}

export async function getJSON<T>(path: string) {
  return request<T>(path, { method: "GET" });
}

export async function putJSON<T>(path: string, body: any) {
  return request<T>(path, { method: "PUT", body: JSON.stringify(body) });
}
