import { useState } from "react"
import { useNavigate, Link } from "react-router-dom"
import { Button } from "@/components/ui/button.jsx"
import { Input } from "@/components/ui/input.jsx"
import { Label } from "@/components/ui/label.jsx"
import { useAuth } from "@/context/AuthContext.jsx"
import { toast } from "sonner"

export default function SignInPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    usernameOrEmail: "",
    password: "",
  })
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await login(form)
      toast.success("Welcome back!")
      navigate("/")
    } catch {
      // error toast handled globally
    } finally {
      setLoading(false)
    }
  }

  return (
      <section className="bg-background min-h-screen pt-24 px-4 flex justify-center">
        <div className="flex flex-col items-center gap-6">
          {/* Logo */}
          <Link
              to="/"
              className="text-2xl font-ui font-semibold text-ui-text tracking-tight"
          >
            aiRchive
          </Link>

          {/* Sign In Card */}
          <form
              onSubmit={handleSubmit}
              className="w-full max-w-md min-w-[24rem] bg-surface rounded-xl shadow-lg border border-border px-8 py-10 space-y-5"
          >
            <h1 className="text-xl font-semibold font-ui text-ui-text text-center">
              Sign In
            </h1>

            <div className="flex flex-col w-full gap-1">
              <Label
                  htmlFor="usernameOrEmail"
                  className="font-ui text-sm text-ui-text"
              >
                Username or Email
              </Label>
              <Input
                  id="usernameOrEmail"
                  name="usernameOrEmail"
                  placeholder="Enter your username or email"
                  value={form.usernameOrEmail}
                  onChange={handleChange}
                  required
              />
            </div>

            <div className="flex flex-col w-full gap-1">
              <Label
                  htmlFor="password"
                  className="font-ui text-sm text-ui-text"
              >
                Password
              </Label>
              <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="Enter your password"
                  value={form.password}
                  onChange={handleChange}
                  required
              />
            </div>

            <Button type="submit" className="w-full mt-2" disabled={loading}>
              {loading ? "Signing in..." : "Sign In"}
            </Button>
          </form>

          {/* Not a user yet */}
          <div className="flex justify-center gap-1 text-sm text-muted-foreground font-ui">
            <p>Don’t have an account?</p>
            <Link
                to="/signup"
                className="text-primary hover:underline font-medium"
            >
              Sign Up
            </Link>
          </div>
        </div>
      </section>
  )
}
