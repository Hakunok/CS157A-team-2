import { BrowserRouter as Router, Routes, Route } from "react-router-dom"
import { useState } from "react"
import Navbar from "./components/Navbar"
import Home from "./pages/Home"
import SignIn from "./pages/SignIn"
import SignUpPage from "./pages/signup/SignUpPage.jsx"
import Dashboard from "./pages/Dashboard"

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)

  const handleLogin = () => setIsAuthenticated(true)
  const handleLogout = () => {
    setIsAuthenticated(false)
  }

  return (
      <Router>
        <div className="flex flex-col h-screen">
          <Navbar isAuthenticated={isAuthenticated} onLogout={handleLogout} />
          <main className="flex-grow overflow-y-auto">
            <Routes>
              <Route path="/" element={<Home isAuthenticated={isAuthenticated} />} />
              <Route path="/signin" element={<SignIn onLogin={handleLogin} />} />
              <Route path="/signup" element={<SignUpPage />} />
              <Route path="/dashboard" element={<Dashboard />} />
            </Routes>
          </main>
        </div>
      </Router>
  )
}

export default App