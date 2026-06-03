import {useState} from "react";
import {useNavigate} from "react-router-dom";
import Input from "../ui/Input";
import Button from "../ui/Button";
import {BookOpenIcon} from "@heroicons/react/24/solid";
import {signup} from "../services/auth";
import {useTranslation} from "react-i18next";

const API_URL = import.meta.env.VITE_API_URL; // http://localhost:8081

export default function SignupPage() {
    const {t, i18n} = useTranslation();
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();
    const [error, setError] = useState("");

    const handleGoogleSignup = () => {
        window.location.href = `${API_URL}/oauth2/authorization/google`;
    };
    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === "Enter") handleSignup();
    };

    const handleSignup = async () => {
        try {
            if (!name || !email || !password) return;
            if (!/\S+@\S+\.\S+/.test(email)) {
                setError(t("Invalid email"));
                return;
            }
            setError("");
            await signup(name, email, password);
            navigate("/dashboard");
        } catch (err: any) {
            setError(err?.message || "Signup failed");
        }
    };

    return (
        <div
            className="min-h-screen flex items-center justify-center bg-gradient-to-br from-purple-100 via-pink-100 to-blue-100 px-4">
            <div className="bg-white rounded-3xl shadow-xl p-10 w-full max-w-md border border-purple-100">
                <BookOpenIcon className="w-12 h-12 text-purple-600 mx-auto mb-4"/>

                <h2 className="text-3xl font-bold mb-2 text-center text-purple-700">
                    {t("signup.title")}
                </h2>

                <p className="text-sm text-gray-600 mb-8 text-center">
                    {t("signup.description")}
                </p>

                <Button
                    variant="outline"
                    className="w-full mb-4 flex items-center justify-center gap-2 border-purple-300 text-purple-700 hover:bg-purple-50"
                    onClick={handleGoogleSignup}
                    type="button"
                >
                    <img src="/Google-Logo.svg" alt="Google" className="w-5 h-5"/>
                    {t("signup.googleButton")}
                </Button>

                <div className="text-center text-xs text-gray-500 mb-4">
                    {t("signup.altButton")}
                </div>

                <label className="block mb-1 text-sm text-gray-700">{t("signup.name")}</label>
                <Input
                    placeholder="Enter your name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="mb-4 bg-purple-50/40 border-purple-200 focus:border-purple-400"
                />

                <label className="block mb-1 text-sm text-gray-700">{t("signup.email")}</label>
                <Input
                    placeholder="Enter your email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="mb-6 bg-purple-50/40 border-purple-200 focus:border-purple-400"
                />

                <label className="block mb-1 text-sm text-gray-700">{t("signup.password")}</label>
                <Input
                    placeholder="Create your password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="mb-6 bg-purple-50/40 border-purple-200 focus:border-purple-400"
                />
                {error && (
                    <div className="p-3 m-1 rounded-lg bg-red-50 border border-red-200">
                        <p className="text-sm text-red-600">{error}</p>
                    </div>
                )}
                <Button
                    className="w-full mb-4 bg-purple-500 hover:bg-purple-600 text-white shadow-md"
                    onClick={handleSignup}
                >
                    {t("signup.button")}
                </Button>

                <p className="text-sm text-center mb-2">
                    {t("signup.alreadyHaveAccount")}{" "}
                    <button
                        type="button"
                        className="text-purple-600 font-medium"
                        onClick={() => navigate("/login")}
                    >
                        {t("signup.signIn")}
                    </button>
                </p>

                <p className="text-xs text-center text-gray-400">
                    {t("signup.demoNote")}
                </p>
            </div>
        </div>
    );
}