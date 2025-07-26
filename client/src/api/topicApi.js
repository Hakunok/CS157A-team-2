import axios from "@/lib/axios"

export const topicApi = {
  getAll: () => axios.get("/topics").then(res => res.data),
  search: (query) => axios.get(`/topics/search?q=${encodeURIComponent(query)}`).then(res => res.data),
  getById: (id) => axios.get(`/topics/${id}`).then(res => res.data),
  create: (data) => axios.post("/topics", data).then(res => res.data),
  update: (id, data) => axios.put(`/topics/${id}`, data).then(res => res.data),
}