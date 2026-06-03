// src/services/notifications.ts
import { API_URL } from './api';

export interface TaskShareInvite {
    inviteId: number;
    inviteToken: string;
    status: 'PENDING' | 'ACCEPTED' | 'DECLINED';
    createdAt: string;
    respondedAt?: string;
    senderUserId: number;
    senderFullName: string;
    senderEmail: string;
    senderProfilePicture?: string;
    taskId: number;
    taskName: string;
    taskDescription?: string;
    taskDeadline?: string;
}

interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
}

function getToken() {
    return localStorage.getItem('token');
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
    const headers = new Headers(options.headers || {});
    headers.set('Content-Type', 'application/json');

    const token = getToken();
    if (token) headers.set('Authorization', `Bearer ${token}`);

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

export async function getPendingInvites(): Promise<TaskShareInvite[]> {
    const response = await request<ApiResponse<TaskShareInvite[]>>('/api/v1/users/invites/pending');
    return response.data;
}

export async function getPendingInviteCount(): Promise<number> {
    const response = await request<ApiResponse<number>>('/api/v1/users/invites/count');
    return response.data;
}

export async function acceptInvite(token: string): Promise<TaskShareInvite> {
    const response = await request<ApiResponse<TaskShareInvite>>(
        `/api/v1/users/invites/accept?token=${encodeURIComponent(token)}`,
        { method: 'PATCH' }
    );
    return response.data;
}

export async function declineInvite(token: string): Promise<TaskShareInvite> {
    const response = await request<ApiResponse<TaskShareInvite>>(
        `/api/v1/users/invites/decline?token=${encodeURIComponent(token)}`,
        { method: 'PATCH' }
    );
    return response.data;
}

export interface ShareTaskRequest {
    receiverUserIds: number[];
    taskIds: number[];
}

export async function shareTasks(shareRequest: ShareTaskRequest): Promise<TaskShareInvite[]> {
    const response = await request<ApiResponse<TaskShareInvite[]>>('/api/v1/users/share', {
        method: 'POST',
        body: JSON.stringify(shareRequest),
    });
    return response.data;
}

export interface UserSearchResult {
    userId: number;
    fullName: string;
    email: string;
    profilePicture?: string;
}

interface BackendUserSearchResult {
    userId: number;
    firstName?: string;
    lastName?: string;
    email: string;
    profilePicture?: string;
}

export async function searchUsers(query: string): Promise<UserSearchResult[]> {
    const response = await request<ApiResponse<BackendUserSearchResult[]>>(
        `/api/v1/users/search?query=${encodeURIComponent(query)}`
    );
    // Map backend firstName/lastName to fullName
    return response.data.map((user) => ({
        userId: user.userId,
        fullName: [user.firstName, user.lastName].filter(Boolean).join(' ') || user.email,
        email: user.email,
        profilePicture: user.profilePicture,
    }));
}