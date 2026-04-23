<template>
  <div class="player-app" :class="{ fullscreen: isFullscreen }">
    <main class="app-shell">
      <section v-if="screen === 'home'" class="screen-center">
        <div class="player-card mode-card">
          <div class="player-top intro-top">
            <div class="brand-inline">
              <div class="brand">DyslexAI</div>
              <div class="subtitle">Leitor guiado</div>
            </div>
            <div class="intro-copy">
              <h1>Escolhe como queres começar</h1>
              <p>
                Podes preparar a leitura a partir de uma imagem ou usar a fala do aluno
                para transcrever e praticar a leitura do que foi dito.
              </p>
            </div>
          </div>

          <div class="player-bottom mode-grid-wrap">
            <div class="mode-grid">
              <button class="mode-option" @click="startImageFlow">
                <span class="mode-icon">🖼️</span>
                <strong>Leitura assistida</strong>
                <span>Carregar uma imagem e transformar o texto em leitura guiada.</span>
              </button>

              <button class="mode-option" @click="startAudioFlow">
                <span class="mode-icon">🎙️</span>
                <strong>Leitura a partir da fala</strong>
                <span>Gravar a fala do aluno, transcrever a frase e praticar a leitura no mesmo leitor.</span>
              </button>
            </div>
          </div>
        </div>
      </section>

      <section v-else-if="screen === 'confirm-image'" class="screen-center">
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
              <button class="soft-action" @click="triggerImageInput">Outra imagem</button>
              <button class="soft-action subtle-btn" @click="goHome">Voltar</button>
            </div>
          </div>
        </div>
      </section>

      <section v-else-if="screen === 'confirm-audio'" class="screen-center">
        <div class="player-card">
          <div class="player-top audio-top">
            <div class="audio-hero recording" :class="{ active: isRecording }">🎙️</div>
            <div class="audio-summary">
              <h2>Leitura a partir da fala</h2>
              <p>
                {{ isRecording
                  ? 'A gravação está em curso. Quando terminares, pára e envia o áudio para transcrição.'
                  : hasRecordedAudio
                    ? 'A gravação está pronta. Podes ouvi-la e enviá-la para obter a frase transcrita.'
                    : 'Clica em Gravar para captar a fala do aluno e preparar a leitura guiada.' }}
              </p>

              <div class="recording-status-row">
                <span class="status-pill" :class="{ live: isRecording, ready: hasRecordedAudio && !isRecording }">
                  {{ isRecording ? 'A gravar' : (hasRecordedAudio ? 'Gravação pronta' : 'À espera de gravação') }}
                </span>
                <span v-if="selectedAudioName" class="status-file">{{ selectedAudioName }}</span>
              </div>

              <audio v-if="audioPreviewUrl" :src="audioPreviewUrl" controls class="audio-player"></audio>
            </div>
          </div>

          <div class="player-bottom audio-actions-bottom">
            <div class="confirm-text">
              <strong>Preparar transcrição</strong>
              <span>Vamos enviar o áudio para obter a frase e iniciar a leitura guiada.</span>
            </div>

            <div class="record-controls">
              <button class="main-action" @click="startRecording" :disabled="isRecording || isRecorderBusy">Gravar</button>
              <button class="soft-action" @click="stopRecording" :disabled="!isRecording">Parar</button>
              <button class="soft-action" @click="clearRecordedAudio" :disabled="isRecording || !hasRecordedAudio">Limpar</button>
              <button class="soft-action" @click="processAudio" :disabled="isRecording || !hasRecordedAudio">Processar</button>
              <button class="soft-action subtle-btn" @click="goHome">Voltar</button>
            </div>
          </div>
        </div>
      </section>

      <section v-else-if="screen === 'processing'" class="screen-center">
        <div class="player-card">
          <div class="player-top processing-top">
            <div class="processing-content">
              <div class="loader-ring"></div>
              <h2>{{ processingTitle }}</h2>
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
              <button class="corner-btn" @click="resetAll" title="Início">
                ↺
              </button>
            </div>

            <div
              v-if="readingMode === 'line'"
              class="reading-focus line-focus"
              :style="{ fontSize: computedLineFontSize + 'px', color: paletteStyles.lineText }"
            >
              {{ currentLines[currentLineIndex] || 'Sem texto disponível.' }}
            </div>

            <div v-else class="reading-focus word-focus" :style="{ fontSize: computedWordContextFontSize + 'px' }">
              <div class="word-context">
                <span
                  v-for="(word, index) in currentWords"
                  :key="`${currentLineIndex}-${index}-${word}`"
                  class="word-chip"
                  :class="{ active: index === currentWordIndex }"
                  :style="index === currentWordIndex
                    ? { ...activeWordStyle, background: paletteStyles.activeBg, color: paletteStyles.activeText }
                    : { color: paletteStyles.inactiveWord }"
                >
                  {{ word }}
                </span>
              </div>
            </div>
          </div>

          <div class="player-bottom reader-bottom">
            <div v-if="hasSpokenText" class="speech-summary-box">
              <span class="speech-label">Frase transcrita</span>
              <strong>{{ spokenText }}</strong>
              <small v-if="spokenTranscription && spokenTranscription !== spokenText">Transcrição bruta: {{ spokenTranscription }}</small>
            </div>

            <div class="transport-row">
              <template v-if="readingMode === 'line'">
                <button class="round-btn" @click="prevLine" :disabled="currentLineIndex === 0">◀</button>
                <button class="play-btn" @click="toggleLineReading">{{ isSpeakingLine ? '❚❚' : '▶' }}</button>
                <button class="round-btn" @click="nextLine" :disabled="currentLineIndex === currentLines.length - 1">▶</button>
              </template>

              <template v-else>
                <button class="round-btn" @click="prevWord">◀</button>
                <button class="play-btn" @click="toggleWordPlay">{{ isPlayingWords ? '❚❚' : '▶' }}</button>
                <button class="round-btn" @click="nextWord">▶</button>
              </template>
            </div>

            <div class="progress-label">
              <span>Linha {{ currentLineIndex + 1 }}/{{ currentLines.length || 1 }}</span>
              <span v-if="readingMode === 'word'">· Palavra {{ currentWordIndex + 1 }}/{{ currentWords.length || 1 }}</span>
            </div>

            <div class="progress-line">
              <div class="progress-fill" :style="{ width: lineProgressPercent + '%' }"></div>
            </div>

            <div class="controls-compact">
              <button class="pill-btn" :class="{ active: currentTextMode === 'original' }" @click="switchTextMode('original')">Original</button>
              <button class="pill-btn" :class="{ active: currentTextMode === 'simplified' }" @click="switchTextMode('simplified')">Simplificado</button>
              <button v-if="hasSpokenText" class="pill-btn" :class="{ active: currentTextMode === 'spoken' }" @click="switchTextMode('spoken')">Falado</button>
              <button class="pill-btn" :class="{ active: readingMode === 'line' }" @click="setReadingMode('line')">Linha</button>
              <button class="pill-btn" :class="{ active: readingMode === 'word' }" @click="setReadingMode('word')">Palavra</button>

              <button v-if="readingMode === 'line'" class="pill-btn" :class="{ active: linePlaybackMode === 'single' }" @click="linePlaybackMode = 'single'">Linha única</button>
              <button v-if="readingMode === 'line'" class="pill-btn" :class="{ active: linePlaybackMode === 'continuous' }" @click="linePlaybackMode = 'continuous'">
                {{ isSpeakingLine && linePlaybackMode === 'continuous' ? 'A ler até ao fim' : 'Até ao fim' }}
              </button>

              <button v-if="readingMode === 'word'" class="pill-btn" :class="{ active: wordAudioMode === 'silent' }" @click="setWordAudioMode('silent')">Sem som</button>
              <button v-if="readingMode === 'word'" class="pill-btn" :class="{ active: wordAudioMode === 'audio' }" @click="setWordAudioMode('audio')">Com som</button>

              <button class="pill-btn" @click="restartReading">Reiniciar</button>
              <button class="pill-btn" @click="stopAllAudio">Parar</button>
              <button class="pill-btn subtle" @click="showSettings = !showSettings">{{ showSettings ? 'Menos' : 'Definições' }}</button>
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

    <input ref="imageInput" type="file" accept="image/*" hidden @change="onImageChange" />
    <input ref="audioInput" type="file" accept="audio/*,.wav,.mp3,.m4a,.ogg" hidden @change="onAudioChange" />

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
          <button class="pill-btn" :class="{ active: currentTextMode === 'original' }" @click="switchTextMode('original')">Original</button>
          <button class="pill-btn" :class="{ active: currentTextMode === 'simplified' }" @click="switchTextMode('simplified')">Simplificado</button>
          <button v-if="hasSpokenText" class="pill-btn" :class="{ active: currentTextMode === 'spoken' }" @click="switchTextMode('spoken')">Falado</button>
        </div>

        <textarea class="text-output" :value="validationText" readonly></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { createInferenceService } from './services/inference/createInferenceService'

