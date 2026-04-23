const envBase = import.meta.env.VITE_API_BASE_URL

export const API_BASE_URL = (envBase || "http://localhost:5000").replace(/\/$/, "")
