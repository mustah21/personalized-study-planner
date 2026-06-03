
interface EmptyStateProps {
  message: string;
}

export default function EmptyState({ message }: EmptyStateProps) {
  return (
    <div
      className="
        text-center 
        text-purple-600 
        py-16 
        bg-purple-50 
        rounded-3xl 
        border 
        border-purple-100 
        shadow-sm
      "
    >
      {message}
    </div>
  );
}