const inference = createInferenceService()


let warmed = false

async function warmBackend() {
  if (warmed) return
  try {
    await inference.health()
    warmed = true
  } catch {
    // ignore
  }
}

const screen = ref('home')
const previewUrl = ref('')
const audioPreviewUrl = ref('')
const selectedFile = ref(null)
const selectedAudioFile = ref(null)
const selectedAudioName = ref('')
const isRecording = ref(false)
const isRecorderBusy = ref(false)

const activeFlow = ref('image')
const openValidation = ref(false)
const showSettings = ref(true)
const isFullscreen = ref(false)
const isLandscape = ref(false)

const imageInput = ref(null)
const audioInput = ref(null)

const readingPalette = ref('yellow')
const fontSize = ref(30)
const readingMode = ref('line')
const currentTextMode = ref('simplified')
const wordAudioMode = ref('silent')
const linePlaybackMode = ref('single')

const currentLineIndex = ref(0)
const currentWordIndex = ref(0)

const isPlayingWords = ref(false)
const isSpeakingLine = ref(false)
const autoAdvanceLine = ref(true)
const speechRate = ref(1.0)

const processingProgress = ref(0)
const processingMessage = ref('A preparar...')
const processingTitle = ref('A preparar a leitura')

let playTimer = null
let wordPlaybackStopped = false
let mediaRecorder = null
let mediaStream = null
let recordingMimeType = ''
let recordedChunks = []

