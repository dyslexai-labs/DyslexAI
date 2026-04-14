<template>
  <div class="player-app" :class="{ fullscreen: isFullscreen }">
    <main class="app-shell">
      <!-- Upload -->
      <section v-if="screen === 'upload'" class="screen-center">
        <div class="player-card">
          <div class="player-top intro-top">
            <div class="brand-inline">
              <div class="brand">DyslexAI</div>
              <div class="subtitle">Leitor guiado</div>
            </div>

            <div class="intro-copy">
              <h1>Leitura assistida</h1>
              <p>
                Carrega uma página fotografada e transforma o texto numa experiência
                de leitura guiada, clara e centrada na criança.
              </p>
            </div>
          </div>

          <div class="player-bottom intro-bottom">
            <button class="main-action" @click="triggerFileInput">Carregar imagem</button>
            <input ref="fileInput" type="file" accept="image/*" hidden @change="onFileChange" />
          </div>
        </div>
      </section>

      <!-- Confirm -->
      <section v-else-if="screen === 'confirm'" class="screen-center">
        <div class="player-card">
          <div class="player-top preview-top">
            <img :src="previewUrl" alt="Pré-visualização" class="preview-image" />
          </div>

          <div class="player-bottom confirm-bottom">
            <div class="confirm-text">
              <strong>Confirmar página</strong>
              <span>Se a imagem estiver bem, podes avançar para preparar a leitura.</span>
            </div>

            <div class="confirm-actions">
              <button class="main-action" @click="processImage">Processar</button>
              <button class="soft-action" @click="triggerFileInput">Outra imagem</button>
            </div>
          </div>
        </div>
      </section>

      <!-- Processing -->
      <section v-else-if="screen === 'processing'" class="screen-center">
        <div class="player-card">
          <div class="player-top processing-top">
            <div class="processing-content">
              <div class="loader-ring"></div>
              <h2>A preparar a leitura</h2>
              <p>{{ processingMessage }}</p>
            </div>
          </div>

          <div class="player-bottom processing-bottom">
            <div class="progress-track">
              <div class="progress-bar" :style="{ width: processingProgress + '%' }"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- Reader -->
      <section v-else class="screen-center">
        <div class="player-card reader-card">
          <div class="player-top reader-top">
            <div class="player-corner left">
              <span class="mini-brand">DyslexAI</span>
            </div>

            <div class="player-corner right">
              <button class="corner-btn" @click="toggleFullscreen" title="Ecrã inteiro">
                {{ isFullscreen ? '⤢' : '⛶' }}
              </button>
              <button class="corner-btn" @click="openValidation = true" title="Validação">
                ☰
              </button>
              <button class="corner-btn" @click="resetAll" title="Nova página">
                ↺
              </button>
            </div>

            <div
              v-if="readingMode === 'line'"
              class="reading-focus line-focus"
              :style="{
                fontSize: computedLineFontSize + 'px',
                color: paletteStyles.lineText
              }"
            >
              {{ currentLines[currentLineIndex] || 'Sem texto disponível.' }}
            </div>

            <div
              v-else
              class="reading-focus word-focus"
              :style="{ fontSize: computedWordContextFontSize + 'px' }"
            >
              <div class="word-context">
                <span
                  v-for="(word, index) in currentWords"
                  :key="`${currentLineIndex}-${index}-${word}`"
                  class="word-chip"
                  :class="{ active: index === currentWordIndex }"
                  :style="index === currentWordIndex
                    ? {
                        ...activeWordStyle,
                        background: paletteStyles.activeBg,
                        color: paletteStyles.activeText
                      }
                    : {
                        color: paletteStyles.inactiveWord
                      }"
                >
                  {{ word }}
                </span>
              </div>
            </div>
          </div>

          <div class="player-bottom reader-bottom">
            <div class="transport-row">
              <template v-if="readingMode === 'line'">
                <button class="round-btn" @click="prevLine" :disabled="currentLineIndex === 0">
                  ◀
                </button>

                <button class="play-btn" @click="toggleLineReading">
                  {{ isSpeakingLine ? '❚❚' : '▶' }}
                </button>

                <button
                  class="round-btn"
                  @click="nextLine"
                  :disabled="currentLineIndex === currentLines.length - 1"
                >
                  ▶
                </button>
              </template>

              <template v-else>
                <button class="round-btn" @click="prevWord">◀</button>

                <button class="play-btn" @click="toggleWordPlay">
                  {{ isPlayingWords ? '❚❚' : '▶' }}
                </button>

                <button class="round-btn" @click="nextWord">▶</button>
              </template>
            </div>

            <div class="progress-label">
              <span>Linha {{ currentLineIndex + 1 }}/{{ currentLines.length || 1 }}</span>
              <span v-if="readingMode === 'word'">
                · Palavra {{ currentWordIndex + 1 }}/{{ currentWords.length || 1 }}
              </span>
            </div>

            <div class="progress-line">
              <div class="progress-fill" :style="{ width: lineProgressPercent + '%' }"></div>
            </div>

            <div class="controls-compact">
              <button
                class="pill-btn"
                :class="{ active: currentTextMode === 'original' }"
                @click="switchTextMode('original')"
              >
                Original
              </button>

              <button
                class="pill-btn"
                :class="{ active: currentTextMode === 'simplified' }"
                @click="switchTextMode('simplified')"
              >
                Simplificado
              </button>

              <button
                class="pill-btn"
                :class="{ active: readingMode === 'line' }"
                @click="setReadingMode('line')"
              >
                Linha
              </button>

              <button
                class="pill-btn"
                :class="{ active: readingMode === 'word' }"
                @click="setReadingMode('word')"
              >
                Palavra
              </button>

              <button
                v-if="readingMode === 'line'"
                class="pill-btn"
                :class="{ active: linePlaybackMode === 'single' }"
                @click="linePlaybackMode = 'single'"
              >
                Linha única
              </button>

              <button
                v-if="readingMode === 'line'"
                class="pill-btn"
                :class="{ active: linePlaybackMode === 'continuous' }"
                @click="linePlaybackMode = 'continuous'"
              >
                {{ isSpeakingLine && linePlaybackMode === 'continuous' ? 'A ler até ao fim' : 'Até ao fim' }}
              </button>

              <button
                v-if="readingMode === 'word'"
                class="pill-btn"
                :class="{ active: wordAudioMode === 'silent' }"
                @click="setWordAudioMode('silent')"
              >
                Sem som
              </button>

              <button
                v-if="readingMode === 'word'"
                class="pill-btn"
                :class="{ active: wordAudioMode === 'audio' }"
                @click="setWordAudioMode('audio')"
              >
                Com som
              </button>

              <button class="pill-btn" @click="restartReading">Reiniciar</button>
              <button class="pill-btn" @click="stopAllAudio">Parar</button>
              <button class="pill-btn subtle" @click="showSettings = !showSettings">
                {{ showSettings ? 'Menos' : 'Definições' }}
              </button>
            </div>

            <div v-if="showSettings" class="settings-panel">
              <div class="setting-block">
                <label>Fonte {{ fontSize }}</label>
                <input v-model.number="fontSize" type="range" min="24" max="44" />
              </div>

              <div class="setting-block">
                <label>Velocidade {{ speechRate.toFixed(2) }}</label>
                <input v-model.number="speechRate" type="range" min="0.6" max="1.8" step="0.05" />
              </div>

              <div class="setting-block">
                <label>Cores de leitura</label>
                <select v-model="readingPalette">
                  <option value="yellow">Amarelo</option>
                  <option value="blue">Azul</option>
                  <option value="green">Verde</option>
                  <option value="pink">Rosa suave</option>
                </select>
              </div>

              <label v-if="readingMode === 'word'" class="check-block">
                <input v-model="autoAdvanceLine" type="checkbox" />
                Passar à linha seguinte
              </label>
            </div>
          </div>
        </div>
      </section>
    </main>

    <input ref="hiddenFileInput" type="file" accept="image/*" hidden @change="onFileChange" />

    <div v-if="openValidation" class="modal-overlay" @click.self="openValidation = false">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <div class="mini-label">Validação</div>
            <h3>Texto completo</h3>
          </div>

          <button class="pill-btn" @click="openValidation = false">Fechar</button>
        </div>

        <div class="modal-tabs">
          <button
            class="pill-btn"
            :class="{ active: currentTextMode === 'original' }"
            @click="switchTextMode('original')"
          >
            Original
          </button>

          <button
            class="pill-btn"
            :class="{ active: currentTextMode === 'simplified' }"
            @click="switchTextMode('simplified')"
          >
            Simplificado
          </button>
        </div>

        <textarea
          class="text-output"
          :value="currentTextMode === 'original' ? correctedText : simplifiedText"
          readonly
        ></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { API_BASE_URL } from "./config";

