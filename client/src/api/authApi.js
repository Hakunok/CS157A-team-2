import axios from "@/lib/axios"

export const authApi = {
  signIn: (data) => axios.post("/auth/signin", data).then(res => res.data),
  signUp: (data) => axios.post("/auth/signup", data).then(res => res.data),
  signOut: () => axios.post("/auth/signout"),
  refresh: () => axios.post("/auth/session/refresh").then(res => res.data),
}