const correctedText = ref('')
const simplifiedText = ref('')
const spokenText = ref('')
const spokenTranscription = ref('')

const originalLines = ref([])
const simplifiedLines = ref([])
const spokenLines = ref([])

const hasSpokenText = computed(() => spokenLines.value.length > 0 || !!spokenText.value)
const hasRecordedAudio = computed(() => !!selectedAudioFile.value)

const currentLines = computed(() => {
  if (currentTextMode.value === 'spoken') return spokenLines.value
  return currentTextMode.value === 'original' ? originalLines.value : simplifiedLines.value
})

const currentWords = computed(() => {
  const line = currentLines.value[currentLineIndex.value] || ''
  return line.split(/\s+/).filter(Boolean)
})

const validationText = computed(() => {
  if (currentTextMode.value === 'spoken') return spokenText.value || spokenTranscription.value
  return currentTextMode.value === 'original' ? correctedText.value : simplifiedText.value
})

const paletteStyles = computed(() => {
  const palettes = {
    yellow: { lineText: '#FFFFFF', inactiveWord: 'rgba(255,255,255,0.84)', activeBg: 'rgba(255, 214, 53, 0.95)', activeText: '#231900' },
    blue: { lineText: '#F8FBFF', inactiveWord: 'rgba(248, 251, 255, 0.72)', activeBg: '#8FD3FF', activeText: '#0B1F2A' },
    green: { lineText: '#F7FFF8', inactiveWord: 'rgba(247, 255, 248, 0.72)', activeBg: '#9BE7B1', activeText: '#102217' },
    pink: { lineText: '#FFF8FC', inactiveWord: 'rgba(255, 248, 252, 0.72)', activeBg: '#FFB8D2', activeText: '#32111F' }
  }
  return palettes[readingPalette.value] || palettes.yellow
})

