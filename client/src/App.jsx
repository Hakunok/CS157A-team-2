import { BrowserRouter } from "react-router-dom"
import AppRoutes from "@/routes/routes"
import Navbar from "@/components/layout/Navbar"

function App() {
  return (
      <BrowserRouter>
        <Navbar />
        <AppRoutes />
      </BrowserRouter>
  )
}

export default App
