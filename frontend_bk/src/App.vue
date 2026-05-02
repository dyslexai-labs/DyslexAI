<template>
  <div class="player-app" :class="{ fullscreen: isFullscreen }">
    <main class="app-shell">
<section v-if="screen === 'home'" class="screen-center home-screen">
  <div class="home-app">
    <header class="home-header-approved">
      <div class="home-brand-approved">
        <div class="dyslex-logo" aria-hidden="true"><span></span></div>
        <div>
          <div class="home-brand-title-approved">Dyslex<span>AI</span></div>
          <div class="home-brand-subtitle-approved">Leitor guiado</div>
        </div>
      </div>

      <div class="home-top-actions">
        <button class="home-history-btn" type="button">◷ <span>Histórico</span></button>
        <button class="home-help-btn-approved" type="button" title="Ajuda">?</button>
        <button class="home-aa-btn" type="button">AA</button>
      </div>
    </header>

    <main class="home-main-approved">
      <section class="home-copy-approved">
        <div class="home-badge-approved">👋 Bem-vindo(a)!</div>
        <h1>Como queres<br />começar hoje?</h1>
        <p>Podes preparar a leitura a partir de uma imagem ou usar a fala do aluno para transcrever e praticar a leitura do que foi dito.</p>

        <button class="home-choice home-choice-image" type="button" @click="startImageFlow">
          <span class="home-choice-icon home-choice-icon-image">🏞️</span>
          <span class="home-choice-text">
            <strong>Leitura assistida</strong>
            <small>Usa uma imagem ou texto para preparar a leitura.</small>
          </span>
          <span class="home-choice-arrow">›</span>
        </button>
      </section>

      <section class="home-actions-approved">
        <button class="home-choice home-choice-audio home-choice-audio-top" type="button" @click="startAudioFlow">
          <span class="home-choice-icon home-choice-icon-audio home-choice-icon-round">🎙️</span>
          <span class="home-choice-text">
            <strong>Leitura a partir da fala</strong>
            <small>Grava a fala do aluno e gera uma frase para leitura guiada.</small>
          </span>
          <span class="home-choice-arrow">›</span>
        </button>

        <button class="home-choice home-choice-audio home-choice-audio-main" type="button" @click="startAudioFlow">
          <span class="home-choice-icon home-choice-icon-audio">🎙️</span>
          <span class="home-choice-text">
            <strong>Leitura a partir da fala</strong>
            <small>Grava a fala do aluno e gera uma frase para leitura guiada.</small>
          </span>
          <span class="home-choice-arrow">›</span>
        </button>

        <div class="home-feature-row" aria-hidden="true">
          <span>◎ <b>Foco</b></span>
          <span>🔊 <b>Áudio</b></span>
          <span>✣ <b>Personalizável</b></span>
          <span>♿ <b>Acessível</b></span>
        </div>
      </section>
    </main>

    <section class="home-recent-approved">
      <span>Recente</span>
      <button type="button" class="recent-card">
        <span class="recent-icon">📄</span>
        <span><strong>Lição – Animais da Quinta</strong><small>30/04/2025 · 2 min</small></span>
        <span class="home-choice-arrow">›</span>
      </button>
      <button type="button" class="privacy-card">
        <span class="recent-icon">🔒</span>
        <span><strong>Privacidade e segurança</strong><small>Os dados são utilizados apenas para melhorar a experiência de leitura guiada.</small></span>
        <span class="home-choice-arrow">›</span>
      </button>
    </section>

    <nav class="home-nav-approved" aria-label="Navegação principal">
      <button class="active" type="button">⌂<span>Início</span></button>
      <button type="button">◷<span>Histórico</span></button>
      <button type="button">⚙<span>Definições</span></button>
      <button type="button">?<span>Ajuda</span></button>
    </nav>
  </div>
