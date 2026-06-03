import { useState } from "react";
import type { Task, TaskStatus } from "../types";
import Button from "../ui/Button";
import Input from "../ui/Input";
import Select from "../ui/Select";
import { createTask } from "../services/tasks";
import { useTranslation } from "react-i18next";

interface NewTaskModalProps {
  onClose: () => void;
  onCreate: (task: Task) => void;
}

export default function NewTaskModal({ onClose, onCreate }: NewTaskModalProps) {
  const { t, i18n } = useTranslation();

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<"low" | "medium" | "high">("medium");
  const [status, setStatus] = useState<TaskStatus>("todo");
  const [deadline, setDeadline] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSubmit = async () => {
    if (!title.trim()) return;

    setLoading(true);
    setError("");
    try {
      const newTask = await createTask({
        title,
        description,
        priority,
        status,
        deadline: deadline || undefined,
        language: i18n.language,
      });

      onCreate(newTask);
      onClose();
    } catch (err) {
      console.error("Failed to create task:", err);
      setError(err instanceof Error ? err.message : t("task.errorCreate"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50">
      <div className="bg-white rounded-3xl p-7 w-full max-w-md shadow-xl border border-purple-100">

        <h2 className="text-2xl font-semibold mb-6 text-purple-700">
          {t("task.createTitle")}
        </h2>

        <div className="space-y-5">

          <div>
            <label className="text-sm font-medium text-gray-600">
              {t("task.title")} *
            </label>
            <Input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder={t("task.placeholderTitle")}
              className="mt-1"
            />
          </div>

          <div>
            <label className="text-sm font-medium text-gray-600">
              {t("task.description")}
            </label>
            <Input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder={t("task.placeholderDescription")}
              className="mt-1"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-gray-600">
                {t("task.priority")}
              </label>
              <Select
                value={priority}
                onChange={(e) => setPriority(e.target.value as any)}
              >
                <option value="low">{t("task.low")}</option>
                <option value="medium">{t("task.medium")}</option>
                <option value="high">{t("task.high")}</option>
              </Select>
            </div>

            <div>
              <label className="text-sm font-medium text-gray-600">
                {t("task.status")}
              </label>
              <Select
                value={status}
                onChange={(e) => setStatus(e.target.value as TaskStatus)}
              >
                <option value="todo">{t("task.todo")}</option>
                <option value="in_progress">{t("task.inProgress")}</option>
                <option value="completed">{t("task.completed")}</option>
              </Select>
            </div>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-600">
              {t("task.deadline")}
            </label>
            <Input
              type="date"
              value={deadline}
              onChange={(e) => setDeadline(e.target.value)}
            />
          </div>
        </div>

        {error && (
          <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <div className="flex justify-end gap-3 mt-8">
          <Button variant="outline" onClick={onClose}>
            {t("task.cancel")}
          </Button>

          <Button onClick={handleSubmit} disabled={loading}>
            {loading ? t("task.creating") : t("task.create")}
          </Button>
        </div>
      </div>
    </div>
  );
}