const screen = ref('upload')
const previewUrl = ref('')
const selectedFile = ref(null)

const openValidation = ref(false)
const showSettings = ref(true)
const isFullscreen = ref(false)
const isLandscape = ref(false)

const fileInput = ref(null)
const hiddenFileInput = ref(null)

const readingPalette = ref('yellow')
const fontSize = ref(30)
const readingMode = ref('line')
const currentTextMode = ref('original')
const wordAudioMode = ref('silent')
const linePlaybackMode = ref('single') // 'single' | 'continuous'

const currentLineIndex = ref(0)
const currentWordIndex = ref(0)

const isPlayingWords = ref(false)
const isSpeakingLine = ref(false)
const autoAdvanceLine = ref(true)
const speechRate = ref(1.0)

const processingProgress = ref(0)
const processingMessage = ref('A preparar a imagem...')

let playTimer = null
let wordPlaybackStopped = false

const correctedText = ref('')
const simplifiedText = ref('')

const originalLines = ref([])
const simplifiedLines = ref([])

const currentLines = computed(() =>
  currentTextMode.value === 'original' ? originalLines.value : simplifiedLines.value
)

const currentWords = computed(() => {
  const line = currentLines.value[currentLineIndex.value] || ''
  return line.split(/\s+/).filter(Boolean)
})

