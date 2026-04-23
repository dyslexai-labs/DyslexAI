import { API_BASE_URL } from '../../config'

export function createWebInferenceProvider() {
  return {
    async health() {
      const response = await fetch(`${API_BASE_URL}/api/health?ts=${Date.now()}`, {
        method: 'GET',
        cache: 'no-store',
      })
      return response.json()
    },

    async processImage(file) {
      const formData = new FormData()
      formData.append('image', file)

      const response = await fetch(`${API_BASE_URL}/api/process`, {
        method: 'POST',
        body: formData,
      })

      const data = await response.json()
      if (!response.ok || !data.success) {
        throw new Error(data.error || 'Erro no processamento da imagem.')
      }

      return {
        ...data,
        meta: {
          ...(data.meta || {}),
          source: 'web',
        },
      }
    },

    async processAudio(file) {
      const formData = new FormData()
      formData.append('audio', file)

      const response = await fetch(`${API_BASE_URL}/api/process-audio`, {
        method: 'POST',
        body: formData,
      })

      const data = await response.json()
      if (!response.ok || !data.success) {
        throw new Error(data.error || 'Erro no processamento do áudio.')
      }

      return {
        ...data,
        meta: {
          ...(data.meta || {}),
          source: 'web',
        },
      }
    },

    async processText(_text) {
      throw new Error('processText ainda não disponível no backend web.')
    },

    async getCapabilities() {
      return {
        text: false,
        image: true,
        audio: true,
        syllables: false,
        source: 'web',
      }
    },
  }
}
