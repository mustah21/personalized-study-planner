
import { ReactNode } from "react";
import { IconType } from "react-icons"; // if you're using react-icons

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  icon?: IconType; // optional icon component
  children?: ReactNode;
}

export default function PageHeader({ title, subtitle, icon: Icon, children }: PageHeaderProps) {
  return (
    <div className="flex items-center justify-between mb-10">

      {/* LEFT SIDE */}
      <div className="flex items-center gap-4">

        {/* ICON */}
        {Icon && (
          <div className="
            w-12 h-12 
            rounded-2xl 
            bg-purple-100 
            text-purple-700 
            flex items-center justify-center 
            shadow-sm
          ">
            <Icon size={26} />
          </div>
        )}

        {/* TEXT */}
        <div>
          <h2 className="text-3xl font-bold text-purple-700">
            {title}
          </h2>

          {subtitle && (
            <p className="text-sm text-gray-500 mt-1">
              {subtitle}
            </p>
          )}
        </div>
      </div>

      {/* RIGHT SIDE (buttons, filters, etc.) */}
      {children}
    </div>
  );
}