</section>
      <section v-else-if="screen === 'select-image-source'" class="screen-center">
        <div class="player-card mode-card">
          <div class="player-top intro-top">
            <div class="brand-inline">
              <div class="brand">DyslexAI</div>
              <div class="subtitle">Leitura assistida</div>
            </div>
            <div class="intro-copy">
              <h1>Escolhe a origem da imagem</h1>
              <p>
                Podes tirar uma fotografia agora ou escolher uma imagem já existente da galeria.
              </p>
            </div>
          </div>

          <div class="player-bottom mode-grid-wrap">
            <div class="mode-grid">
              <button class="mode-option" @click="takePhoto">
                <span class="mode-icon">📷</span>
                <strong>Tirar fotografia</strong>
                <span>Usar a câmara para fotografar a página ou exercício.</span>
              </button>

              <button class="mode-option" @click="pickFromGallery">
                <span class="mode-icon">🖼️</span>
                <strong>Escolher da galeria</strong>
                <span>Selecionar uma imagem já guardada no dispositivo.</span>
              </button>
            </div>

            <div style="margin-top: 16px; text-align: center;">
              <button class="soft-action subtle-btn" @click="goHome">Voltar</button>
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
              <button class="soft-action" @click="startImageFlow">Outra imagem</button>
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

              <div class="audio-exercise-config">
                <label>Idade
                  <select v-model="readingAgeGroup">
                    <option value="6-7">6-7</option>
                    <option value="8-10">8-10</option>
                    <option value="11-13">11-13</option>
                  </select>
                </label>
                <label>Nível
                  <select v-model="readingLevel">
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                  </select>
                </label>
                <label>Tipo
                  <select v-model="readingType">
                    <option value="simple_sentence">Frase simples</option>
                    <option value="rhyme">Lengalenga</option>
                    <option value="tongue_twister">Trava-línguas</option>
                  </select>
                </label>
                <button class="soft-action" @click="generateReadingPhrase"
                  :disabled="isGeneratingPhrase || isRecording || isRecorderBusy">
                  {{ isGeneratingPhrase ? 'A gerar frase...' : 'Gerar frase' }}
                </button>
                <button v-if="expectedReadingText" class="soft-action" @click="generateReadingPhrase"
                  :disabled="isGeneratingPhrase || isRecording || isRecorderBusy">
                  Nova frase
                </button>
              </div>

              <div v-if="isGeneratingPhrase" class="expected-reading-box">
                <span class="speech-label">Preparação</span>
                <strong>A gerar frase...</strong>
              </div>

              <div v-if="expectedReadingText" class="expected-reading-box">
                <span class="speech-label">Frase a ler</span>
                <strong>{{ expectedReadingText }}</strong>
              </div>

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
              <button class="main-action" @click="startRecording"
                :disabled="isRecording || isRecorderBusy || !expectedReadingText || isGeneratingPhrase">
                {{ isRecording ? 'A gravar...' : 'Gravar' }}
              </button>
              <button class="soft-action" @click="stopRecording" :disabled="!isRecording">Parar</button>
              <button class="soft-action" @click="clearRecordedAudio"
                :disabled="isRecording || !hasRecordedAudio">Limpar</button>
              <button class="soft-action" @click="processAudio"
                :disabled="isRecording || !hasRecordedAudio || !expectedReadingText">Processar</button>
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

            <div v-if="readingMode === 'line'" class="reading-focus line-focus"
              :style="{ fontSize: computedLineFontSize + 'px', color: paletteStyles.lineText }">
              {{ currentLines[currentLineIndex] || 'Sem texto disponível.' }}
            </div>

            <div v-else class="reading-focus word-focus" :style="{ fontSize: computedWordContextFontSize + 'px' }">
              <div class="word-context">
                <span v-for="(word, index) in currentWords" :key="`${currentLineIndex}-${index}-${word}`"
                  class="word-chip" :class="{ active: index === currentWordIndex }" :style="index === currentWordIndex
                    ? { ...activeWordStyle, background: paletteStyles.activeBg, color: paletteStyles.activeText }
                    : { color: paletteStyles.inactiveWord }">
                  {{ word }}
                </span>
              </div>
            </div>
          </div>

          <div class="player-bottom reader-bottom">
            <div v-if="expectedReadingText" class="speech-summary-box">
              <span class="speech-label">Frase esperada</span>
              <strong>{{ expectedReadingText }}</strong>
            </div>

            <div v-if="audioIssues.length" class="speech-summary-box issues-box">
              <span class="speech-label">Dificuldades encontradas</span>
              <ul class="issues-list">
                <li v-for="(issue, index) in audioIssues" :key="`${issue.type || 'issue'}-${index}`">
                  <strong>{{ issue.type || 'other' }}</strong> — {{ issue.message || 'Diferença identificada.' }}
                  <span v-if="issue.expected || issue.heard"> ({{ issue.expected || '—' }} / {{ issue.heard ||
                    issue.fragment || '—' }})</span>
                </li>
              </ul>
            </div>

            <div v-if="hasSpokenText" class="speech-summary-box">
              <span class="speech-label">Frase transcrita</span>
              <strong>{{ spokenText }}</strong>
              <small v-if="spokenTranscription && spokenTranscription !== spokenText">Transcrição bruta: {{
                spokenTranscription }}</small>
            </div>

            <div class="transport-row">
              <template v-if="readingMode === 'line'">
                <button class="round-btn" @click="prevLine" :disabled="currentLineIndex === 0">◀</button>
                <button class="play-btn" @click="toggleLineReading">{{ isSpeakingLine ? '❚❚' : '▶' }}</button>
                <button class="round-btn" @click="nextLine"
                  :disabled="currentLineIndex === currentLines.length - 1">▶</button>
              </template>

              <template v-else>
                <button class="round-btn" @click="prevWord">◀</button>
                <button class="play-btn" @click="toggleWordPlay">{{ isPlayingWords ? '❚❚' : '▶' }}</button>
                <button class="round-btn" @click="nextWord">▶</button>
              </template>
            </div>

            <div class="progress-label">
              <span>Linha {{ currentLineIndex + 1 }}/{{ currentLines.length || 1 }}</span>
              <span v-if="readingMode === 'word'">· Palavra {{ currentWordIndex + 1 }}/{{ currentWords.length || 1
                }}</span>
            </div>

            <div class="progress-line">
              <div class="progress-fill" :style="{ width: lineProgressPercent + '%' }"></div>
            </div>

            <div class="controls-compact">
              <button class="pill-btn" :class="{ active: currentTextMode === 'original' }"
                @click="switchTextMode('original')">Original</button>
              <button class="pill-btn" :class="{ active: currentTextMode === 'simplified' }"
                @click="switchTextMode('simplified')">Simplificado</button>
              <button v-if="hasSpokenText" class="pill-btn" :class="{ active: currentTextMode === 'spoken' }"
                @click="switchTextMode('spoken')">Falado</button>
              <button class="pill-btn" :class="{ active: readingMode === 'line' }"
                @click="setReadingMode('line')">Linha</button>
              <button class="pill-btn" :class="{ active: readingMode === 'word' }"
                @click="setReadingMode('word')">Palavra</button>

              <button v-if="readingMode === 'line'" class="pill-btn" :class="{ active: linePlaybackMode === 'single' }"
                @click="linePlaybackMode = 'single'">Linha única</button>
              <button v-if="readingMode === 'line'" class="pill-btn"
                :class="{ active: linePlaybackMode === 'continuous' }" @click="linePlaybackMode = 'continuous'">
                {{ isSpeakingLine && linePlaybackMode === 'continuous' ? 'A ler até ao fim' : 'Até ao fim' }}
              </button>

              <button v-if="readingMode === 'word'" class="pill-btn" :class="{ active: wordAudioMode === 'silent' }"
                @click="setWordAudioMode('silent')">Sem som</button>
              <button v-if="readingMode === 'word'" class="pill-btn" :class="{ active: wordAudioMode === 'audio' }"
                @click="setWordAudioMode('audio')">Com som</button>

              <button class="pill-btn" @click="restartReading">Reiniciar</button>
              <button class="pill-btn" @click="stopAllAudio">Parar</button>
              <button class="pill-btn subtle" @click="showSettings = !showSettings">{{ showSettings ? 'Menos' :
                'Definições'
                }}</button>
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
          <button class="pill-btn" :class="{ active: currentTextMode === 'original' }"
            @click="switchTextMode('original')">Original</button>
          <button class="pill-btn" :class="{ active: currentTextMode === 'simplified' }"
            @click="switchTextMode('simplified')">Simplificado</button>
          <button v-if="hasSpokenText" class="pill-btn" :class="{ active: currentTextMode === 'spoken' }"
            @click="switchTextMode('spoken')">Falado</button>
        </div>

        <textarea class="text-output" :value="validationText" readonly></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera'
import { createInferenceService } from './services/inference/createInferenceService'
import { Capacitor } from '@capacitor/core'

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
const selectedImageBase64 = ref('')
const selectedImageMimeType = ref('image/jpeg')
const selectedAudioFile = ref(null)
const selectedAudioName = ref('')
const expectedReadingText = ref('')
const readingAgeGroup = ref('8-10')
const readingLevel = ref('1')
const readingType = ref('simple_sentence')
const audioIssues = ref([])
const isGeneratingPhrase = ref(false)
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
  if (currentTextMode.value === 'spoken' && !hasSpokenText.value) {
    currentTextMode.value = simplifiedLines.value.length ? 'simplified' : 'original'
  }
  if (currentTextMode.value === 'simplified' && !simplifiedLines.value.length && originalLines.value.length) {
    currentTextMode.value = 'original'
  }
})

function updateLayoutFlags() {
  isLandscape.value = window.innerWidth > window.innerHeight && window.innerWidth <= 950
}

function isNativeAndroid() {
  return Capacitor.isNativePlatform() && Capacitor.getPlatform() === 'android'
}