const lineProgressPercent = computed(() => {
  if (!currentLines.value.length) return 0
  return ((currentLineIndex.value + 1) / currentLines.value.length) * 100
})

const computedLineFontSize = computed(() => isLandscape.value ? Math.max(22, Math.min(fontSize.value - 2, 34)) : fontSize.value)
const computedWordContextFontSize = computed(() => isLandscape.value ? Math.max(18, Math.min(fontSize.value - 8, 28)) : Math.max(20, fontSize.value - 6))
const activeWordStyle = computed(() => isLandscape.value ? { fontSize: '1.25em', transform: 'translateY(-1px)' } : { fontSize: '1.4em', transform: 'translateY(-2px)' })

watch(currentLineIndex, () => { currentWordIndex.value = 0 })
watch(readingMode, () => {
  stopAllAudio()
  currentWordIndex.value = 0
  if (readingMode.value !== 'line') linePlaybackMode.value = 'single'
})
watch(wordAudioMode, () => stopAllAudio())
watch(currentTextMode, () => {
  if (currentTextMode.value === 'spoken' && !hasSpokenText.value) currentTextMode.value = 'simplified'
})

function updateLayoutFlags() {
  isLandscape.value = window.innerWidth > window.innerHeight && window.innerWidth <= 950
}

function startImageFlow() {
  activeFlow.value = 'image'
  triggerImageInput()
}

function startAudioFlow() {
  activeFlow.value = 'audio'
  clearRecordedAudio()
  screen.value = 'confirm-audio'
}

function triggerImageInput() {
  imageInput.value?.click()
}

function triggerAudioInput() {
  audioInput.value?.click()
}

async function startRecording() {
  if (isRecording.value || isRecorderBusy.value) return
  if (!(navigator.mediaDevices && window.MediaRecorder)) {
    alert('A gravação de áudio não é suportada neste navegador.')
    return
  }

  try {
    isRecorderBusy.value = true
    clearRecordedAudio()
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    recordingMimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
      ? 'audio/webm;codecs=opus'
      : (MediaRecorder.isTypeSupported('audio/webm') ? 'audio/webm' : '')
    mediaRecorder = recordingMimeType ? new MediaRecorder(mediaStream, { mimeType: recordingMimeType }) : new MediaRecorder(mediaStream)
    recordedChunks = []

    mediaRecorder.ondataavailable = (event) => {
      if (event.data && event.data.size > 0) recordedChunks.push(event.data)
    }

    mediaRecorder.onstart = () => {
      isRecording.value = true
      selectedAudioName.value = 'gravacao-aluno.webm'
    }

    mediaRecorder.onerror = (event) => {
      console.error('Erro na gravação:', event)
      alert('Não foi possível gravar o áudio.')
      stopMediaTracks()
      isRecording.value = false
    }

    mediaRecorder.onstop = () => {
      const blobType = recordingMimeType || recordedChunks[0]?.type || 'audio/webm'
      const audioBlob = new Blob(recordedChunks, { type: blobType })
      if (audioBlob.size > 0) {
        const extension = blobType.includes('ogg') ? 'ogg' : (blobType.includes('wav') ? 'wav' : 'webm')
        selectedAudioFile.value = new File([audioBlob], `gravacao-aluno.${extension}`, { type: blobType })
        selectedAudioName.value = selectedAudioFile.value.name
        if (audioPreviewUrl.value) URL.revokeObjectURL(audioPreviewUrl.value)
        audioPreviewUrl.value = URL.createObjectURL(audioBlob)
      }
      stopMediaTracks()
      isRecording.value = false
    }

    mediaRecorder.start()
  } catch (error) {
    console.error(error)
    alert('Não foi possível aceder ao microfone.')
    stopMediaTracks()
    isRecording.value = false
  } finally {
    isRecorderBusy.value = false
  }
}

function stopRecording() {
  if (mediaRecorder && isRecording.value) mediaRecorder.stop()
}

function stopMediaTracks() {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop())
    mediaStream = null
  }
}

