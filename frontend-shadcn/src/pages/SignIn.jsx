import { Button } from "@/components/ui/button"

export default function SignIn({ onLogin }) {
  return (
      <div className="max-w-sm mx-auto mt-8">
        <h2 className="text-xl mb-4">Sign In</h2>
        {/* Replace with real form */}
        <Button onClick={onLogin}>Mock Login</Button>
      </div>
  )
}