const paletteStyles = computed(() => {
  const palettes = {
    yellow: {
      lineText: '#FFFFFF',
      inactiveWord: 'rgba(255,255,255,0.84)',
      activeBg: 'rgba(255, 214, 53, 0.95)',
      activeText: '#231900'
    },
    blue: {
      lineText: '#F8FBFF',
      inactiveWord: 'rgba(248, 251, 255, 0.72)',
      activeBg: '#8FD3FF',
      activeText: '#0B1F2A'
    },
    green: {
      lineText: '#F7FFF8',
      inactiveWord: 'rgba(247, 255, 248, 0.72)',
      activeBg: '#9BE7B1',
      activeText: '#102217'
    },
    pink: {
      lineText: '#FFF8FC',
      inactiveWord: 'rgba(255, 248, 252, 0.72)',
      activeBg: '#FFB8D2',
      activeText: '#32111F'
    }
  }

  return palettes[readingPalette.value] || palettes.yellow
})

const lineProgressPercent = computed(() => {
  if (!currentLines.value.length) return 0
  return ((currentLineIndex.value + 1) / currentLines.value.length) * 100
})

const computedLineFontSize = computed(() => {
  if (isLandscape.value) return Math.max(22, Math.min(fontSize.value - 2, 34))
  return fontSize.value
})

const computedWordContextFontSize = computed(() => {
  if (isLandscape.value) return Math.max(18, Math.min(fontSize.value - 8, 28))
  return Math.max(20, fontSize.value - 6)
})