function clearRecordedAudio() {
  if (isRecording.value) return
  if (audioPreviewUrl.value) URL.revokeObjectURL(audioPreviewUrl.value)
  audioPreviewUrl.value = ''
  selectedAudioFile.value = null
  selectedAudioName.value = ''
  isRecording.value = false
  isRecorderBusy.value = false
  recordedChunks = []
}

async function onImageChange(event) {
  const sourceFile = event.target.files?.[0]
  if (!sourceFile) return

  try {
    const buffer = await sourceFile.arrayBuffer()

    const safeFile = new File(
      [buffer],
      sourceFile.name || 'page.jpg',
      {
        type: sourceFile.type || 'image/jpeg',
        lastModified: Date.now(),
      }
    )

    selectedFile.value = safeFile

    if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = URL.createObjectURL(safeFile)

    event.target.value = ''
    screen.value = 'confirm-image'
  } catch (error) {
    console.error('Erro ao preparar imagem:', error)
    alert('Não foi possível abrir a imagem selecionada.')
  }
}

function onAudioChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  clearRecordedAudio()
  selectedAudioFile.value = file
  selectedAudioName.value = file.name
  audioPreviewUrl.value = URL.createObjectURL(file)
  screen.value = 'confirm-audio'
}

async function processRealImage(file) {
  const data = await inference.processImage(file)

  correctedText.value = data.original_text || ''
  simplifiedText.value = data.simplified_text || ''
  originalLines.value = Array.isArray(data.original_lines) ? data.original_lines : []
  simplifiedLines.value = Array.isArray(data.simplified_lines) ? data.simplified_lines : []
}

async function processRealAudio(file) {
  const data = await inference.processAudio(file)

  spokenTranscription.value = data.transcription || ''
  spokenText.value = data.clean_text || data.spoken_text || data.transcription || ''
  spokenLines.value = Array.isArray(data.spoken_lines) && data.spoken_lines.length
    ? data.spoken_lines
    : (spokenText.value ? [spokenText.value] : [])

  if (!correctedText.value) {
    correctedText.value = spokenText.value
    originalLines.value = [...spokenLines.value]
  }
  if (!simplifiedText.value) {
    simplifiedText.value = spokenText.value
    simplifiedLines.value = [...spokenLines.value]
  }
}

async function processImage() {
  if (!selectedFile.value) {
    alert('Nenhuma imagem selecionada.')
    return
  }

  try {
    screen.value = 'processing'
    processingTitle.value = 'A preparar a leitura'
    processingProgress.value = 15
    processingMessage.value = 'A preparar a imagem...'
    await new Promise(resolve => setTimeout(resolve, 180))
    processingProgress.value = 45
    processingMessage.value = 'A extrair e simplificar o texto...'
    await processRealImage(selectedFile.value)
    processingProgress.value = 100
    processingMessage.value = 'Concluído.'
    enterReader('simplified')
  } catch (error) {
    console.error(error)
    alert(error.message || 'Ocorreu um erro ao processar a imagem.')
    screen.value = 'confirm-image'
  }
}

async function processAudio() {
  if (!selectedAudioFile.value) {
    alert('Ainda não existe uma gravação pronta.')
    return
  }

  try {
    screen.value = 'processing'
    processingTitle.value = 'A preparar a leitura a partir da fala'
    processingProgress.value = 15
    processingMessage.value = 'A enviar áudio...'
    await new Promise(resolve => setTimeout(resolve, 180))
    processingProgress.value = 55
    processingMessage.value = 'A transcrever a fala...'
    await processRealAudio(selectedAudioFile.value)
    processingProgress.value = 100
    processingMessage.value = 'Concluído.'
    enterReader('spoken')
  } catch (error) {
    console.error(error)
    alert(error.message || 'Ocorreu um erro ao processar o áudio.')
    screen.value = 'confirm-audio'
  }
}

function enterReader(defaultTextMode = 'original') {
  stopAllAudio()
  currentTextMode.value = defaultTextMode
  readingMode.value = 'line'
  linePlaybackMode.value = 'single'
  currentLineIndex.value = 0
  currentWordIndex.value = 0
  openValidation.value = false
  screen.value = 'reader'
}

