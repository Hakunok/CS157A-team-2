import axios from "@/lib/axios";

export const publicationApi = {
  getAll: () => axios.get("/publications").then(res => res.data),
  getById: (id) => axios.get(`/publications/${id}`).then(res => res.data),
  create: (data) => axios.post("/publications", data).then(res => res.data),
  update: (id, data) => axios.patch(`/publications/${id}`, data).then(res => res.data),
  getMineByAuthor: () => axios.get("/publications/me").then(res => res.data),
  getMineBySubmitter: () => axios.get("/publications/submitter").then(res => res.data),
  like: (id) => axios.post(`/publications/${id}/like`).then(res => res.data),
}
