// src/components/NotificationPopup.tsx
import React from "react";

export type NotificationType = "success" | "error" | "info" | "warning";

type Props = {
  open: boolean;
  type: NotificationType;
  title: string;
  message?: string;
  onClose: () => void;
  closeOnBackdrop?: boolean;
};

function Icon({ type }: { type: NotificationType }) {
  if (type === "success") {
    return (
      <div className="w-16 h-16 rounded-2xl bg-green-100 flex items-center justify-center shadow-sm">
        <svg width="34" height="34" viewBox="0 0 24 24" fill="none" className="text-green-600">
          <path
            d="M20 6L9 17l-5-5"
            stroke="currentColor"
            strokeWidth="2.6"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </div>
    );
  }

  if (type === "error") {
    return (
      <div className="w-16 h-16 rounded-2xl bg-red-100 flex items-center justify-center shadow-sm">
        <svg width="34" height="34" viewBox="0 0 24 24" fill="none" className="text-red-600">
          <path
            d="M18 6L6 18M6 6l12 12"
            stroke="currentColor"
            strokeWidth="2.6"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </div>
    );
  }

  if (type === "warning") {
    return (
      <div className="w-16 h-16 rounded-2xl bg-yellow-100 flex items-center justify-center shadow-sm">
        <svg width="34" height="34" viewBox="0 0 24 24" fill="none" className="text-yellow-700">
          <path d="M12 9v5" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" />
          <path d="M12 17h.01" stroke="currentColor" strokeWidth="3.2" strokeLinecap="round" />
          <path
            d="M10.3 4.5h3.4L22 19.1c.4.7-.1 1.6-.9 1.6H2.9c-.8 0-1.3-.9-.9-1.6L10.3 4.5Z"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinejoin="round"
          />
        </svg>
      </div>
    );
  }

  // info
  return (
    <div className="w-16 h-16 rounded-2xl bg-blue-100 flex items-center justify-center shadow-sm">
      <svg width="34" height="34" viewBox="0 0 24 24" fill="none" className="text-blue-600">
        <path d="M12 10v7" stroke="currentColor" strokeWidth="2.6" strokeLinecap="round" />
        <path d="M12 7h.01" stroke="currentColor" strokeWidth="3.2" strokeLinecap="round" />
        <path
          d="M12 22c5.5 0 10-4.5 10-10S17.5 2 12 2 2 6.5 2 12s4.5 10 10 10Z"
          stroke="currentColor"
          strokeWidth="2"
        />
      </svg>
    </div>
  );
}

export default function NotificationPopup({
  open,
  type,
  title,
  message,
  onClose,
  closeOnBackdrop = true,
}: Props) {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/40 px-4"
      onClick={(e) => {
        if (!closeOnBackdrop) return;
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="w-full max-w-md bg-white rounded-2xl shadow-2xl px-8 py-7 text-center">
        <div className="flex justify-center mb-4">
          <Icon type={type} />
        </div>

        <h3 className="text-xl font-semibold text-gray-900">{title}</h3>

        {message ? (
          <p className="mt-2 text-sm text-gray-600 leading-relaxed">{message}</p>
        ) : null}

        <div className="mt-6 flex items-center justify-center gap-3">
          <button
            onClick={onClose}
            className="px-7 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700"
          >
            OK
          </button>
        </div>
      </div>
    </div>
  );
}