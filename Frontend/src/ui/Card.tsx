
import { ReactNode } from "react";

interface CardProps {
  children: ReactNode;
  className?: string;
}

export default function Card({ children, className = "" }: CardProps) {
  return (
    <div
      className={`
        bg-white 
        rounded-3xl 
        p-6 
        shadow-sm 
        border 
        border-purple-100 
        ${className}
      `}
    >
      {children}
    </div>
  );
}
