const mockDelayMs = 250

const phraseExamples = {
  simple_sentence: 'O gato comeu o rato.',
  rhyme: 'A bola rola na escola.',
  tongue_twister: 'Três tigres tristes trazem trigo.',
}

const silentWavDataUrl = 'data:audio/wav;base64,UklGRiQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA='

function delay(ms = mockDelayMs) {
  return new Promise(resolve => window.setTimeout(resolve, ms))
}

export function createMockInferenceProvider() {
  return {
    async health() {
      await delay(80)
      return {
        ok: true,
        ready: true,
        source: 'mock',
      }
    },

    async getCapabilities() {
      return {
        text: true,
        image: true,
        audio: true,
        syllables: false,
        source: 'mock',
      }
    },

    async processText(text = '') {
      await delay()
      const cleanText = String(text || '').trim() || 'O gato comeu o rato.'
      return {
        success: true,
        original_text: cleanText,
        simplified_text: cleanText,
        original_lines: [cleanText],
        simplified_lines: [cleanText],
        meta: { source: 'mock' },
      }
    },

    async processImage(_payload) {
      await delay()
      return {
        success: true,
        original_text: 'O gato comeu o rato.',
        simplified_text: 'O gato comeu o rato.',
        original_lines: ['O gato comeu o rato.'],
        simplified_lines: ['O gato comeu o rato.'],
        meta: { source: 'mock' },
      }
    },

    async generateReadingPhrase(options = {}) {
      await delay()
      return {
        success: true,
        text: phraseExamples[options.type] || phraseExamples.simple_sentence,
        meta: {
          source: 'mock',
          ageGroup: options.ageGroup || '8-10',
          level: options.level || '1',
          type: options.type || 'simple_sentence',
        },
      }
    },

    async processAudio(_file, expectedText = '') {
      await delay()
      const expected = String(expectedText || '').trim() || phraseExamples.simple_sentence
      const spoken = expected === phraseExamples.simple_sentence
        ? 'O gato comeu um pássaro.'
        : expected

      return {
        success: true,
        transcription: spoken,
        clean_text: spoken,
        spoken_text: spoken,
        spoken_lines: [spoken],
        issues: expected === spoken
          ? []
          : [{
            type: 'substituicao',
            expected: 'rato',
            heard: 'pássaro',
            message: 'A palavra final foi diferente da frase esperada.',
          }],
        meta: { source: 'mock' },
      }
    },

    async speak({ text } = {}) {
      console.info('[DyslexAI mock] speak:', text)
      await delay(120)
      return { success: true, source: 'mock' }
    },

    async stopSpeaking() {
      return { success: true, source: 'mock' }
    },

    async startWavRecording() {
      await delay(80)
      return { success: true, source: 'mock' }
    },

    async stopWavRecording() {
      await delay(120)
      return {
        success: true,
        audioBase64: silentWavDataUrl,
        mimeType: 'audio/wav',
        filename: 'gravacao-mock.wav',
        source: 'mock',
      }
    },
  }
}
