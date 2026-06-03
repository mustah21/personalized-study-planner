import { useState, useEffect, useRef } from "react";
import Button from "../ui/Button";
import Input from "../ui/Input";
import type { Task } from "../types";
import { searchUsers, shareTasks, type UserSearchResult } from "../services/notifications";
import { useTranslation } from "react-i18next";

interface ShareTaskModalProps {
  tasks: Task[];
  onClose: () => void;
}

export default function ShareTaskModal({
  tasks,
  onClose,
}: ShareTaskModalProps) {
  const { t } = useTranslation();

  const [selectedTaskIds, setSelectedTaskIds] = useState<Set<string>>(
    new Set()
  );
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<UserSearchResult[]>([]);
  const [selectedUsers, setSelectedUsers] = useState<UserSearchResult[]>([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [searching, setSearching] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const searchRef = useRef<HTMLDivElement>(null);
  const searchRequestId = useRef(0);

  // Debounced user search
  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      setShowDropdown(false);
      return;
    }

    const currentRequestId = ++searchRequestId.current;

    const handler = setTimeout(async () => {
      try {
        setSearching(true);
        const results = await searchUsers(searchQuery);

        if (currentRequestId !== searchRequestId.current) return;

        const filteredResults = results.filter(
          (user) => !selectedUsers.some((u) => u.userId === user.userId)
        );
        setSearchResults(filteredResults);
        setShowDropdown(filteredResults.length > 0);
      } catch (err) {
        console.error("Search failed:", err);
      } finally {
        if (currentRequestId === searchRequestId.current) {
          setSearching(false);
        }
      }
    }, 300);

    return () => clearTimeout(handler);
  }, [searchQuery, selectedUsers]);

  // Close dropdown
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const isTaskShareable = (taskId: string) => {
    const numId = parseInt(taskId, 10);
    return !isNaN(numId) && numId > 0;
  };

  const shareableTasks = tasks.filter((t) => isTaskShareable(t.id));

  const toggleTask = (taskId: string) => {
    if (!isTaskShareable(taskId)) return;
    const newSelected = new Set(selectedTaskIds);
    if (newSelected.has(taskId)) newSelected.delete(taskId);
    else newSelected.add(taskId);
    setSelectedTaskIds(newSelected);
  };

  const toggleAll = () => {
    if (selectedTaskIds.size === shareableTasks.length) {
      setSelectedTaskIds(new Set());
    } else {
      setSelectedTaskIds(new Set(shareableTasks.map((t) => t.id)));
    }
  };

  const handleSelectUser = (user: UserSearchResult) => {
    setSelectedUsers([...selectedUsers, user]);
    setSearchQuery("");
    setShowDropdown(false);
    setSearchResults([]);
  };

  const handleRemoveUser = (userId: number) => {
    setSelectedUsers(selectedUsers.filter((u) => u.userId !== userId));
  };

  const handleShare = async () => {
    if (selectedTaskIds.size === 0) {
      setError(t("share.error.selectTask"));
      return;
    }

    if (selectedUsers.length === 0) {
      setError(t("share.error.selectUser"));
      return;
    }

    const numericTaskIds = Array.from(selectedTaskIds)
      .map((id) => parseInt(id, 10))
      .filter((id) => !isNaN(id) && id > 0);

    if (numericTaskIds.length === 0) {
      setError(t("share.error.invalidTasks"));
      return;
    }

    setLoading(true);
    setError("");
    setSuccess("");

    try {
      await shareTasks({
        receiverUserIds: selectedUsers.map((u) => u.userId),
        taskIds: numericTaskIds,
      });

      setSuccess(t("share.success", { count: selectedUsers.length }));

      setSelectedTaskIds(new Set());
      setSelectedUsers([]);
      setSearchQuery("");

      setTimeout(() => onClose(), 1000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to share tasks");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50">
      <div className="bg-white rounded-3xl p-8 w-full max-w-2xl shadow-xl border border-purple-100 max-h-[80vh] overflow-y-auto">
        <h2 className="text-2xl font-bold text-purple-700 mb-6">
          {t("share.title")}
        </h2>

        {/* Search */}
        <div className="mb-6" ref={searchRef}>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            {t("share.searchRecipients")}
          </label>

          {selectedUsers.length > 0 && (
            <div className="flex flex-wrap gap-2 mb-3">
              {selectedUsers.map((user) => (
                <div
                  key={user.userId}
                  className="flex items-center gap-2 px-3 py-1.5 bg-purple-100 rounded-full border border-purple-200"
                >
                  <span className="text-sm text-purple-800 truncate max-w-32">
                    {user.fullName || user.email}
                  </span>
                  <button
                    type="button"
                    onClick={() => handleRemoveUser(user.userId)}
                    disabled={loading}
                  >
                    ✕
                  </button>
                </div>
              ))}
            </div>
          )}

          <div className="relative">
            <Input
              type="text"
              placeholder={t("share.searchPlaceholder")}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onFocus={() => searchResults.length > 0 && setShowDropdown(true)}
              disabled={loading}
            />

            {showDropdown && searchResults.length > 0 && (
              <div className="absolute z-10 w-full mt-1 bg-white rounded-xl shadow-lg border max-h-48 overflow-y-auto">
                {searchResults.map((user) => (
                  <button
                    key={user.userId}
                    type="button"
                    onClick={() => handleSelectUser(user)}
                    className="w-full p-3 text-left hover:bg-purple-50"
                  >
                    {user.fullName || user.email}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Select All */}
        <div className="mb-4 pb-4 border-b">
          <label className="flex items-center gap-3 cursor-pointer">
            <input
              type="checkbox"
              checked={
                selectedTaskIds.size === shareableTasks.length &&
                shareableTasks.length > 0
              }
              onChange={toggleAll}
              disabled={loading}
            />
            <span>
              {t("share.selectAll", {
                selected: selectedTaskIds.size,
                total: shareableTasks.length,
              })}
            </span>
          </label>
        </div>

        {/* Tasks */}
        {tasks.length === 0 ? (
          <p className="text-center text-gray-500 py-8">
            {t("share.noTasks")}
          </p>
        ) : (
          <div className="space-y-3 mb-6">
            {tasks.map((task) => {
              const isShareable = isTaskShareable(task.id);
              return (
                <label key={task.id} className="flex gap-3">
                  <input
                    type="checkbox"
                    checked={selectedTaskIds.has(task.id)}
                    onChange={() => toggleTask(task.id)}
                    disabled={!isShareable}
                  />
                  <div>
                    {task.title}
                    {!isShareable && (
                      <span className="ml-2 text-xs text-orange-500">
                        {t("share.localTask")}
                      </span>
                    )}
                    {task.deadline && (
                      <p className="text-xs text-gray-500">
                        {t("share.due")}:{" "}
                        {new Date(task.deadline).toLocaleDateString()}
                      </p>
                    )}
                  </div>
                </label>
              );
            })}
          </div>
        )}

        {/* Messages */}
        {error && <p className="text-red-500">{error}</p>}
        {success && <p className="text-green-500">{success}</p>}

        {/* Buttons */}
        <div className="flex justify-end gap-3">
          <Button onClick={onClose}>{t("share.cancel")}</Button>
          <Button onClick={handleShare} disabled={loading}>
            {loading ? t("share.sharing") : t("share.shareButton")}
          </Button>
        </div>
      </div>
    </div>
  );
}