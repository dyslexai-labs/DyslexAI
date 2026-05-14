import { DyslexAIPlugin } from './capacitorDyslexAI'
import { IMAGE_INFERENCE_TIMEOUT_MS } from '../../config'
import { locale } from '../../i18n'

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

    async getModelState() {
      return DyslexAIPlugin.getModelState()
    },

    async ensureModelReady() {
      return DyslexAIPlugin.ensureModelReady()
    },

    async processText(text) {
      return DyslexAIPlugin.processText({ text, locale: locale.value })
    },

    async processImage(payload) {
      if (!payload?.imageBase64) {
        throw new Error('Imagem vazia ou não preparada.')
      }

      return withTimeout(
        DyslexAIPlugin.processImage({
          imageBase64: payload.imageBase64,
          mimeType: payload.mimeType || 'image/jpeg',
          locale: locale.value,
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
        locale: locale.value,
      })
    },

    async processAudio(file, expectedText = '') {
      const audioBase64 = await fileToDataUrl(file)

      return DyslexAIPlugin.processAudio({
        audioBase64,
        mimeType: file?.type || 'audio/wav',
        expectedText,
        locale: locale.value,
      })
    },

    async speak({ text, rate = 1.0 } = {}) {
      return DyslexAIPlugin.speak({
        text,
        rate,
        locale: locale.value,
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
