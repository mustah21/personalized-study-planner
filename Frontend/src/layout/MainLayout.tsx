
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { logout, getCurrentUser } from "../services/auth";
import { useEffect, useState } from "react";
import LLMTaskGeneratorModal from "../pages/LLMTaskGeneratorModal";
import { useTranslation } from "react-i18next";

export default function MainLayout() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const user = getCurrentUser();
  const [open, setOpen] = useState(false);
  const [showLLMModal, setShowLLMModal] = useState(false);

//   {showLLMModal && (
//   <LLMTaskGeneratorModal onClose={() => setShowLLMModal(false)} />
// )}

  
  const [theme, setTheme] = useState(
    localStorage.getItem("theme") || "light"
  );

  useEffect(() => {
    if (theme === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
    localStorage.setItem("theme", theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(theme === "light" ? "dark" : "light");
  };


  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-blue-50">

      {/* MOBILE TOP BAR */}
      <header className="md:hidden fixed top-0 left-0 w-full bg-white border-b border-purple-100 shadow-sm p-4 flex justify-between items-center z-40">
        <h1 className="text-xl font-bold text-purple-700">{t("layout.title")}</h1>

        {/* HAMBURGER */}
        <button
          onClick={() => setOpen(true)}
          className="text-purple-700 text-3xl leading-none"
        >
          ☰
        </button>
      </header>

      {/* OVERLAY (click to close) */}
      {open && (
        <div
          onClick={() => setOpen(false)}
          className="fixed inset-0 bg-black/30 backdrop-blur-sm z-40 md:hidden"
        />
      )}

      {/* FLOATING SIDEBAR */}
      <aside
        className={`
          fixed top-0 left-0 h-full w-64 
          bg-white border-r border-purple-100 shadow-xl
          p-6 flex flex-col gap-8
          transform transition-transform duration-300
          ${open ? "translate-x-0" : "-translate-x-64"}
          md:translate-x-0
          z-50
          overflow-y-auto
        `}
      >
        {/* LOGO + CLOSE */}
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-purple-700">{t("layout.title")}</h1>

          <button
            onClick={() => setOpen(false)}
            className="text-purple-700 text-3xl leading-none md:hidden"
          >
            ✕
          </button>
        </div>

        {/* NAVIGATION */}
        <nav className="flex flex-col gap-3 text-sm font-medium">
          <NavLink
            to="/dashboard"
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              `px-4 py-2 rounded-xl transition ${isActive
                ? "bg-purple-100 text-purple-700 shadow-sm"
                : "text-gray-600 hover:bg-purple-50"
              }`
            }
          >
            {t("dashboard.title")}
          </NavLink>

          <NavLink
            to="/calendar"
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              `px-4 py-2 rounded-xl transition ${isActive
                ? "bg-purple-100 text-purple-700 shadow-sm"
                : "text-gray-600 hover:bg-purple-50"
              }`
            }
          >
            {t("calendar.title")}
          </NavLink>

          <NavLink
            to="/profile"
            onClick={() => setOpen(false)}
            className={({ isActive }) =>
              `px-4 py-2 rounded-xl transition ${isActive
                ? "bg-purple-100 text-purple-700 shadow-sm"
                : "text-gray-600 hover:bg-purple-50"
              }`
            }
          >
            {t("profile.title")}
          </NavLink>
        </nav>

        {/* USER + LOGOUT */}
        <div className="mt-auto flex items-center justify-between pb-4">
          <div
            className="
              w-10 h-10 rounded-full bg-purple-300 text-purple-900 
              flex items-center justify-center font-semibold shadow-sm
            "
          >
            {user?.username?.[0]?.toUpperCase()}
          </div>


          <button
            onClick={handleLogout}
            className="
              text-sm px-4 py-1.5 rounded-xl 
              border border-purple-300 text-purple-700 
              hover:bg-purple-100 transition
            "
          >
            {t("nav.logout")}
          </button>

        </div>
      </aside>

      {/* MAIN CONTENT */}
      <main className="pt-20 md:pt-0 md:ml-64 px-6 py-10">
        <Outlet />
      </main>
    </div>
  );
}
