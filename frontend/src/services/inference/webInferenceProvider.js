import { API_BASE_URL, IMAGE_INFERENCE_TIMEOUT_MS } from '../../config'

function isAbortError(error) {
  return error?.name === 'AbortError'
}

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
      const controller = new AbortController()
      const timeoutId = window.setTimeout(() => controller.abort(), IMAGE_INFERENCE_TIMEOUT_MS)

      try {
        const response = await fetch(`${API_BASE_URL}/api/process`, {
          method: 'POST',
          body: formData,
          signal: controller.signal,
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
      } catch (error) {
        if (isAbortError(error)) {
          throw new Error(`A inferência da imagem excedeu o tempo limite de ${Math.round(IMAGE_INFERENCE_TIMEOUT_MS / 1000)} segundos.`)
        }
        throw error
      } finally {
        window.clearTimeout(timeoutId)
      }
    },

    async generateReadingPhrase(_options) {
      throw new Error('generateReadingPhrase ainda não disponível no backend web.')
    },

    async processAudio(file, expectedText) {
      const formData = new FormData()
      formData.append('audio', file)
      if (expectedText) formData.append('expected_text', expectedText)

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
