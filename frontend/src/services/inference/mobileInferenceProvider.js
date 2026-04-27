import { DyslexAIPlugin } from './capacitorDyslexAI'

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

      return DyslexAIPlugin.processImage({
        imageBase64: payload.imageBase64,
        mimeType: payload.mimeType || 'image/jpeg',
      })
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
        mimeType: file?.type || 'audio/webm',
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