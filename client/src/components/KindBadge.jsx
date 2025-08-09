import { Badge } from "@/components/badge.jsx";
import { cn } from "@/lib/utils.js";

export function KindBadge({ kind }) {
  let variantClass = "";
  const baseClass = "transition-colors cursor-default";

  switch (kind) {
    case "PAPER":
      variantClass =
          "bg-blue-500/10 text-blue-600 border border-blue-500/20 hover:bg-blue-500/20";
      break;
    case "BLOG":
      variantClass =
          "bg-green-500/10 text-green-600 border border-green-500/20 hover:bg-green-500/20";
      break;
    case "ARTICLE":
      variantClass =
          "bg-purple-500/10 text-purple-600 border border-purple-500/20 hover:bg-purple-500/20";
      break;
    case "COLLECTION":
      variantClass =
          "bg-teal-500/10 text-teal-600 border border-teal-500/20 hover:bg-teal-500/20";
      break;
    default:
      variantClass =
          "bg-gray-500/10 text-gray-600 border border-gray-500/20 hover:bg-gray-500/20";
  }

  return <Badge className={cn(baseClass, variantClass)}>{kind}</Badge>;
}

export function DefaultBadge() {
  return (
      <Badge className="bg-blue-500/10 text-blue-600 border border-blue-500/20 hover:bg-blue-500/20 text-xs font-medium cursor-default">
        Default
      </Badge>
  );
}