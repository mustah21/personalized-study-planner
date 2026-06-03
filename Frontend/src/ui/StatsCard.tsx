
interface StatsCardProps {
  label: string;
  value: number;
  color?: "purple" | "blue" | "yellow" | "green"; // <-- ADD THIS
}

const colorMap = {
  purple: "bg-purple-100 text-purple-700 border-purple-200",
  blue: "bg-blue-100 text-blue-700 border-blue-200",
  yellow: "bg-yellow-100 text-yellow-700 border-yellow-200",
  green: "bg-green-100 text-green-700 border-green-200",
};

export default function StatsCard({
  label,
  value,
  color = "purple",
}: StatsCardProps) {
  return (
    <div
      className={`
        rounded-3xl p-6 shadow-sm border
        ${colorMap[color]}
      `}
    >
      <p className="text-sm font-medium opacity-80">{label}</p>
      <h3 className="text-3xl font-bold mt-1">{value}</h3>
    </div>
  );
}
