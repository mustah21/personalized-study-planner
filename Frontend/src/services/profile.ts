// src/services/profile.ts
import type { User } from "../types";
import { getJSON, putJSON } from "./api";

type ApiResponse<T> = {
  status: number;
  message: string;
  data: T;
};

type BackendUser = {
  userId: number;
  firstName?: string | null;
  lastName?: string | null;
  email: string;
  profilePicture?: string | null;
 
};

const BIO_KEY = "profile_bio";

function mapBackendUser(u: BackendUser): User {
  const email = u.email ?? "";
  const username = email ? email.split("@")[0] : "user";
  const firstName = u.firstName ?? "";
  const lastName = u.lastName ?? "";
  const name = [firstName, lastName].filter(Boolean).join(" ").trim();

  const bio = localStorage.getItem(BIO_KEY) ?? "";

  return {
    id: String(u.userId),
    username,
    email,
    name: name || username,
    bio, 
    firstName,
    lastName,
    profilePicture: u.profilePicture ?? "",
  };
}

export async function fetchMe(): Promise<User> {
  const res = await getJSON<ApiResponse<BackendUser>>("/api/v1/users/me");
  return mapBackendUser(res.data);
}

export async function saveProfile(payload: {
  firstName: string;
  lastName: string;
  profilePicture: string;
  bio: string; 
}): Promise<User> {
 
  localStorage.setItem(BIO_KEY, payload.bio ?? "");


  await putJSON<ApiResponse<BackendUser>>("/api/v1/users/update", {
    firstName: payload.firstName,
    lastName: payload.lastName,
    profilePicture: payload.profilePicture,
  });

  const me = await fetchMe();


  localStorage.setItem("user", JSON.stringify(me));

  return me;
}