// components/LanguageDropdown.tsx
import { useState } from "react";
import { useTranslation } from "react-i18next";

const languages = [
    { code: "en", label: "English", flag: "🇬🇧" },
    { code: "fi", label: "Suomi", flag: "🇫🇮" },
    { code: "vi", label: "Tiếng Việt", flag: "🇻🇳" },
    { code: "ne", label: "नेपाली", flag: "🇳🇵" },
];

export default function LanguageDropdown() {
    const { i18n } = useTranslation();
    const [open, setOpen] = useState(false);

    const current = languages.find((l) => l.code === i18n.language) ?? languages[0];

    return (
        <div className="relative inline-block text-left">
            <button
                onClick={() => setOpen((o) => !o)}
                className="flex items-center gap-2 rounded-lg border border-purple-300 bg-white px-3 py-2 text-sm font-medium text-purple-700 shadow-sm hover:bg-purple-50 focus:outline-none"
            >
                <span>{current.flag}</span>
                <span>{current.label}</span>
                <span className="ml-1 text-xs text-purple-400">▾</span>
            </button>

            {open && (
                <>
                    {/* invisible overlay to close on outside click */}
                    <div
                        className="fixed inset-0 z-40"
                        onClick={() => setOpen(false)}
                    />
                    <div className="absolute right-0 z-50 mt-1 w-44 rounded-xl border border-purple-100 bg-white shadow-lg">
                        {languages.map((lang) => (
                            <button
                                key={lang.code}
                                className={`flex w-full items-center gap-3 px-4 py-2 text-left text-sm transition-colors first:rounded-t-xl last:rounded-b-xl
                  ${i18n.language === lang.code
                                        ? "bg-purple-50 font-semibold text-purple-700"
                                        : "text-gray-700 hover:bg-purple-50 hover:text-purple-700"
                                    }`}
                                onClick={() => {
                                    i18n.changeLanguage(lang.code);
                                    setOpen(false);
                                }}
                            >
                                <span className="text-base">{lang.flag}</span>
                                <span>{lang.label}</span>
                                {i18n.language === lang.code && (
                                    <span className="ml-auto text-purple-500">✓</span>
                                )}
                            </button>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
}