const activeWordStyle = computed(() => {
  if (isLandscape.value) {
    return {
      fontSize: '1.25em',
      transform: 'translateY(-1px)'
    }
  }

  return {
    fontSize: '1.4em',
    transform: 'translateY(-2px)'
  }
})

watch(currentLineIndex, () => {
  currentWordIndex.value = 0
})

watch(readingMode, () => {
  stopAllAudio()
  currentWordIndex.value = 0

  if (readingMode.value !== 'line') {
    linePlaybackMode.value = 'single'
  }
})

watch(wordAudioMode, () => {
  stopAllAudio()
})

function updateLayoutFlags() {
  isLandscape.value = window.innerWidth > window.innerHeight && window.innerWidth <= 950
}

function triggerFileInput() {
  const input = hiddenFileInput.value || fileInput.value
  if (input) input.click()
}

function onFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return

  selectedFile.value = file

  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }

  previewUrl.value = URL.createObjectURL(file)
  screen.value = 'confirm'
}

async function processRealImage(file) {
  const formData = new FormData()
  formData.append('image', file)

  const response = await fetch(`${API_BASE_URL}/api/process`, {
    method: 'POST',
    body: formData
  })

  const data = await response.json()

  if (!response.ok || !data.success) {
    throw new Error(data.error || 'Erro no processamento da imagem.')
  }

  correctedText.value = data.original_text || ''
  simplifiedText.value = data.simplified_text || ''

  originalLines.value = Array.isArray(data.original_lines) ? data.original_lines : []
  simplifiedLines.value = Array.isArray(data.simplified_lines) ? data.simplified_lines : []
}

async function processImage() {
  if (!selectedFile.value) {
    alert('Nenhuma imagem selecionada.')
    return
  }

  try {
    screen.value = 'processing'
    processingProgress.value = 15
    processingMessage.value = 'A enviar imagem...'

    await new Promise(resolve => setTimeout(resolve, 200))

    processingProgress.value = 45
    processingMessage.value = 'A processar texto...'

    await processRealImage(selectedFile.value)

    processingProgress.value = 85
    processingMessage.value = 'A preparar a leitura...'

    await new Promise(resolve => setTimeout(resolve, 200))

    processingProgress.value = 100
    processingMessage.value = 'Concluído.'

    currentTextMode.value = 'original'
    readingMode.value = 'line'
    linePlaybackMode.value = 'single'
    currentLineIndex.value = 0
    currentWordIndex.value = 0

    screen.value = 'reader'
  } catch (error) {
    console.error(error)
    alert(error.message || 'Ocorreu um erro ao processar a imagem.')
    screen.value = 'confirm'
  }
}

function switchTextMode(mode) {
  currentTextMode.value = mode
  currentLineIndex.value = 0
  currentWordIndex.value = 0
  stopAllAudio()
}

function setReadingMode(mode) {
  readingMode.value = mode
}

function setWordAudioMode(mode) {
  wordAudioMode.value = mode
}

function prevLine() {
  stopAllAudio()
  if (currentLineIndex.value > 0) currentLineIndex.value -= 1
}

function nextLine() {
  stopAllAudio()
  if (currentLineIndex.value < currentLines.value.length - 1) currentLineIndex.value += 1
}

function prevWord() {
  stopAllAudio()

  if (currentWordIndex.value > 0) {
    currentWordIndex.value -= 1
    return
  }

  if (currentLineIndex.value > 0) {
    currentLineIndex.value -= 1
    const previousLine = currentLines.value[currentLineIndex.value] || ''
    const previousWords = previousLine.split(/\s+/).filter(Boolean)
    currentWordIndex.value = Math.max(0, previousWords.length - 1)
  }
}

function nextWord() {
  stopAllAudio()

  if (currentWordIndex.value < currentWords.value.length - 1) {
    currentWordIndex.value += 1
    return
  }

  if (autoAdvanceLine.value && currentLineIndex.value < currentLines.value.length - 1) {
    currentLineIndex.value += 1
    currentWordIndex.value = 0
  }
}

