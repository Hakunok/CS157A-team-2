import { BrowserRouter } from "react-router-dom"
import AppRoutes from "@/routes/AppRoutes"
import Navbar from "@/components/layout/Navbar"
import {Toaster} from "sonner";

function App() {
  return (
      <BrowserRouter>
        <Navbar />
        <Toaster richColors position="top-center" />
        <AppRoutes />
      </BrowserRouter>
  )
}

export default App
