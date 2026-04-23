import { DyslexAIPlugin } from './capacitorDyslexAI'

export function createMobileInferenceProvider() {
  return {
    async health() {
      return DyslexAIPlugin.health()
    },

    async processText(text) {
      return DyslexAIPlugin.processText({ text })
    },

    async processImage(file) {
      const payload = await buildImagePayload(file)
      return DyslexAIPlugin.processImage(payload)
    },

    async processAudio(_file) {
      throw new Error('processAudio local ainda não implementado.')
    },

    async getCapabilities() {
      return DyslexAIPlugin.getCapabilities()
    },
  }
}

async function buildImagePayload(file) {
  if (!file) {
    throw new Error('Nenhuma imagem selecionada.')
  }

  const arrayBuffer = await file.arrayBuffer()
  const base64 = arrayBufferToBase64(arrayBuffer)

  if (!base64) {
    throw new Error('Não foi possível converter a imagem selecionada.')
  }

  return {
    imageBase64: base64,
    mimeType: file.type || 'image/jpeg',
    filename: file.name || 'page.jpg',
  }
}

function arrayBufferToBase64(buffer) {
  const bytes = new Uint8Array(buffer)
  const chunkSize = 0x8000
  let binary = ''

  for (let i = 0; i < bytes.length; i += chunkSize) {
    const chunk = bytes.subarray(i, i + chunkSize)
    binary += String.fromCharCode(...chunk)
  }

  return btoa(binary)
}