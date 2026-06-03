// =========================
// Task Types
// =========================

export type TaskStatus = "todo" | "in_progress" | "completed";


 
export interface Task {
  id: string;
  title: string;
  description?: string;
  status: TaskStatus;
  deadline?: string;
  createdAt: string;
  priority?: "low" | "medium" | "high";
  sharedByEmail?: string;
  language: string;
}

// =========================
// User Types
// =========================

export interface User {
  id: string;
  username: string;
  email: string;
  name?: string;
  bio?: string;
  firstName?: string;
  lastName?: string;
  profilePicture?: string;
}


// =========================
// Calendar Event Types
// =========================

export type EventType = "Class" | "Exam" | "Assignment" | "Other";

export interface CalendarEvent {
  id: string;
  title: string;
  type: EventType;
  date: string; // ISO format: "2026-01-19"
}

// =========================
interface StatsCardProps {
  label: string;
  value: number;
  color?: "purple" | "blue" | "yellow" | "green";
}
