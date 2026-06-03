import { Link } from "react-router-dom";
import Button from "../ui/Button";
import { useTranslation } from "react-i18next";

export default function NotFoundPage() {
  const { t, i18n } = useTranslation();
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-100 via-pink-100 to-blue-100 px-4">
      <div className="text-center space-y-6">
        <div className="mx-auto w-24 h-24 rounded-full bg-purple-100 flex items-center justify-center">
          <span className="text-5xl font-bold text-purple-600">?</span>
        </div>
        <div>
          <h1 className="text-2xl font-bold text-purple-700">{t("notfound.title")}</h1>
          <p className="text-gray-600 mt-2">
            {t("notfound.description")}
          </p>
        </div>
        <Link to="/">
          <Button className="bg-purple-500 hover:bg-purple-600 text-white shadow-md">
            {t("notfound.backToHome")}
          </Button>
        </Link>
      </div>
    </div>
  );
}