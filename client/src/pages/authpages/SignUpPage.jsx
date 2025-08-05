import { useState } from "react"
import { useNavigate, Link } from "react-router-dom"
import { Button } from "@/components/ui/button.jsx"
import { Input } from "@/components/ui/input.jsx"
import { Label } from "@/components/ui/label.jsx"
import { useAuth } from "@/context/AuthContext.jsx"
import { toast } from "sonner"

export default function SignUpPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    email: "",
    username: "",
    password: "",
    firstName: "",
    lastName: "",
  })
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await register(form)
      toast.success("Account created!")
      navigate("/")
    } catch {
      // error toast already shown via interceptor
    } finally {
      setLoading(false)
    }
  }

  return (
      <section className="bg-background min-h-screen pt-24 px-4 flex justify-center">
      <div className="flex flex-col items-center gap-6">
          {/* Logo */}
          <Link to="/" className="text-2xl font-ui font-semibold text-ui-text tracking-tight">
            aiRchive
          </Link>

          {/* Sign Up Card */}
          <form
              onSubmit={handleSubmit}
              className="w-full max-w-md bg-surface rounded-xl shadow-lg border border-border px-6 py-8 space-y-4"
          >
            <h1 className="text-xl font-semibold font-ui text-ui-text text-center">
              Create an Account
            </h1>

            <div className="flex gap-4">
              <div className="flex flex-col w-full gap-1">
                <Label htmlFor="firstName" className="font-ui text-sm text-ui-text">First Name</Label>
                <Input
                    id="firstName"
                    name="firstName"
                    placeholder="First Name"
                    value={form.firstName}
                    onChange={handleChange}
                    required
                />
              </div>
              <div className="flex flex-col w-full gap-1">
                <Label htmlFor="lastName" className="font-ui text-sm text-ui-text">Last Name</Label>
                <Input
                    id="lastName"
                    name="lastName"
                    placeholder="Last Name"
                    value={form.lastName}
                    onChange={handleChange}
                    required
                />
              </div>
            </div>

            <div className="flex flex-col gap-1">
              <Label htmlFor="username" className="font-ui text-sm text-ui-text">Username</Label>
              <Input
                  id="username"
                  name="username"
                  placeholder="Username"
                  value={form.username}
                  onChange={handleChange}
                  required
              />
            </div>

            <div className="flex flex-col gap-1">
              <Label htmlFor="email" className="font-ui text-sm text-ui-text">Email</Label>
              <Input
                  id="email"
                  name="email"
                  type="email"
                  placeholder="Email"
                  value={form.email}
                  onChange={handleChange}
                  required
              />
            </div>

            <div className="flex flex-col gap-1">
              <Label htmlFor="password" className="font-ui text-sm text-ui-text">Password</Label>
              <Input
                  id="password"
                  name="password"
                  type="password"
                  placeholder="Password"
                  value={form.password}
                  onChange={handleChange}
                  required
              />
            </div>

            <Button type="submit" className="w-full mt-2" disabled={loading}>
              {loading ? "Signing up..." : "Sign Up"}
            </Button>
          </form>

          {/* Already a user */}
          <div className="flex justify-center gap-1 text-sm text-muted-foreground font-ui">
            <p>Already a user?</p>
            <Link
                to="/signin"
                className="text-primary hover:underline font-medium"
            >
              Login
            </Link>
          </div>
        </div>
      </section>
  )
}