function goHome() {
  stopAllAudio()
  screen.value = 'home'
}

function switchTextMode(mode) {
  currentTextMode.value = mode
  currentLineIndex.value = 0
  currentWordIndex.value = 0
  stopAllAudio()
}

function setReadingMode(mode) { readingMode.value = mode }
function setWordAudioMode(mode) { wordAudioMode.value = mode }
function prevLine() { stopAllAudio(); if (currentLineIndex.value > 0) currentLineIndex.value -= 1 }
function nextLine() { stopAllAudio(); if (currentLineIndex.value < currentLines.value.length - 1) currentLineIndex.value += 1 }

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
  return voices.find(v => v.lang === 'pt-PT') || voices.find(v => v.lang?.toLowerCase().startsWith('pt')) || voices[0] || null
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
  if ('speechSynthesis' in window) window.speechSynthesis.cancel()
}

function stopAllAudio() {
  stopWordPlay()
  if ('speechSynthesis' in window) window.speechSynthesis.cancel()
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
        setTimeout(() => { if (isSpeakingLine.value) speakCurrentLine() }, 120)
        return
      }
    }
    isSpeakingLine.value = false
  }
  utterance.onerror = () => { isSpeakingLine.value = false }
  window.speechSynthesis.speak(utterance)
}

function buildReadableWord(word) {
  const cleaned = (word || '').trim()
  return cleaned || ''
}

function speakCurrentWordAndWait() {
  return new Promise(resolve => {
    if (!('speechSynthesis' in window)) return resolve()
    const word = buildReadableWord(currentWords.value[currentWordIndex.value] || '')
    if (!word) return resolve()
    window.speechSynthesis.cancel()
    isSpeakingLine.value = true
    const utterance = buildUtterance(word)
    utterance.onend = () => { isSpeakingLine.value = false; resolve() }
    utterance.onerror = () => { isSpeakingLine.value = false; resolve() }
    window.speechSynthesis.speak(utterance)
  })
}

function toggleWordPlay() {
  if (isPlayingWords.value) stopWordPlay()
  else startWordPlay()
}

