import { ButtonHTMLAttributes } from "react";

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "secondary" | "outline";
}

export default function Button({ variant = "primary", className = "", ...props }: Props) {
  const base =
    "px-4 py-2 rounded-md font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed";

  const variants = {
    primary: "bg-blue-600 text-white hover:bg-blue-700",
    secondary: "bg-gray-200 text-gray-800 hover:bg-gray-300 dark:bg-slate-700 dark:text-slate-50",
    outline:
      "border border-gray-300 dark:border-slate-600 text-gray-800 dark:text-slate-50 hover:bg-gray-100 dark:hover:bg-slate-800",
  };

  return <button className={`${base} ${variants[variant]} ${className}`} {...props} />;
}