function restartReading() {
  currentLineIndex.value = 0
  currentWordIndex.value = 0
  stopAllAudio()
}

function getPortugueseVoice() {
  const voices = window.speechSynthesis.getVoices()
  return (
    voices.find(v => v.lang === 'pt-PT') ||
    voices.find(v => v.lang?.toLowerCase().startsWith('pt')) ||
    voices[0] ||
    null
  )
}

function buildUtterance(text) {
  const utterance = new SpeechSynthesisUtterance(text)
  const voice = getPortugueseVoice()

  if (voice) {
    utterance.voice = voice
    utterance.lang = voice.lang
  } else {
    utterance.lang = 'pt-PT'
  }

  utterance.rate = Number(speechRate.value)
  utterance.pitch = 1
  utterance.volume = 1
  return utterance
}

function stopWordPlay() {
  wordPlaybackStopped = true

  if (playTimer) {
    clearTimeout(playTimer)
    playTimer = null
  }

  isPlayingWords.value = false

  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel()
  }
}

function stopAllAudio() {
  stopWordPlay()

  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel()
  }

  isSpeakingLine.value = false
}

function toggleLineReading() {
  if (isSpeakingLine.value) {
    stopAllAudio()
    return
  }

  isSpeakingLine.value = true
  speakCurrentLine()
}

function speakCurrentLine() {
  if (!('speechSynthesis' in window)) return

  const text = currentLines.value[currentLineIndex.value] || ''
  if (!text) {
    isSpeakingLine.value = false
    return
  }

  window.speechSynthesis.cancel()

  const utterance = buildUtterance(text)

  utterance.onend = () => {
    if (readingMode.value === 'line' && linePlaybackMode.value === 'continuous' && isSpeakingLine.value) {
      const isLastLine = currentLineIndex.value >= currentLines.value.length - 1

      if (!isLastLine) {
        currentLineIndex.value += 1

        setTimeout(() => {
          if (isSpeakingLine.value) {
            speakCurrentLine()
          }
        }, 120)

        return
      }
    }

    isSpeakingLine.value = false
  }

  utterance.onerror = () => {
    isSpeakingLine.value = false
  }

  window.speechSynthesis.speak(utterance)
}

function buildReadableWord(word) {
  const cleaned = (word || '').trim()
  if (!cleaned) return ''
  return cleaned
}

function speakCurrentWordAndWait() {
  return new Promise(resolve => {
    if (!('speechSynthesis' in window)) {
      resolve()
      return
    }

    const word = buildReadableWord(currentWords.value[currentWordIndex.value] || '')
    if (!word) {
      resolve()
      return
    }

    window.speechSynthesis.cancel()
    isSpeakingLine.value = true

    const utterance = buildUtterance(word)

    utterance.onend = () => {
      isSpeakingLine.value = false
      resolve()
    }

    utterance.onerror = () => {
      isSpeakingLine.value = false
      resolve()
    }

    window.speechSynthesis.speak(utterance)
  })
}

function toggleWordPlay() {
  if (isPlayingWords.value) {
    stopWordPlay()
  } else {
    startWordPlay()
  }
}

async function startWordPlay() {
  stopAllAudio()
  isPlayingWords.value = true
  wordPlaybackStopped = false

  while (isPlayingWords.value && !wordPlaybackStopped) {
    if (wordAudioMode.value === 'audio') {
      await speakCurrentWordAndWait()
    } else {
      await wait(Math.max(180, 420 / Number(speechRate.value)))
    }

    if (wordPlaybackStopped || !isPlayingWords.value) break

    const isLastWord = currentWordIndex.value >= currentWords.value.length - 1
    const isLastLine = currentLineIndex.value >= currentLines.value.length - 1

    if (!isLastWord) {
      currentWordIndex.value += 1
      continue
    }

    if (autoAdvanceLine.value && !isLastLine) {
      currentLineIndex.value += 1
      currentWordIndex.value = 0
      continue
    }

    break
  }

  isPlayingWords.value = false
  isSpeakingLine.value = false
}

