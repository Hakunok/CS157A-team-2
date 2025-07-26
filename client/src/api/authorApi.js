import axios from "@/lib/axios"

export const authorApi = {
  getMyProfile: () => axios.get("/authors/me").then(res => res.data),
  updateBio: (data) => axios.patch("/authors/me", data).then(res => res.data),
  getAll: () => axios.get("/authors").then(res => res.data),
  createExternalAuthor: (data) => axios.post("/authors", data).then(res => res.data),
  linkToExisting: (authorId) => axios.post(`/authors/link?authorId=${authorId}`).then(res => res.data),
}