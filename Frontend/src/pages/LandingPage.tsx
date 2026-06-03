import { Link } from "react-router-dom";
import Button from "../ui/Button";
import { useTranslation } from "react-i18next";
import { useState } from "react";
import LanguageDropdown from "../components/LanguageDropdown";

export default function LandingPage() {
  const { t, i18n } = useTranslation();

  const [open, setOpen] = useState(false);
  const languages = [
    { code: "en", label: "English" },
    { code: "fi", label: "Suomi" },
    { code: "vn", label: "Vietnamese" },
    { code: "ne", label: "Nepalese" },
  ];



  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-100 via-pink-100 to-blue-100 flex items-center justify-center px-4">
      <div className="max-w-4xl mx-auto text-center space-y-8 animate-pageFade">
        <LanguageDropdown />

        {/* Hero Section */}
        <div className="space-y-4">
          <div className="inline-block">
            <div className="w-20 h-20 mx-auto bg-linear-to-br from-purple-500 to-blue-500 rounded-2xl flex items-center justify-center shadow-lg">
              <svg
                className="w-10 h-10 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
                />
              </svg>
            </div>
          </div>

          <h1 className="text-5xl md:text-6xl font-bold text-transparent bg-clip-text bg-linear-to-r from-purple-600 to-blue-600">
            {t("welcome")}
          </h1>

          <p className="text-xl md:text-2xl text-gray-600 max-w-2xl mx-auto">
            {t("landing.description")}
          </p>
        </div>

        {/* Features */}
        <div className="grid md:grid-cols-3 gap-6 pt-8">
          <div className="bg-white rounded-2xl p-6 shadow-md border border-purple-100">
            <div className="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center mb-4 mx-auto">
              <svg
                className="w-6 h-6 text-purple-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                />
              </svg>
            </div>
            <h3 className="font-semibold text-purple-700 mb-2">{t("landing.features.taskManagement.title")}</h3>
            <p className="text-sm text-gray-600">
              {t("landing.features.taskManagement.description")}
            </p>
          </div>

          <div className="bg-white rounded-2xl p-6 shadow-md border border-blue-100">
            <div className="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center mb-4 mx-auto">
              <svg
                className="w-6 h-6 text-blue-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            </div>
            <h3 className="font-semibold text-blue-700 mb-2">{t("landing.features.calendar.title")}</h3>
            <p className="text-sm text-gray-600">
              {t("landing.features.calendar.description")}
            </p>
          </div>

          <div className="bg-white rounded-2xl p-6 shadow-md border border-purple-100">
            <div className="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center mb-4 mx-auto">
              <svg
                className="w-6 h-6 text-purple-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                />
              </svg>
            </div>
            <h3 className="font-semibold text-purple-700 mb-2">{t("landing.features.collaboration.title")}</h3>
            <p className="text-sm text-gray-600">
              {t("landing.features.collaboration.description")}
            </p>
          </div>
        </div>

        {/* CTA */}
        <div className="pt-8">
          <Link to="/signup">
            <Button className="bg-linear-to-r from-purple-500 to-blue-500 hover:from-purple-600 hover:to-blue-600 text-white shadow-lg px-8 py-3 text-lg">
              {t("landing.cta.getStarted")}
            </Button>
          </Link>
          <p className="mt-4 text-gray-600">
            {t("landing.cta.alreadyHaveAccount")}
            <Link to="/login" className="text-purple-600 hover:text-purple-700 font-medium">
              {t("landing.cta.signIn")}
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}