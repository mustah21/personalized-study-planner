import {useState} from "react";
import {useNavigate} from "react-router-dom";
import Input from "../ui/Input";
import Button from "../ui/Button";
import {BookOpenIcon} from "@heroicons/react/24/solid";
import {loginWithEmail} from "../services/auth";
import {useTranslation} from "react-i18next";

const API_URL = import.meta.env.VITE_API_URL;

export default function LoginPage() {
    const {t, i18n} = useTranslation();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();
    const [error, setError] = useState("");


    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === "Enter") handleLogin();
    };

    const handleGoogleLogin = () => {
        window.location.href = `${API_URL}/oauth2/authorization/google`;
    };

    const handleLogin = async () => {
        try {
            if (!email || !password) return;

            await loginWithEmail(email, password);
            navigate("/dashboard");
        } catch (err: any) {
            setError(err?.message || "Login failed");
        }
    };

    return (
        <div
            className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-100 via-pink-100 to-blue-100 px-4">
            <div className="bg-white rounded-3xl shadow-xl p-10 w-full max-w-md border border-purple-100">
                <BookOpenIcon className="w-12 h-12 text-purple-600 mx-auto mb-4"/>

                <h2 className="text-3xl font-bold mb-2 text-center text-purple-700">
                    {t("login.title")}
                </h2>

                <p className="text-sm text-gray-600 mb-8 text-center">
                    {t("login.description")}
                </p>

                <Button
                    variant="outline"
                    className="w-full mb-4 flex items-center justify-center gap-2 border-purple-300 text-purple-700 hover:bg-purple-50"
                    onClick={handleGoogleLogin}
                    type="button"
                >
                    <img src="/Google-Logo.svg" alt="Google" className="w-5 h-5"/>
                    {t("login.googleButton")}
                </Button>

                <div className="text-center text-xs text-gray-500 mb-4">
                    {t("login.altButton")}
                </div>

                <label className="block mb-1 text-sm text-gray-700">{t("login.email")}</label>
                <Input
                    placeholder="Enter your email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="mb-6 bg-purple-50/40 border-purple-200 focus:border-purple-400"
                />

                <label className="block mb-1 text-sm text-gray-700">{t("login.password")}</label>
                <Input
                    type="password"
                    placeholder="Enter your password"
                    value={password}
                    onKeyDown={handleKeyDown}
                    onChange={(e) => setPassword(e.target.value)}
                    className="mb-6"
                />
                {error && (
                    <div className="p-3 m-1 rounded-lg bg-red-50 border border-red-200">
                        <p className="text-sm text-red-600">{error}</p>
                    </div>
                )}

                <Button
                    className="w-full mb-4 bg-purple-500 hover:bg-purple-600 text-white shadow-md"
                    onClick={handleLogin}
                >
                    {t("login.button")}
                </Button>

                <p className="text-sm text-center mb-2">
                    {t("login.noAccount")}{" "}
                    <button
                        type="button"
                        className="text-purple-600 font-medium"
                        onClick={() => navigate("/signup")}
                    >
                        {t("login.signUp")}
                    </button>
                </p>


                <p className="text-xs text-center text-gray-400">
                    {t("login.demoNote")}
                </p>
            </div>
        </div>
    );
}