function wait(ms) {
  return new Promise(resolve => {
    playTimer = setTimeout(resolve, ms)
  })
}

async function toggleFullscreen() {
  try {
    if (!document.fullscreenElement) {
      await document.documentElement.requestFullscreen()
      isFullscreen.value = true
    } else {
      await document.exitFullscreen()
      isFullscreen.value = false
    }
  } catch (error) {
    console.error('Erro ao alternar ecrã inteiro:', error)
  }
}

function onFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement
  setTimeout(updateLayoutFlags, 50)
}

function resetAll() {
  stopAllAudio()

  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }

  screen.value = 'upload'
  previewUrl.value = ''
  selectedFile.value = null
  correctedText.value = ''
  simplifiedText.value = ''
  originalLines.value = []
  simplifiedLines.value = []
  openValidation.value = false
  showSettings.value = true
  currentTextMode.value = 'original'
  readingPalette.value = 'yellow'
  readingMode.value = 'line'
  wordAudioMode.value = 'silent'
  linePlaybackMode.value = 'single'
  currentLineIndex.value = 0
  currentWordIndex.value = 0
  processingProgress.value = 0
  processingMessage.value = 'A preparar a imagem...'
}

onMounted(() => {
  updateLayoutFlags()
  window.addEventListener('resize', updateLayoutFlags)
  document.addEventListener('fullscreenchange', onFullscreenChange)

  if ('speechSynthesis' in window) {
    window.speechSynthesis.getVoices()
    window.speechSynthesis.onvoiceschanged = () => {
      window.speechSynthesis.getVoices()
    }
  }
})

onBeforeUnmount(() => {
  stopAllAudio()
  window.removeEventListener('resize', updateLayoutFlags)
  document.removeEventListener('fullscreenchange', onFullscreenChange)

  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
})
</script>

<style scoped>
:global(body) {
  margin: 0;
  font-family: Inter, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: #111827;
  background: #edf1f6;
}

* {
  box-sizing: border-box;
}

button,
input,
textarea,
select {
  font: inherit;
}

