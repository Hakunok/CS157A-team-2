export default function Spinner({ size = "md" }) {
  const sizes = {
    sm: "w-4 h-4 border-2",
    md: "w-6 h-6 border-[3px]",
    lg: "w-8 h-8 border-4",
  }

  return (
      <div
          className={`${sizes[size]} border-[--color-secondary] border-t-[--color-primary] rounded-full animate-spin`}
      />
  )
}
