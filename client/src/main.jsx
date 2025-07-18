import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import {ThemeProvider} from "@/components/ui/theme-provider.jsx";

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <ThemeProvider defaultTheme="dark" storageKey="aiRchive-theme">
      <App />
    </ThemeProvider>
  </StrictMode>,
)