function startImageFlow() {
  activeFlow.value = 'image'
  selectedFile.value = null
  selectedImageBase64.value = ''
  selectedImageMimeType.value = 'image/jpeg'
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  screen.value = 'select-image-source'
}

function setSelectedImageFromDataUrl(dataUrl, mimeType = 'image/jpeg') {
  selectedImageBase64.value = dataUrl
  selectedImageMimeType.value = mimeType || 'image/jpeg'

  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = dataUrl

  selectedFile.value = { name: 'imagem-selecionada', type: selectedImageMimeType.value }
  screen.value = 'confirm-image'
}

async function takePhoto() {
  try {
    const image = await Camera.getPhoto({
      quality: 75,
      width: 1000,
      resultType: CameraResultType.DataUrl,
      source: CameraSource.Camera,
      allowEditing: false,
    })

    if (!image.dataUrl) throw new Error('A fotografia não devolveu dados.')

    const mimeType = `image/${image.format || 'jpeg'}`
    setSelectedImageFromDataUrl(image.dataUrl, mimeType)
  } catch (error) {
    console.error('[DyslexAI] Erro ao tirar fotografia:', error)
    if (error?.message !== 'User cancelled photos app') {
      alert('Não foi possível tirar fotografia.')
    }
  }
}

async function pickFromGallery() {
  try {
    const image = await Camera.getPhoto({
      quality: 75,
      width: 1000,
      resultType: CameraResultType.DataUrl,
      source: CameraSource.Photos,
      allowEditing: false,
    })

    if (!image.dataUrl) throw new Error('A imagem não devolveu dados.')

    const mimeType = `image/${image.format || 'jpeg'}`
    setSelectedImageFromDataUrl(image.dataUrl, mimeType)
  } catch (error) {
    console.error('[DyslexAI] Erro ao escolher imagem:', error)
    if (error?.message !== 'User cancelled photos app') {
      alert('Não foi possível escolher imagem.')
    }
  }
}

async function startAudioFlow() {
  activeFlow.value = 'audio'
  clearRecordedAudio()
  audioIssues.value = []
  spokenText.value = ''
  spokenTranscription.value = ''
  expectedReadingText.value = ''
  screen.value = 'confirm-audio'

  try {
    await generateReadingPhrase()
  } catch (error) {
    console.error('[DyslexAI] Erro ao preparar leitura por voz:', error)
  }
}

function triggerImageInput() {
  imageInput.value?.click()
}

function triggerAudioInput() {
  audioInput.value?.click()
}

async function startRecording() {
  if (isRecording.value || isRecorderBusy.value) return

  if (!expectedReadingText.value) {
    alert('Gera primeiro uma frase para o aluno ler.')
    return
  }

  if (isNativeAndroid() && inference.startWavRecording) {
    try {
      isRecorderBusy.value = true
      clearRecordedAudio()

      await inference.startWavRecording()

      isRecording.value = true
      selectedAudioName.value = 'gravacao-aluno.wav'
      recordingMimeType = 'audio/wav'

      return
    } catch (error) {
      console.error('[DyslexAI] Erro ao iniciar gravação WAV nativa:', error)
      alert('Não foi possível iniciar a gravação de áudio.')
      isRecording.value = false
      return
    } finally {
      isRecorderBusy.value = false
    }
  }

  if (!(navigator.mediaDevices && window.MediaRecorder)) {
    alert('A gravação de áudio não é suportada neste navegador.')
    return
  }

  try {
    isRecorderBusy.value = true
    clearRecordedAudio()
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    recordingMimeType = MediaRecorder.isTypeSupported('audio/wav')
      ? 'audio/wav'
      : MediaRecorder.isTypeSupported('audio/mp4')
        ? 'audio/mp4'
        : MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
          ? 'audio/webm;codecs=opus'
          : (MediaRecorder.isTypeSupported('audio/webm') ? 'audio/webm' : '')

    mediaRecorder = recordingMimeType
      ? new MediaRecorder(mediaStream, { mimeType: recordingMimeType })
      : new MediaRecorder(mediaStream)

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
      const blobType = recordingMimeType || recordedChunks[0]?.type || 'audio/wav'
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

async function stopRecording() {
  if (!isRecording.value) return

  if (isNativeAndroid() && inference.stopWavRecording) {
    try {
      isRecorderBusy.value = true

      const result = await inference.stopWavRecording()

      if (!result?.audioBase64) {
        throw new Error('A gravação WAV nativa não devolveu áudio.')
      }

      const audioBlob = dataUrlToBlob(result.audioBase64, result.mimeType || 'audio/wav')
      selectedAudioFile.value = new File([audioBlob], result.filename || 'gravacao-aluno.wav', {
        type: result.mimeType || 'audio/wav',
      })

      selectedAudioName.value = selectedAudioFile.value.name

      if (audioPreviewUrl.value) URL.revokeObjectURL(audioPreviewUrl.value)
      audioPreviewUrl.value = URL.createObjectURL(audioBlob)

      isRecording.value = false
      recordingMimeType = 'audio/wav'
      return
    } catch (error) {
      console.error('[DyslexAI] Erro ao parar gravação WAV nativa:', error)
      alert('Não foi possível terminar a gravação de áudio.')
      isRecording.value = false
      return
    } finally {
      isRecorderBusy.value = false
    }
  }

  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
  }
}

function dataUrlToBlob(dataUrl, fallbackMimeType = 'application/octet-stream') {
  const [header, base64] = String(dataUrl || '').split(',')

  if (!base64) {
    throw new Error('Data URL inválido.')
  }

  const mimeMatch = header.match(/data:([^;]+);base64/i)
  const mimeType = mimeMatch?.[1] || fallbackMimeType

  const binary = atob(base64)
  const bytes = new Uint8Array(binary.length)

  for (let i = 0; i < binary.length; i += 1) {
    bytes[i] = binary.charCodeAt(i)
  }

  return new Blob([bytes], { type: mimeType })
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
  audioIssues.value = []
}

async function generateReadingPhrase() {
  try {
    isGeneratingPhrase.value = true
    expectedReadingText.value = ''
    clearRecordedAudio()
    audioIssues.value = []
    spokenText.value = ''
    spokenTranscription.value = ''

    console.log('[DyslexAI] A gerar frase...', {
      ageGroup: readingAgeGroup.value,
      level: readingLevel.value,
      type: readingType.value,
    })

    const result = await inference.generateReadingPhrase({
      ageGroup: readingAgeGroup.value,
      level: readingLevel.value,
      type: readingType.value,
    })

    console.log('[DyslexAI] Resultado da geração de frase:', result)

    expectedReadingText.value = (result?.text || '').trim()

    if (!expectedReadingText.value) {
      throw new Error('Não foi possível gerar a frase.')
    }
  } catch (error) {
    console.error('[DyslexAI] Erro ao gerar frase:', error)
    alert(error?.message || 'Erro ao gerar frase.')
  } finally {
    isGeneratingPhrase.value = false
  }
}

function readFileAsDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(reader.error)
    reader.readAsDataURL(file)
  })
}

