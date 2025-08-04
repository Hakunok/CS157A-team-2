import axios from "axios"
import { toast } from "sonner"

const baseURL = "/api" // Vite proxy: localhost:8080/server_war_exploded/api

const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
})

// Error interceptor
api.interceptors.response.use(
    (res) => res,
    (err) => {
      const error = err?.response?.data
      const message = error?.message || "Something went wrong"
      toast.error(message)
      return Promise.reject(err)
    }
)

// -----------------------------
// Auth API
// -----------------------------
export const authApi = {
  login: (data) => api.post("/auth/login", data).then((res) => res.data),
  register: (data) => api.post("/auth/register", data).then((res) => res.data),
  logout: () => api.post("/auth/logout"),
  refresh: () => api.get("/auth/me").then((res) => res.data),
  validate: (data) => api.post("/auth/validate", data).then((res) => res.data),
}

// -----------------------------
// User Profile + Stats API
// -----------------------------
export const userApi = {
  getStats: () => api.get("/users/me/stats").then((res) => res.data),
  getInteractions: (limit = 5) =>
      api.get(`/users/me/interactions?limit=${limit}`).then((res) => res.data),
  getPlatformStats: () =>
      api.get("/users/stats/platform").then((res) => res.data),
  recalculateAffinities: () => api.post("/users/me/affinities"),
}

// -----------------------------
// Author Request API
// -----------------------------
export const authorRequestApi = {
  submit: () => api.post("/author-requests"),
  getStatus: () =>
      api.get("/author-requests/status").then((res) => res.data),
  getPending: (page = 1, pageSize = 20) =>
      api.get(`/author-requests/pending?page=${page}&pageSize=${pageSize}`).then((res) => res.data),
  getPendingCount: () =>
      api.get("/author-requests/pending/count").then((res) => res.data),
  approve: (accountId) =>
      api.post(`/author-requests/${accountId}/approve`),
}

// -----------------------------
// Collections API
// -----------------------------
export const collectionApi = {
  create: (data) => api.post("/collections", data).then((res) => res.data),
  getMine: () => api.get("/collections/my").then((res) => res.data),
  updateVisibility: (id, isPublic) =>
      api.put(`/collections/${id}/visibility?public=${isPublic}`),
  delete: (id) => api.delete(`/collections/${id}`),
  saveToDefault: (pubId) => api.post(`/collections/default/save/${pubId}`),
  removeFromDefault: (pubId) => api.delete(`/collections/default/save/${pubId}`),
  isSaved: (pubId) => api.get(`/collections/default/has/${pubId}`).then((res) => res.data),
  listSaved: () => api.get("/collections/default/publications").then((res) => res.data),
}

// -----------------------------
// Topics API
// -----------------------------
export const topicApi = {
  getAll: () => api.get("/topics").then((res) => res.data),
  search: (query) => api.get(`/topics/search?q=${query}`).then((res) => res.data),
  create: (data) => api.post("/topics", data).then((res) => res.data),
  update: (id, data) => api.put(`/topics/${id}`, data),
  delete: (id) => api.delete(`/topics/${id}`),
}

// -----------------------------
// Publications API
// -----------------------------
export const publicationApi = {
  createDraft: (data) => api.post("/publications", data).then((res) => res.data),
  editDraft: (id, data) => api.put(`/publications/${id}`, data).then((res) => res.data),
  publish: (id, data) => api.post(`/publications/${id}/publish`, data).then((res) => res.data),
  getById: (id) => api.get(`/publications/${id}`).then((res) => res.data),
  search: (query) => api.get(`/publications/search?q=${query}`).then((res) => res.data),
  getMyPublications: () => api.get("/publications/my").then((res) => res.data),
  getRecommendations: ({ kind, topicId, page = 1, pageSize = 10 }) =>
      api.get("/publications/recommendations", {
        params: { kind, topicId, page, pageSize },
      }).then((res) => res.data),
  like: (id) => api.post(`/publications/${id}/like`),
  unlike: (id) => api.delete(`/publications/${id}/like`),
  hasLiked: (id) => api.get(`/publications/${id}/like`).then((res) => res.data),
  view: (id) => api.post(`/publications/${id}/view`),
  getByEmail: (email) => api.get(`/publications/person-by-email/${email}`).then((res) => res.data),
  createAuthor: (data) => api.post("/publications/create-author", data).then((res) => res.data),
}

export { api }
