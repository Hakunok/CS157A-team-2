import { Badge } from "@/components/ui/badge"

export function KindBadge({ kind }) {
  let className = ""
  switch (kind) {
    case "PAPER":
      className = "bg-blue-500/10 text-blue-600 border border-blue-500/20"
      break
    case "BLOG":
      className = "bg-green-500/10 text-green-600 border border-green-500/20"
      break
    case "ARTICLE":
      className = "bg-purple-500/10 text-purple-600 border border-purple-500/20"
      break
    default:
      className = "bg-gray-500/10 text-gray-600 border border-gray-500/20"
  }

  return <Badge className={className}>{kind}</Badge>
}