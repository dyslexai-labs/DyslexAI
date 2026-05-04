import { DyslexAIPlugin } from './capacitorDyslexAI'
import { IMAGE_INFERENCE_TIMEOUT_MS } from '../../config'

function withTimeout(promise, timeoutMs, message) {
  let timeoutId
  const timeout = new Promise((_, reject) => {
    timeoutId = window.setTimeout(() => reject(new Error(message)), timeoutMs)
  })

  return Promise.race([promise, timeout])
    .finally(() => window.clearTimeout(timeoutId))
}

export function createMobileInferenceProvider() {
  return {
    async health() {
      return DyslexAIPlugin.health()
    },

    async getCapabilities() {
      return DyslexAIPlugin.getCapabilities()
    },

    async processText(text) {
      return DyslexAIPlugin.processText({ text })
    },

    async processImage(payload) {
      if (!payload?.imageBase64) {
        throw new Error('Imagem vazia ou não preparada.')
      }

      return withTimeout(
        DyslexAIPlugin.processImage({
          imageBase64: payload.imageBase64,
          mimeType: payload.mimeType || 'image/jpeg',
        }),
        IMAGE_INFERENCE_TIMEOUT_MS,
        `A inferência da imagem excedeu o tempo limite de ${Math.round(IMAGE_INFERENCE_TIMEOUT_MS / 1000)} segundos.`
      )
    },

    async generateReadingPhrase(options = {}) {
      return DyslexAIPlugin.generateReadingPhrase({
        ageGroup: options.ageGroup || '8-10',
        level: options.level || '1',
        type: options.type || 'simple_sentence',
      })
    },

    async processAudio(file, expectedText = '') {
      const audioBase64 = await fileToDataUrl(file)

      return DyslexAIPlugin.processAudio({
        audioBase64,
        mimeType: file?.type || 'audio/wav',
        expectedText,
      })
    },

    async speak({ text, rate = 1.0 } = {}) {
      return DyslexAIPlugin.speak({
        text,
        rate,
      })
    },

    async stopSpeaking() {
      return DyslexAIPlugin.stopSpeaking()
    },

    async startWavRecording() {
      return DyslexAIPlugin.startWavRecording()
    },

    async stopWavRecording() {
      return DyslexAIPlugin.stopWavRecording()
    },
  }
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}
