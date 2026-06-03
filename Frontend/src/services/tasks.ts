// src/services/tasks.ts
import type { Task, TaskStatus } from '../types';
import { API_URL } from './api';

interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
}

interface BackendTask {
    taskId: number;
    userId: number;
    taskName: string;
    taskDescription?: string;
    taskDeadline?: string;
    priority: 'LOW' | 'MEDIUM' | 'HIGH';
    status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';
    completed: boolean;
    sharedByEmail?: string;
    language: string;
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

function mapBackendToFrontend(task: BackendTask): Task {
    const statusMap: Record<string, TaskStatus> = {
        'PENDING': 'todo',
        'IN_PROGRESS': 'in_progress',
        'COMPLETED': 'completed',
    };
    const priorityMap: Record<string, 'low' | 'medium' | 'high'> = {
        'LOW': 'low',
        'MEDIUM': 'medium',
        'HIGH': 'high',
    };

    return {
        id: String(task.taskId),
        title: task.taskName,
        description: task.taskDescription,
        status: statusMap[task.status] || 'todo',
        deadline: task.taskDeadline,
        createdAt: new Date().toISOString(), // Backend doesn't return createdAt, using placeholder
        priority: priorityMap[task.priority] || 'medium',
        sharedByEmail: task.sharedByEmail,
        language: task.language,
    };
}

function mapFrontendToBackend(task: Partial<Task>): Partial<BackendTask> {
    const statusMap: Record<TaskStatus, string> = {
        'todo': 'PENDING',
        'in_progress': 'IN_PROGRESS',
        'completed': 'COMPLETED',
    };
    const priorityMap: Record<string, string> = {
        'low': 'LOW',
        'medium': 'MEDIUM',
        'high': 'HIGH',
    };

    // Convert date string (YYYY-MM-DD) to ISO datetime format for backend
    let taskDeadline: string | undefined;
    if (task.deadline) {
        // If it's just a date, append time to make it a valid LocalDateTime
        if (task.deadline.length === 10) {
            taskDeadline = task.deadline + 'T23:59:59';
        } else {
            taskDeadline = task.deadline;
        }
    }

    return {
        taskName: task.title,
        taskDescription: task.description,
        taskDeadline,
        priority: priorityMap[task.priority || 'medium'] as 'LOW' | 'MEDIUM' | 'HIGH',
        status: statusMap[task.status || 'todo'] as 'PENDING' | 'IN_PROGRESS' | 'COMPLETED',
        completed: task.status === 'completed',
        language: task.language,
    };
}

export async function getTasks(): Promise<Task[]> {
    try {
        const response = await request<ApiResponse<BackendTask[]>>('/api/v1/task/user');
        return response.data.map(mapBackendToFrontend);
    } catch (error) {
        console.error('Failed to fetch tasks from API:', error);
        // Don't fallback to localStorage - return empty array to prevent stale/wrong user data
        return [];
    }
}

export async function createTask(partial: Omit<Task, 'id' | 'createdAt'>): Promise<Task> {
    const backendTask = mapFrontendToBackend(partial as Partial<Task>);
    const response = await request<ApiResponse<BackendTask>>('/api/v1/task/create', {
        method: 'POST',
        body: JSON.stringify(backendTask),
    });
    return mapBackendToFrontend(response.data);
}

export async function updateTask(task: Task): Promise<Task> {
    try {
        const backendTask = mapFrontendToBackend(task);
        const response = await request<ApiResponse<BackendTask>>(`/api/v1/task/update/${task.id}`, {
            method: 'PUT',
            body: JSON.stringify(backendTask),
        });
        return mapBackendToFrontend(response.data);
    } catch (error) {
        console.error('Failed to update task on API:', error);
        return task;
    }
}

export async function updateTaskStatus(id: string, status: TaskStatus): Promise<Task | null> {
    try {
        const statusMap: Record<TaskStatus, string> = {
            'todo': 'PENDING',
            'in_progress': 'IN_PROGRESS',
            'completed': 'COMPLETED',
        };
        const response = await request<ApiResponse<BackendTask>>(`/api/v1/task/update/${id}/status?status=${statusMap[status]}`, {
            method: 'PATCH',
        });
        return mapBackendToFrontend(response.data);
    } catch (error) {
        console.error('Failed to update task status on API:', error);
        return null;
    }
}

export async function updateTaskCompleted(id: string, completed: boolean): Promise<Task | null> {
    const response = await request<ApiResponse<BackendTask>>( `/api/v1/task/update/${id}/complete?completed=${completed}`,
        { method: 'PATCH' }
    );
    return mapBackendToFrontend(response.data); }

export async function deleteTask(id: string): Promise<boolean> {
    try {
        await request<ApiResponse<void>>(`/api/v1/task/delete/${id}`, {
            method: 'DELETE',
        });
        return true;
    } catch (error) {
        console.error('Failed to delete task on API:', error);
        return false;
    }
}