
import { InputHTMLAttributes } from "react";

export default function Input({
  className = "",
  ...props
}: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={`
        w-full 
        px-4 
        py-2.5 
        rounded-xl 
        border 
        border-purple-200 
        bg-purple-50/40 
        text-gray-800 
        focus:border-purple-400 
        focus:ring-2 
        focus:ring-purple-200 
        outline-none 
        transition-all 
        ${className}
      `}
      {...props}
    />
  );
}
