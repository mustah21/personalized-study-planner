
import { useEffect, useRef } from "react";
import type { Task } from "../types";

interface TaskCardProps {
  task: Task;
  onUpdate: (updated: Task) => void;
  onDelete: (id: string) => void;
  onEdit: (task: Task) => void;
  openMenuId: string | null;
  setOpenMenuId: (id: string | null) => void;
}

export default function TaskCard({
  task,
  onUpdate,
  onDelete,
  onEdit,
  openMenuId,
  setOpenMenuId,
}: TaskCardProps) {
  const menuRef = useRef<HTMLDivElement | null>(null);

  const priority = task.priority ?? "medium";
  const description = task.description ?? "";

  // Pastel priority colors
  const priorityColors = {
    high: "bg-red-100 text-red-700 border-red-300",
    medium: "bg-purple-100 text-purple-700 border-purple-300",
    low: "bg-blue-100 text-blue-700 border-blue-300",
  };

  // Soft pastel left border
  const borderColor = {
    high: "border-l-4 border-red-300",
    medium: "border-l-4 border-purple-300",
    low: "border-l-4 border-blue-300",
  };

  const isOpen = openMenuId === task.id;

  const toggleComplete = () => {
    onUpdate({
      ...task,
      status: task.status === "completed" ? "todo" : "completed",
    });
  };

  // Close menu when clicking outside
  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setOpenMenuId(null);
      }
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  return (
    <div
      className={`
        relative 
        bg-white 
        rounded-2xl 
        p-5 
        mb-4 
        shadow-sm 
        hover:shadow-md 
        transition-all 
        border 
        border-gray-100 
        ${borderColor[priority]}
      `}
    >
      {/* Three dots */}
      <button
        className="absolute top-3 right-3 text-gray-400 hover:text-gray-600"
        onClick={(e) => {
          e.stopPropagation();
          setOpenMenuId(isOpen ? null : task.id);
        }}
      >
        ⋮
      </button>

      {/* Dropdown */}
      {isOpen && (
        <div
          ref={menuRef}
          className="
            absolute 
            right-2 
            top-12 
            bg-white 
            shadow-lg 
            rounded-xl 
            w-44 
            z-[9999] 
            border 
            border-gray-200 
            py-2
            animate-fadeIn
          "
        >
          <button
            className="block w-full text-left px-4 py-2 hover:bg-purple-50"
            onClick={() => {
              setOpenMenuId(null);
              onEdit(task);
            }}
          >
            ✏️ Edit
          </button>

          <button className="block w-full text-left px-4 py-2 hover:bg-purple-50">
            ⏰ Set Reminder
          </button>

          <button
            className="block w-full text-left px-4 py-2 text-red-600 hover:bg-red-50"
            onClick={() => {
              setOpenMenuId(null);
              onDelete(task.id);
            }}
          >
            🗑️ Delete
          </button>
        </div>
      )}

      {/* CARD CONTENT */}
      <div className="flex items-start gap-4">
        {/* Checkbox */}
        <button
          onClick={toggleComplete}
          className={`
            mt-1 w-6 h-6 rounded-full border-2 flex items-center justify-center transition
            ${
              task.status === "completed"
                ? "bg-green-400 border-green-400"
                : "border-gray-300"
            }
          `}
        >
          {task.status === "completed" && (
            <span className="text-white text-sm">✓</span>
          )}
        </button>

        <div className="flex-1">
          <h3
            className={`
              text-lg font-semibold 
              ${task.status === "completed" ? "line-through text-gray-400" : "text-gray-800"}
            `}
          >
            {task.title}
          </h3>

          {description && (
            <p className="text-sm text-gray-600 mt-1">{description}</p>
          )}

            <p className="text-xs text-gray-400 mt-2">
                Deadline:{" "}
                {task.deadline
                    ? new Date(task.deadline).toLocaleString("en-GB", {
                        day: "2-digit",
                        month: "2-digit",
                        year: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                    })
                    : "No deadline"}
            </p>

          {task.sharedByEmail && (
            <p className="text-xs text-purple-500 mt-1 bg-purple-50 px-2 py-1 rounded-lg inline-block">
              Received: {task.sharedByEmail}
            </p>
          )}

          <div className="flex items-center gap-3 mt-3">
            <span
              className={`
                px-3 py-1 text-xs font-medium rounded-full border 
                ${priorityColors[priority]}
              `}
            >
              {priority.charAt(0).toUpperCase() + priority.slice(1)} Priority
            </span>

            <span className="text-xs text-gray-500">
              {task.status === "completed"
                ? "Completed"
                : task.status === "in_progress"
                ? "In Progress"
                : "To Do"}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