async function onImageChange(event) {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    selectedFile.value = file
    selectedImageMimeType.value = file.type || 'image/jpeg'

    const dataUrl = await readFileAsDataUrl(file)
    setSelectedImageFromDataUrl(dataUrl, selectedImageMimeType.value)
  } catch (error) {
    console.error('[DyslexAI] Erro ao ler imagem selecionada:', error)
    alert('Não foi possível ler a imagem selecionada.')
    selectedFile.value = null
    selectedImageBase64.value = ''
    previewUrl.value = ''
  } finally {
    event.target.value = ''
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

async function processRealImage() {
  if (!selectedImageBase64.value) {
    throw new Error('Não existe imagem preparada.')
  }

  const data = await inference.processImage({
    imageBase64: selectedImageBase64.value,
    mimeType: selectedImageMimeType.value,
  })

  correctedText.value = data.original_text || ''
  simplifiedText.value = data.simplified_text || ''
  originalLines.value = Array.isArray(data.original_lines) ? data.original_lines : []
  simplifiedLines.value = Array.isArray(data.simplified_lines) ? data.simplified_lines : []
}

async function processRealAudio(file) {
  const data = await inference.processAudio(file, expectedReadingText.value)
  const normalized = normalizeAudioResult(data)

  spokenTranscription.value = normalized.transcription
  spokenText.value = normalized.clean_text || normalized.spoken_text || normalized.transcription
  spokenLines.value = normalized.spoken_lines.length
    ? normalized.spoken_lines
    : (spokenText.value ? [spokenText.value] : [])
  audioIssues.value = normalized.issues

  correctedText.value = expectedReadingText.value || spokenText.value
  originalLines.value = correctedText.value ? [correctedText.value] : []
  simplifiedText.value = spokenText.value
  simplifiedLines.value = [...spokenLines.value]
}

function normalizeAudioResult(data = {}) {
  const parsed = parseMaybeJsonFromModel(
    data.clean_text || data.spoken_text || data.transcription || data.raw || ''
  )

  const result = parsed && typeof parsed === 'object'
    ? { ...data, ...parsed }
    : { ...data }

  const transcription = cleanAudioText(result.transcription || '')
  const cleanText = cleanAudioText(result.clean_text || result.spoken_text || transcription)
  const spokenTextValue = cleanAudioText(result.spoken_text || cleanText || transcription)

  return {
    success: result.success !== false,
    transcription,
    clean_text: cleanText,
    spoken_text: spokenTextValue,
    spoken_lines: normalizeAudioLines(
      result.spoken_lines,
      spokenTextValue || cleanText || transcription
    ),
    issues: normalizeAudioIssues(result.issues),
  }
}

function parseMaybeJsonFromModel(value) {
  const cleaned = cleanAudioText(value)
  if (!cleaned) return null

  const firstBrace = cleaned.indexOf('{')
  const lastBrace = cleaned.lastIndexOf('}')

  if (firstBrace < 0 || lastBrace <= firstBrace) {
    return null
  }

  try {
    return JSON.parse(cleaned.slice(firstBrace, lastBrace + 1))
  } catch (error) {
    console.warn('Não foi possível interpretar JSON de áudio devolvido pelo modelo.', error)
    return null
  }
}

function cleanAudioText(value) {
  return String(value || '')
    .trim()
    .replace(/^```(?:json|text)?\s*/i, '')
    .replace(/```$/i, '')
    .replace(/^json\s*/i, '')
    .trim()
}

function normalizeAudioLines(lines, fallbackText) {
  if (Array.isArray(lines)) {
    const cleaned = lines
      .map(line => cleanAudioText(line))
      .filter(line => line && line !== '{' && line !== '}')
      .filter(line => !/^"?(transcription|clean_text|issues)"?\s*:/i.test(line))

    if (cleaned.length) {
      return cleaned
    }
  }

  const fallback = cleanAudioText(fallbackText)
  return fallback ? [fallback] : []
}

function normalizeAudioIssues(issues) {
  if (Array.isArray(issues)) {
    return issues
  }

  if (!issues) {
    return []
  }

  const text = String(issues).trim()

  if (
    text.toLowerCase() === 'não há erros.' ||
    text.toLowerCase() === 'não há erros' ||
    text.toLowerCase() === 'sem erros'
  ) {
    return []
  }

  return [
    {
      type: 'observacao',
      message: text,
    },
  ]
}

async function processImage() {
  if (!selectedImageBase64.value) {
    alert('Nenhuma imagem selecionada.')
    return
  }

  try {
    screen.value = 'processing'
    processingTitle.value = 'A preparar a leitura'
    processingProgress.value = 15
    processingMessage.value = 'A enviar imagem...'
    await new Promise(resolve => setTimeout(resolve, 180))
    processingProgress.value = 45
    processingMessage.value = 'A processar texto...'
    await processRealImage()
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
  if (!expectedReadingText.value) {
    alert('Gera primeiro a frase que o aluno deve ler.')
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


function resolveAvailableTextMode(mode = 'simplified') {
  if (mode === 'spoken') {
    if (hasSpokenText.value) return 'spoken'
    return simplifiedLines.value.length ? 'simplified' : 'original'
  }

  if (mode === 'simplified') {
    return simplifiedLines.value.length ? 'simplified' : 'original'
  }

  if (mode === 'original') {
    return originalLines.value.length ? 'original' : (simplifiedLines.value.length ? 'simplified' : 'original')
  }

  return simplifiedLines.value.length ? 'simplified' : 'original'
}

function enterReader(defaultTextMode = 'simplified') {
  stopAllAudio()
  currentTextMode.value = resolveAvailableTextMode(defaultTextMode)
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
  currentTextMode.value = resolveAvailableTextMode(mode)
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
  if (!('speechSynthesis' in window)) return null
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

function canUseBrowserTts() {
  return 'speechSynthesis' in window && typeof SpeechSynthesisUtterance !== 'undefined'
}

function stopNativeTts() {
  if (typeof inference.stopSpeaking === 'function') {
    inference.stopSpeaking().catch(() => { })
  }
}

function speakWithBrowserTts(text) {
  return new Promise(resolve => {
    if (!canUseBrowserTts()) {
      resolve(false)
      return
    }

    window.speechSynthesis.cancel()
    const utterance = buildUtterance(text)
    utterance.onend = () => resolve(true)
    utterance.onerror = () => resolve(false)
    window.speechSynthesis.speak(utterance)
  })
}

async function speakTextAndWait(text) {
  const cleanText = String(text || '').trim()
  if (!cleanText) return false

  if (typeof inference.speak === 'function') {
    try {
      await inference.speak({
        text: cleanText,
        rate: Number(speechRate.value),
      })
      return true
    } catch (error) {
      console.warn('[DyslexAI] TTS nativo falhou; a tentar fallback Web Speech.', error)
    }
  }

  return speakWithBrowserTts(cleanText)
}

function stopWordPlay() {
  wordPlaybackStopped = true
  if (playTimer) {
    clearTimeout(playTimer)
    playTimer = null
  }
  isPlayingWords.value = false
  stopNativeTts()
  if (canUseBrowserTts()) window.speechSynthesis.cancel()
}

function stopAllAudio() {
  stopWordPlay()
  stopNativeTts()
  if (canUseBrowserTts()) window.speechSynthesis.cancel()
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

async function speakCurrentLine() {
  const text = currentLines.value[currentLineIndex.value] || ''
  if (!text) {
    isSpeakingLine.value = false
    return
  }

  await speakTextAndWait(text)

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

function buildReadableWord(word) {
  return String(word || '').replace(/[“”"«».,;:!?()\[\]]+/g, '').trim()
}

async function speakCurrentWordAndWait() {
  const word = buildReadableWord(currentWords.value[currentWordIndex.value] || '')
  if (!word) return

  isSpeakingLine.value = true
  await speakTextAndWait(word)
  isSpeakingLine.value = false
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
  selectedImageBase64.value = ''
  selectedImageMimeType.value = 'image/jpeg'
  clearRecordedAudio()
}

function resetAll() {
  stopAllAudio()
  clearPreviewUrls()
  screen.value = 'home'
  activeFlow.value = 'image'
  selectedFile.value = null
  selectedImageBase64.value = ''
  selectedImageMimeType.value = 'image/jpeg'
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

* {
  box-sizing: border-box;
}

button,
input,
textarea,
select,
audio {
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
.processing-content h2,
.audio-summary h2 {
  margin: 0 0 10px 0;
  font-size: clamp(2rem, 4vw, 3rem);
  line-height: 1.05;
}

.intro-copy p,
.processing-content p,
.audio-summary p {
  margin: 0;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.86);
  font-size: 1.02rem;
}

.mode-grid-wrap {
  padding-top: 24px;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

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

.mode-option:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(17, 24, 39, 0.12);
}

.mode-option strong {
  font-size: 1.2rem;
}

.mode-option span:last-child {
  line-height: 1.55;
  color: #4b5563;
}

.mode-icon {
  font-size: 2rem;
}

.confirm-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}

.confirm-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #4b5563;
}

.confirm-text strong {
  color: #111827;
}

.confirm-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.main-action,
.soft-action,
.pill-btn,
.round-btn,
.play-btn,
.corner-btn {
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

.soft-action,
.pill-btn {
  padding: 12px 16px;
  background: rgba(17, 24, 39, 0.1);
  color: #111827;
  font-weight: 600;
}

.subtle-btn {
  background: rgba(17, 24, 39, 0.06);
}

.pill-btn.active {
  background: #111827;
  color: white;
}

.pill-btn.subtle {
  background: rgba(17, 24, 39, 0.08);
}

.round-btn,
.play-btn,
.corner-btn {
  width: 52px;
  height: 52px;
  background: rgba(255, 255, 255, 0.18);
  color: white;
  font-size: 1.15rem;
}

.play-btn {
  background: #111827;
  color: white;
}

.main-action:disabled,
.soft-action:disabled,
.pill-btn:disabled,
.round-btn:disabled,
.play-btn:disabled,
.corner-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.preview-top,
.processing-top,
.audio-top {
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

.audio-top {
  gap: 18px;
  text-align: center;
}

.audio-hero {
  font-size: 4rem;
}

.audio-hero.recording.active {
  animation: pulseMic 1s ease-in-out infinite;
}

.audio-summary {
  max-width: 640px;
  display: grid;
  gap: 12px;
}

.recording-status-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.status-pill {
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: white;
  font-weight: 700;
}

.status-pill.live {
  background: rgba(239, 68, 68, 0.95);
}

.status-pill.ready {
  background: rgba(34, 197, 94, 0.9);
  color: #06230f;
}

.status-file {
  color: rgba(255, 255, 255, 0.78);
  font-size: 0.95rem;
}

.audio-player {
  width: min(460px, 100%);
}

.audio-actions-bottom {
  display: grid;
  gap: 16px;
}

.record-controls {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.loader-ring {
  width: 68px;
  height: 68px;
  border-radius: 50%;
  border: 6px solid rgba(255, 255, 255, 0.2);
  border-top-color: #ffd500;
  animation: spin 1s linear infinite;
  margin: 0 auto 18px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulseMic {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.08);
  }
}

.progress-track,
.progress-line {
  width: 100%;
  background: rgba(17, 24, 39, 0.14);
  border-radius: 999px;
  overflow: hidden;
  height: 10px;
}

.progress-bar,
.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: #111827;
  transition: width 0.3s ease;
}

.reader-card {
  overflow: visible;
}

.reader-top {
  min-height: 420px;
  display: grid;
  align-items: center;
  padding-top: 72px;
  padding-bottom: 72px;
}

.player-corner {
  position: absolute;
  top: 20px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.player-corner.left {
  left: 22px;
}

.player-corner.right {
  right: 22px;
}

.mini-brand {
  font-weight: 800;
  letter-spacing: 0.04em;
}

.reading-focus {
  width: 100%;
  text-align: center;
}

.line-focus {
  max-width: 760px;
  margin: 0 auto;
  line-height: 1.5;
  font-weight: 700;
}

.word-focus {
  max-width: 900px;
  margin: 0 auto;
}

.word-context {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px;
}

.word-chip {
  padding: 12px 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  transition: all 0.2s ease;
  font-weight: 700;
}

.reader-bottom {
  display: grid;
  gap: 16px;
}

.speech-summary-box {
  background: rgba(255, 255, 255, 0.55);
  border-radius: 20px;
  padding: 14px 16px;
  color: #111827;
  display: grid;
  gap: 4px;
}

.speech-label {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #6b7280;
}

.speech-summary-box small {
  color: #4b5563;
}

.transport-row {
  display: flex;
  justify-content: center;
  gap: 14px;
}

.progress-label {
  display: flex;
  justify-content: center;
  gap: 8px;
  font-weight: 600;
  color: #3f3f46;
  flex-wrap: wrap;
}

.controls-compact {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.settings-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  background: rgba(255, 255, 255, 0.45);
  border-radius: 24px;
  padding: 16px;
}

.setting-block {
  display: grid;
  gap: 8px;
  color: #111827;
}

.check-block {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #111827;
  font-weight: 600;
}

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

.modal-header,
.modal-tabs {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.mini-label {
  font-size: 0.78rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #6b7280;
}

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

  .mode-grid,
  .settings-panel {
    grid-template-columns: 1fr;
  }

  .player-top {
    min-height: 280px;
  }

  .reader-top {
    min-height: 360px;
  }
}

/* ===== Home — versão compacta e proporcional ===== */
.home-screen {
  width: 100%;
}

.home-shell {
  width: min(760px, 100%);
  min-height: 0;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 28px;
  padding: clamp(18px, 3vw, 28px);
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.home-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.home-logo {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 1.95rem;
  font-weight: 800;
  color: #1a73e8;
  background: linear-gradient(135deg, #e8f0fe 0%, #f8fbff 100%);
  border: 1px solid #dbe7ff;
}

.home-brand-title {
  font-size: clamp(1.8rem, 3vw, 2.2rem);
  font-weight: 800;
  line-height: 1.05;
  color: #0f172a;
}

.home-brand-subtitle {
  margin-top: 4px;
  font-size: 0.98rem;
  color: #64748b;
}

.home-help-btn {
  width: 48px;
  height: 48px;
  border-radius: 999px;
  border: 1px solid #dbe2ea;
  background: #ffffff;
  color: #0f172a;
  font-size: 1.15rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}

.home-intro {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 520px;
}

.home-badge {
  align-self: flex-start;
  padding: 8px 14px;
  border-radius: 999px;
  background: #e8f0fe;
  color: #1a73e8;
  font-weight: 600;
  font-size: 0.95rem;
}

.home-intro h1 {
  margin: 0;
  font-size: clamp(2.4rem, 5.3vw, 4rem);
  line-height: 1.02;
  font-weight: 800;
  letter-spacing: -0.04em;
  color: #0b1736;
  max-width: 9ch;
}

.home-intro p {
  margin: 0;
  font-size: 1.05rem;
  line-height: 1.4;
  color: #667085;
}

.home-options {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  width: 100%;
}

.home-option-card {
  width: 100%;
  border: 1px solid #e6ebf2;
  border-radius: 24px;
  background: #ffffff;
  padding: 18px;
  display: grid;
  grid-template-columns: 88px 1fr 48px;
  align-items: center;
  gap: 16px;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.05);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.home-option-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.home-option-card--blue {
  border-left: 5px solid #4285f4;
}

.home-option-card--green {
  border-left: 5px solid #34a853;
}

.home-option-icon {
  width: 88px;
  height: 88px;
  border-radius: 22px;
  display: grid;
  place-items: center;
  font-size: 2.4rem;
  background: #eef5ff;
}

.home-option-card--green .home-option-icon {
  background: #eef9f0;
}

.home-option-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.home-option-text strong {
  font-size: clamp(1.25rem, 2vw, 1.65rem);
  line-height: 1.15;
  color: #0f172a;
}

.home-option-text span {
  font-size: 1rem;
  line-height: 1.35;
  color: #667085;
}

.home-option-arrow {
  width: 44px;
  height: 44px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: #edf3ff;
  color: #4285f4;
  font-size: 1.45rem;
  font-weight: 700;
}

.home-option-card--green .home-option-arrow {
  background: #eaf7ec;
  color: #34a853;
}

@media (max-width: 768px) {
  .home-shell {
    width: 100%;
    padding: 16px;
    gap: 16px;
    border-radius: 22px;
  }

  .home-option-card {
    grid-template-columns: 74px 1fr 42px;
    padding: 14px;
    gap: 12px;
  }

  .home-option-icon {
    width: 74px;
    height: 74px;
    font-size: 2rem;
    border-radius: 18px;
  }

  .home-option-text strong {
    font-size: 1.14rem;
  }

  .home-option-text span {
    font-size: 0.92rem;
  }

  .home-option-arrow {
    width: 38px;
    height: 38px;
    font-size: 1.2rem;
  }
}

@media (min-width: 900px) and (orientation: landscape) {
  .app-shell {
    max-width: 920px;
    padding: 16px;
  }

  .screen-center.home-screen {
    min-height: calc(100vh - 32px);
  }

  .home-shell {
    width: 100%;
    padding: 20px 22px;
    gap: 16px;
  }

  .home-intro h1 {
    font-size: clamp(2.8rem, 4vw, 3.8rem);
  }
}

/* ===== HOME — alinhado com a proposta aprovada ===== */

.home-screen {
  width: 100%;
}

.home-shell {
  width: 100%;
  display: flex;
  justify-content: center;
}

.home-card {
  width: min(1180px, 100%);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 34px;
  padding: 28px;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.08);
  border: 1px solid #e7edf5;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 18px;
}

.home-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.home-logo {
  width: 68px;
  height: 68px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #e8f0fe 0%, #f6f9ff 100%);
  color: #1a73e8;
  font-size: 2.1rem;
  font-weight: 800;
  border: 1px solid #d8e5ff;
}

.home-brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.home-brand-title {
  font-size: 2rem;
  line-height: 1;
  font-weight: 800;
  color: #0b1736;
}

.home-brand-subtitle {
  font-size: 0.98rem;
  color: #667085;
}

.home-help-btn {
  width: 54px;
  height: 54px;
  border-radius: 999px;
  border: 1px solid #dde5ee;
  background: #fff;
  color: #0b1736;
  font-size: 1.3rem;
  font-weight: 700;
  cursor: pointer;
}

.home-badge {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 8px 16px;
  border-radius: 999px;
  background: #e8f0fe;
  color: #1a73e8;
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 18px;
}

.home-content {
  display: grid;
  grid-template-columns: 1fr;
  gap: 22px;
}

.home-left {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.home-left h1 {
  margin: 0;
  max-width: 8ch;
  font-size: clamp(3.2rem, 6vw, 5.2rem);
  line-height: 0.98;
  letter-spacing: -0.05em;
  font-weight: 800;
  color: #091a44;
}

.home-left p {
  margin: 0;
  font-size: 1.2rem;
  line-height: 1.3;
  color: #667085;
}

.home-option-card,
.home-preview-card {
  width: 100%;
  border-radius: 26px;
  background: #fff;
  border: 1px solid #e6ebf2;
  display: grid;
  grid-template-columns: 96px 1fr 54px;
  align-items: center;
  gap: 16px;
  padding: 18px;
  text-align: left;
}

.home-option-card {
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.05);
}

.home-option-card--blue,
.home-preview-card--blue {
  border-left: 5px solid #4285f4;
}

.home-option-card--green,
.home-preview-card--green {
  border-left: 5px solid #34a853;
}

.home-option-icon,
.home-preview-icon {
  width: 96px;
  height: 96px;
  border-radius: 22px;
  display: grid;
  place-items: center;
  font-size: 2.6rem;
  background: #eef5ff;
}

.home-option-card--green .home-option-icon,
.home-preview-card--green .home-preview-icon {
  background: #eef9f0;
}

.home-option-text,
.home-preview-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.home-option-text strong,
.home-preview-text strong {
  font-size: 1.45rem;
  line-height: 1.1;
  color: #0f172a;
}

.home-option-text span,
.home-preview-text span {
  font-size: 1rem;
  line-height: 1.3;
  color: #667085;
}

.home-option-arrow,
.home-preview-arrow {
  width: 46px;
  height: 46px;
  border-radius: 999px;
  display: grid;
  place-items: center;
  font-size: 1.45rem;
  font-weight: 700;
  background: #edf3ff;
  color: #4285f4;
}

.home-option-card--green .home-option-arrow,
.home-preview-card--green .home-preview-arrow {
  background: #eaf7ec;
  color: #34a853;
}

.home-right {
  display: none;
}

.home-pill-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.home-pill {
  padding: 10px 14px;
  border-radius: 999px;
  background: #f4f7fb;
  border: 1px solid #e5ebf3;
  color: #475467;
  font-size: 0.92rem;
  font-weight: 600;
}

@media (min-width: 1100px) and (orientation: landscape) {
  .home-card {
    padding: 28px 30px 24px;
  }

  .home-content {
    grid-template-columns: minmax(420px, 560px) minmax(360px, 1fr);
    align-items: start;
    gap: 30px;
  }

  .home-right {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding-top: 8px;
  }

  .home-left {
    gap: 18px;
  }

  .home-left h1 {
    font-size: clamp(4rem, 5vw, 5.4rem);
  }

  .home-option-card {
    max-width: 640px;
  }

  .home-preview-card {
    background: #f8fbff;
  }

  .home-preview-card--green {
    background: #f8fcf8;
  }
}

@media (max-width: 900px) {
  .home-card {
    padding: 18px;
    border-radius: 28px;
  }

  .home-header {
    margin-bottom: 14px;
  }

  .home-logo {
    width: 58px;
    height: 58px;
    font-size: 1.9rem;
  }

  .home-brand-title {
    font-size: 1.75rem;
  }

  .home-help-btn {
    width: 48px;
    height: 48px;
    font-size: 1.15rem;
  }

  .home-badge {
    margin-bottom: 14px;
  }

  .home-left h1 {
    max-width: 7ch;
    font-size: clamp(2.8rem, 9vw, 4.4rem);
  }

  .home-left p {
    font-size: 1.05rem;
  }

  .home-option-card {
    grid-template-columns: 78px 1fr 42px;
    padding: 14px;
    gap: 12px;
    border-radius: 22px;
  }

  .home-option-icon {
    width: 78px;
    height: 78px;
    font-size: 2.2rem;
    border-radius: 18px;
  }

  .home-option-text strong {
    font-size: 1.2rem;
  }

  .home-option-text span {
    font-size: 0.92rem;
  }

  .home-option-arrow {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }
}

/* ===== HOME APROVADA — retrato + horizontal ===== */
.home-screen { width: 100%; min-height: 100svh; place-items: stretch; background: #fff; }
.home-app { width: 100%; min-height: 100svh; height: 100svh; overflow: hidden; background: radial-gradient(circle at 82% 20%, rgba(66,133,244,.07), transparent 32%), #fff; color: #0b1736; display: grid; grid-template-rows: auto 1fr auto auto; padding: max(18px, env(safe-area-inset-top)) clamp(22px,5vw,64px) max(12px, env(safe-area-inset-bottom)); }
.home-header-approved { display:flex; align-items:flex-start; justify-content:space-between; gap:16px; min-height:76px; }
.home-brand-approved { display:flex; align-items:center; gap:14px; }
.dyslex-logo { width:54px; height:54px; position:relative; border-radius:14px; background: conic-gradient(from 180deg,#34a853 0 25%,#4285f4 0 50%,#ea4335 0 75%,#fbbc04 0); clip-path: polygon(0 0,56% 0,100% 32%,100% 68%,56% 100%,0 100%,0 72%,50% 72%,72% 58%,72% 42%,50% 28%,0 28%); box-shadow:0 8px 20px rgba(66,133,244,.12); }
.dyslex-logo span { position:absolute; inset:14px 18px 14px 14px; background:#fff; border-radius:8px; }
.home-brand-title-approved { font-size:clamp(2rem,4vw,2.8rem); line-height:1; font-weight:850; letter-spacing:-.04em; color:#08152f; }
.home-brand-title-approved span { color:#1a73e8; }
.home-brand-subtitle-approved { margin-top:8px; color:#667085; font-size:clamp(1rem,1.8vw,1.18rem); line-height:1.1; }
.home-top-actions { display:flex; align-items:center; gap:14px; }
.home-help-btn-approved,.home-aa-btn,.home-history-btn { border:1px solid #dfe7f2; background:#fff; color:#0b1736; box-shadow:0 6px 16px rgba(15,23,42,.04); }
.home-help-btn-approved,.home-aa-btn { width:54px; height:54px; border-radius:999px; font-size:1.55rem; font-weight:800; }
.home-aa-btn { display:none; color:#fff; border:0; background:linear-gradient(135deg,#6f8cff,#6256f6); font-size:1rem; }
.home-history-btn { display:none; height:48px; border-radius:14px; padding:0 18px; gap:8px; align-items:center; font-weight:700; }
.home-main-approved { display:grid; grid-template-columns:1fr; align-content:start; gap:clamp(16px,2.8vh,28px); min-height:0; }
.home-copy-approved { display:flex; flex-direction:column; align-items:flex-start; min-width:0; }
.home-badge-approved { display:inline-flex; align-items:center; gap:6px; padding:7px 14px; border-radius:999px; margin:clamp(12px,2.4vh,30px) 0 clamp(20px,3.2vh,34px); background:#e8f0fe; color:#1a73e8; font-weight:750; font-size:clamp(.95rem,1.8vw,1.12rem); }
.home-copy-approved h1 { margin:0; max-width:9.8ch; color:#08152f; font-size:clamp(3.1rem,10vw,5.25rem); line-height:1.04; letter-spacing:-.055em; font-weight:850; }
.home-copy-approved p { margin:clamp(20px,3vh,30px) 0 clamp(26px,4vh,42px); max-width:560px; color:#667085; font-size:clamp(1.05rem,2.7vw,1.42rem); line-height:1.45; }
.home-actions-approved { display:grid; gap:clamp(14px,2vh,26px); align-content:start; }
.home-choice,.recent-card,.privacy-card { width:100%; border:1px solid #e3eaf4; background:rgba(255,255,255,.92); border-radius:28px; display:grid; grid-template-columns:clamp(94px,18vw,132px) 1fr 56px; align-items:center; gap:clamp(18px,3vw,28px); padding:clamp(18px,3.2vw,26px); text-align:left; box-shadow:0 12px 30px rgba(15,23,42,.07); cursor:pointer; color:#0b1736; }
.home-choice-image { border-left:4px solid #1a73e8; }
.home-choice-audio { border-left:4px solid #34a853; }
.home-choice-icon,.recent-icon { width:clamp(88px,17vw,124px); height:clamp(88px,17vw,124px); border-radius:24px; display:grid; place-items:center; font-size:clamp(2.8rem,6vw,4rem); background:linear-gradient(135deg,#dff0ff,#f5fbff); }
.home-choice-icon-audio { background:linear-gradient(135deg,#ddf8e5,#f7fff8); color:#24a148; }
.home-choice-icon-round { border-radius:999px; border:2px dashed #1a73e8; background:#f3f7ff; }
.home-choice-text { display:flex; min-width:0; flex-direction:column; gap:10px; }
.home-choice-text strong,.recent-card strong,.privacy-card strong { color:#08152f; font-size:clamp(1.45rem,3.2vw,2rem); line-height:1.15; font-weight:850; }
.home-choice-text small,.recent-card small,.privacy-card small { color:#667085; font-size:clamp(1rem,2.4vw,1.28rem); line-height:1.4; font-weight:500; }
.home-choice-arrow { width:54px; height:54px; border-radius:999px; display:grid; place-items:center; background:#eef4ff; color:#1a73e8; font-size:2.3rem; line-height:1; font-weight:500; }
.home-choice-audio .home-choice-arrow { background:#eaf8ed; color:#24a148; }
.home-choice-audio-top,.home-feature-row { display:none; }
.home-recent-approved { display:grid; gap:12px; margin-top:clamp(10px,1.4vh,18px); }
.home-recent-approved>span { color:#667085; font-size:1rem; font-weight:700; }
.recent-card,.privacy-card { grid-template-columns:54px 1fr 38px; border-radius:18px; padding:12px 14px; gap:14px; box-shadow:none; }
.recent-icon { width:54px; height:54px; border-radius:14px; font-size:1.6rem; }
.recent-card strong,.privacy-card strong { font-size:1rem; }
.recent-card small,.privacy-card small { font-size:.92rem; display:block; margin-top:3px; }
.privacy-card { background:#edf5ff; }
.home-nav-approved { margin-top:14px; height:76px; border-top:1px solid #edf1f6; display:grid; grid-template-columns:repeat(3,1fr); align-items:center; gap:6px; }
.home-nav-approved button { border:0; background:transparent; color:#56657a; display:grid; gap:4px; place-items:center; font-size:1.9rem; font-weight:700; }
.home-nav-approved button span { font-size:.95rem; }
.home-nav-approved button.active { color:#1a73e8; }
.home-nav-approved button:nth-child(4) { display:none; }
@media (max-width:430px){ .home-app{padding-inline:18px}.home-copy-approved h1{font-size:clamp(2.75rem,12vw,3.8rem)}.home-copy-approved p{font-size:1rem;margin-block:16px 20px}.home-choice{grid-template-columns:76px 1fr 42px;gap:12px;padding:14px;border-radius:22px}.home-choice-icon{width:76px;height:76px;border-radius:18px;font-size:2.3rem}.home-choice-arrow{width:40px;height:40px;font-size:1.8rem}.home-choice-text strong{font-size:1.22rem}.home-choice-text small{font-size:.95rem}.home-recent-approved{gap:10px} }
@media (max-height:840px) and (orientation:portrait){ .home-app{padding-top:12px}.home-header-approved{min-height:62px}.dyslex-logo{width:46px;height:46px}.home-help-btn-approved{width:46px;height:46px}.home-badge-approved{margin:10px 0 14px}.home-copy-approved h1{font-size:clamp(2.7rem,9vw,3.8rem)}.home-copy-approved p{margin:14px 0 18px;line-height:1.34}.home-choice{padding:13px}.home-choice-icon{width:74px;height:74px}.home-recent-approved{display:none} }
@media (orientation:landscape) and (min-width:780px){ .app-shell{max-width:none;padding:0}.screen-center.home-screen{min-height:100svh}.home-app{padding:max(18px,env(safe-area-inset-top)) clamp(48px,5vw,86px) max(12px,env(safe-area-inset-bottom));grid-template-rows:auto 1fr auto}.home-header-approved{min-height:78px}.home-help-btn-approved{display:none}.home-history-btn,.home-aa-btn{display:inline-flex}.home-main-approved{grid-template-columns:minmax(360px,.88fr) minmax(470px,1fr);align-items:center;gap:clamp(42px,7vw,92px)}.home-badge-approved{margin:0 0 24px}.home-copy-approved{padding-left:clamp(16px,5vw,90px)}.home-copy-approved h1{font-size:clamp(3.8rem,5.6vw,5.7rem)}.home-copy-approved p{font-size:clamp(1.08rem,1.55vw,1.38rem);max-width:540px;margin:24px 0 28px}.home-choice-image{max-width:620px}.home-actions-approved{padding-right:clamp(16px,4vw,70px);gap:34px}.home-choice-audio-top{display:grid;background:#f4f8ff;border-left:0}.home-choice-audio-main{background:#fbfffc;border:1px solid #b9e7c5}.home-choice{grid-template-columns:120px 1fr 62px;padding:30px 34px;border-radius:22px}.home-choice-icon{width:110px;height:110px;font-size:3.6rem}.home-choice-text strong{font-size:clamp(1.45rem,2vw,2rem)}.home-choice-text small{font-size:clamp(1rem,1.25vw,1.24rem)}.home-feature-row{display:flex;align-items:center;justify-content:space-between;gap:10px;padding:12px 16px;border-radius:18px;background:#fff;box-shadow:0 10px 28px rgba(15,23,42,.07);color:#667085;font-size:clamp(.72rem,.85vw,.9rem)}.home-feature-row span{white-space:nowrap}.home-feature-row b{color:#1f2937}.home-recent-approved{display:none}.home-nav-approved{width:min(92%,1480px);justify-self:center;height:84px;margin-top:18px;border:1px solid #edf1f6;border-radius:999px;box-shadow:0 10px 28px rgba(15,23,42,.06);grid-template-columns:repeat(4,1fr)}.home-nav-approved button:nth-child(4){display:grid} }
@media (orientation:landscape) and (max-height:650px){ .home-app{padding-block:12px 8px}.home-header-approved{min-height:58px}.dyslex-logo{width:44px;height:44px}.home-brand-title-approved{font-size:2rem}.home-brand-subtitle-approved{margin-top:4px}.home-copy-approved h1{font-size:clamp(3.2rem,5vw,4.4rem)}.home-copy-approved p{margin:16px 0 18px;line-height:1.32}.home-choice{padding:18px 22px;grid-template-columns:90px 1fr 52px}.home-choice-icon{width:84px;height:84px;font-size:2.8rem}.home-actions-approved{gap:18px}.home-feature-row{display:none}.home-nav-approved{height:64px;margin-top:8px}.home-nav-approved button{font-size:1.45rem}.home-nav-approved button span{font-size:.82rem} }

</style>