const envBase = import.meta.env.VITE_API_BASE_URL
const envImageInferenceTimeout = Number(import.meta.env.VITE_IMAGE_INFERENCE_TIMEOUT_MS)
const envMockInference = String(import.meta.env.VITE_DYSLEXAI_MOCK || '').toLowerCase()

export const API_BASE_URL = (envBase || "http://localhost:5000").replace(/\/$/, "")
export const IMAGE_INFERENCE_TIMEOUT_MS = Number.isFinite(envImageInferenceTimeout) && envImageInferenceTimeout > 0
  ? envImageInferenceTimeout
  : 900000
export const MOCK_INFERENCE_ENABLED = ['1', 'true', 'yes', 'on'].includes(envMockInference)
