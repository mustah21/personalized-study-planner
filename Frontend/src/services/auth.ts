// src/services/auth.ts
import type { User } from "../types";
import { postJSON } from "./api";

let currentUser: User | null = null;

type AuthResponse = {
  status: number;
  message: string;
  data: {
    token: string;
    user: any;
  };
};

function mapUser(u: any): User {
  const email = u.email ?? "";
  return {
    id: String(u.userId ?? u.id ?? "1"),
    username: u.username ?? (email ? email.split("@")[0] : "user"),
    email,
    name: [u.firstName, u.lastName].filter(Boolean).join(" ") || u.name || "",
    bio: u.bio ?? "",

    firstName: u.firstName ?? "",
    lastName: u.lastName ?? "",
    profilePicture: u.profilePicture ?? "",
  };
}

export async function loginWithEmail(email: string, password?: string): Promise<User> {
  if (!password) throw new Error("Missing password. Call loginWithEmail(email, password).");

  const res = await postJSON<AuthResponse>("/api/v1/users/login", { email, password });

  localStorage.setItem("token", res.data.token);
  localStorage.setItem("user", JSON.stringify(res.data.user));

  currentUser = mapUser(res.data.user);
  return currentUser;
}

export async function signup(fullName: string, email: string, password: string): Promise<User> {
  const res = await postJSON<AuthResponse>("/api/v1/users/register", { fullName, email, password });

  localStorage.setItem("token", res.data.token);
  localStorage.setItem("user", JSON.stringify(res.data.user));

  currentUser = mapUser(res.data.user);
  return currentUser;
}

export function getCurrentUser(): User | null {
  if (currentUser) return currentUser;

  const raw = localStorage.getItem("user");
  if (!raw) return null;

  try {
    const u = JSON.parse(raw);
    currentUser = mapUser(u);
    return currentUser;
  } catch {
    return null;
  }
}

export function logout(): void {
  currentUser = null;
  localStorage.removeItem("token");
  localStorage.removeItem("user");
}