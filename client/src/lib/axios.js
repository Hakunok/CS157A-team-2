import axios from "axios"
import {toast} from "sonner";

const baseURL =
    import.meta.env.MODE === "development" ? "http://localhost:8080/api" : "/api"

const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  }
})

api.interceptors.response.use(
    (res) => res,
    (err) => {
      const message =
          err.response?.data?.message || "An error occurred. Please try again."
      console.error(`[API Error]: ${message}`)

      toast.error(message)

      return Promise.reject(err)
    }
)

export default api