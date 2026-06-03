
interface AvatarProps {
  name: string;
  size?: "md" | "lg";
}

export default function Avatar({ name, size = "md" }: AvatarProps) {
  const sizes: Record<"md" | "lg", string> = {
    md: "w-10 h-10 text-lg",
    lg: "w-16 h-16 text-2xl",
  };

  return (
    <div
      className={`
        rounded-full 
        bg-purple-300 
        text-purple-900 
        flex items-center justify-center 
        font-semibold 
        ${sizes[size]}
        shadow-sm
      `}
    >
      {name?.charAt(0).toUpperCase()}
    </div>
  );
}

