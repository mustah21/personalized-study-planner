import { useState } from "react";
import type { Task, TaskStatus } from "../types";
import Button from "../ui/Button";
import Input from "../ui/Input";
import Select from "../ui/Select";
import { updateTask } from "../services/tasks";
import { useTranslation } from "react-i18next";

interface EditTaskModalProps {
  task: Task;
  onClose: () => void;
  onSave: (updated: Task) => void;
}

export default function EditTaskModal({
  task,
  onClose,
  onSave,
}: EditTaskModalProps) {
  const { t } = useTranslation();

  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description ?? "");
  const [priority, setPriority] = useState<"low" | "medium" | "high">(
    task.priority ?? "medium"
  );
  const [status, setStatus] = useState<TaskStatus>(task.status);
  const [deadline, setDeadline] = useState(task.deadline ?? "");
  const [loading, setLoading] = useState(false);

  const handleSave = async () => {
    if (!title.trim()) return;

    setLoading(true);
    try {
      const updatedTask = await updateTask({
        ...task,
        title,
        description,
        priority,
        status,
        deadline: deadline || undefined,
      });

      onSave(updatedTask);
      onClose();
    } catch (error) {
      console.error("Failed to update task:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50">
      <div className="bg-white rounded-3xl p-7 w-full max-w-md shadow-xl border border-purple-100">

        <h2 className="text-2xl font-semibold mb-6 text-purple-700">
          {t("task.editTitle")}
        </h2>

        <div className="space-y-5">

          <div>
            <label className="text-sm font-medium text-gray-600">
              {t("task.title")} *
            </label>
            <Input value={title} onChange={(e) => setTitle(e.target.value)} />
          </div>

          <div>
            <label className="text-sm font-medium text-gray-600">
              {t("task.description")}
            </label>
            <Input
              value={description}
              onChange={(e) => setDescription(e.target.value)}
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

        <div className="flex justify-end gap-3 mt-8">
          <Button variant="outline" onClick={onClose}>
            {t("task.cancel")}
          </Button>

          <Button onClick={handleSave} disabled={loading}>
            {loading ? t("task.saving") : t("task.save")}
          </Button>
        </div>
      </div>
    </div>
  );
}