.player-app {
  min-height: 100vh;
  background: linear-gradient(180deg, #eef2f7 0%, #f5f7fb 100%);
}

.app-shell {
  max-width: 1060px;
  margin: 0 auto;
  padding: 18px;
}

.screen-center {
  min-height: calc(100vh - 36px);
  display: grid;
  place-items: center;
}

.player-card {
  width: min(900px, 100%);
  border-radius: 34px;
  overflow: hidden;
  box-shadow: 0 18px 48px rgba(17, 24, 39, 0.14);
  background: #fff;
}

.player-top {
  position: relative;
  min-height: 320px;
  padding: 24px;
  background:
    radial-gradient(circle at top left, rgba(171, 86, 230, 0.45), transparent 34%),
    linear-gradient(135deg, #572171 0%, #2d163f 48%, #19141f 100%);
  color: white;
}

.player-bottom {
  padding: 20px 22px 24px;
  background: linear-gradient(180deg, #ffd500 0%, #ffbe00 56%, #f4a400 100%);
}

.brand-inline {
  display: inline-flex;
  flex-direction: column;
  gap: 2px;
}

.brand {
  font-size: 1.15rem;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.subtitle {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.78);
}

.intro-top {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.intro-copy {
  max-width: 720px;
  margin-top: auto;
}

.intro-copy h1,
.processing-content h2 {
  margin: 0 0 10px 0;
  font-size: clamp(2rem, 4vw, 3rem);
  line-height: 1.05;
}

.intro-copy p,
.processing-content p {
  margin: 0;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.86);
  font-size: 1.02rem;
}

.intro-bottom,
.confirm-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.main-action,
.soft-action,
.round-btn,
.play-btn,
.pill-btn,
.corner-btn {
  border: none;
  cursor: pointer;
  transition: transform 0.15s ease, opacity 0.15s ease;
}

.main-action:hover,
.soft-action:hover,
.round-btn:hover,
.play-btn:hover,
.pill-btn:hover,
.corner-btn:hover {
  transform: translateY(-1px);
}

.main-action {
  min-height: 52px;
  padding: 0 22px;
  border-radius: 999px;
  background: #ffffff;
  color: #3b2b00;
  font-weight: 800;
  box-shadow: 0 10px 20px rgba(73, 49, 0, 0.15);
}

.soft-action {
  min-height: 48px;
  padding: 0 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
  color: #3b2b00;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.38);
}

.preview-top {
  display: grid;
  place-items: center;
}

.preview-image {
  width: 100%;
  max-height: 56vh;
  object-fit: contain;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
}

.confirm-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #3a2a00;
}

.confirm-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.processing-top {
  display: grid;
  place-items: center;
  text-align: center;
}

.processing-content {
  display: grid;
  gap: 14px;
  justify-items: center;
}

.loader-ring {
  width: 56px;
  height: 56px;
  border-radius: 999px;
  border: 5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  animation: spin 1s linear infinite;
}

.progress-track,
.progress-line {
  width: min(720px, 100%);
  margin: 0 auto;
  height: 10px;
  border-radius: 999px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.42);
}

.progress-bar,
.progress-fill {
  height: 100%;
  border-radius: 999px;
  background: #ffffff;
  transition: width 0.25s ease;
}

.reader-card {
  min-height: calc(100vh - 36px);
}

.reader-top {
  display: flex;
  align-items: center;
  justify-content: center;
}

.player-corner {
  position: absolute;
  top: 18px;
  z-index: 3;
}

.player-corner.left {
  left: 18px;
}

.player-corner.right {
  right: 18px;
  display: flex;
  gap: 8px;
}

.mini-brand {
  font-size: 0.86rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.8);
}

.corner-btn {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff8ee;
  backdrop-filter: blur(6px);
}

.reading-focus {
  width: min(860px, 100%);
  text-align: center;
  font-weight: 800;
  line-height: 1.55;
  z-index: 2;
}

.line-focus {
  text-wrap: balance;
}

.word-focus {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 24px;
}

.word-context {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px 12px;
  line-height: 1.85;
  max-width: 860px;
}

.word-chip {
  opacity: 0.9;
  padding: 4px 8px;
  border-radius: 10px;
  transition: all 0.18s ease;
}

.word-chip.active {
  opacity: 1;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
}

.reader-bottom {
  display: grid;
  gap: 14px;
}

.transport-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
}

.round-btn {
  width: 54px;
  height: 54px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #7a5a00;
  box-shadow: 0 8px 18px rgba(73, 49, 0, 0.14);
  font-weight: 800;
}

.play-btn {
  width: 78px;
  height: 78px;
  border-radius: 999px;
  background: #f0f2f7;
  color: #efb100;
  font-size: 1.8rem;
  box-shadow: 0 14px 28px rgba(73, 49, 0, 0.18);
}

.round-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
  transform: none;
}

.progress-label {
  text-align: center;
  color: rgba(62, 43, 0, 0.9);
  font-weight: 700;
  font-size: 0.95rem;
}

.controls-compact {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.pill-btn {
  border-radius: 999px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.24);
  color: #4a3500;
  font-weight: 700;
}

.pill-btn.active {
  background: #ffffff;
  color: #2f2300;
  box-shadow: 0 8px 18px rgba(73, 49, 0, 0.12);
}

.pill-btn.subtle {
  background: rgba(255, 255, 255, 0.14);
}

.settings-panel {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  align-items: end;
  padding-top: 4px;
}

.setting-block {
  display: grid;
  gap: 8px;
}

.setting-block label,
.check-block {
  color: rgba(62, 43, 0, 0.9);
  font-weight: 700;
  font-size: 0.95rem;
}

