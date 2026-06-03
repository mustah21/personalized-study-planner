// src/pages/DashboardPage.tsx
import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PageHeader from "../ui/PageHeader";
import StatsCard from "../ui/StatsCard";
import Input from "../ui/Input";
import Select from "../ui/Select";
import Button from "../ui/Button";
import EmptyState from "../ui/EmptyState";
import type { Task, TaskStatus } from "../types";
import TaskCard from "../components/TaskCard";
import NewTaskModal from "../components/NewTaskModal";
import EditTaskModal from "../components/EditTaskModal";
import ShareTaskModal from "../components/ShareTaskModal";
import NotificationBell from "../components/NotificationBell";
import LLMTaskGeneratorModal from "./LLMTaskGeneratorModal";
import { getTasks, deleteTask } from "../services/tasks";
import NotificationPopup, { NotificationType } from "../components/NotificationPopup";

type StatusFilter = "all" | TaskStatus;
type SortBy = "created" | "deadline" | "priority";

export default function DashboardPage() {
    const { t } = useTranslation();

    const [tasks, setTasks] = useState<Task[]>([]);
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState<StatusFilter>("all");
    const [sortBy, setSortBy] = useState<SortBy>("created");

    const [showNewModal, setShowNewModal] = useState(false);
    const [showShareModal, setShowShareModal] = useState(false);
    const [editingTask, setEditingTask] = useState<Task | null>(null);
    const [deleteId, setDeleteId] = useState<string | null>(null);
    const [showLLMModal, setShowLLMModal] = useState(false);

    const [openMenuId, setOpenMenuId] = useState<string | null>(null);

    // ===== NOTI =====
    const [notiOpen, setNotiOpen] = useState(false);
    const [notiType, setNotiType] = useState<NotificationType>("info");
    const [notiTitle, setNotiTitle] = useState("");
    const [notiMessage, setNotiMessage] = useState<string | undefined>(undefined);

    const showNoti = (type: NotificationType, title: string, message?: string) => {
        setNotiType(type);
        setNotiTitle(title);
        setNotiMessage(message);
        setNotiOpen(true);
    };
    // ================================

    const handleCreateTask = (task: Task) => {
        setTasks((prev) => [...prev, task]);
        showNoti("success", t("noti.success"), t("noti.taskCreated"));
    };

    const handleUpdateTask = (updated: Task) => {
        setTasks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
        showNoti("success", t("noti.updated"), t("noti.taskUpdated"));
    };

    const handleDeleteTask = (id: string) => {
        setDeleteId(id);
    };

    const confirmDelete = async () => {
        if (!deleteId) return;
        try {
            await deleteTask(deleteId);
            setTasks((prev) => prev.filter((t) => t.id !== deleteId));
        } catch (error) {
            console.error("Failed to delete task:", error);
        }
        setDeleteId(null);
        showNoti("error", t("noti.deleted"), t("noti.taskDeleted"));
    };

    const handleShareTasks = async (selectedTaskIds: string[], recipientEmail: string) => {
        console.log(`Sharing tasks ${selectedTaskIds.join(", ")} to ${recipientEmail}`);
        showNoti("info", t("noti.shared"), t("noti.tasksSharedWith", { email: recipientEmail }));
        setShowShareModal(false);
    };

    const loadTasks = async () => {
        try {
            const tasksFromApi = await getTasks();
            setTasks(tasksFromApi);
            localStorage.removeItem("tasks");
        } catch (error) {
            console.error("Failed to load tasks:", error);
            setTasks([]);
        }
    };

    useEffect(() => {
        loadTasks();
    }, []);

    const stats = {
        total: tasks.length,
        todo: tasks.filter((t) => t.status === "todo").length,
        inProgress: tasks.filter((t) => t.status === "in_progress").length,
        completed: tasks.filter((t) => t.status === "completed").length,
    };

    const filtered = tasks.filter((t) => {
        const matchesStatus = statusFilter === "all" || t.status === statusFilter;
        const matchesSearch = t.title.toLowerCase().includes(search.toLowerCase());
        return matchesStatus && matchesSearch;
    });

    let sorted = [...filtered];

    if (sortBy === "created") {
        sorted.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    } else if (sortBy === "deadline") {
        sorted.sort(
            (a, b) =>
                new Date(a.deadline ?? 0).getTime() - new Date(b.deadline ?? 0).getTime()
        );
    } else if (sortBy === "priority") {
        const order = { high: 3, medium: 2, low: 1 };
        sorted.sort(
            (a, b) =>
                (order[b.priority ?? "medium"] ?? 2) - (order[a.priority ?? "medium"] ?? 2)
        );
    }

    return (
        <div className="animate-pageFade space-y-8">
            {/* NOTI POPUP */}
            <NotificationPopup
                open={notiOpen}
                type={notiType}
                title={notiTitle}
                message={notiMessage}
                onClose={() => setNotiOpen(false)}
            />

            {/* HEADER */}
            <PageHeader title={t("dashboard.title")} subtitle={t("dashboard.subtitle")}>
                <div className="flex items-center gap-3">
                    <NotificationBell
                        onTaskAccepted={() => {
                            loadTasks();
                        }}
                    />
                    <Button
                        className="bg-purple-500 hover:bg-purple-600 text-white shadow-md"
                        onClick={() => setShowShareModal(true)}
                    >
                        {t("dashboard.shareTasks")}
                    </Button>

                    <button
                        onClick={() => setShowLLMModal(true)}
                        className="
              fixed bottom-6 right-6 z-40
              bg-purple-500 hover:bg-purple-600
              text-white text-2xl
              w-14 h-14 rounded-full
              shadow-lg flex items-center justify-center
              dark:bg-purple-600 dark:hover:bg-purple-700
            "
                    >
                    </button>

                    <Button
                        className="bg-purple-500 hover:bg-purple-600 text-white shadow-md"
                        onClick={() => setShowNewModal(true)}
                    >
                        {t("dashboard.newTask")}
                    </Button>
                </div>
            </PageHeader>

            {/* NEW TASK MODAL */}
            {showNewModal && (
                <NewTaskModal
                    onClose={() => setShowNewModal(false)}
                    onCreate={(task) => {
                        handleCreateTask(task);
                        setShowNewModal(false);
                    }}
                />
            )}

            {/* SHARE TASKS MODAL */}
            {showShareModal && (
                <ShareTaskModal
                    tasks={tasks}
                    onClose={() => setShowShareModal(false)}
                />
            )}

            {/* EDIT TASK MODAL */}
            {editingTask && (
                <EditTaskModal
                    task={editingTask}
                    onClose={() => setEditingTask(null)}
                    onSave={(updated) => {
                        handleUpdateTask(updated);
                        setEditingTask(null);
                    }}
                />
            )}

            {showLLMModal && (
                <LLMTaskGeneratorModal
                    onClose={() => setShowLLMModal(false)}
                    onAddTask={(task) => {
                        handleCreateTask(task);
                        setShowLLMModal(false);
                    }}
                />
            )}

            {/* DELETE CONFIRMATION */}
            {deleteId && (
                <div className="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50">
                    <div className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-xl border border-purple-100">
                        <h3 className="text-lg font-semibold text-purple-700 mb-2">
                            {t("dashboard.deleteConfirmTitle")}
                        </h3>
                        <p className="text-sm text-gray-600 mb-4">
                            {t("dashboard.deleteConfirmMessage")}
                        </p>
                        <div className="flex justify-end gap-3">
                            <Button
                                variant="outline"
                                className="border-purple-300 text-purple-600 hover:bg-purple-100"
                                onClick={() => setDeleteId(null)}
                            >
                                {t("dashboard.cancel")}
                            </Button>
                            <Button className="bg-red-500 hover:bg-red-600 text-white" onClick={confirmDelete}>
                                {t("dashboard.delete")}
                            </Button>
                        </div>
                    </div>
                </div>
            )}

            {/* STATS */}
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                <StatsCard label={t("dashboard.stats.total")} value={stats.total} color="purple" />
                <StatsCard label={t("dashboard.stats.todo")} value={stats.todo} color="blue" />
                <StatsCard label={t("dashboard.stats.inProgress")} value={stats.inProgress} color="yellow" />
                <StatsCard label={t("dashboard.stats.completed")} value={stats.completed} color="green" />
            </div>

            {/* FILTERS */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 w-full mt-6">
                <Input
                    placeholder={t("dashboard.searchPlaceholder")}
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="bg-white border-purple-200 focus:border-purple-400"
                />

                <Select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
                    className="bg-white border-purple-200 focus:border-purple-400"
                >
                    <option value="all">{t("dashboard.filter.all")}</option>
                    <option value="todo">{t("dashboard.filter.todo")}</option>
                    <option value="in_progress">{t("dashboard.filter.inProgress")}</option>
                    <option value="completed">{t("dashboard.filter.completed")}</option>
                </Select>

                <Select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value as SortBy)}
                    className="bg-white border-purple-200 focus:border-purple-400"
                >
                    <option value="created">{t("dashboard.sort.created")}</option>
                    <option value="deadline">{t("dashboard.sort.deadline")}</option>
                    <option value="priority">{t("dashboard.sort.priority")}</option>
                </Select>
            </div>

            {/* TASK LIST */}
            {sorted.length === 0 ? (
                <EmptyState message={t("dashboard.emptyState")} />
            ) : (
                <div className="space-y-4 relative overflow-visible">
                    {sorted.map((t) => (
                        <TaskCard
                            key={t.id}
                            task={t}
                            onUpdate={handleUpdateTask}
                            onDelete={handleDeleteTask}
                            onEdit={(task) => setEditingTask(task)}
                            openMenuId={openMenuId}
                            setOpenMenuId={setOpenMenuId}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}