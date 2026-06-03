import { useState, useEffect, useRef } from "react";
import {
  getPendingInvites,
  acceptInvite,
  declineInvite,
  type TaskShareInvite,
} from "../services/notifications";
import Button from "../ui/Button";

interface NotificationBellProps {
  onTaskAccepted?: () => void;
}

export default function NotificationBell({
  onTaskAccepted,
}: NotificationBellProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [invites, setInvites] = useState<TaskShareInvite[]>([]);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const panelRef = useRef<HTMLDivElement>(null);

  // Fetch pending invites
  const fetchInvites = async () => {
    try {
      setLoading(true);
      const data = await getPendingInvites();
      setInvites(data);
    } catch (error) {
      console.error("Failed to fetch invites:", error);
    } finally {
      setLoading(false);
    }
  };

  // Fetch invites on mount and periodically
  useEffect(() => {
    fetchInvites();
    const interval = setInterval(fetchInvites, 3000); // Poll every 3 seconds
    return () => clearInterval(interval);
  }, []);

  // Close panel when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        panelRef.current &&
        !panelRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);

  const handleAccept = async (invite: TaskShareInvite) => {
    try {
      setActionLoading(invite.inviteId);
      await acceptInvite(invite.inviteToken);
      setInvites((prev) => prev.filter((i) => i.inviteId !== invite.inviteId));
      onTaskAccepted?.();
    } catch (error) {
      console.error("Failed to accept invite:", error);
    } finally {
      setActionLoading(null);
    }
  };

  const handleDecline = async (invite: TaskShareInvite) => {
    try {
      setActionLoading(invite.inviteId);
      await declineInvite(invite.inviteToken);
      setInvites((prev) => prev.filter((i) => i.inviteId !== invite.inviteId));
    } catch (error) {
      console.error("Failed to decline invite:", error);
    } finally {
      setActionLoading(null);
    }
  };

  const pendingCount = invites.length;

  return (
    <div className="relative" ref={panelRef}>
      {/* Bell Icon Button */}
      <button
        onClick={() => {
          const willOpen = !isOpen;
          setIsOpen(willOpen);
          if (willOpen) fetchInvites(); // Refresh when opening
        }}
        className="relative p-2 rounded-xl hover:bg-purple-100 transition-colors"
        aria-label="Notifications"
      >
        {/* Bell SVG Icon */}
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="w-6 h-6 text-purple-600"
        >
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>

        {/* Notification Badge */}
        {pendingCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 flex items-center justify-center min-w-[18px] h-[18px] px-1 text-xs font-bold text-white bg-red-500 rounded-full">
            {pendingCount > 99 ? "99+" : pendingCount}
          </span>
        )}
      </button>

      {/* Notification Panel Dropdown */}
      {isOpen && (
        <div className="absolute right-0 top-full mt-2 w-80 max-h-96 overflow-y-auto bg-white rounded-2xl shadow-xl border border-purple-100 z-50">
          {/* Header */}
          <div className="sticky top-0 bg-white px-4 py-3 border-b border-purple-100 rounded-t-2xl">
            <h3 className="text-lg font-semibold text-purple-700">
              Notifications
            </h3>
            {pendingCount > 0 && (
              <p className="text-sm text-gray-500">
                {pendingCount} pending invite{pendingCount !== 1 ? "s" : ""}
              </p>
            )}
          </div>

          {/* Content */}
          <div className="p-2">
            {loading && invites.length === 0 ? (
              <div className="flex items-center justify-center py-8">
                <div className="animate-spin rounded-full h-6 w-6 border-2 border-purple-500 border-t-transparent"></div>
              </div>
            ) : invites.length === 0 ? (
              <div className="py-8 text-center">
                <div className="text-gray-400 mb-2">
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.5"
                    className="w-12 h-12 mx-auto"
                  >
                    <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                    <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                  </svg>
                </div>
                <p className="text-sm text-gray-500">
                  No pending notifications
                </p>
              </div>
            ) : (
              <div className="space-y-2">
                {invites.map((invite) => (
                  <div
                    key={invite.inviteId}
                    className="p-3 rounded-xl bg-purple-50 border border-purple-100 hover:bg-purple-100/50 transition"
                  >
                    {/* Sender Info */}
                    <p className="text-xs text-gray-500 mb-2">
                      From: {invite.senderEmail}
                    </p>

                    {/* Task Info */}
                    <div className="mb-3">
                      <h4 className="font-medium text-gray-800 truncate">
                        {invite.taskName}
                      </h4>
                      {invite.taskDescription && (
                        <p className="text-sm text-gray-600 line-clamp-2 mt-1">
                          {invite.taskDescription}
                        </p>
                      )}
                      {invite.taskDeadline && (
                        <p className="text-xs text-gray-500 mt-1">
                          Due:{" "}
                          {new Date(invite.taskDeadline).toLocaleDateString()}
                        </p>
                      )}
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        className="flex-1 bg-purple-500 hover:bg-purple-600 text-white"
                        onClick={() => handleAccept(invite)}
                        disabled={actionLoading === invite.inviteId}
                      >
                        {actionLoading === invite.inviteId
                          ? "Processing..."
                          : "Accept"}
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="flex-1 border-red-300 text-red-600 hover:bg-red-50"
                        onClick={() => handleDecline(invite)}
                        disabled={actionLoading === invite.inviteId}
                      >
                        Deny
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
