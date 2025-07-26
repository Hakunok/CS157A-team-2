import axios from "@/lib/axios";

export const userApi = {
  getMe: () => axios.get("/users/me").then(res => res.data),
  updateMe: (data) => axios.patch("/users/me", data).then(res => res.data),
  adminUpdateUser: (userId, data) => axios.patch(`/users/${userId}`, data).then(res => res.data),
  validateUsername: (value) => axios.post("/users/validation/username", { value }).then(res => res.data),
  validateEmail: (value) => axios.post("/users/validation/email", { value }).then(res => res.data),
  validatePassword: (value) => axios.post("/users/validation/password", { value }).then(res => res.data),
  validateFirstName: (value) => axios.post("/users/validation/firstname", { value }).then(res => res.data),
  validateLastName: (value) => axios.post("/users/validation/lastname", { value }).then(res => res.data),
}