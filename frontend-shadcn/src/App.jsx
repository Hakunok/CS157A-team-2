import { BrowserRouter as Router, Routes, Route } from "react-router-dom"
import { useState } from "react"
import Navbar from "./components/Navbar"
import Home from "./pages/Home"
import SignIn from "./pages/SignIn"
import SignUpPage from "./pages/SignUpPage.jsx"
import Dashboard from "./pages/Dashboard"

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  const handleLogin = () => setIsAuthenticated(true)
  const handleLogout = () => {
    localStorage.removeItem("token")
    setIsAuthenticated(false)
  }

  return (
      <Router>
        <Navbar isAuthenticated={isAuthenticated} onLogout={handleLogout} />
        <div className="p-4">
          <Routes>
            <Route path="/" element={<Home isAuthenticated={isAuthenticated} />} />
            <Route path="/signin" element={<SignIn onLogin={handleLogin} />} />
            <Route path="/signup" element={<SignUpPage onLogin={handleLogin} />} />
            <Route path="/dashboard" element={<Dashboard />} />
          </Routes>
        </div>
      </Router>
  )
}

export default App
