import axios from "axios"
import { toast } from "sonner"
import qs from "qs"

// Vite proxy for dev: localhost:8080/server_war_exploded/api
const baseURL = "/api"

const api = axios.create({
  baseURL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
})

// Toast backend exceptions and errors
api.interceptors.response.use(
    (res) => res,
    (err) => {
      const error = err?.response?.data
      const message = error?.message || "Something went wrong"
      toast.error(message)
      return Promise.reject(err)
    }
)

// Auth REST API
// /api/auth/*
export const authApi = {
  login: (data) => api.post("/auth/login", data).then((res) => res.data),
  register: (data) => api.post("/auth/register", data).then((res) => res.data),
  logout: () => api.post("/auth/logout"),
  refresh: () => api.get("/auth/me").then((res) => res.data),
}


// Users & Stats REST API
// /api/users/*
export const userApi = {
  getStats: () => api.get("/users/me/stats").then((res) => res.data),
  getInteractions: (limit = 5) =>
      api.get(`/users/me/interactions?limit=${limit}`).then((res) => res.data),
  getPlatformStats: () =>
      api.get("/users/stats/platform").then((res) => res.data),
  recalculateAffinities: () => api.post("/users/me/affinities"),
}


// Author Request REST API
// /api/author-requests/*
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

// Collections REST API
// /api/collections/*
export const collectionApi = {
  create: (data) => api.post("/collections", data).then((res) => res.data),
  update: (id, data) => api.put(`/collections/${id}`, data).then((res) => res.data),
  getMine: () => api.get("/collections/my").then((res) => res.data),
  getById: (id) => api.get(`/collections/${id}`).then((res) => res.data),
  getRecommendations: ({ page = 1, pageSize = 10 } = {}) =>
      api.get(`/collections/recommendations?page=${page}&pageSize=${pageSize}`).then((res) => res.data),
  delete: (id) => api.delete(`/collections/${id}`),

  saveToDefault: (pubId) => api.post(`/collections/default/save/${pubId}`),
  removeFromDefault: (pubId) => api.delete(`/collections/default/save/${pubId}`),
  isSaved: (pubId) => api.get(`/collections/default/has/${pubId}`).then((res) => res.data),
  listSaved: () => api.get("/collections/default/publications").then((res) => res.data),

  addToCollection: (collectionId, pubId) =>
      api.post(`/collections/${collectionId}/add/${pubId}`),

  removeFromCollection: (collectionId, pubId) =>
      api.delete(`/collections/${collectionId}/remove/${pubId}`),
}

// Topics REST API
// /api/topics/*
export const topicApi = {
  getAll: () => api.get("/topics").then((res) => res.data),
  search: (query) => api.get(`/topics/search?q=${query}`).then((res) => res.data),
  create: (data) => api.post("/topics", data).then((res) => res.data),
  update: (id, data) => api.put(`/topics/${id}`, data),
  delete: (id) => api.delete(`/topics/${id}`),
}

// Publications REST API
// /api/publications/*
export const publicationApi = {
  createDraft: (data) => api.post("/publications", data).then((res) => res.data),
  editDraft: (id, data) => api.put(`/publications/${id}`, data).then((res) => res.data),
  publish: (id, data) => api.post(`/publications/${id}/publish`, data).then((res) => res.data),
  getById: (id) => api.get(`/publications/${id}`).then((res) => res.data),
  search: (query) => api.get(`/publications/search?q=${query}`).then((res) => res.data),
  getMyPublications: () => api.get("/publications/my").then((res) => res.data),
  getRecommendations: ({ kinds = [], topicIds = [], page = 1, pageSize = 10 } = {}) =>
      api.get("/publications/recommendations", {
        params: {
          ...(kinds.length > 0 && { kinds }),
          ...(topicIds.length > 0 && { topicId: topicIds }),
          page,
          pageSize
        },
        paramsSerializer: {
          serialize: (params) =>
              qs.stringify(params, { arrayFormat: "repeat" })
        }
      }).then(res => res.data),
  like: (id) => api.post(`/publications/${id}/like`),
  unlike: (id) => api.delete(`/publications/${id}/like`),
  hasLiked: (id) => api.get(`/publications/${id}/like`).then((res) => res.data),
  view: (id) => api.post(`/publications/${id}/view`),
  getByEmail: (email) => api.get(`/publications/person-by-email/${email}`).then((res) => res.data),
  createAuthor: (data) => api.post("/publications/create-author", data).then((res) => res.data),
}

export { api }