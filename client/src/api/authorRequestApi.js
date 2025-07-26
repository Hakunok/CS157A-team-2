import axios from "@/lib/axios"

export const authorRequestApi = {
  submit: () => axios.post("/author-requests").then(res => res.data),
  getMine: () => axios.get("/author-requests/me").then(res => res.data),
  getAll: () => axios.get("/author-requests").then(res => res.data),
  approve: (id) => axios.patch(`/author-requests/${id}/approve`).then(res => res.data),
  reject: (id) => axios.patch(`/author-requests/${id}/reject`).then(res => res.data),
}