async function startWordPlay() {
  stopAllAudio()
  isPlayingWords.value = true
  wordPlaybackStopped = false
  while (isPlayingWords.value && !wordPlaybackStopped) {
    if (wordAudioMode.value === 'audio') await speakCurrentWordAndWait()
    else await wait(Math.max(180, 420 / Number(speechRate.value)))

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
  return new Promise(resolve => { playTimer = setTimeout(resolve, ms) })
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

function clearPreviewUrls() {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  clearRecordedAudio()
}

function resetAll() {
  stopAllAudio()
  clearPreviewUrls()
  screen.value = 'home'
  activeFlow.value = 'image'
  selectedFile.value = null
  clearRecordedAudio()
  isRecording.value = false
  isRecorderBusy.value = false
  correctedText.value = ''
  simplifiedText.value = ''
  spokenText.value = ''
  spokenTranscription.value = ''
  originalLines.value = []
  simplifiedLines.value = []
  spokenLines.value = []
  openValidation.value = false
  showSettings.value = true
  currentTextMode.value = 'simplified'
  readingPalette.value = 'yellow'
  readingMode.value = 'line'
  wordAudioMode.value = 'silent'
  linePlaybackMode.value = 'single'
  currentLineIndex.value = 0
  currentWordIndex.value = 0
  processingProgress.value = 0
  processingMessage.value = 'A preparar...'
  processingTitle.value = 'A preparar a leitura'
}

onMounted(() => {
  warmBackend()
  updateLayoutFlags()
  window.addEventListener('resize', updateLayoutFlags)
  document.addEventListener('fullscreenchange', onFullscreenChange)
  if ('speechSynthesis' in window) {
    window.speechSynthesis.getVoices()
    window.speechSynthesis.onvoiceschanged = () => { window.speechSynthesis.getVoices() }
  }
})

onBeforeUnmount(() => {
  stopAllAudio()
  stopRecording()
  stopMediaTracks()
  window.removeEventListener('resize', updateLayoutFlags)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  clearPreviewUrls()
})
</script>

<style scoped>
:global(body) {
  margin: 0;
  font-family: Inter, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: #111827;
  background: #edf1f6;
}

* { box-sizing: border-box; }
button, input, textarea, select, audio { font: inherit; }

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
  width: min(920px, 100%);
  border-radius: 34px;
  overflow: hidden;
  box-shadow: 0 18px 48px rgba(17, 24, 39, 0.14);
  background: #fff;
}

.player-top {
  position: relative;
  min-height: 320px;
  padding: 24px;
  background: radial-gradient(circle at top left, rgba(171, 86, 230, 0.45), transparent 34%), linear-gradient(135deg, #572171 0%, #2d163f 48%, #19141f 100%);
  color: white;
}

.player-bottom {
  padding: 20px 22px 24px;
  background: linear-gradient(180deg, #ffd500 0%, #ffbe00 56%, #f4a400 100%);
}

.brand-inline { display: inline-flex; flex-direction: column; gap: 2px; }
.brand { font-size: 1.15rem; font-weight: 800; letter-spacing: 0.02em; }
.subtitle { font-size: 0.85rem; color: rgba(255, 255, 255, 0.78); }
.intro-top { display: flex; flex-direction: column; justify-content: space-between; }
.intro-copy { max-width: 720px; margin-top: auto; }
.intro-copy h1, .processing-content h2, .audio-summary h2 { margin: 0 0 10px 0; font-size: clamp(2rem, 4vw, 3rem); line-height: 1.05; }
.intro-copy p, .processing-content p, .audio-summary p { margin: 0; line-height: 1.6; color: rgba(255, 255, 255, 0.86); font-size: 1.02rem; }

.mode-grid-wrap { padding-top: 24px; }
.mode-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.mode-option {
  border: none;
  border-radius: 24px;
  padding: 24px;
  min-height: 170px;
  text-align: left;
  background: rgba(255, 255, 255, 0.92);
  color: #1f2937;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.mode-option:hover { transform: translateY(-2px); box-shadow: 0 10px 24px rgba(17, 24, 39, 0.12); }
.mode-option strong { font-size: 1.2rem; }
.mode-option span:last-child { line-height: 1.55; color: #4b5563; }
.mode-icon { font-size: 2rem; }

.confirm-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}
.confirm-text { display: flex; flex-direction: column; gap: 6px; color: #4b5563; }
.confirm-text strong { color: #111827; }
.confirm-actions { display: flex; gap: 10px; flex-wrap: wrap; }
.main-action, .soft-action, .pill-btn, .round-btn, .play-btn, .corner-btn {
  border: none;
  border-radius: 999px;
  cursor: pointer;
}
.main-action {
  padding: 14px 22px;
  background: #111827;
  color: white;
  font-weight: 700;
}
.soft-action, .pill-btn {
  padding: 12px 16px;
  background: rgba(17, 24, 39, 0.1);
  color: #111827;
  font-weight: 600;
}
.subtle-btn { background: rgba(17, 24, 39, 0.06); }
.pill-btn.active { background: #111827; color: white; }
.pill-btn.subtle { background: rgba(17, 24, 39, 0.08); }
.round-btn, .play-btn, .corner-btn {
  width: 52px;
  height: 52px;
  background: rgba(255,255,255,0.18);
  color: white;
  font-size: 1.15rem;
}
.play-btn { background: #111827; color: white; }
.main-action:disabled, .soft-action:disabled, .pill-btn:disabled, .round-btn:disabled, .play-btn:disabled, .corner-btn:disabled { opacity: 0.45; cursor: not-allowed; }

.preview-top, .processing-top, .audio-top {
  display: grid;
  place-items: center;
}
.preview-image {
  max-width: 100%;
  max-height: 420px;
  border-radius: 22px;
  object-fit: contain;
  box-shadow: 0 12px 28px rgba(17, 24, 39, 0.28);
}
.audio-top { gap: 18px; text-align: center; }
.audio-hero { font-size: 4rem; }
.audio-hero.recording.active { animation: pulseMic 1s ease-in-out infinite; }
.audio-summary { max-width: 640px; display: grid; gap: 12px; }
.recording-status-row { display: flex; justify-content: center; align-items: center; gap: 10px; flex-wrap: wrap; }
.status-pill { padding: 8px 12px; border-radius: 999px; background: rgba(255,255,255,0.14); color: white; font-weight: 700; }
.status-pill.live { background: rgba(239, 68, 68, 0.95); }
.status-pill.ready { background: rgba(34, 197, 94, 0.9); color: #06230f; }
.status-file { color: rgba(255,255,255,0.78); font-size: 0.95rem; }
.audio-player { width: min(460px, 100%); }
.audio-actions-bottom { display: grid; gap: 16px; }
.record-controls { display: flex; gap: 10px; flex-wrap: wrap; justify-content: flex-end; }

.loader-ring {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  border: 6px solid rgba(255,255,255,0.2);
  border-top-color: #ffd500;
  animation: spin 1s linear infinite;
  margin: 0 auto 18px;
}
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes pulseMic { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.08); } }
.progress-track, .progress-line {
  width: 100%;
  background: rgba(17, 24, 39, 0.14);
  border-radius: 999px;
  overflow: hidden;
  height: 10px;
}
.progress-bar, .progress-fill {
  height: 100%;
  border-radius: inherit;
  background: #111827;
  transition: width 0.3s ease;
}

.reader-card { overflow: visible; }
.reader-top { min-height: 420px; display: grid; align-items: center; padding-top: 72px; padding-bottom: 72px; }
.player-corner {
  position: absolute;
  top: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}
.player-corner.left { left: 22px; }
.player-corner.right { right: 22px; }
.mini-brand { font-weight: 800; letter-spacing: 0.04em; }
.reading-focus { width: 100%; text-align: center; }
.line-focus {
  max-width: 760px;
  margin: 0 auto;
  line-height: 1.5;
  font-weight: 700;
}
.word-focus { max-width: 900px; margin: 0 auto; }
.word-context { display: flex; flex-wrap: wrap; justify-content: center; gap: 14px; }
.word-chip {
  padding: 12px 18px;
  border-radius: 999px;
  background: rgba(255,255,255,0.08);
  transition: all 0.2s ease;
  font-weight: 700;
}
.reader-bottom { display: grid; gap: 16px; }
.speech-summary-box {
  background: rgba(255,255,255,0.55);
  border-radius: 20px;
  padding: 14px 16px;
  color: #111827;
  display: grid;
  gap: 4px;
}
.speech-label { font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.08em; color: #6b7280; }
.speech-summary-box small { color: #4b5563; }
.transport-row { display: flex; justify-content: center; gap: 14px; }
.progress-label { display: flex; justify-content: center; gap: 8px; font-weight: 600; color: #3f3f46; flex-wrap: wrap; }
.controls-compact { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
.settings-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  background: rgba(255,255,255,0.45);
  border-radius: 24px;
  padding: 16px;
}
.setting-block { display: grid; gap: 8px; color: #111827; }
.check-block { display: flex; align-items: center; gap: 10px; color: #111827; font-weight: 600; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.5);
  display: grid;
  place-items: center;
  padding: 20px;
}
.modal-card {
  width: min(880px, 100%);
  background: white;
  border-radius: 28px;
  padding: 22px;
  display: grid;
  gap: 16px;
}
.modal-header, .modal-tabs { display: flex; justify-content: space-between; gap: 10px; flex-wrap: wrap; align-items: center; }
.mini-label { font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.08em; color: #6b7280; }
.text-output {
  width: 100%;
  min-height: 240px;
  resize: vertical;
  border-radius: 18px;
  border: 1px solid #e5e7eb;
  padding: 16px;
  color: #111827;
}

@media (max-width: 820px) {
  .mode-grid, .settings-panel { grid-template-columns: 1fr; }
  .player-top { min-height: 280px; }
  .reader-top { min-height: 360px; }
}
</style>
