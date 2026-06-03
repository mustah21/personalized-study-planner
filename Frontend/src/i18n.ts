import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import fi from "./translations/fi";
import ne from "./translations/ne";
import vi from "./translations/vi";

const en = {
  translation: {
    "layout.title": "Study Planner",
    "nav.dashboard": "Dashboard",
    "nav.calendar": "Calendar",
    "nav.profile": "Profile",
    "nav.logout": "Logout",

    "welcome": "Welcome to the Personalized Study Planner!",
    "landing.description":
      "Organize your studies, track your progress, and achieve your academic goals",
    "landing.features.taskManagement.title": "Task Management",
    "landing.features.taskManagement.description":
      "Create, organize, and track your study tasks efficiently",
    "landing.features.calendar.title": "Calendar View",
    "landing.features.calendar.description":
      "Visualize deadlines and plan your study schedule",
    "landing.features.collaboration.title": "Collaboration",
    "landing.features.collaboration.description":
      "Share tasks and collaborate with study partners",
    "landing.cta.getStarted": "Get Started",
    "landing.cta.alreadyHaveAccount": "Already have an account?",
    "landing.cta.signIn": "Sign in",

    "login.title": "Welcome Back!",
    "login.description":
      "Please sign in to continue organizing your studies",
    "login.googleButton": "Continue with Google",
    "login.altButton": "OR CONTINUE WITH EMAIL",
    "login.email": "Email",
    "login.password": "Password",
    "login.button": "Sign In",
    "login.noAccount": "Don't have an account?",
    "login.signUp": "Sign Up",
    "login.demoNote": "Demo app – any email will work for testing",

    "signup.title": "Create Your Account",
    "signup.description":
      "Start organizing your studies and tracking your progress",
    "signup.googleButton": "Sign up with Google",
    "signup.altButton": "OR SIGN UP WITH EMAIL",
    "signup.name": "Full Name",
    "signup.email": "Email",
    "signup.password": "Password",
    "signup.button": "Create Account",
    "signup.alreadyHaveAccount": "Already have an account?",
    "signup.signIn": "Sign in",
    "signup.demoNote":
      "Demo app – any name and email will work for testing",

    "dashboard.title": "Dashboard",
    "dashboard.subtitle": "Overview of your study tasks",
    "dashboard.newTask": "+ New Task",
    "dashboard.shareTasks": "Share Tasks",
    "dashboard.deleteConfirmTitle": "Delete task?",
    "dashboard.deleteConfirmMessage":
      "Are you sure you want to delete this task?",
    "dashboard.cancel": "Cancel",
    "dashboard.delete": "Delete",
    "dashboard.emptyState":
      "No tasks yet. Create your first task to get started!",
    "dashboard.searchPlaceholder": "Search tasks...",
    "dashboard.stats.total": "Total Tasks",
    "dashboard.stats.todo": "To Do",
    "dashboard.stats.inProgress": "In Progress",
    "dashboard.stats.completed": "Completed",
    "dashboard.filter.all": "All Status",
    "dashboard.filter.todo": "To Do",
    "dashboard.filter.inProgress": "In Progress",
    "dashboard.filter.completed": "Completed",
    "dashboard.sort.created": "Created Date",
    "dashboard.sort.deadline": "Deadline",
    "dashboard.sort.priority": "Priority",

    "noti.success": "Success!",
    "noti.updated": "Updated!",
    "noti.deleted": "Deleted",
    "noti.shared": "Shared!",
    "noti.taskCreated": "Task created successfully.",
    "noti.taskUpdated": "Task updated successfully.",
    "noti.taskDeleted": "Task has been deleted.",
    "noti.tasksSharedWith": "Tasks shared with {{email}}",

    "share.title": "Share Tasks",
    "share.searchRecipients": "Search Recipients",
    "share.searchPlaceholder":
      "Search by name or email to add recipients...",
    "share.selectAll": "Select All Shareable ({{selected}}/{{total}})",
    "share.localOnly": "local only",
    "share.noTasks": "No tasks available",
    "share.due": "Due",
    "share.cancel": "Cancel",
    "share.shareButton": "Share Tasks",
    "share.sharing": "Sharing...",
    "share.error.selectTask": "Please select at least one task",
    "share.error.selectUser": "Please select at least one recipient",
    "share.error.invalidTasks":
      "Selected tasks cannot be shared. Please create tasks using the backend API first.",
    "share.success": "Tasks sent to {{count}} recipient(s) successfully.",
    "share.localTask": "(Local only - cannot share)",

    "notfound.title": "Page Not Found",
    "notfound.description":
      "The page you are looking for does not exist.",
    "notfound.backToHome": "Back to Home",

    "profile.title": "Profile",
    "profile.subtitle": "Manage your account details",
    "profile.name": "Name",
    "profile.email": "Email",
    "profile.firstName": "First Name",
    "profile.lastName": "Last Name",
    "profile.bio": "Bio",
    "profile.bioPlaceholder": "Tell us about yourself...",
    "profile.pictureUrl": "Profile Picture URL",
    "profile.emailNote": "Email cannot be changed",
    "profile.saving": "Saving...",
    "profile.save": "Save",
    "profile.updateButton": "Update Profile",
    "profile.stats": "Account Statistics",
    "profile.tasksCreated": "Tasks Created",
    "profile.tasksCompleted": "Tasks Completed",
    "profile.tasksShared": "Tasks Shared",
    "profile.preferences": "Preferences",
    "profile.emailNotifications": "Email Notifications",
    "profile.emailNotificationsDesc":
      "Receive email reminders for tasks",
    "profile.calendarIntegration": "Calendar Integration",
    "profile.calendarIntegrationDesc": "Sync with Google Calendar",
    "profile.configured": "Configured",
    "profile.connected": "Connected",

    "calendar.title": "Calendar",
    "calendar.subtitle": "View your schedule and deadlines",
    "calendar.previous": "Previous",
    "calendar.next": "Next",
    "calendar.scheduledItems": "{{count}} item(s) scheduled",
    "calendar.noEvents": "No tasks or events scheduled for this day.",
    "addevent.title": "Add New Event",
    "addevent.name": "Event Name",
    "addevent.date": "Event Date",
    "addevent.type": "Event Type",
    "addevent.saveButton": "Save Event",
    "addevent.cancelButton": "Cancel",
    "addevent.placeholderName": "e.g., Physics Lecture",
    "eventtype.class": "Class",
    "eventtype.study": "Study Session",
    "eventtype.exam": "Exam",
    "eventtype.assignment": "Assignment",
    "eventtype.other": "Other",
    "eventtype.amounts": "No tasks or events scheduled for this day.",

    "llm.title": "AI Task Generator",
    "llm.promptPlaceholder": "What do you want to learn?",
    "llm.generateButton": "Generate Tasks",
    "llm.generating": "Generating...",
    "llm.deadline": "Deadline",
    "llm.priority": "Priority",
    "llm.accept": "Accept",
    "llm.reject": "Reject",
    "llm.close": "Close",

    "newtask.title": "Create New Task",
    "newtask.labelTitle": "Title *",
    "newtask.labelDescription": "Description",
    "newtask.placeholderTitle": "Complete Math Assignment",
    "newtask.placeholderDescription": "Add details about your task…",
    "newtask.priority": "Priority",
    "newtask.status": "Status",
    "newtask.deadline": "Deadline",
    "newtask.cancel": "Cancel",
    "newtask.create": "Create Task",
    "newtask.creating": "Creating...",
    "newtask.error": "Failed to create task",

    "edittask.title": "Edit Task",
    "edittask.save": "Save Changes",
    "edittask.saving": "Saving...",

    "task.edit": "Edit",
    "task.setReminder": "Set Reminder",
    "task.delete": "Delete",
    "task.createdOn": "Created on",
    "task.received": "Received",
    "task.priorityLabel": "Priority",
    "task.status.completed": "Completed",
    "task.status.in_progress": "In Progress",
    "task.status.todo": "To Do",

    "priority.low": "Low",
    "priority.medium": "Medium",
    "priority.high": "High",

    task: {
      createTitle: "Create New Task",
      editTitle: "Edit Task",
      title: "Title",
      description: "Description",
      priority: "Priority",
      status: "Status",
      deadline: "Deadline",
      placeholderTitle: "Complete Math Assignment",
      placeholderDescription: "Add details about your task...",
      low: "Low",
      medium: "Medium",
      high: "High",
      todo: "To Do",
      inProgress: "In Progress",
      completed: "Completed",
      cancel: "Cancel",
      create: "Create Task",
      creating: "Creating...",
      save: "Save Changes",
      saving: "Saving...",
      errorCreate: "Failed to create task",
    },
  },
};

const savedLang = localStorage.getItem("i18n_lang") || "en";

i18n.use(initReactI18next).init({
  lng: savedLang,
  fallbackLng: "en",
  interpolation: { escapeValue: false },
  resources: { en, fi, ne, vi },
});

i18n.on("languageChanged", (lng) => {
  localStorage.setItem("i18n_lang", lng);
});

export default i18n;