.setting-block select,
.setting-block input[type='range'] {
  width: 100%;
}

.setting-block select {
  min-height: 42px;
  border: none;
  border-radius: 14px;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.48);
  color: #3e2b00;
}

.check-block {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.36);
  display: grid;
  place-items: center;
  padding: 18px;
  z-index: 30;
}

.modal-card {
  width: min(920px, 100%);
  border-radius: 28px;
  background: #ffffff;
  box-shadow: 0 20px 48px rgba(17, 24, 39, 0.2);
  padding: 20px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 14px;
}

.mini-label {
  font-size: 0.8rem;
  color: #6b7280;
  margin-bottom: 4px;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.35rem;
}

.modal-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.text-output {
  width: 100%;
  min-height: 360px;
  border: 1px solid #e5e7eb;
  border-radius: 18px;
  padding: 14px;
  resize: vertical;
  background: #fafafa;
  color: #111827;
  line-height: 1.65;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 920px) {
  .settings-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .app-shell {
    padding: 14px;
  }

  .player-top {
    min-height: 260px;
    padding: 18px;
  }

  .player-bottom {
    padding: 18px;
  }

  .reader-card {
    min-height: calc(100vh - 28px);
  }

  .settings-panel {
    grid-template-columns: 1fr;
  }

  .confirm-bottom,
  .intro-bottom,
  .modal-header {
    flex-direction: column;
    align-items: stretch;
  }

  .main-action,
  .soft-action {
    width: 100%;
    justify-content: center;
  }

  .controls-compact {
    gap: 8px;
  }

  .pill-btn {
    padding: 9px 12px;
    font-size: 0.92rem;
  }
}

@media (max-width: 920px) and (orientation: landscape) {
  .app-shell {
    padding: 8px;
  }

  .screen-center {
    min-height: calc(100vh - 16px);
  }

  .player-card {
    width: min(100%, 980px);
    border-radius: 22px;
  }

  .reader-card {
    min-height: calc(100vh - 16px);
  }

  .player-top {
    min-height: 152px;
    padding: 14px 16px 10px;
  }

  .player-bottom {
    padding: 10px 14px 12px;
  }

  .player-corner {
    top: 10px;
  }

  .player-corner.left {
    left: 12px;
  }

  .player-corner.right {
    right: 12px;
    gap: 6px;
  }

  .corner-btn {
    width: 32px;
    height: 32px;
    border-radius: 10px;
    font-size: 0.95rem;
  }

  .mini-brand {
    font-size: 0.78rem;
  }

  .reading-focus {
    width: min(100%, 900px);
    line-height: 1.35;
  }

  .line-focus {
    padding-top: 8px;
  }

  .word-focus {
    padding-top: 10px;
  }

  .word-context {
    gap: 8px 10px;
    line-height: 1.55;
    max-width: 100%;
  }

  .word-chip {
    padding: 3px 7px;
  }

  .reader-bottom {
    gap: 8px;
  }

  .transport-row {
    gap: 12px;
  }

  .round-btn {
    width: 44px;
    height: 44px;
    font-size: 0.95rem;
  }

  .play-btn {
    width: 58px;
    height: 58px;
    font-size: 1.35rem;
  }

  .progress-label {
    font-size: 0.85rem;
  }

  .controls-compact {
    gap: 6px;
  }

  .pill-btn {
    padding: 7px 10px;
    font-size: 0.82rem;
  }

  .settings-panel {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 8px;
    padding-top: 2px;
  }

  .setting-block label,
  .check-block {
    font-size: 0.8rem;
  }

  .setting-block select {
    min-height: 34px;
    font-size: 0.82rem;
  }

  .setting-block input[type='range'] {
    height: 18px;
  }
}

.player-app.fullscreen .app-shell {
  max-width: 100%;
  padding: 8px;
}

.player-app.fullscreen .player-card.reader-card {
  width: 100%;
  min-height: calc(100vh - 16px);
}
</style>