<template>
  <div class="player-app" :class="[viewportClasses, { 'is-entry-screen': screen === 'home', 'is-app-screen': screen !== 'home' }]">
    <!-- Native-only gate: blocks the app until the Gemma 4 LiteRT-LM file exists and the runtime is ready. -->
    <section v-if="nativeModelGateVisible" class="model-setup-view" :aria-label="t('model.preparing')">
      <div class="model-setup-panel">
        <div class="model-setup-brand">
          <div class="app-entry-logo" aria-hidden="true">
            <span class="app-entry-logo-blue"></span>
            <span class="app-entry-logo-red"></span>
            <span class="app-entry-logo-yellow"></span>
            <span class="app-entry-logo-green"></span>
          </div>
          <div>
            <div class="model-setup-eyebrow">DyslexAI</div>
            <h1>{{ modelSetupTitle }}</h1>
          </div>
        </div>

        <p class="model-setup-copy">{{ t('model.firstLaunch') }}</p>
        <p class="model-setup-copy">{{ t('model.wifi') }}</p>

        <div class="model-progress-block" role="status" aria-live="polite">
          <div class="model-progress-header">
            <span>{{ modelSetupMessage }}</span>
            <strong>{{ modelProgressPercent }}%</strong>
          </div>
          <div class="model-progress-track" aria-hidden="true">
            <div class="model-progress-fill" :style="{ width: `${modelProgressPercent}%` }"></div>
          </div>
          <div class="model-progress-meta">
            {{ modelProgressLabel }}
          </div>
        </div>

        <button v-if="modelStatus === 'error'" class="model-retry-button" @click="retryModelDownload">
          {{ t('model.retry') }}
        </button>
      </div>
    </section>

    <main class="app-shell">
      <section v-if="screen === 'home'" class="app-entry-view" :aria-label="t('home.modesLabel')">
        <header class="app-entry-header">
          <div class="app-entry-brand">
            <div class="app-entry-logo" aria-hidden="true">
              <span class="app-entry-logo-blue"></span>
              <span class="app-entry-logo-red"></span>
              <span class="app-entry-logo-yellow"></span>
              <span class="app-entry-logo-green"></span>
            </div>
            <div class="app-entry-brand-copy">
              <div class="app-entry-brand-title">Dyslex<span>AI</span></div>
              <div class="app-entry-brand-subtitle">{{ t('app.subtitle') }}</div>
            </div>
          </div>

          <div class="app-entry-actions">
            <LanguageToggle />
            <button class="app-entry-home-button" :title="t('app.home')" :aria-label="t('app.home')" @click="goHome">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6h-4v6H5a1 1 0 0 1-1-1z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round" />
            </svg>
            </button>
          </div>
        </header>

        <main class="app-entry-main">
          <section class="app-entry-intro">
            <div class="app-entry-badge">{{ t('home.badge') }}</div>
            <h1>{{ t('home.title') }}</h1>
            <p>{{ t('home.description') }}</p>
          </section>

          <section class="app-entry-options" :aria-label="t('home.modesLabel')">
            <button class="app-entry-option app-entry-option-blue" @click="startImageFlow">
              <div class="app-entry-option-icon" aria-hidden="true">
                <svg viewBox="0 0 96 96" role="img" focusable="false">
                  <rect x="6" y="8" width="84" height="80" rx="18" fill="#dff1ff" />
                  <circle cx="68" cy="30" r="9" fill="#fbbc04" />
                  <path d="M12 70l23-27 18 20 11-13 20 20v10H12z" fill="#34a853" />
                  <path d="M12 69l26-31 24 31z" fill="#2f80ed" opacity=".9" />
                  <path d="M42 69l19-23 24 23z" fill="#1a73e8" opacity=".78" />
                  <path d="M16 34c7-11 21-11 28 0 6-5 15-3 19 4H16z" fill="#fff" opacity=".92" />
                </svg>
              </div>
              <div class="app-entry-option-copy">
                <strong>{{ t('home.assistedTitle') }}</strong>
                <span>{{ t('home.assistedDescription') }}</span>
              </div>
              <div class="app-entry-option-arrow" aria-hidden="true">&gt;</div>
            </button>

            <button class="app-entry-option app-entry-option-green" @click="startAudioFlow">
              <div class="app-entry-option-icon" aria-hidden="true">
                <svg viewBox="0 0 96 96" role="img" focusable="false">
                  <rect x="6" y="8" width="84" height="80" rx="18" fill="#e5f6e8" />
                  <rect x="37" y="18" width="22" height="42" rx="11" fill="#34a853" />
                  <path d="M26 44c0 14 9 25 22 25s22-11 22-25" fill="none" stroke="#34a853" stroke-width="7" stroke-linecap="round" />
                  <path d="M48 69v13M34 82h28" stroke="#34a853" stroke-width="7" stroke-linecap="round" />
                  <path d="M61 31h8M61 43h8" stroke="#34a853" stroke-width="5" stroke-linecap="round" opacity=".75" />
                </svg>
              </div>
              <div class="app-entry-option-copy">
                <strong>{{ t('home.speechTitle') }}</strong>
                <span>{{ t('home.speechDescription') }}</span>
              </div>
              <div class="app-entry-option-arrow" aria-hidden="true">&gt;</div>
            </button>
          </section>
        </main>
      </section>

      <ImageSourceView
        v-else-if="screen === 'select-image-source'"
        @take-photo="takePhoto"
        @pick-gallery="pickFromGallery"
        @go-home="goHome"
      />

      <ImageConfirmView
        v-else-if="screen === 'confirm-image'"
        :preview-url="previewUrl"
        @process-image="processImage"
        @choose-other="startImageFlow"
        @go-home="goHome"
      />

      <AudioPrepareView
        v-else-if="screen === 'confirm-audio'"
        v-model:reading-age-group="readingAgeGroup"
        v-model:reading-level="readingLevel"
        v-model:reading-type="readingType"
        v-model:show-syllables="showSyllables"
        :audio-preview-url="audioPreviewUrl"
        :expected-reading-text="expectedReadingText"
        :has-recorded-audio="hasRecordedAudio"
        :is-generating-phrase="isGeneratingPhrase"
        :is-recorder-busy="isRecorderBusy"
        :is-recording="isRecording"
        :recording-elapsed-label="recordingElapsedLabel"
        :selected-audio-name="selectedAudioName"
        @clear-recorded-audio="clearRecordedAudio"
        @generate-phrase="generateReadingPhrase"
        @go-home="goHome"
        @process-audio="processAudio"
        @start-recording="startRecording"
        @stop-recording="stopRecording"
      />

      <ProcessingView
        v-else-if="screen === 'processing'"
        :active-flow="activeFlow"
        :processing-title="processingTitle"
        :processing-message="processingMessage"
        :processing-progress="processingProgress"
        @go-home="goHome"
      />

      <ReaderView
        v-else
        v-model:auto-advance-line="autoAdvanceLine"
        v-model:font-size="fontSize"
        v-model:line-playback-mode="linePlaybackMode"
        v-model:reading-palette="readingPalette"
        v-model:show-settings="showSettings"
        v-model:speech-rate="speechRate"
        :active-flow="activeFlow"
        :active-word-style="activeWordStyle"
        :audio-issues="audioIssues"
        :audio-feedback-text="audioFeedbackText"
        :computed-line-font-size="computedLineFontSize"
        :computed-word-context-font-size="computedWordContextFontSize"
        :current-line-index="currentLineIndex"
        :current-lines="currentLines"
        :current-text-mode="currentTextMode"
        :current-word-index="currentWordIndex"
        :current-words="currentWords"
        :expected-reading-text="expectedReadingText"
        :has-spoken-text="hasSpokenText"
        :is-fullscreen="isFullscreen"
        :is-playing-words="isPlayingWords"
        :is-speaking-line="isSpeakingLine"
        :line-progress-percent="lineProgressPercent"
        :palette-styles="paletteStyles"
        :reading-mode="readingMode"
        :show-syllables="showSyllables"
        :spoken-text="spokenText"
        :spoken-transcription="spokenTranscription"
        :word-audio-mode="wordAudioMode"
        @next-line="nextLine"
        @next-word="nextWord"
        @open-validation="openValidation = true"
        @prev-line="prevLine"
        @prev-word="prevWord"
        @reset-all="resetAll"
        @restart-reading="restartReading"
        @set-reading-mode="setReadingMode"
        @set-word-audio-mode="setWordAudioMode"
        @speak-text="speakTextAndWait"
        @stop-all-audio="stopAllAudio"
        @switch-text-mode="switchTextMode"
        @toggle-syllables="toggleSyllables"
        @toggle-fullscreen="toggleFullscreen"
        @toggle-line-reading="toggleLineReading"
        @toggle-word-play="toggleWordPlay"
      />
    </main>

    <input ref="imageInput" type="file" accept="image/*" hidden @change="onImageChange" />
    <input ref="audioInput" type="file" accept="audio/*,.wav,.mp3,.m4a,.ogg" hidden @change="onAudioChange" />

    <div v-if="openValidation" class="modal-overlay" @click.self="openValidation = false">
      <div class="modal-card">
        <div class="modal-header">
          <div>
            <div class="mini-label">{{ t('app.validation') }}</div>
            <h3>{{ t('app.fullText') }}</h3>
          </div>
          <button class="pill-btn" @click="openValidation = false">{{ t('app.close') }}</button>
        </div>

        <div class="modal-tabs">
          <button class="pill-btn" :class="{ active: currentTextMode === 'original' }"
            @click="switchTextMode('original')">{{ t('reader.original') }}</button>
          <button class="pill-btn" :class="{ active: currentTextMode === 'simplified' }"
            @click="switchTextMode('simplified')">{{ t('reader.simplified') }}</button>
          <button v-if="hasSpokenText" class="pill-btn" :class="{ active: currentTextMode === 'spoken' }"
            @click="switchTextMode('spoken')">{{ t('reader.spoken') }}</button>
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
import { DyslexAIPlugin } from './services/inference/capacitorDyslexAI'
import { useRoute, useRouter } from 'vue-router'
import { routeScreenNames } from './router'
import AudioPrepareView from './views/AudioPrepareView.vue'
import ImageConfirmView from './views/ImageConfirmView.vue'
import ImageSourceView from './views/ImageSourceView.vue'
import ProcessingView from './views/ProcessingView.vue'
import ReaderView from './views/ReaderView.vue'
import LanguageToggle from './components/common/LanguageToggle.vue'
import { t } from './i18n'

const inference = createInferenceService()
const route = useRoute()
const router = useRouter()

let warmed = false
let modelProgressListener = null

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

// Model setup states are driven by the Android Capacitor plugin: checking, downloading, installed, error.
const modelStatus = ref('checking')
const modelProgressPercent = ref(0)
const modelDownloadedMb = ref(0)
const modelTotalMb = ref(0)
const modelSetupMessage = ref(t('model.preparing'))
const modelErrorMessage = ref('')
const modelReadyForApp = ref(false)
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
const audioComparisonSummary = ref('')
const audioPositiveFeedback = ref('')
const audioImprovementTip = ref('')
const isGeneratingPhrase = ref(false)
const isRecording = ref(false)
const isRecorderBusy = ref(false)
const recordingElapsedMs = ref(0)

const nativeModelGateVisible = computed(() => {
  return isNativeAndroid() && !modelReadyForApp.value
})

const modelSetupTitle = computed(() => {
  if (modelStatus.value === 'downloading') return t('model.preparing')
  if (modelStatus.value === 'error') return modelSetupMessage.value || t('model.downloadFailed')
  return t('model.preparing')
})

const modelProgressLabel = computed(() => {
  if (modelStatus.value === 'error') {
    return modelErrorMessage.value || t('model.connectionHint')
  }

  const downloaded = formatModelSize(modelDownloadedMb.value)
  const total = modelTotalMb.value > 0 ? formatModelSize(modelTotalMb.value) : t('model.calculating')
  return `${downloaded} / ${total}`
})

const activeFlow = ref('image')
const openValidation = ref(false)
const showSettings = ref(true)
const showSyllables = ref(false)
const isFullscreen = ref(false)
const isLandscape = ref(false)
const viewportSize = ref('medium')

function setScreen(nextScreen) {
  const safeScreen = routeScreenNames.includes(nextScreen) ? nextScreen : 'home'
  screen.value = safeScreen
  refreshLayoutAfterPaint()

  if (route.name !== safeScreen) {
    router.push({ name: safeScreen }).catch(() => { })
  }
}

watch(() => route.name, (routeName) => {
  if (routeScreenNames.includes(routeName) && screen.value !== routeName) {
    screen.value = routeName
    refreshLayoutAfterPaint()
  }
}, { immediate: true })

const imageInput = ref(null)
const audioInput = ref(null)

const readingPalette = ref('yellow')
const fontSize = ref(18)
const readingMode = ref('line')
const currentTextMode = ref('simplified')
const wordAudioMode = ref('audio')
const linePlaybackMode = ref('single')

const currentLineIndex = ref(0)
const currentWordIndex = ref(0)

const isPlayingWords = ref(false)
const isSpeakingLine = ref(false)
const autoAdvanceLine = ref(true)
const speechRate = ref(1.0)

const processingProgress = ref(0)
const processingMessage = ref(t('processing.preparing'))
const processingTitle = ref(t('processing.titleImage'))

const imageOptimizationMaxEdge = 900
const imageOptimizationQuality = 0.72

let playTimer = null
let wordPlaybackStopped = false
let mediaRecorder = null
let mediaStream = null
let recordingMimeType = ''
let recordedChunks = []
let recordingTimer = null
let recordingStartedAt = 0

const correctedText = ref('')
const simplifiedText = ref('')
const spokenText = ref('')
const spokenTranscription = ref('')

const originalLines = ref([])
const simplifiedLines = ref([])
const spokenLines = ref([])
const syllabifiedOriginalText = ref('')
const syllabifiedSimplifiedText = ref('')
const syllabifiedSpokenText = ref('')

const hasSpokenText = computed(() => spokenLines.value.length > 0 || !!spokenText.value)
const hasRecordedAudio = computed(() => !!selectedAudioFile.value)

const audioFeedbackText = computed(() => {
  return [
    audioComparisonSummary.value,
    audioPositiveFeedback.value,
    audioImprovementTip.value,
  ]
    .map(part => cleanAudioText(part))
    .filter(Boolean)
    .join(' ')
})

const recordingElapsedLabel = computed(() => formatElapsedTime(recordingElapsedMs.value))

const currentLines = computed(() => {
  if (!showSyllables.value) {
    if (currentTextMode.value === 'spoken') return spokenLines.value
    return currentTextMode.value === 'original' ? originalLines.value : simplifiedLines.value
  }

  if (currentTextMode.value === 'spoken') {
    return syllabifyDisplayLines(
      spokenLines.value,
      syllabifiedSpokenText.value,
      spokenText.value || spokenTranscription.value
    )
  }

  if (currentTextMode.value === 'original') {
    return syllabifyDisplayLines(
      originalLines.value,
      syllabifiedOriginalText.value,
      correctedText.value
    )
  }

  return syllabifyDisplayLines(
    simplifiedLines.value,
    syllabifiedSimplifiedText.value,
    simplifiedText.value
  )
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
    yellow: { lineText: '#FFFFFF', inactiveWord: '#172033', activeBg: 'rgba(255, 214, 53, 0.95)', activeText: '#231900' },
    blue: { lineText: '#F8FBFF', inactiveWord: '#172033', activeBg: '#8FD3FF', activeText: '#0B1F2A' },
    green: { lineText: '#F7FFF8', inactiveWord: '#172033', activeBg: '#9BE7B1', activeText: '#102217' },
    pink: { lineText: '#FFF8FC', inactiveWord: '#172033', activeBg: '#FFB8D2', activeText: '#32111F' }
  }
  return palettes[readingPalette.value] || palettes.yellow
})

const lineProgressPercent = computed(() => {
  if (!currentLines.value.length) return 0
  return ((currentLineIndex.value + 1) / currentLines.value.length) * 100
})

const currentLineTextLength = computed(() => String(currentLines.value[currentLineIndex.value] || '').length)
const longLineFontReduction = computed(() => {
  const length = currentLineTextLength.value
  if (length > 140) return 6
  if (length > 100) return 5
  if (length > 70) return 4
  if (length > 48) return 2
  return 0
})
const computedLineFontSize = computed(() => {
  const base = isLandscape.value ? Math.max(14, Math.min(fontSize.value - 2, 18)) : Math.max(15, Math.min(fontSize.value, 20))
  return Math.max(isLandscape.value ? 12 : 14, base - longLineFontReduction.value)
})
const computedWordContextFontSize = computed(() => {
  const base = isLandscape.value ? Math.max(12, Math.min(fontSize.value - 5, 15)) : Math.max(13, Math.min(fontSize.value - 3, 17))
  return Math.max(isLandscape.value ? 11 : 12, base - Math.ceil(longLineFontReduction.value / 2))
})
const activeWordStyle = computed(() => ({}))
const viewportClasses = computed(() => ({
  fullscreen: isFullscreen.value,
  'layout-landscape': isLandscape.value,
  'layout-portrait': !isLandscape.value,
  [`layout-${viewportSize.value}`]: true,
}))

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
  const width = window.innerWidth || 0
  const height = window.innerHeight || 0
  const landscape = width > height
  const longSide = Math.max(width, height)
  const shortSide = Math.min(width, height)

  isLandscape.value = landscape

  if (landscape) {
    if (shortSide < 331 || longSide < 620) {
      viewportSize.value = 'small'
    } else if (longSide < 980 || shortSide < 520) {
      viewportSize.value = 'medium'
    } else {
      viewportSize.value = 'large'
    }
    return
  }

  if (width < 430) {
    viewportSize.value = 'small'
  } else if (width < 820) {
    viewportSize.value = 'medium'
  } else {
    viewportSize.value = 'large'
  }
}

function refreshLayoutAfterPaint() {
  window.requestAnimationFrame(() => {
    updateLayoutFlags()
    window.requestAnimationFrame(updateLayoutFlags)
  })
}

function isNativeAndroid() {
  return Capacitor.isNativePlatform() && Capacitor.getPlatform() === 'android'
}

function formatModelSize(sizeMb) {
  const value = Number(sizeMb || 0)
  if (value >= 1024) {
    return `${(value / 1024).toFixed(1)} GB`
  }
  return `${value.toFixed(1)} MB`
}

function applyModelProgress(payload = {}) {
  const status = payload.status || 'checking'
  modelStatus.value = status
  modelProgressPercent.value = Math.max(0, Math.min(100, Number(payload.percent || 0)))
  modelDownloadedMb.value = Number(payload.downloadedMb || 0)
  modelTotalMb.value = Number(payload.totalMb || 0)

  if (payload.message && payload.message !== 'Downloading model...' && payload.message !== 'Model installed successfully') {
    modelSetupMessage.value = payload.message
  } else if (status === 'downloading') {
    modelSetupMessage.value = t('model.downloading')
  } else if (status === 'installed') {
    modelSetupMessage.value = t('model.installed')
  }

  if (status === 'error') {
    modelErrorMessage.value = payload.error || t('model.connectionHint')
  }
}

async function prepareNativeModel() {
  if (!isNativeAndroid()) {
    return
  }

  modelStatus.value = 'checking'
  modelReadyForApp.value = false
  modelSetupMessage.value = t('model.preparing')
  modelErrorMessage.value = ''

  try {
    const state = await DyslexAIPlugin.getModelState()
    applyModelProgress(state)

    const result = await DyslexAIPlugin.ensureModelReady()
    applyModelProgress({
      ...(result?.model || {}),
      status: 'installed',
      percent: 100,
      message: t('model.installed'),
    })

    warmed = true
    window.setTimeout(() => {
      modelReadyForApp.value = true
      refreshLayoutAfterPaint()
    }, 900)
  } catch (error) {
    modelStatus.value = 'error'
    modelSetupMessage.value = error?.message?.includes('runtime failed')
      ? t('model.failed')
      : t('model.downloadFailed')
    modelErrorMessage.value = error?.message || t('model.connectionHint')
  }
}

function retryModelDownload() {
  prepareNativeModel()
}

function startImageFlow() {
  activeFlow.value = 'image'
  selectedFile.value = null
  selectedImageBase64.value = ''
  selectedImageMimeType.value = 'image/jpeg'
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  setScreen('select-image-source')
}
async function setSelectedImageFromDataUrl(dataUrl, mimeType = 'image/jpeg') {
  let preparedImage

  if (isNativeAndroid()) {
    try {
      const result = await DyslexAIPlugin.prepareImage({
        imageBase64: dataUrl,
        mode: 'balanced',
      })

      preparedImage = {
        dataUrl: result.imageBase64,
        mimeType: result.mimeType || 'image/jpeg',
      }
    } catch (error) {
      console.warn('[DyslexAI] Falhou pré-processamento Android. A usar fallback Canvas.', error)
      preparedImage = await optimizeImageForInference(dataUrl)
    }
  } else {
    preparedImage = await optimizeImageForInference(dataUrl)
  }

  selectedImageBase64.value = preparedImage.dataUrl
  selectedImageMimeType.value = preparedImage.mimeType || mimeType || 'image/jpeg'

  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = preparedImage.dataUrl

  selectedFile.value = { name: 'imagem-selecionada', type: selectedImageMimeType.value }
  setScreen('confirm-image')
}
function optimizeImageForInference(dataUrl) {
  return new Promise((resolve) => {
    const image = new Image()

    image.onload = () => {
      const sourceWidth = image.naturalWidth || image.width
      const sourceHeight = image.naturalHeight || image.height

      if (!sourceWidth || !sourceHeight) {
        resolve({ dataUrl, mimeType: 'image/jpeg' })
        return
      }

      const scale = Math.min(1, imageOptimizationMaxEdge / Math.max(sourceWidth, sourceHeight))
      const targetWidth = Math.max(1, Math.round(sourceWidth * scale))
      const targetHeight = Math.max(1, Math.round(sourceHeight * scale))

      const canvas = document.createElement('canvas')
      canvas.width = targetWidth
      canvas.height = targetHeight

      const context = canvas.getContext('2d', { alpha: false })
      if (!context) {
        resolve({ dataUrl, mimeType: 'image/jpeg' })
        return
      }

      context.fillStyle = '#ffffff'
      context.fillRect(0, 0, targetWidth, targetHeight)
      context.drawImage(image, 0, 0, targetWidth, targetHeight)

      const optimizedDataUrl = canvas.toDataURL('image/jpeg', imageOptimizationQuality)

      resolve({ dataUrl: optimizedDataUrl, mimeType: 'image/jpeg' })
    }

    image.onerror = () => {
      console.warn('[DyslexAI] Não foi possível otimizar a imagem; será usada a imagem original.')
      resolve({ dataUrl, mimeType: 'image/jpeg' })
    }

    image.src = dataUrl
  })
}

async function takePhoto() {
  try {
    const image = await Camera.getPhoto({
      quality: 90,
      width: 1800,
      resultType: CameraResultType.DataUrl,
      source: CameraSource.Camera,
      allowEditing: false,
      correctOrientation: true,
    })

    if (!image.dataUrl) throw new Error(t('errors.photoEmpty'))

    const mimeType = `image/${image.format || 'jpeg'}`
    await setSelectedImageFromDataUrl(image.dataUrl, mimeType)
  } catch (error) {
    console.error('[DyslexAI] Erro ao tirar fotografia:', error)
    if (error?.message !== 'User cancelled photos app') {
      alert(t('errors.photoFailed'))
    }
  }
}

async function pickFromGallery() {
  try {
    const image = await Camera.getPhoto({
      quality: 90,
      width: 2200,
      resultType: CameraResultType.DataUrl,
      source: CameraSource.Photos,
      allowEditing: false,
      correctOrientation: true,
    })

    if (!image.dataUrl) throw new Error(t('errors.imageEmpty'))

    const mimeType = `image/${image.format || 'jpeg'}`
    await setSelectedImageFromDataUrl(image.dataUrl, mimeType)
  } catch (error) {
    console.error('[DyslexAI] Erro ao escolher imagem:', error)
    if (error?.message !== 'User cancelled photos app') {
      alert(t('errors.pickImageFailed'))
    }
  }
}

async function startAudioFlow() {
  activeFlow.value = 'audio'
  clearRecordedAudio()
  audioIssues.value = []
  clearAudioFeedback()
  spokenText.value = ''
  spokenTranscription.value = ''
  expectedReadingText.value = ''
  syllabifiedOriginalText.value = ''
  syllabifiedSimplifiedText.value = ''
  syllabifiedSpokenText.value = ''
  setScreen('confirm-audio')

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
    alert(t('errors.phraseFirst'))
    return
  }

  if (isNativeAndroid() && inference.startWavRecording) {
    try {
      isRecorderBusy.value = true
      clearRecordedAudio()

      await inference.startWavRecording()

      isRecording.value = true
      startRecordingTimer()
      selectedAudioName.value = 'gravacao-aluno.wav'
      recordingMimeType = 'audio/wav'

      return
    } catch (error) {
      console.error('[DyslexAI] Erro ao iniciar gravação WAV nativa:', error)
      alert(error?.message || t('errors.recordingStartFailed'))
      isRecording.value = false
      stopRecordingTimer()
      return
    } finally {
      isRecorderBusy.value = false
    }
  }

  if (!(navigator.mediaDevices && window.MediaRecorder)) {
    alert(t('errors.recordingUnsupported'))
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
      startRecordingTimer()
      selectedAudioName.value = 'gravacao-aluno.webm'
    }

    mediaRecorder.onerror = (event) => {
      console.error('Erro na gravação:', event)
      alert(t('errors.recordingFailed'))
      stopMediaTracks()
      isRecording.value = false
      stopRecordingTimer()
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
      stopRecordingTimer()
    }

    mediaRecorder.start()
  } catch (error) {
    console.error(error)
    alert(t('errors.microphoneFailed'))
    stopMediaTracks()
    isRecording.value = false
    stopRecordingTimer()
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
        throw new Error(t('errors.nativeAudioEmpty'))
      }

      const audioBlob = dataUrlToBlob(result.audioBase64, result.mimeType || 'audio/wav')
      selectedAudioFile.value = new File([audioBlob], result.filename || 'gravacao-aluno.wav', {
        type: result.mimeType || 'audio/wav',
      })

      selectedAudioName.value = selectedAudioFile.value.name

      if (audioPreviewUrl.value) URL.revokeObjectURL(audioPreviewUrl.value)
      audioPreviewUrl.value = URL.createObjectURL(audioBlob)

      isRecording.value = false
      stopRecordingTimer()
      recordingMimeType = 'audio/wav'
      return
    } catch (error) {
      console.error('[DyslexAI] Erro ao parar gravação WAV nativa:', error)
      alert(t('errors.recordingStopFailed'))
      isRecording.value = false
      stopRecordingTimer()
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
    throw new Error(t('errors.invalidDataUrl'))
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
  resetRecordingTimer()
  recordedChunks = []
  audioIssues.value = []
  clearAudioFeedback()
}

function startRecordingTimer() {
  stopRecordingTimer(false)
  recordingStartedAt = Date.now()
  recordingElapsedMs.value = 0
  recordingTimer = window.setInterval(() => {
    recordingElapsedMs.value = Date.now() - recordingStartedAt
  }, 250)
}

function stopRecordingTimer(reset = false) {
  if (recordingTimer) {
    window.clearInterval(recordingTimer)
    recordingTimer = null
  }
  if (recordingStartedAt && !reset) {
    recordingElapsedMs.value = Date.now() - recordingStartedAt
  }
  if (reset) {
    recordingElapsedMs.value = 0
  }
  recordingStartedAt = 0
}

function resetRecordingTimer() {
  stopRecordingTimer(true)
}

function formatElapsedTime(milliseconds) {
  const totalSeconds = Math.max(0, Math.floor(Number(milliseconds || 0) / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
}

async function generateReadingPhrase() {
  try {
    isGeneratingPhrase.value = true
    expectedReadingText.value = ''
    syllabifiedOriginalText.value = ''
    syllabifiedSimplifiedText.value = ''
    syllabifiedSpokenText.value = ''
    clearRecordedAudio()
    audioIssues.value = []
    clearAudioFeedback()
    spokenText.value = ''
    spokenTranscription.value = ''

    const result = await inference.generateReadingPhrase({
      ageGroup: readingAgeGroup.value,
      level: readingLevel.value,
      type: readingType.value,
    })

    expectedReadingText.value = (result?.text || '').trim()
    syllabifiedOriginalText.value = syllabifyText(expectedReadingText.value)
    syllabifiedSimplifiedText.value = syllabifiedOriginalText.value

    if (!expectedReadingText.value) {
      throw new Error(t('errors.phraseFailed'))
    }
  } catch (error) {
    console.error('[DyslexAI] Erro ao gerar frase:', error)
    alert(error?.message || t('errors.phraseGeneric'))
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
    await setSelectedImageFromDataUrl(dataUrl, selectedImageMimeType.value)
  } catch (error) {
    console.error('[DyslexAI] Erro ao ler imagem selecionada:', error)
    alert(t('errors.readImageFailed'))
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
  setScreen('confirm-audio')
}

async function processRealImage() {
  if (!selectedImageBase64.value) {
    throw new Error(t('errors.noPreparedImage'))
  }

  const data = await inference.processImage({
    imageBase64: selectedImageBase64.value,
    mimeType: selectedImageMimeType.value,
  })

  correctedText.value = data.original_text || ''
  simplifiedText.value = data.simplified_text || ''
  originalLines.value = Array.isArray(data.original_lines) ? data.original_lines : []
  simplifiedLines.value = Array.isArray(data.simplified_lines) ? data.simplified_lines : []
  syllabifiedOriginalText.value = syllabifyText(correctedText.value)
  syllabifiedSimplifiedText.value = syllabifyText(simplifiedText.value)
  syllabifiedSpokenText.value = ''
}

async function processRealAudio(file) {
  const data = await inference.processAudio(file, expectedReadingText.value)
  const normalized = normalizeAudioResult(data)

  if (normalized.no_speech_detected) {
    throw new NoSpeechDetectedError(
      normalized.comparison_summary || normalized.improvement_tip || t('errors.noSpeech')
    )
  }

  spokenTranscription.value = normalized.transcription
  spokenText.value = normalized.clean_text || normalized.spoken_text || normalized.transcription
  spokenLines.value = normalized.spoken_lines.length
    ? normalized.spoken_lines
    : (spokenText.value ? [spokenText.value] : [])
  audioIssues.value = normalized.issues
  audioComparisonSummary.value = normalized.comparison_summary
  audioPositiveFeedback.value = normalized.positive_feedback
  audioImprovementTip.value = normalized.improvement_tip
  syllabifiedOriginalText.value = normalized.syllabified_expected_text || syllabifyText(expectedReadingText.value)
  syllabifiedSpokenText.value = normalized.syllabified_spoken_text || syllabifyText(spokenText.value)

  correctedText.value = expectedReadingText.value || spokenText.value
  originalLines.value = correctedText.value ? [correctedText.value] : []
  simplifiedText.value = spokenText.value
  simplifiedLines.value = [...spokenLines.value]
  syllabifiedSimplifiedText.value = syllabifiedSpokenText.value
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
    no_speech_detected: result.no_speech_detected === true,
    comparison_summary: cleanAudioText(result.comparison_summary || result.feedback_comment || ''),
    positive_feedback: cleanAudioText(result.positive_feedback || ''),
    improvement_tip: cleanAudioText(result.improvement_tip || ''),
    syllabified_expected_text: cleanAudioText(result.syllabified_expected_text || ''),
    syllabified_spoken_text: cleanAudioText(result.syllabified_spoken_text || ''),
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

function clearAudioFeedback() {
  audioComparisonSummary.value = ''
  audioPositiveFeedback.value = ''
  audioImprovementTip.value = ''
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

function splitDisplayLines(text) {
  const cleaned = cleanAudioText(text)
  return cleaned ? cleaned.split(/\r?\n+/).map(line => line.trim()).filter(Boolean) : []
}

function syllabifyDisplayLines(lines, syllabifiedText, fallbackText) {
  if (Array.isArray(lines) && lines.length) {
    return lines.map(line => syllabifyText(line)).filter(Boolean)
  }

  return splitDisplayLines(syllabifiedText || syllabifyText(fallbackText))
}

function syllabifyText(text) {
  return String(text || '')
    .split(/(\s+)/)
    .map(part => /\s+/.test(part) ? part : syllabifyWord(part))
    .join('')
    .replace(/\s+([,.!?;:])/g, '$1')
    .trim()
}

function syllabifyWord(value) {
  const match = String(value || '').match(/^([^\p{L}\p{N}]*)([\p{L}\p{N}À-ÿ]+)([^\p{L}\p{N}]*)$/u)
  if (!match) return value

  const [, prefix, word, suffix] = match
  if (word.length <= 3) return value

  const vowels = 'aeiouáéíóúâêôãõàüAEIOUÁÉÍÓÚÂÊÔÃÕÀÜ'
  const chars = Array.from(word)
  const chunks = []
  let current = ''

  for (let index = 0; index < chars.length; index += 1) {
    const char = chars[index]
    const next = chars[index + 1] || ''
    const nextAfter = chars[index + 2] || ''
    const previous = chars[index - 1] || ''

    current += char

    const isVowel = vowels.includes(char)
    const nextIsVowel = vowels.includes(next)
    const nextAfterIsVowel = vowels.includes(nextAfter)
    const isLast = index === chars.length - 1
    const keepDiphthong = isVowel && nextIsVowel && /[iuoãeõáéíóúâêôãõ]/i.test(next)

    if (isLast) {
      chunks.push(current)
      break
    }

    if (isVowel && !keepDiphthong && next && !nextIsVowel && nextAfterIsVowel) {
      chunks.push(current)
      current = ''
      continue
    }

    if (!isVowel && vowels.includes(previous) && nextIsVowel && current.length > 1) {
      const consonant = current.slice(-1)
      chunks.push(current.slice(0, -1))
      current = consonant
    }
  }

  const result = chunks.filter(Boolean).join('-')
  return result.includes('-') ? `${prefix}${result}${suffix}` : value
}

async function processImage() {
  if (!selectedImageBase64.value) {
    alert(t('errors.noImageSelected'))
    return
  }

  try {
    setScreen('processing')
    processingTitle.value = t('processing.titleImage')
    processingProgress.value = 15
    processingMessage.value = t('processing.sendingImage')
    await new Promise(resolve => setTimeout(resolve, 180))
    processingProgress.value = 45
    processingMessage.value = t('processing.processingText')
    await processRealImage()
    processingProgress.value = 100
    processingMessage.value = t('processing.done')
    enterReader('simplified')
  } catch (error) {
    console.error(error)
    alert(error.message || t('errors.processImageFailed'))
    setScreen('confirm-image')
  }
}

async function processAudio() {
  if (!selectedAudioFile.value) {
    alert(t('errors.noRecording'))
    return
  }
  if (!expectedReadingText.value) {
    alert(t('errors.expectedPhraseFirst'))
    return
  }

  try {
    setScreen('processing')
    processingTitle.value = t('processing.titleAudio')
    processingProgress.value = 15
    processingMessage.value = t('processing.sendingAudio')
    await new Promise(resolve => setTimeout(resolve, 180))
    processingProgress.value = 55
    processingMessage.value = t('processing.transcribing')
    await processRealAudio(selectedAudioFile.value)
    processingProgress.value = 100
    processingMessage.value = t('processing.done')
    enterReader('spoken')
  } catch (error) {
    console.error(error)
    alert(error instanceof NoSpeechDetectedError
      ? error.message
      : (error.message || t('errors.processAudioFailed')))
    setScreen('confirm-audio')
  }
}

class NoSpeechDetectedError extends Error {
  constructor(message) {
    super(message)
    this.name = 'NoSpeechDetectedError'
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
  setScreen('reader')
}

function goHome() {
  stopAllAudio()
  setScreen('home')
}

function switchTextMode(mode) {
  currentTextMode.value = resolveAvailableTextMode(mode)
  currentLineIndex.value = 0
  currentWordIndex.value = 0
  stopAllAudio()
}

function toggleSyllables() {
  showSyllables.value = !showSyllables.value
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
    return inference.stopSpeaking().catch(() => { })
  }
  return Promise.resolve()
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
  if (canUseBrowserTts()) window.speechSynthesis.cancel()
  return stopNativeTts()
}

async function stopAllAudio() {
  wordPlaybackStopped = true
  if (playTimer) {
    clearTimeout(playTimer)
    playTimer = null
  }
  isPlayingWords.value = false
  await stopNativeTts()
  if (canUseBrowserTts()) window.speechSynthesis.cancel()
  isSpeakingLine.value = false
}

async function toggleLineReading() {
  if (isSpeakingLine.value) {
    stopAllAudio()
    return
  }
  await stopAllAudio()
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
  await stopAllAudio()
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
  setScreen('home')
  activeFlow.value = 'image'
  selectedFile.value = null
  selectedImageBase64.value = ''
  selectedImageMimeType.value = 'image/jpeg'
  clearRecordedAudio()
  isRecording.value = false
  isRecorderBusy.value = false
  resetRecordingTimer()
  correctedText.value = ''
  simplifiedText.value = ''
  spokenText.value = ''
  spokenTranscription.value = ''
  clearAudioFeedback()
  originalLines.value = []
  simplifiedLines.value = []
  spokenLines.value = []
  syllabifiedOriginalText.value = ''
  syllabifiedSimplifiedText.value = ''
  syllabifiedSpokenText.value = ''
  openValidation.value = false
  showSettings.value = true
  currentTextMode.value = 'simplified'
  readingPalette.value = 'yellow'
  readingMode.value = 'line'
  wordAudioMode.value = 'audio'
  linePlaybackMode.value = 'single'
  currentLineIndex.value = 0
  currentWordIndex.value = 0
  processingProgress.value = 0
  processingMessage.value = t('processing.preparing')
  processingTitle.value = t('processing.titleImage')
}

onMounted(async () => {
  if (isNativeAndroid()) {
    modelProgressListener = await DyslexAIPlugin.addListener('modelDownloadProgress', applyModelProgress)
    prepareNativeModel()
  } else {
    warmBackend()
  }

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
  stopRecordingTimer()
  stopMediaTracks()
  window.removeEventListener('resize', updateLayoutFlags)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
  if (modelProgressListener) {
    modelProgressListener.remove()
    modelProgressListener = null
  }
  clearPreviewUrls()
})
</script>

<style>
body {
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

.model-setup-view {
  position: fixed;
  inset: 0;
  z-index: 5000;
  width: 100vw;
  height: 100dvh;
  display: grid;
  place-items: center;
  padding: max(24px, env(safe-area-inset-top)) max(24px, env(safe-area-inset-right)) max(24px, env(safe-area-inset-bottom)) max(24px, env(safe-area-inset-left));
  overflow: auto;
  background: linear-gradient(180deg, #f8fbff 0%, #eef3f8 100%);
}

.model-setup-panel {
  width: min(520px, 100%);
  border: 1px solid #dde6f0;
  border-radius: 8px;
  padding: clamp(22px, 5vw, 36px);
  background: #ffffff;
  box-shadow: 0 18px 48px rgba(20, 33, 61, 0.12);
}

.model-setup-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 22px;
}

.model-setup-eyebrow {
  color: #5f6f86;
  font-size: 0.82rem;
  font-weight: 800;
  text-transform: uppercase;
}

.model-setup-panel h1 {
  margin: 2px 0 0;
  color: #14213d;
  font-size: clamp(1.45rem, 5vw, 2rem);
  line-height: 1.1;
}

.model-setup-copy {
  margin: 0 0 8px;
  color: #4b5f79;
  font-size: 1rem;
  line-height: 1.5;
}

.model-progress-block {
  margin-top: 24px;
}

.model-progress-header,
.model-progress-meta {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  color: #223047;
  font-size: 0.95rem;
}

.model-progress-header strong {
  font-variant-numeric: tabular-nums;
}

.model-progress-track {
  height: 12px;
  margin: 12px 0 10px;
  overflow: hidden;
  border-radius: 999px;
  background: #d9e2ec;
}

.model-progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #1a73e8 0%, #34a853 100%);
  transition: width 180ms ease;
}

.model-progress-meta {
  color: #607086;
  font-variant-numeric: tabular-nums;
}

.model-retry-button {
  width: 100%;
  min-height: 48px;
  margin-top: 24px;
  border: 0;
  border-radius: 8px;
  color: #fff;
  background: #1a73e8;
  font-weight: 800;
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

.subtitle {
  font-size: 13.5px;
  line-height: 1.4;
  color: #6b7280;
  text-align: left;          /* 🔥 IMPORTANTE */
  margin-bottom: 12px;
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
  text-align: justify;
}

.line-focus {
  max-width: 760px;
  margin: 0 auto;
  line-height: 1.5;
  font-weight: 700;
  text-align: justify;
  text-align-last: left;
}

.word-focus {
  max-width: 900px;
  margin: 0 auto;
}

.word-context {
  display: block;
  text-align: justify;
  text-align-last: left;
}

.word-chip {
  display: inline;
  margin-right: .2em;
  padding: 0 .08em;
  border-radius: 3px;
  background: transparent;
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
  grid-column: 1 / -1;
  justify-content: center;
  width: 100%;
  white-space: nowrap;
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
  font-size: .95rem;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

@media (orientation: landscape) and (max-height: 620px) {
  .text-output {
    font-size: .86rem !important;
    line-height: 1.28 !important;
    min-height: 180px !important;
  }
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


/* =========================================================
   DyslexAI Android UI — proposta aprovada (Google clean)
   Apenas camada visual. Não altera backend, inferência ou Capacitor.
   ========================================================= */
html, body, #app { width:100%; min-width:0; min-height:100%; overflow-x:hidden; background:#f6f8fc; }
body { margin:0; font-family:Inter, Roboto, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; color:#0f172a; -webkit-font-smoothing:antialiased; }
.player-app { width:100%; min-height:100dvh; background:linear-gradient(180deg,#f8fbff 0%,#eef3f8 100%); overflow-x:hidden; }
.app-shell { width:100%; max-width:none; min-height:100dvh; margin:0; padding:max(10px, env(safe-area-inset-top)) max(14px, env(safe-area-inset-right)) max(10px, env(safe-area-inset-bottom)) max(14px, env(safe-area-inset-left)); }
.screen-center { width:100%; min-height:calc(100dvh - 20px); display:grid; place-items:center; overflow:hidden; }
button, select, input, textarea, audio { font:inherit; }
.home-screen { place-items:stretch; }
.google-home { width:min(100%,760px); min-height:calc(100dvh - 20px); margin:0 auto; background:#fff; border:1px solid #e5eaf2; box-shadow:0 10px 30px rgba(15,23,42,.05); display:grid; grid-template-rows:auto 1fr auto; overflow:hidden; }
.google-home-header { display:flex; align-items:flex-start; justify-content:space-between; gap:16px; padding:28px 30px 10px; }
.home-brand { display:flex; align-items:center; gap:14px; min-width:0; }
.home-logo-mark { position:relative; width:58px; height:58px; border-radius:17px; overflow:hidden; background:#fff; box-shadow:inset 0 0 0 1px rgba(15,23,42,.04); flex:0 0 auto; }
.home-logo-mark span { position:absolute; display:block; }
.logo-blue { inset:0 0 28px 0; background:#1a73e8; border-radius:18px 18px 6px 6px; }
.logo-red { left:0; top:22px; width:28px; height:18px; background:#ea4335; }
.logo-yellow { left:0; bottom:0; width:28px; height:26px; background:#fbbc04; }
.logo-green { right:0; bottom:0; width:34px; height:34px; background:#34a853; border-radius:0 0 18px 0; }
.home-brand-title { font-size:clamp(2rem,4.8vw,2.7rem); line-height:1; font-weight:800; letter-spacing:-.04em; color:#0b1736; }
.home-brand-title span { color:#1a73e8; }
.home-brand-subtitle { margin-top:7px; font-size:clamp(1rem,2.2vw,1.25rem); line-height:1.2; color:#667085; }
.home-help-btn { width:46px; height:46px; border-radius:999px; border:1px solid #dfe6ef; background:#fff; color:#111827; font-size:1.25rem; font-weight:800; box-shadow:0 4px 14px rgba(15,23,42,.04); }
.google-home-main { min-height:0; display:flex; flex-direction:column; gap:14px; padding:12px 30px 14px; overflow:hidden; }
.home-intro-panel { display:grid; gap:16px; }
.home-badge { justify-self:start; display:inline-flex; align-items:center; min-height:34px; padding:6px 14px; border-radius:999px; background:#e8f0fe; color:#1a73e8; font-size:clamp(.92rem,1.8vw,1.05rem); font-weight:650; margin:0; }
.home-intro-panel h1 { margin:0; max-width:10ch; font-size:clamp(2.65rem,8.6vw,4.4rem); line-height:1.03; letter-spacing:-.055em; font-weight:850; color:#0b1736; }
.home-intro-panel p { margin:0; max-width:550px; font-size:clamp(1.08rem,2.8vw,1.45rem); line-height:1.42; color:#667085; }
.home-actions-panel { display:grid; gap:14px; min-height:0; }
.home-option-card { width:100%; min-width:0; border:1px solid #e5eaf2; border-radius:24px; background:#fff; padding:16px; display:grid; grid-template-columns:clamp(86px,18vw,118px) minmax(0,1fr) 48px; align-items:center; gap:18px; text-align:left; cursor:pointer; box-shadow:0 8px 22px rgba(15,23,42,.07); }
.home-option-card--blue { border-left:4px solid #1a73e8; } .home-option-card--green { border-left:4px solid #34a853; }
.home-option-icon { width:clamp(86px,18vw,118px); aspect-ratio:1; border-radius:20px; display:grid; place-items:center; font-size:clamp(2.6rem,6vw,4rem); line-height:1; background:linear-gradient(135deg,#eaf4ff 0%,#d9edff 100%); }
.home-option-card--green .home-option-icon { background:linear-gradient(135deg,#eaf8ed 0%,#d7f1db 100%); }
.home-option-text { min-width:0; display:flex; flex-direction:column; gap:8px; }
.home-option-text strong { font-size:clamp(1.35rem,3.2vw,1.95rem); line-height:1.12; letter-spacing:-.03em; color:#101828; }
.home-option-text span { font-size:clamp(.98rem,2.2vw,1.18rem); line-height:1.35; color:#667085; }
.home-option-arrow, .recent-arrow { width:46px; height:46px; border-radius:999px; display:grid; place-items:center; color:#1a73e8; background:#f1f6ff; font-size:1.9rem; font-weight:500; }
.home-option-card--green .home-option-arrow { color:#1e9e4a; background:#eaf8ed; }
.home-recent-card, .home-safety-card { display:grid; grid-template-columns:auto 1fr auto; align-items:center; gap:14px; border:1px solid #e5eaf2; border-radius:20px; background:#fff; padding:12px 16px; }
.recent-title { grid-column:1/-1; color:#667085; font-weight:650; font-size:1rem; margin-bottom:-2px; }
.recent-row { display:contents; }
.recent-icon, .safety-icon { width:54px; height:54px; border-radius:16px; display:grid; place-items:center; background:#eef5ff; font-size:1.7rem; }
.home-safety-card { background:#eef5ff; }
.home-recent-card strong, .home-safety-card strong { display:block; color:#172033; font-size:1.02rem; line-height:1.2; }
.home-recent-card span, .home-safety-card span { display:block; margin-top:4px; color:#667085; font-size:.95rem; line-height:1.3; }
.home-bottom-nav { display:grid; grid-template-columns:repeat(4,1fr); gap:6px; padding:10px 26px 14px; border-top:1px solid #e5eaf2; background:rgba(255,255,255,.95); }
.home-bottom-nav button { border:0; background:transparent; color:#64748b; display:grid; place-items:center; gap:3px; font-size:1.55rem; padding:8px 4px; border-radius:18px; }
.home-bottom-nav button span { font-size:.82rem; font-weight:600; }
.home-bottom-nav button.active { color:#1a73e8; }
.player-card { width:min(100%,560px); max-height:calc(100dvh - 24px); border-radius:26px; overflow:hidden; background:#fff; border:1px solid #e5eaf2; box-shadow:0 16px 38px rgba(15,23,42,.08); }
.player-top { min-height:auto; padding:22px; color:#101828; background:#fff; }
.player-bottom { padding:18px 22px 22px; background:#fff; border-top:1px solid #e5eaf2; }
.brand, .subtitle, .mini-brand { color:#101828; }
.intro-copy h1, .processing-content h2, .audio-summary h2 { font-size:clamp(1.75rem,5vw,2.5rem); line-height:1.06; font-weight:800; letter-spacing:-.04em; color:#101828; }
.intro-copy p, .processing-content p, .audio-summary p { color:#667085; font-size:clamp(1rem,2.4vw,1.12rem); line-height:1.45; }
.mode-grid { grid-template-columns:1fr; gap:12px; }
.mode-option { min-height:auto; border:1px solid #e5eaf2; background:#fff; border-radius:22px; box-shadow:0 8px 20px rgba(15,23,42,.05); }
.audio-top { display:grid; grid-template-columns:88px 1fr; align-items:start; gap:18px; text-align:left; }
.audio-hero { width:88px; height:88px; border-radius:999px; display:grid; place-items:center; color:#1a73e8; background:#e8f0fe; border:2px dashed #1a73e8; font-size:3rem; }
.audio-summary { max-width:none; gap:12px; }
.audio-exercise-config { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:10px; }
.audio-exercise-config label { display:grid; gap:5px; color:#667085; font-size:.85rem; font-weight:650; }
.audio-exercise-config label:last-of-type { grid-column:1/-1; }
.audio-exercise-config select, .setting-block select, .setting-block input { width:100%; border:1px solid #e5eaf2; background:#fff; border-radius:14px; padding:10px 12px; color:#101828; }
.expected-reading-box, .speech-summary-box { background:#f8fbff; border:1px solid #e5eaf2; border-radius:18px; padding:14px; color:#101828; }
.status-pill { background:#eef5ff; color:#1a73e8; }
.record-controls, .confirm-actions { justify-content:stretch; }
.record-controls button, .confirm-actions button { flex:1 1 auto; }
.main-action { background:#1a73e8; color:#fff; }
.soft-action, .pill-btn { background:#f3f6fb; color:#344054; }
.reader-card { width:min(100%,900px); max-height:calc(100dvh - 24px); overflow:hidden; }
.reader-top { min-height:0; height:min(46dvh,360px); padding:64px 24px 30px; display:grid; place-items:center; background:linear-gradient(180deg,#f8fbff 0%,#eef5ff 100%); }
.player-corner { top:18px; }
.corner-btn, .round-btn, .play-btn { width:46px; height:46px; border:1px solid #e5eaf2; background:#fff; color:#344054; box-shadow:0 5px 12px rgba(15,23,42,.05); }
.play-btn { background:#1a73e8; border-color:#1a73e8; color:#fff; }
.line-focus { max-width:none; width:100%; font-weight:800; color:#101828 !important; line-height:1.22; letter-spacing:0; text-align:justify; text-align-last:left; }
.word-chip { background:transparent; border:0; color:#344054; padding:0 .08em; border-radius:3px; box-shadow:none; }
.reader-bottom { max-height:calc(54dvh - 24px); overflow:auto; display:grid; gap:12px; }
.transport-row { gap:14px; }
.progress-line { height:5px; background:#d7dee9; }
.progress-fill { background:#1a73e8; }
.progress-label { color:#667085; }
.controls-compact { gap:8px; }
.pill-btn { border:1px solid #e5eaf2; border-radius:999px; padding:10px 14px; }
.pill-btn.active { background:#e8f0fe; color:#1a73e8; border-color:#bcd4ff; }
.settings-panel { grid-template-columns:repeat(3,minmax(0,1fr)); background:#f8fbff; border:1px solid #e5eaf2; }
@media (orientation:landscape) and (min-width:840px) {
  .app-shell { padding:16px clamp(20px,4vw,56px); }
  .screen-center { min-height:calc(100dvh - 32px); }
  .google-home { width:min(100%,1440px); min-height:calc(100dvh - 32px); border-radius:30px; grid-template-rows:auto 1fr auto; }
  .google-home-header { padding:26px 34px 8px; }
  .google-home-main { display:grid; grid-template-columns:minmax(360px,.9fr) minmax(440px,1.1fr); grid-template-rows:1fr auto; column-gap:clamp(28px,5vw,72px); row-gap:18px; align-items:center; padding:10px clamp(40px,7vw,90px) 8px; overflow:hidden; }
  .home-intro-panel { align-self:center; }
  .home-intro-panel h1 { font-size:clamp(3.4rem,5.3vw,5.9rem); }
  .home-intro-panel p { font-size:clamp(1.1rem,1.55vw,1.55rem); }
  .home-actions-panel { align-self:center; gap:22px; }
  .home-option-card { min-height:142px; padding:20px; grid-template-columns:108px minmax(0,1fr) 52px; }
  .home-option-icon { width:108px; font-size:3.6rem; }
  .home-recent-card, .home-safety-card { display:none; }
  .home-bottom-nav { width:min(100%,1320px); justify-self:center; margin:0 34px 18px; border:1px solid #e5eaf2; border-radius:28px; box-shadow:0 8px 24px rgba(15,23,42,.06); }
  .player-card:not(.reader-card) { width:min(100%,820px); }
  .reader-card { width:min(100%,960px); }
}
@media (orientation:landscape) and (max-height:760px) {
  .google-home-header { padding-top:18px; } .home-logo-mark { width:48px; height:48px; } .home-brand-title { font-size:1.75rem; } .home-brand-subtitle { font-size:.95rem; } .home-help-btn { width:42px; height:42px; }
  .home-intro-panel { gap:12px; } .home-intro-panel h1 { font-size:clamp(2.8rem,4.8vw,4.8rem); } .home-intro-panel p { font-size:clamp(1rem,1.35vw,1.3rem); line-height:1.34; }
  .home-option-card { min-height:120px; grid-template-columns:88px minmax(0,1fr) 48px; padding:16px; } .home-option-icon { width:88px; border-radius:20px; font-size:3rem; }
  .home-bottom-nav { padding:8px 24px; margin-bottom:10px; } .home-bottom-nav button { padding:4px; font-size:1.25rem; } .home-bottom-nav button span { font-size:.76rem; }
}
@media (max-width:620px) {
  .app-shell { padding:0; } .screen-center { min-height:100dvh; } .google-home { width:100%; min-height:100dvh; border:0; }
  .google-home-header { padding:24px 22px 8px; } .google-home-main { padding:12px 22px 10px; gap:12px; }
  .home-recent-card, .home-safety-card { padding:10px 12px; }
  .home-option-card { grid-template-columns:74px minmax(0,1fr) 42px; gap:12px; padding:14px; } .home-option-icon { width:74px; border-radius:18px; }
  .nav-help { display:none !important; } .home-bottom-nav { grid-template-columns:repeat(3,1fr); padding-left:12px; padding-right:12px; }
  .player-card, .reader-card { width:100%; max-height:100dvh; border-radius:0; border:0; }
  .audio-top { grid-template-columns:1fr; text-align:center; } .audio-hero { margin:0 auto; } .audio-exercise-config, .settings-panel { grid-template-columns:1fr; }
}


/* =========================================================
   AJUSTE FINAL — Home vertical/horizontal
   ========================================================= */
html, body, #app { width:100%; height:100%; min-width:0; margin:0; overflow:hidden; background:#f7faff; }
* { box-sizing:border-box; }
.player-app,.app-shell,.home-screen { width:100vw; height:100dvh; min-height:100dvh; max-width:100vw; overflow:hidden; margin:0; }
.player-app { background:#f7faff; color:#14213d; }
.app-shell { padding:0; }
.screen-center.home-screen { display:block; min-height:100dvh; place-items:unset; }
.google-home { width:100vw; height:100dvh; min-height:100dvh; max-width:100vw; margin:0; border:0; border-radius:0; background:#fff; box-shadow:none; overflow:hidden; display:grid; grid-template-rows:auto minmax(0,1fr) auto; }
.google-home-header { width:100%; min-width:0; padding:clamp(16px,3.2vh,26px) clamp(20px,5vw,34px) 8px; align-items:flex-start; }
.home-brand { gap:11px; align-items:center; }
.home-logo-mark { width:clamp(42px,7vw,56px); height:clamp(42px,7vw,56px); border-radius:0; box-shadow:none; background:transparent; overflow:visible; flex:0 0 auto; }
.home-logo-mark img { display:block; width:100%; height:100%; object-fit:contain; }
.home-brand-title { font-size:clamp(1.9rem,5.2vw,2.75rem); line-height:.95; font-weight:780; letter-spacing:-.045em; color:#12203b; }
.home-brand-title span { color:#1a73e8; }
.home-brand-subtitle { margin-top:4px; font-size:clamp(.96rem,2.4vw,1.25rem); line-height:1.15; color:#68758a; }
.home-help-btn { width:clamp(34px,6.2vw,44px); height:clamp(34px,6.2vw,44px); border-radius:999px; font-size:clamp(1rem,2.4vw,1.25rem); color:#172033; background:#fff; border:1px solid #e5ebf3; box-shadow:0 2px 10px rgba(16,24,40,.04); }
.google-home-main { min-height:0; overflow:hidden; padding:6px clamp(20px,5vw,34px) 8px; display:grid; grid-template-rows:auto auto minmax(0,1fr); gap:clamp(12px,2.2vh,18px); }
.home-intro-panel { display:grid; justify-items:start; gap:clamp(8px,1.5vh,12px); }
.home-badge { min-height:30px; padding:5px 12px; font-size:clamp(.88rem,2.2vw,1rem); background:#eaf2ff; color:#1a73e8; font-weight:680; }
.home-intro-panel h1 { margin:0; max-width:13.5ch; font-size:clamp(2rem,6.9vw,3.15rem); line-height:1.08; letter-spacing:-.055em; font-weight:780; color:#13213d; }
.home-intro-panel p { max-width:38ch; margin:0; font-size:clamp(.98rem,2.9vw,1.28rem); line-height:1.35; color:#6b7689; }
.home-actions-panel { min-height:0; display:grid; align-content:start; gap:clamp(12px,2vh,16px); }
.home-option-card { width:100%; min-width:0; min-height:clamp(106px,15.5vh,132px); padding:clamp(12px,2.2vh,18px); grid-template-columns:clamp(76px,18vw,104px) minmax(0,1fr) clamp(38px,7.8vw,48px); gap:clamp(12px,3.2vw,18px); border-radius:22px; border:1px solid #e7ecf4; background:#fff; box-shadow:0 8px 22px rgba(16,24,40,.07); }
.home-option-card--blue { border-left:4px solid #1a73e8; }
.home-option-card--green { border-left:4px solid #34a853; }
.home-option-icon { width:clamp(76px,18vw,104px); height:clamp(76px,18vw,104px); aspect-ratio:1/1; border-radius:19px; display:grid; place-items:center; font-size:0; }
.home-option-icon svg { width:100%; height:100%; display:block; }
.home-option-text { min-width:0; gap:6px; }
.home-option-text strong { font-size:clamp(1.18rem,3.6vw,1.55rem); line-height:1.12; letter-spacing:-.035em; font-weight:760; color:#172033; }
.home-option-text span { font-size:clamp(.88rem,2.5vw,1.06rem); line-height:1.32; color:#6b7689; }
.home-option-arrow { width:clamp(38px,7.8vw,48px); height:clamp(38px,7.8vw,48px); font-size:clamp(1.25rem,3vw,1.75rem); background:#eef5ff; color:#1a73e8; }
.home-option-card--green .home-option-arrow { background:#eaf7ed; color:#24a148; }
.home-recent-card,.home-safety-card { display:none!important; }
.home-bottom-nav { width:min(360px,calc(100vw - 40px)); justify-self:center; margin:0 auto max(10px,env(safe-area-inset-bottom)); padding:6px; display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:6px; border:1px solid #e7ecf4; border-radius:22px; background:rgba(255,255,255,.96); box-shadow:0 8px 24px rgba(16,24,40,.08); }
.home-bottom-nav button { min-width:0; min-height:62px; padding:8px 12px; border:0; border-radius:17px; background:transparent; color:#667085; display:grid; place-items:center; gap:3px; font-size:0; }
.home-bottom-nav button svg { width:26px; height:26px; }
.home-bottom-nav button span { font-size:.88rem; font-weight:720; }
.home-bottom-nav button.active { background:#eaf2ff; color:#1a73e8; }
@media (orientation:landscape) and (min-width:720px) {
  .google-home-header { padding:clamp(12px,2.8vh,20px) clamp(28px,5vw,56px) 4px; }
  .home-logo-mark { width:clamp(36px,4.2vw,50px); height:clamp(36px,4.2vw,50px); }
  .home-brand-title { font-size:clamp(1.55rem,3.2vw,2.3rem); }
  .home-brand-subtitle { font-size:clamp(.78rem,1.6vw,1rem); }
  .google-home-main { padding:clamp(4px,1.2vh,10px) clamp(28px,6vw,70px) 8px; grid-template-columns:minmax(0,.95fr) minmax(340px,1.05fr); grid-template-rows:1fr; gap:clamp(22px,5vw,58px); align-items:center; }
  .home-intro-panel { align-self:center; justify-self:end; max-width:520px; gap:clamp(7px,1.6vh,13px); }
  .home-intro-panel h1 { max-width:12.5ch; font-size:clamp(2.25rem,5.1vw,4.1rem); line-height:1.04; }
  .home-intro-panel p { max-width:36ch; font-size:clamp(.9rem,1.7vw,1.22rem); line-height:1.32; }
  .home-actions-panel { align-self:center; justify-self:stretch; gap:clamp(12px,2.8vh,22px); }
  .home-option-card { min-height:clamp(96px,21vh,136px); grid-template-columns:clamp(72px,10vw,104px) minmax(0,1fr) clamp(36px,5vw,48px); padding:clamp(12px,2.4vh,20px); }
  .home-option-icon { width:clamp(72px,10vw,104px); height:clamp(72px,10vw,104px); }
  .home-option-text strong { font-size:clamp(1.05rem,2.1vw,1.55rem); }
  .home-option-text span { font-size:clamp(.78rem,1.55vw,1.02rem); }
  .home-bottom-nav { margin-bottom:max(8px,env(safe-area-inset-bottom)); min-width:300px; }
}
@media (orientation:landscape) and (max-height:560px) {
  .google-home-header { padding-top:8px; padding-bottom:0; }
  .home-help-btn { width:34px; height:34px; font-size:1rem; }
  .google-home-main { padding-top:2px; padding-bottom:4px; }
  .home-intro-panel h1 { font-size:clamp(2rem,4.8vw,3.45rem); }
  .home-intro-panel p { font-size:clamp(.78rem,1.45vw,1rem); line-height:1.25; }
  .home-badge { min-height:26px; padding:3px 10px; font-size:.82rem; }
  .home-option-card { min-height:clamp(84px,22vh,108px); padding:10px 12px; }
  .home-option-icon { width:clamp(62px,9vw,84px); height:clamp(62px,9vw,84px); }
  .home-bottom-nav { min-width:280px; padding:5px; }
  .home-bottom-nav button { min-height:48px; padding:4px 10px; }
  .home-bottom-nav button svg { width:22px; height:22px; }
  .home-bottom-nav button span { font-size:.78rem; }
}


/* =========================================================
   AJUSTE HOME V3 — vertical primeiro, horizontal controlado
   Mantém lógica; corrige proporções, ícone, cores e espaços.
   ========================================================= */
html, body, #app {
  width: 100% !important;
  height: 100% !important;
  margin: 0 !important;
  overflow: hidden !important;
  background: #ffffff !important;
}
* { box-sizing: border-box; }

.player-app,
.app-shell,
.screen-center.home-screen {
  width: 100vw !important;
  height: 100dvh !important;
  min-height: 100dvh !important;
  max-width: 100vw !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
  background: #ffffff !important;
}

.google-home {
  width: 100vw !important;
  height: 100dvh !important;
  min-height: 100dvh !important;
  max-width: 100vw !important;
  margin: 0 !important;
  border: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 56%, #f6f9fd 100%) !important;
  display: grid !important;
  grid-template-rows: auto minmax(0, 1fr) auto !important;
  overflow: hidden !important;
}

.google-home-header {
  padding: max(12px, env(safe-area-inset-top)) 18px 4px !important;
  display: flex !important;
  align-items: flex-start !important;
  justify-content: space-between !important;
}

.home-brand { display: flex !important; align-items: center !important; gap: 9px !important; }
.home-logo-mark { position: relative !important; width: 34px !important; height: 34px !important; min-width: 34px !important; border-radius: 0 !important; overflow: visible !important; background: transparent !important; box-shadow: none !important; }
.home-logo-mark img { display: none !important; }
.home-logo-mark span { position: absolute !important; display: block !important; }
.logo-blue { left: 0; top: 0; width: 34px; height: 20px; background: #1a73e8; border-radius: 11px 12px 5px 5px; }
.logo-red { left: 0; top: 14px; width: 17px; height: 10px; background: #ea4335; border-radius: 3px 0 0 3px; }
.logo-yellow { left: 0; bottom: 0; width: 17px; height: 14px; background: #fbbc04; border-radius: 0 0 3px 9px; }
.logo-green { right: 0; bottom: 0; width: 20px; height: 20px; background: #34a853; border-radius: 0 0 10px 0; }

.home-brand-title { font-size: clamp(1.38rem, 5.1vw, 1.82rem) !important; line-height: .98 !important; letter-spacing: -0.045em !important; font-weight: 760 !important; color: #14213d !important; }
.home-brand-title span { color: #1a73e8 !important; }
.home-brand-subtitle { margin-top: 2px !important; font-size: clamp(.72rem, 2.4vw, .92rem) !important; line-height: 1.1 !important; color: #6f7b8f !important; }
.home-help-btn { width: 32px !important; height: 32px !important; min-width: 32px !important; border-radius: 999px !important; border: 1px solid #e5ebf3 !important; background: rgba(255, 255, 255, .94) !important; color: #18243d !important; font-size: 1rem !important; font-weight: 760 !important; box-shadow: 0 3px 10px rgba(16, 24, 40, .04) !important; }

.google-home-main { min-height: 0 !important; overflow: hidden !important; padding: 6px 18px 8px !important; display: grid !important; grid-template-rows: auto auto minmax(0, 1fr) !important; gap: clamp(10px, 1.6vh, 14px) !important; }
.home-intro-panel { display: grid !important; justify-items: start !important; gap: clamp(7px, 1.15vh, 10px) !important; }
.home-badge { min-height: 26px !important; padding: 4px 10px !important; border-radius: 999px !important; background: #eaf2ff !important; color: #1a73e8 !important; font-size: clamp(.78rem, 2.45vw, .9rem) !important; font-weight: 680 !important; }
.home-intro-panel h1 { margin: 0 !important; max-width: 14ch !important; font-size: clamp(1.58rem, 6.15vw, 2.18rem) !important; line-height: 1.12 !important; letter-spacing: -0.045em !important; font-weight: 720 !important; color: #17233d !important; }
.home-intro-panel p { margin: 0 !important; max-width: 38ch !important; font-size: clamp(.82rem, 2.75vw, 1rem) !important; line-height: 1.36 !important; color: #6a7588 !important; }

.home-actions-panel { min-height: 0 !important; display: grid !important; align-content: start !important; gap: 11px !important; }
.home-option-card { width: 100% !important; min-width: 0 !important; min-height: clamp(92px, 14.2vh, 116px) !important; padding: 12px 12px !important; display: grid !important; grid-template-columns: clamp(62px, 16vw, 82px) minmax(0, 1fr) 36px !important; gap: 12px !important; align-items: center !important; border: 1px solid #e7ecf4 !important; border-radius: 19px !important; background: rgba(255, 255, 255, .98) !important; box-shadow: 0 7px 20px rgba(16, 24, 40, .065) !important; }
.home-option-card--blue { border-left: 3px solid #1a73e8 !important; }
.home-option-card--green { border-left: 3px solid #34a853 !important; }
.home-option-icon { width: clamp(62px, 16vw, 82px) !important; height: clamp(62px, 16vw, 82px) !important; border-radius: 17px !important; overflow: hidden !important; background: #eaf4ff !important; }
.home-option-card--green .home-option-icon { background: #eaf8ed !important; }
.home-option-icon svg { width: 100% !important; height: 100% !important; display: block !important; }
.home-option-text { gap: 4px !important; min-width: 0 !important; }
.home-option-text strong { font-size: clamp(1.02rem, 3.55vw, 1.34rem) !important; line-height: 1.12 !important; letter-spacing: -0.03em !important; font-weight: 720 !important; color: #172033 !important; }
.home-option-text span { font-size: clamp(.78rem, 2.55vw, .96rem) !important; line-height: 1.32 !important; color: #697588 !important; }
.home-option-arrow { width: 34px !important; height: 34px !important; border-radius: 999px !important; background: #eef5ff !important; color: #1a73e8 !important; font-size: 1.35rem !important; }
.home-option-card--green .home-option-arrow { background: #eaf8ed !important; color: #24a148 !important; }
.home-recent-card, .home-safety-card { display: none !important; }

.home-bottom-nav { width: min(300px, calc(100vw - 44px)) !important; justify-self: center !important; margin: 0 auto max(8px, env(safe-area-inset-bottom)) !important; padding: 5px !important; display: grid !important; grid-template-columns: repeat(2, minmax(0, 1fr)) !important; gap: 5px !important; border: 1px solid #e7ecf4 !important; border-radius: 19px !important; background: rgba(255, 255, 255, .97) !important; box-shadow: 0 7px 22px rgba(16, 24, 40, .08) !important; }
.home-bottom-nav button { min-height: 54px !important; padding: 6px 10px !important; border-radius: 15px !important; color: #667085 !important; }
.home-bottom-nav button.active { background: #eaf2ff !important; color: #1a73e8 !important; }
.home-bottom-nav button svg { width: 22px !important; height: 22px !important; }
.home-bottom-nav button span { font-size: .78rem !important; font-weight: 700 !important; }

@media (orientation: portrait) { .google-home-main { align-content: start !important; } }

@media (orientation: landscape) and (min-width: 700px) {
  .google-home-header { padding: max(8px, env(safe-area-inset-top)) clamp(22px, 3.6vw, 42px) 2px !important; }
  .home-logo-mark { width: 32px !important; height: 32px !important; min-width: 32px !important; }
  .logo-blue { width:32px; height:19px; } .logo-red { top:13px; width:16px; height:10px; } .logo-yellow { width:16px; height:13px; } .logo-green { width:19px; height:19px; }
  .home-brand-title { font-size: clamp(1.35rem, 2.7vw, 1.95rem) !important; }
  .home-brand-subtitle { font-size: clamp(.68rem, 1.35vw, .88rem) !important; }
  .home-help-btn { width: 30px !important; height: 30px !important; min-width: 30px !important; }
  .google-home-main { padding: 4px clamp(22px, 4vw, 54px) 8px !important; grid-template-columns: minmax(0, .92fr) minmax(300px, 1.08fr) !important; grid-template-rows: minmax(0, 1fr) !important; gap: clamp(18px, 4vw, 48px) !important; align-items: center !important; }
  .home-intro-panel { justify-self: end !important; align-self: center !important; max-width: 430px !important; gap: 7px !important; }
  .home-intro-panel h1 { max-width: 14ch !important; font-size: clamp(1.9rem, 4vw, 3.15rem) !important; line-height: 1.08 !important; }
  .home-intro-panel p { max-width: 34ch !important; font-size: clamp(.76rem, 1.45vw, .98rem) !important; line-height: 1.27 !important; }
  .home-badge { min-height: 24px !important; padding: 3px 9px !important; font-size: .78rem !important; }
  .home-actions-panel { justify-self: stretch !important; align-self: center !important; gap: clamp(9px, 2.2vh, 15px) !important; }
  .home-option-card { min-height: clamp(76px, 22vh, 112px) !important; grid-template-columns: clamp(56px, 8.4vw, 82px) minmax(0, 1fr) 34px !important; padding: clamp(9px, 2vh, 14px) !important; gap: 11px !important; }
  .home-option-icon { width: clamp(56px, 8.4vw, 82px) !important; height: clamp(56px, 8.4vw, 82px) !important; }
  .home-option-text strong { font-size: clamp(.98rem, 1.9vw, 1.32rem) !important; }
  .home-option-text span { font-size: clamp(.72rem, 1.35vw, .9rem) !important; line-height: 1.25 !important; }
  .home-bottom-nav { width: min(280px, 35vw) !important; margin-bottom: max(6px, env(safe-area-inset-bottom)) !important; padding: 4px !important; }
  .home-bottom-nav button { min-height: 44px !important; padding: 4px 8px !important; }
  .home-bottom-nav button svg { width: 20px !important; height: 20px !important; }
  .home-bottom-nav button span { font-size: .72rem !important; }
}

@media (orientation: landscape) and (max-height: 430px) {
  .google-home-header { padding-top: 5px !important; }
  .google-home-main { padding-top: 0 !important; padding-bottom: 4px !important; }
  .home-intro-panel h1 { font-size: clamp(1.65rem, 3.5vw, 2.55rem) !important; }
  .home-intro-panel p { font-size: clamp(.68rem, 1.2vw, .82rem) !important; max-width: 32ch !important; }
  .home-option-card { min-height: 68px !important; }
  .home-option-icon { width: 52px !important; height: 52px !important; border-radius: 14px !important; }
  .home-bottom-nav button { min-height: 38px !important; }
}
.title {
  font-size: clamp(20px, 4.2vw, 24px);
  font-weight: 600;
  line-height: 1.25;
  text-align: left;          /* 🔥 CRÍTICO */
  color: #1f2937;
  margin-top: 4px;
  margin-bottom: 6px;
}

.top-section {
  padding: 14px 16px 6px;
  text-align: left; /* 🔥 garantir alinhamento */
}

.card {
  display: flex;
  align-items: center;
  justify-content: space-between; /* 🔥 garante seta dentro */
  gap: 12px;

  padding: 14px;
  border-radius: 16px;
  background: #ffffff;

  box-shadow: 0 2px 6px rgba(0,0,0,0.06);

  margin-bottom: 10px;
}

.card-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1; /* 🔥 ocupa espaço corretamente */
}

.card-icon {
  width: 52px;   /* 🔥 antes estava pequeno */
  height: 52px;
  border-radius: 12px;

  display: flex;
  align-items: center;
  justify-content: center;
}

.card-arrow {
  flex-shrink: 0;

  width: 32px;
  height: 32px;

  display: flex;
  align-items: center;
  justify-content: center;

  border-radius: 50%;
  background: #eef2ff;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.card-desc {
  font-size: 13px;
  color: #6b7280;
}

.container {
  display: flex;
  flex-direction: column;

  min-height: 100vh;
  padding: 0 16px;
}
.cards {
  flex: 1;               /* 🔥 CRÍTICO */
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

/* ===== CORREÇÃO REAL — HOME VERTICAL/HORIZONTAL ===== */

.home-intro-panel {
  justify-items: start !important;
  text-align: left !important;
}

.home-intro-panel h1 {
  max-width: 15ch !important;
  font-size: clamp(1.55rem, 5.2vw, 2.15rem) !important;
  line-height: 1.14 !important;
  font-weight: 720 !important;
  letter-spacing: -0.035em !important;
  text-align: left !important;
  color: #17233d !important;
}

.home-intro-panel p {
  max-width: 36ch !important;
  font-size: clamp(0.84rem, 2.35vw, 0.98rem) !important;
  line-height: 1.35 !important;
  text-align: left !important;
  color: #6b7689 !important;
}

.home-option-card {
  grid-template-columns: 76px minmax(0, 1fr) 38px !important;
  padding: 14px !important;
  gap: 14px !important;
  overflow: hidden !important;
}

.home-option-icon {
  width: 76px !important;
  height: 76px !important;
  min-width: 76px !important;
}

.home-option-text {
  min-width: 0 !important;
  overflow: hidden !important;
}

.home-option-text strong {
  font-size: clamp(1.04rem, 3vw, 1.28rem) !important;
  line-height: 1.12 !important;
}

.home-option-text span {
  font-size: clamp(0.78rem, 2.15vw, 0.92rem) !important;
  line-height: 1.3 !important;
}

.home-option-arrow {
  width: 34px !important;
  height: 34px !important;
  min-width: 34px !important;
  justify-self: end !important;
  align-self: center !important;
}

/* Horizontal */
@media (orientation: landscape) and (min-width: 700px) {
  .google-home-main {
    grid-template-columns: minmax(280px, 0.78fr) minmax(360px, 1.22fr) !important;
    gap: 24px !important;
    align-items: center !important;
  }

  .home-intro-panel {
    justify-self: start !important;
    align-self: center !important;
    max-width: 360px !important;
  }

  .home-intro-panel h1 {
    font-size: clamp(1.7rem, 3.6vw, 2.65rem) !important;
    max-width: 13ch !important;
  }

  .home-intro-panel p {
    font-size: clamp(0.76rem, 1.25vw, 0.9rem) !important;
    max-width: 32ch !important;
  }

  .home-actions-panel {
    justify-self: stretch !important;
    max-width: 520px !important;
  }

  .home-option-card {
    grid-template-columns: 72px minmax(0, 1fr) 36px !important;
    min-height: 96px !important;
  }

  .home-option-icon {
    width: 72px !important;
    height: 72px !important;
  }
}
/* ===== FIX REAL HOME (compacto e proporcional) ===== */

/* reduzir espaço topo */
.google-home-header {
  padding: 12px 16px 4px !important;
}

/* título compacto (menos espaço vazio) */
.home-intro-panel h1 {
  font-size: clamp(1.6rem, 5vw, 2.1rem) !important;
  line-height: 1.1 !important;
  margin-bottom: 4px !important;
  max-width: 12ch !important;
}

/* texto mais denso (tirar "vazio") */
.home-intro-panel p {
  font-size: clamp(0.85rem, 2.4vw, 1rem) !important;
  line-height: 1.25 !important;
  max-width: 32ch !important;
}

/* reduzir espaço entre blocos */
.google-home-main {
  gap: 10px !important;
  padding: 6px 16px 6px !important;
}

/* CARDS MAIS COMPACTOS */
.home-option-card {
  min-height: 84px !important;   /* 🔥 antes era gigante */
  padding: 10px 12px !important;
  grid-template-columns: 64px 1fr 36px !important;
  gap: 10px !important;
}

/* ÍCONES MAIORES (como pediste) */
.home-option-icon {
  width: 64px !important;
  height: 64px !important;
}

/* TEXTO DOS CARDS */
.home-option-text strong {
  font-size: 1.05rem !important;
}

.home-option-text span {
  font-size: 0.8rem !important;
  line-height: 1.25 !important;
}

/* SETA MAIOR (🔥 importante) */
.home-option-arrow {
  width: 36px !important;
  height: 36px !important;
  font-size: 1.4rem !important;
  font-weight: 700 !important;
}

/* NAV MAIS COMPACTA */
.home-bottom-nav {
  padding: 4px !important;
}

.home-bottom-nav button {
  min-height: 48px !important;
}

.home-bottom-nav button svg {
  width: 20px !important;
  height: 20px !important;
}

.home-bottom-nav button span {
  font-size: 0.7rem !important;
}

/* ===== HOME FINAL — conforme referência anotada ===== */
.google-home {
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 52%, #f4f8fd 100%) !important;
  grid-template-rows: auto minmax(0, 1fr) auto !important;
}

.google-home-header {
  padding: max(12px, env(safe-area-inset-top)) 16px 2px !important;
}

.home-logo-mark {
  width: 36px !important;
  height: 36px !important;
  min-width: 36px !important;
}

.logo-blue {
  width: 36px !important;
  height: 21px !important;
}

.logo-red {
  top: 15px !important;
  width: 18px !important;
  height: 11px !important;
}

.logo-yellow {
  width: 18px !important;
  height: 15px !important;
}

.logo-green {
  width: 21px !important;
  height: 21px !important;
}

.home-brand-title {
  font-size: clamp(1.75rem, 6.2vw, 2.35rem) !important;
  line-height: .9 !important;
  font-weight: 820 !important;
  letter-spacing: -0.045em !important;
}

.home-brand-subtitle {
  font-size: clamp(.95rem, 3.2vw, 1.15rem) !important;
  margin-top: 4px !important;
}

.home-help-btn {
  width: 34px !important;
  height: 34px !important;
}

.google-home-main {
  padding: 6px 16px 8px !important;
  display: grid !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
  gap: clamp(12px, 2vh, 18px) !important;
  align-content: start !important;
}

.home-intro-panel {
  gap: 8px !important;
}

.home-badge {
  min-height: 30px !important;
  padding: 5px 12px !important;
  font-size: clamp(.9rem, 2.9vw, 1.03rem) !important;
}

.home-intro-panel h1 {
  max-width: 11ch !important;
  margin: 0 !important;
  font-size: clamp(2.25rem, 9vw, 3.35rem) !important;
  line-height: .98 !important;
  font-weight: 840 !important;
  letter-spacing: -0.045em !important;
  color: #14213d !important;
}

.home-intro-panel p {
  max-width: 36ch !important;
  margin: 0 !important;
  font-size: clamp(1rem, 3.4vw, 1.24rem) !important;
  line-height: 1.34 !important;
  color: #657185 !important;
}

.home-actions-panel {
  display: grid !important;
  align-content: start !important;
  gap: clamp(12px, 2vh, 18px) !important;
  padding-top: 0 !important;
}

.home-option-card {
  min-height: clamp(122px, 18.5vh, 158px) !important;
  grid-template-columns: clamp(86px, 21vw, 112px) minmax(0, 1fr) auto !important;
  gap: clamp(12px, 3vw, 18px) !important;
  padding: clamp(14px, 2.4vh, 18px) !important;
  border-radius: 20px !important;
  box-shadow: 0 7px 20px rgba(16, 24, 40, .075) !important;
}

.home-option-icon {
  width: clamp(86px, 21vw, 112px) !important;
  height: clamp(86px, 21vw, 112px) !important;
  min-width: clamp(86px, 21vw, 112px) !important;
  border-radius: 18px !important;
}

.home-option-text strong {
  font-size: clamp(1.28rem, 4.4vw, 1.72rem) !important;
  line-height: 1.08 !important;
  font-weight: 780 !important;
}

.home-option-text span {
  font-size: clamp(.92rem, 2.8vw, 1.08rem) !important;
  line-height: 1.28 !important;
}

.home-option-arrow {
  display: none !important;
}

.home-option-cta {
  min-width: clamp(58px, 14vw, 76px) !important;
  min-height: 38px !important;
  padding: 0 12px !important;
  border-radius: 999px !important;
  display: grid !important;
  place-items: center !important;
  justify-self: end !important;
  align-self: center !important;
  background: #eaf2ff !important;
  color: #1a73e8 !important;
  font-size: clamp(.9rem, 2.3vw, 1rem) !important;
  font-weight: 780 !important;
  white-space: nowrap !important;
}

.home-option-card--green .home-option-cta {
  background: #eaf8ed !important;
  color: #24a148 !important;
}

.home-bottom-nav {
  width: min(310px, calc(100vw - 38px)) !important;
  margin-bottom: max(8px, env(safe-area-inset-bottom)) !important;
}

.home-bottom-nav button {
  min-height: 52px !important;
}

@media (max-width: 390px) and (orientation: portrait) {
  .home-intro-panel h1 {
    font-size: clamp(2rem, 8.4vw, 2.75rem) !important;
  }

  .home-intro-panel p {
    font-size: .95rem !important;
  }

  .home-option-card {
    min-height: 112px !important;
    grid-template-columns: 78px minmax(0, 1fr) auto !important;
  }

  .home-option-icon {
    width: 78px !important;
    height: 78px !important;
    min-width: 78px !important;
  }

  .home-option-text strong {
    font-size: 1.14rem !important;
  }

  .home-option-text span {
    font-size: .82rem !important;
  }

  .home-option-cta {
    min-width: 54px !important;
    min-height: 34px !important;
    padding: 0 10px !important;
    font-size: .84rem !important;
  }
}

@media (orientation: landscape) and (min-width: 700px) {
  .google-home-header {
    padding: max(8px, env(safe-area-inset-top)) clamp(22px, 4vw, 46px) 2px !important;
  }

  .google-home-main {
    grid-template-columns: minmax(260px, .82fr) minmax(390px, 1.18fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    gap: clamp(24px, 5vw, 58px) !important;
    padding: 4px clamp(24px, 5vw, 64px) 8px !important;
  }

  .home-intro-panel {
    justify-self: start !important;
    max-width: 420px !important;
  }

  .home-intro-panel h1 {
    max-width: 11ch !important;
    font-size: clamp(2.15rem, 4.45vw, 3.9rem) !important;
  }

  .home-intro-panel p {
    max-width: 35ch !important;
    font-size: clamp(.88rem, 1.55vw, 1.16rem) !important;
  }

  .home-actions-panel {
    max-width: 620px !important;
    justify-self: stretch !important;
  }

  .home-option-card {
    min-height: clamp(108px, 24vh, 146px) !important;
    grid-template-columns: clamp(82px, 10vw, 108px) minmax(0, 1fr) auto !important;
  }

  .home-option-icon {
    width: clamp(82px, 10vw, 108px) !important;
    height: clamp(82px, 10vw, 108px) !important;
    min-width: clamp(82px, 10vw, 108px) !important;
  }

  .home-option-text strong {
    font-size: clamp(1.2rem, 2.25vw, 1.65rem) !important;
  }

  .home-option-text span {
    font-size: clamp(.82rem, 1.45vw, 1rem) !important;
  }

  .home-option-cta {
    min-width: 72px !important;
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .home-brand-title {
    font-size: 1.45rem !important;
  }

  .home-brand-subtitle {
    font-size: .75rem !important;
  }

  .home-intro-panel h1 {
    font-size: clamp(1.75rem, 3.8vw, 2.55rem) !important;
  }

  .home-intro-panel p {
    font-size: clamp(.72rem, 1.25vw, .84rem) !important;
  }

  .home-option-card {
    min-height: 82px !important;
  }

  .home-option-icon {
    width: 62px !important;
    height: 62px !important;
    min-width: 62px !important;
  }

  .home-bottom-nav button {
    min-height: 40px !important;
  }
}

/* ===== HOME FINAL V2 — ajustes pedidos nas anotações ===== */
.google-home-header {
  padding: max(12px, env(safe-area-inset-top)) 16px 0 !important;
}

.home-brand {
  gap: 8px !important;
}

.home-logo-mark {
  width: 32px !important;
  height: 32px !important;
  min-width: 32px !important;
}

.logo-blue {
  width: 32px !important;
  height: 19px !important;
}

.logo-red {
  top: 13px !important;
  width: 16px !important;
  height: 10px !important;
}

.logo-yellow {
  width: 16px !important;
  height: 13px !important;
}

.logo-green {
  width: 19px !important;
  height: 19px !important;
}

.home-brand-title {
  font-size: clamp(1.48rem, 5vw, 2rem) !important;
  line-height: .95 !important;
}

.home-brand-subtitle {
  margin-top: 2px !important;
  font-size: clamp(.78rem, 2.5vw, .98rem) !important;
}

.home-help-btn {
  width: 30px !important;
  height: 30px !important;
  min-width: 30px !important;
}

.google-home-main {
  padding: 4px 16px 8px !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
  gap: clamp(10px, 1.8vh, 14px) !important;
}

.home-intro-panel {
  gap: 6px !important;
}

.home-badge {
  min-height: 28px !important;
  padding: 4px 11px !important;
  font-size: clamp(.82rem, 2.55vw, .94rem) !important;
  margin: 0 !important;
}

.home-intro-panel h1 {
  max-width: 15ch !important;
  font-size: clamp(1.92rem, 7.3vw, 2.72rem) !important;
  line-height: 1.04 !important;
  letter-spacing: -0.04em !important;
  margin: 0 !important;
}

.home-intro-panel p {
  max-width: 37ch !important;
  font-size: clamp(.9rem, 2.85vw, 1.06rem) !important;
  line-height: 1.33 !important;
}

.home-actions-panel {
  gap: 10px !important;
  align-content: start !important;
}

.home-option-card {
  min-height: clamp(96px, 15.5vh, 122px) !important;
  grid-template-columns: clamp(66px, 17vw, 84px) minmax(0, 1fr) 40px !important;
  gap: 12px !important;
  padding: 12px 12px !important;
  border-radius: 18px !important;
}

.home-option-icon {
  width: clamp(66px, 17vw, 84px) !important;
  height: clamp(66px, 17vw, 84px) !important;
  min-width: clamp(66px, 17vw, 84px) !important;
  border-radius: 16px !important;
}

.home-option-text {
  gap: 4px !important;
}

.home-option-text strong {
  font-size: clamp(1.06rem, 3.4vw, 1.34rem) !important;
  line-height: 1.12 !important;
}

.home-option-text span {
  font-size: clamp(.78rem, 2.3vw, .92rem) !important;
  line-height: 1.28 !important;
}

.home-option-arrow {
  width: 38px !important;
  height: 38px !important;
  min-width: 38px !important;
  display: grid !important;
  place-items: center !important;
  justify-self: end !important;
  align-self: center !important;
  border-radius: 999px !important;
  background: #eaf2ff !important;
  color: #1a73e8 !important;
  font-family: Arial, Helvetica, sans-serif !important;
  font-size: 1.45rem !important;
  font-weight: 900 !important;
  line-height: 1 !important;
}

.home-option-card--green .home-option-arrow {
  background: #eaf8ed !important;
  color: #24a148 !important;
}

.home-option-cta {
  display: none !important;
}

.home-bottom-nav {
  width: min(286px, calc(100vw - 40px)) !important;
  padding: 4px !important;
}

.home-bottom-nav button {
  min-height: 48px !important;
}

@media (max-width: 390px) and (orientation: portrait) {
  .home-intro-panel h1 {
    font-size: clamp(1.76rem, 7vw, 2.34rem) !important;
  }

  .home-intro-panel p {
    font-size: .88rem !important;
  }

  .home-option-card {
    min-height: 92px !important;
    grid-template-columns: 62px minmax(0, 1fr) 36px !important;
    gap: 10px !important;
  }

  .home-option-icon {
    width: 62px !important;
    height: 62px !important;
    min-width: 62px !important;
  }

  .home-option-text strong {
    font-size: 1rem !important;
  }

  .home-option-text span {
    font-size: .76rem !important;
  }

  .home-option-arrow {
    width: 34px !important;
    height: 34px !important;
    min-width: 34px !important;
    font-size: 1.32rem !important;
  }
}

@media (orientation: landscape) and (min-width: 700px) {
  .google-home-main {
    grid-template-columns: minmax(250px, .86fr) minmax(370px, 1.14fr) !important;
    gap: clamp(18px, 4vw, 42px) !important;
    padding: 2px clamp(24px, 4.5vw, 56px) 8px !important;
  }

  .home-intro-panel {
    max-width: 390px !important;
  }

  .home-intro-panel h1 {
    max-width: 14ch !important;
    font-size: clamp(1.85rem, 3.7vw, 3.05rem) !important;
    line-height: 1.04 !important;
  }

  .home-intro-panel p {
    max-width: 34ch !important;
    font-size: clamp(.78rem, 1.35vw, .98rem) !important;
  }

  .home-option-card {
    min-height: clamp(88px, 20vh, 118px) !important;
    grid-template-columns: clamp(66px, 8.5vw, 86px) minmax(0, 1fr) 40px !important;
    padding: clamp(10px, 1.9vh, 14px) !important;
  }

  .home-option-icon {
    width: clamp(66px, 8.5vw, 86px) !important;
    height: clamp(66px, 8.5vw, 86px) !important;
    min-width: clamp(66px, 8.5vw, 86px) !important;
  }

  .home-option-text strong {
    font-size: clamp(1rem, 1.85vw, 1.28rem) !important;
  }

  .home-option-text span {
    font-size: clamp(.72rem, 1.22vw, .88rem) !important;
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .home-intro-panel h1 {
    font-size: clamp(1.55rem, 3.25vw, 2.25rem) !important;
  }

  .home-option-card {
    min-height: 72px !important;
  }

  .home-option-icon {
    width: 54px !important;
    height: 54px !important;
    min-width: 54px !important;
  }
}

/* ===== HOME TARGET — layout da referência final ===== */
html,
body,
#app {
  background: #f6f8fc !important;
}

.player-app,
.app-shell,
.screen-center.home-screen {
  background: #f6f8fc !important;
}

.google-home {
  width: 100vw !important;
  height: 100dvh !important;
  background: #ffffff !important;
  grid-template-rows: auto minmax(0, 1fr) auto !important;
  border: 0 !important;
  border-radius: 0 0 18px 18px !important;
  box-shadow: 0 1px 8px rgba(15, 23, 42, .08) !important;
}

.google-home-header {
  padding: max(28px, env(safe-area-inset-top)) 28px 0 !important;
  align-items: flex-start !important;
}

.home-brand {
  gap: 9px !important;
}

.home-logo-mark {
  width: 28px !important;
  height: 28px !important;
  min-width: 28px !important;
  border-radius: 0 !important;
  overflow: visible !important;
}

.home-logo-mark span {
  position: absolute !important;
  display: block !important;
}

.logo-blue {
  left: 0 !important;
  top: 0 !important;
  width: 28px !important;
  height: 28px !important;
  background: #1a73e8 !important;
  border-radius: 5px 14px 14px 5px !important;
}

.logo-red {
  left: 0 !important;
  top: 0 !important;
  width: 11px !important;
  height: 10px !important;
  background: #ea4335 !important;
  border-radius: 4px 0 0 0 !important;
}

.logo-yellow {
  left: 0 !important;
  bottom: 0 !important;
  width: 11px !important;
  height: 10px !important;
  background: #fbbc04 !important;
  border-radius: 0 0 0 4px !important;
}

.logo-green {
  left: 0 !important;
  top: 9px !important;
  width: 13px !important;
  height: 10px !important;
  background: #34a853 !important;
  border-radius: 0 !important;
}

.home-brand-title {
  font-size: 1.45rem !important;
  line-height: 1 !important;
  letter-spacing: -0.025em !important;
  font-weight: 800 !important;
  color: #1f55d6 !important;
}

.home-brand-title span {
  color: #1a73e8 !important;
}

.home-brand-subtitle {
  margin-top: 4px !important;
  font-size: .84rem !important;
  line-height: 1.15 !important;
  color: #64748b !important;
}

.home-help-btn {
  width: 36px !important;
  height: 36px !important;
  min-width: 36px !important;
  border-radius: 999px !important;
  border: 1px solid #dfe6ef !important;
  background: #fff !important;
  color: #334155 !important;
  font-size: 1rem !important;
  font-weight: 800 !important;
  box-shadow: 0 3px 10px rgba(15, 23, 42, .05) !important;
}

.google-home-main {
  display: grid !important;
  grid-template-rows: auto auto !important;
  align-content: start !important;
  gap: 26px !important;
  padding: 36px 16px 0 !important;
  overflow: hidden !important;
}

.home-intro-panel {
  gap: 10px !important;
  padding: 0 14px !important;
}

.home-badge {
  display: none !important;
}

.home-intro-panel h1 {
  max-width: 14ch !important;
  margin: 0 !important;
  color: #17233d !important;
  font-size: clamp(2rem, 6.4vw, 2.45rem) !important;
  line-height: 1.08 !important;
  letter-spacing: -0.035em !important;
  font-weight: 780 !important;
}

.home-intro-panel p {
  max-width: 36ch !important;
  margin: 0 !important;
  color: #5f6b7c !important;
  font-size: clamp(.88rem, 2.45vw, 1rem) !important;
  line-height: 1.42 !important;
}

.home-actions-panel {
  display: grid !important;
  gap: 12px !important;
  align-content: start !important;
}

.home-option-card {
  min-height: 104px !important;
  padding: 14px !important;
  grid-template-columns: 82px minmax(0, 1fr) 36px !important;
  gap: 16px !important;
  border: 1px solid #e8edf5 !important;
  border-left: 1px solid #e8edf5 !important;
  border-radius: 12px !important;
  background: #ffffff !important;
  box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
}

.home-option-card--blue,
.home-option-card--green {
  border-left: 1px solid #e8edf5 !important;
}

.home-option-icon {
  width: 78px !important;
  height: 78px !important;
  min-width: 78px !important;
  border-radius: 12px !important;
  background: #eaf4ff !important;
  overflow: hidden !important;
}

.home-option-card--green .home-option-icon {
  background: #eaf8ed !important;
}

.home-option-text {
  gap: 6px !important;
}

.home-option-text strong {
  color: #172033 !important;
  font-size: clamp(1.1rem, 3.1vw, 1.24rem) !important;
  line-height: 1.16 !important;
  letter-spacing: -0.02em !important;
  font-weight: 760 !important;
}

.home-option-text span {
  color: #5f6b7c !important;
  font-size: clamp(.82rem, 2.15vw, .92rem) !important;
  line-height: 1.35 !important;
}

.home-option-arrow {
  width: 34px !important;
  height: 34px !important;
  min-width: 34px !important;
  display: grid !important;
  place-items: center !important;
  border-radius: 999px !important;
  background: #eaf2ff !important;
  color: #1a73e8 !important;
  font-family: Arial, Helvetica, sans-serif !important;
  font-size: 1.45rem !important;
  font-weight: 900 !important;
  line-height: .9 !important;
}

.home-option-card--green .home-option-arrow {
  background: #eaf8ed !important;
  color: #24a148 !important;
}

.home-bottom-nav {
  width: auto !important;
  margin: 0 16px max(8px, env(safe-area-inset-bottom)) !important;
  padding: 10px 0 8px !important;
  border: 0 !important;
  border-top: 1px solid #e6ebf2 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  background: #fff !important;
  display: grid !important;
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  gap: 0 !important;
}

.home-bottom-nav button {
  width: 86px !important;
  min-height: 58px !important;
  justify-self: center !important;
  border-radius: 12px !important;
  padding: 8px 10px !important;
  color: #64748b !important;
  background: transparent !important;
}

.home-bottom-nav button.active {
  background: #eaf2ff !important;
  color: #1a73e8 !important;
}

.home-bottom-nav button svg {
  width: 22px !important;
  height: 22px !important;
}

.home-bottom-nav button span {
  font-size: .78rem !important;
  font-weight: 650 !important;
}

@media (min-width: 500px) {
  .google-home {
    width: min(100vw, 430px) !important;
    margin: 0 auto !important;
    border: 1px solid #e6ebf2 !important;
    border-radius: 0 0 16px 16px !important;
  }
}

@media (orientation: landscape) and (min-width: 700px) {
  .google-home {
    width: min(100vw, 720px) !important;
  }

  .google-home-header {
    padding: max(14px, env(safe-area-inset-top)) 26px 0 !important;
  }

  .google-home-main {
    grid-template-columns: minmax(230px, .86fr) minmax(350px, 1.14fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    gap: 24px !important;
    padding: 14px 26px 0 !important;
  }

  .home-intro-panel {
    padding: 0 !important;
  }

  .home-intro-panel h1 {
    max-width: 13ch !important;
    font-size: clamp(1.75rem, 3.2vw, 2.25rem) !important;
  }

  .home-intro-panel p {
    font-size: clamp(.76rem, 1.25vw, .9rem) !important;
  }

  .home-option-card {
    min-height: 88px !important;
    grid-template-columns: 66px minmax(0, 1fr) 34px !important;
    gap: 12px !important;
    padding: 10px 12px !important;
  }

  .home-option-icon {
    width: 64px !important;
    height: 64px !important;
    min-width: 64px !important;
  }

  .home-option-text strong {
    font-size: 1rem !important;
  }

  .home-option-text span {
    font-size: .76rem !important;
  }
}

/* ===== FULL WIDTH FINAL — ocupar todo o ecrã ===== */
.google-home {
  width: 100vw !important;
  max-width: 100vw !important;
  margin: 0 !important;
  border: 0 !important;
  border-radius: 0 !important;
}

@media (min-width: 500px) {
  .google-home {
    width: 100vw !important;
    max-width: 100vw !important;
    margin: 0 !important;
    border: 0 !important;
    border-radius: 0 !important;
  }
}

@media (orientation: landscape) and (min-width: 700px) {
  .google-home {
    width: 100vw !important;
    max-width: 100vw !important;
  }

  .google-home-main {
    grid-template-columns: minmax(0, .92fr) minmax(420px, 1.08fr) !important;
    padding-left: clamp(28px, 6vw, 76px) !important;
    padding-right: clamp(28px, 6vw, 76px) !important;
  }

  .home-actions-panel {
    max-width: none !important;
  }
}

/* ===== HOME REQUESTED TUNING — header menor, badge e cartões maiores ===== */
.google-home-header {
  padding-top: max(20px, env(safe-area-inset-top)) !important;
}

.home-logo-mark {
  width: 24px !important;
  height: 24px !important;
  min-width: 24px !important;
}

.logo-blue {
  width: 24px !important;
  height: 24px !important;
}

.logo-red {
  width: 9px !important;
  height: 9px !important;
}

.logo-yellow {
  width: 9px !important;
  height: 9px !important;
}

.logo-green {
  top: 8px !important;
  width: 11px !important;
  height: 9px !important;
}

.home-brand-title {
  font-size: clamp(1.18rem, 4.2vw, 1.55rem) !important;
  letter-spacing: -0.018em !important;
}

.home-brand-subtitle {
  margin-top: 2px !important;
  font-size: clamp(.68rem, 2.1vw, .78rem) !important;
}

.google-home-main {
  padding-top: 18px !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
  gap: 18px !important;
}

.home-intro-panel {
  grid-template-columns: minmax(0, 1fr) auto !important;
  align-items: start !important;
  gap: 8px 12px !important;
}

.home-badge {
  display: inline-flex !important;
  grid-column: 2 !important;
  grid-row: 1 !important;
  justify-self: end !important;
  align-self: start !important;
  min-height: 30px !important;
  padding: 5px 12px !important;
  font-size: .86rem !important;
  white-space: nowrap !important;
}

.home-intro-panel h1 {
  grid-column: 1 / -1 !important;
  grid-row: 2 !important;
  font-size: clamp(1.62rem, 5.15vw, 2rem) !important;
}

.home-intro-panel p {
  grid-column: 1 / -1 !important;
  grid-row: 3 !important;
}

.home-actions-panel {
  align-content: stretch !important;
  grid-template-rows: repeat(2, minmax(126px, 1fr)) !important;
}

.home-option-card {
  min-height: 126px !important;
  grid-template-columns: clamp(94px, 24vw, 118px) minmax(0, 1fr) 46px !important;
  padding: 14px 16px !important;
}

.home-option-icon {
  width: clamp(94px, 24vw, 118px) !important;
  height: clamp(94px, 24vw, 118px) !important;
  min-width: clamp(94px, 24vw, 118px) !important;
}

.home-option-arrow {
  width: 46px !important;
  height: 46px !important;
  min-width: 46px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  font-size: 1.45rem !important;
  line-height: 1 !important;
  padding-bottom: 2px !important;
  overflow: hidden !important;
}

@media (max-width: 390px) and (orientation: portrait) {
  .home-actions-panel {
    grid-template-rows: repeat(2, minmax(112px, 1fr)) !important;
  }

  .home-option-card {
    min-height: 112px !important;
    grid-template-columns: 82px minmax(0, 1fr) 42px !important;
  }

  .home-option-icon {
    width: 82px !important;
    height: 82px !important;
    min-width: 82px !important;
  }

  .home-option-arrow {
    width: 42px !important;
    height: 42px !important;
    min-width: 42px !important;
    font-size: 1.35rem !important;
  }
}

@media (orientation: landscape) and (min-width: 700px) {
  .google-home-header {
    padding-top: max(10px, env(safe-area-inset-top)) !important;
  }

  .google-home-main {
    padding-top: 8px !important;
  }

  .home-intro-panel h1 {
    font-size: clamp(1.25rem, 2.15vw, 1.62rem) !important;
  }

  .home-actions-panel {
    grid-template-rows: repeat(2, minmax(104px, 1fr)) !important;
  }

  .home-option-card {
    min-height: 104px !important;
    grid-template-columns: clamp(78px, 10vw, 98px) minmax(0, 1fr) 44px !important;
  }

  .home-option-icon {
    width: clamp(78px, 10vw, 98px) !important;
    height: clamp(78px, 10vw, 98px) !important;
    min-width: clamp(78px, 10vw, 98px) !important;
  }

  .home-option-arrow {
    width: 44px !important;
    height: 44px !important;
    min-width: 44px !important;
  }
}

/* ===== HOME FINAL POLISH — badge à direita e rebordo subtil ===== */
.google-home {
  border: 1px solid #e7ecf4 !important;
  border-radius: 10px !important;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .72) !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .google-home-main {
    position: relative !important;
  }

  .home-badge {
    position: absolute !important;
    top: 10px !important;
    right: clamp(28px, 6vw, 76px) !important;
    grid-column: auto !important;
    grid-row: auto !important;
    z-index: 2 !important;
  }

  .home-intro-panel {
    padding-top: 0 !important;
  }
}

/* ===== HOME HORIZONTAL BALANCE — composição centrada e rebordo visível ===== */
.google-home {
  width: calc(100vw - 12px) !important;
  height: calc(100dvh - 12px) !important;
  min-height: calc(100dvh - 12px) !important;
  margin: 6px !important;
  border: 1px solid #d9e2ee !important;
  border-radius: 14px !important;
  box-shadow: 0 8px 24px rgba(15, 23, 42, .07) !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .google-home-header {
    position: relative !important;
  }

  .google-home-main {
    grid-template-columns: minmax(260px, 420px) minmax(420px, 620px) !important;
    justify-content: center !important;
    align-items: center !important;
    gap: clamp(30px, 5vw, 64px) !important;
    padding-left: clamp(28px, 5vw, 64px) !important;
    padding-right: clamp(28px, 5vw, 64px) !important;
  }

  .home-intro-panel {
    justify-self: center !important;
    align-self: center !important;
    width: min(100%, 420px) !important;
  }

  .home-actions-panel {
    justify-self: center !important;
    align-self: center !important;
    width: min(100%, 620px) !important;
  }

  .home-badge {
    top: calc(max(10px, env(safe-area-inset-top)) + 2px) !important;
    right: clamp(72px, 7vw, 98px) !important;
  }
}

/* ===== HOME HORIZONTAL NOTES — badge no topo e grelha centrada ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .google-home {
    position: relative !important;
    width: calc(100vw - 16px) !important;
    height: calc(100dvh - 16px) !important;
    min-height: calc(100dvh - 16px) !important;
    margin: 8px !important;
    border: 1px solid #cfd9e6 !important;
    border-radius: 18px !important;
    box-shadow: 0 10px 28px rgba(15, 23, 42, .09) !important;
  }

  .google-home-header {
    padding: max(12px, env(safe-area-inset-top)) clamp(24px, 4vw, 42px) 0 !important;
  }

  .google-home-main {
    position: static !important;
    grid-template-columns: minmax(360px, 440px) minmax(440px, 590px) !important;
    justify-content: center !important;
    align-items: center !important;
    column-gap: clamp(24px, 3.6vw, 46px) !important;
    padding: 0 clamp(34px, 5.5vw, 76px) 8px !important;
  }

  .home-intro-panel {
    justify-self: start !important;
    width: min(100%, 420px) !important;
    max-width: 420px !important;
  }

  .home-intro-panel h1 {
    max-width: 17ch !important;
    font-size: clamp(1.45rem, 2.35vw, 1.95rem) !important;
    line-height: 1.1 !important;
  }

  .home-intro-panel p {
    max-width: 38ch !important;
    font-size: clamp(.8rem, 1.15vw, .92rem) !important;
    line-height: 1.35 !important;
  }

  .home-actions-panel {
    justify-self: start !important;
    width: min(100%, 590px) !important;
    max-width: 590px !important;
  }

  .home-badge {
    position: absolute !important;
    top: max(13px, env(safe-area-inset-top)) !important;
    right: clamp(78px, 7.5vw, 110px) !important;
    grid-column: auto !important;
    grid-row: auto !important;
    z-index: 3 !important;
  }
}

/* ===== HOME HORIZONTAL FINAL POSITION ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .google-home-main {
    grid-template-columns: minmax(380px, 470px) minmax(430px, 570px) !important;
    justify-content: center !important;
    align-items: center !important;
    column-gap: clamp(22px, 3.5vw, 46px) !important;
    padding-left: clamp(18px, 3.2vw, 44px) !important;
    padding-right: clamp(28px, 4.5vw, 62px) !important;
  }

  .home-intro-panel {
    justify-self: start !important;
    align-self: center !important;
    width: min(100%, 470px) !important;
    max-width: 470px !important;
    transform: none !important;
  }

  .home-intro-panel h1,
  .home-intro-panel p {
    transform: translate(-28px, 34px) !important;
  }

  .home-intro-panel h1 {
    max-width: 16ch !important;
  }

  .home-intro-panel p {
    max-width: 39ch !important;
  }

  .home-actions-panel {
    justify-self: start !important;
    align-self: center !important;
    width: min(100%, 570px) !important;
    max-width: 570px !important;
    transform: translateY(58px) !important;
  }

  .home-badge {
    position: fixed !important;
    top: max(18px, env(safe-area-inset-top)) !important;
    right: clamp(72px, 7vw, 106px) !important;
    transform: none !important;
    z-index: 5 !important;
  }
}

/* ===== HOME BRAND ICON SIZE ===== */
.home-logo-mark {
  width: 32px !important;
  height: 32px !important;
  min-width: 32px !important;
}

.logo-blue {
  width: 32px !important;
  height: 32px !important;
  border-radius: 6px 16px 16px 6px !important;
}

.logo-red {
  width: 13px !important;
  height: 11px !important;
}

.logo-yellow {
  width: 13px !important;
  height: 11px !important;
}

.logo-green {
  top: 10px !important;
  width: 15px !important;
  height: 12px !important;
}

/* ===== FLOW SCREENS — visual alinhado com a home ===== */
.mode-card {
  width: calc(100vw - 16px) !important;
  min-height: calc(100dvh - 16px) !important;
  height: calc(100dvh - 16px) !important;
  margin: 8px !important;
  border: 1px solid #d9e2ee !important;
  border-radius: 18px !important;
  box-shadow: 0 10px 28px rgba(15, 23, 42, .09) !important;
  background: #fff !important;
  display: grid !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
}

.mode-card .player-top,
.mode-card .player-bottom {
  background: #fff !important;
  color: #172033 !important;
  border: 0 !important;
}

.mode-card .player-top {
  min-height: auto !important;
  padding: max(20px, env(safe-area-inset-top)) clamp(22px, 5vw, 34px) 10px !important;
}

.mode-card .player-bottom {
  padding: 8px clamp(22px, 5vw, 34px) max(18px, env(safe-area-inset-bottom)) !important;
}

.screen-brand {
  display: flex !important;
  align-items: center !important;
  gap: 9px !important;
  margin-bottom: clamp(20px, 5vh, 42px) !important;
}

.screen-brand .brand {
  color: #1f55d6 !important;
  font-size: clamp(1.35rem, 4.6vw, 1.75rem) !important;
  line-height: 1 !important;
}

.screen-brand .subtitle {
  margin: 3px 0 0 !important;
  color: #64748b !important;
  font-size: clamp(.78rem, 2.3vw, .95rem) !important;
}

.mode-card .intro-copy {
  max-width: 42ch !important;
  margin: 0 !important;
}

.mode-card .intro-copy h1 {
  color: #17233d !important;
  font-size: clamp(1.8rem, 7vw, 2.55rem) !important;
  line-height: 1.06 !important;
  letter-spacing: -0.035em !important;
  font-weight: 800 !important;
  margin: 0 0 10px !important;
}

.mode-card .intro-copy p {
  color: #657185 !important;
  font-size: clamp(.92rem, 2.8vw, 1.08rem) !important;
  line-height: 1.38 !important;
}

.mode-card .mode-grid-wrap {
  display: grid !important;
  align-content: start !important;
  gap: 14px !important;
}

.mode-card .mode-grid {
  grid-template-columns: 1fr !important;
  gap: 12px !important;
}

.mode-card .mode-option {
  width: 100% !important;
  min-height: 112px !important;
  padding: 14px !important;
  border: 1px solid #e8edf5 !important;
  border-radius: 14px !important;
  background: #fff !important;
  box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
  display: grid !important;
  grid-template-columns: 78px minmax(0, 1fr) !important;
  grid-template-rows: auto auto !important;
  column-gap: 16px !important;
  row-gap: 6px !important;
  align-items: center !important;
}

.mode-card .mode-icon {
  grid-row: 1 / 3 !important;
  width: 78px !important;
  height: 78px !important;
  border-radius: 12px !important;
  display: grid !important;
  place-items: center !important;
  overflow: hidden !important;
  background: #eaf4ff !important;
  font-size: 0 !important;
}

.mode-card .mode-icon svg {
  width: 100% !important;
  height: 100% !important;
  display: block !important;
}

.mode-card .mode-icon--gallery {
  background: #eaf8ed !important;
}

.mode-card .mode-option strong {
  color: #172033 !important;
  font-size: clamp(1.12rem, 3.4vw, 1.35rem) !important;
  line-height: 1.12 !important;
  letter-spacing: -0.02em !important;
}

.mode-card .mode-option span:last-child {
  color: #5f6b7c !important;
  font-size: clamp(.84rem, 2.25vw, .98rem) !important;
  line-height: 1.35 !important;
}

.mode-card .subtle-btn {
  background: #eef5ff !important;
  color: #1a73e8 !important;
  border: 1px solid #dbeafe !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .mode-card {
    grid-template-columns: minmax(340px, 460px) minmax(460px, 620px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    justify-content: center !important;
    align-items: center !important;
    column-gap: clamp(26px, 4vw, 58px) !important;
  }

  .mode-card .player-top {
    align-self: center !important;
    padding: 0 !important;
  }

  .mode-card .player-bottom {
    align-self: center !important;
    padding: 0 !important;
  }

  .screen-brand {
    margin-bottom: 30px !important;
  }

  .mode-card .intro-copy h1 {
    font-size: clamp(1.55rem, 2.7vw, 2.15rem) !important;
  }

  .mode-card .intro-copy p {
    font-size: clamp(.82rem, 1.25vw, .96rem) !important;
  }

  .mode-card .mode-option {
    min-height: 108px !important;
    grid-template-columns: 76px minmax(0, 1fr) !important;
  }

  .mode-card .mode-icon {
    width: 76px !important;
    height: 76px !important;
  }
}

/* ===== ASSISTED IMAGE SOURCE — mesmo sistema da home ===== */
.assisted-home .google-home-main {
  padding-top: 18px !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
  gap: 18px !important;
}

.assisted-home .home-badge {
  position: static !important;
  transform: none !important;
  grid-column: auto !important;
  grid-row: auto !important;
  justify-self: start !important;
  display: inline-flex !important;
}

.assisted-home .assisted-intro-panel h1,
.assisted-home .assisted-intro-panel p {
  transform: none !important;
}

.assisted-home .assisted-intro-panel h1 {
  max-width: 13ch !important;
  font-size: clamp(1.82rem, 5.8vw, 2.25rem) !important;
}

.assisted-home .assisted-intro-panel p {
  max-width: 34ch !important;
}

.assisted-home .assisted-actions-panel {
  align-content: stretch !important;
  grid-template-rows: repeat(2, minmax(126px, 1fr)) !important;
}

.assisted-home .assisted-option-card {
  min-height: 126px !important;
  grid-template-columns: clamp(94px, 24vw, 118px) minmax(0, 1fr) 46px !important;
}

.assisted-home .assisted-option-icon {
  width: clamp(94px, 24vw, 118px) !important;
  height: clamp(94px, 24vw, 118px) !important;
  min-width: clamp(94px, 24vw, 118px) !important;
}

.assisted-home .assisted-option-icon svg {
  width: 100% !important;
  height: 100% !important;
  display: block !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .assisted-home .google-home-main {
    position: static !important;
    grid-template-columns: minmax(380px, 470px) minmax(430px, 570px) !important;
    justify-content: center !important;
    align-items: center !important;
    column-gap: clamp(22px, 3.5vw, 46px) !important;
    padding: 0 clamp(28px, 4.5vw, 62px) 8px clamp(18px, 3.2vw, 44px) !important;
  }

  .assisted-home .home-badge {
    position: static !important;
    transform: none !important;
    top: auto !important;
    right: auto !important;
  }

  .assisted-home .assisted-intro-panel {
    justify-self: start !important;
    align-self: center !important;
    width: min(100%, 470px) !important;
    max-width: 470px !important;
    transform: translate(-28px, 34px) !important;
  }

  .assisted-home .assisted-intro-panel h1,
  .assisted-home .assisted-intro-panel p {
    transform: none !important;
  }

  .assisted-home .assisted-intro-panel h1 {
    max-width: 15ch !important;
    font-size: clamp(1.45rem, 2.35vw, 1.95rem) !important;
  }

  .assisted-home .assisted-intro-panel p {
    max-width: 34ch !important;
    font-size: clamp(.8rem, 1.15vw, .92rem) !important;
  }

  .assisted-home .assisted-actions-panel {
    justify-self: start !important;
    align-self: center !important;
    width: min(100%, 570px) !important;
    max-width: 570px !important;
    transform: translateY(58px) !important;
    grid-template-rows: repeat(2, minmax(104px, 1fr)) !important;
  }

  .assisted-home .assisted-option-card {
    min-height: 104px !important;
    grid-template-columns: clamp(78px, 10vw, 98px) minmax(0, 1fr) 44px !important;
  }

  .assisted-home .assisted-option-icon {
    width: clamp(78px, 10vw, 98px) !important;
    height: clamp(78px, 10vw, 98px) !important;
    min-width: clamp(78px, 10vw, 98px) !important;
  }
}

/* ===== SPEECH FLOW — alinhado com home/leitura assistida ===== */
.speech-home .google-home-main {
  padding-top: 18px !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
  gap: 14px !important;
}

.speech-home .speech-intro-panel h1,
.speech-home .speech-intro-panel p {
  transform: none !important;
}

.speech-home .speech-intro-panel h1 {
  max-width: 13ch !important;
  font-size: clamp(1.82rem, 5.8vw, 2.25rem) !important;
}

.speech-home .speech-intro-panel p {
  max-width: 34ch !important;
}

.speech-work-panel {
  min-height: 0 !important;
  display: grid !important;
  gap: 10px !important;
  align-content: start !important;
}

.speech-config-card,
.speech-phrase-card,
.speech-record-card,
.speech-record-controls {
  border: 1px solid #e8edf5 !important;
  border-radius: 14px !important;
  background: #fff !important;
  box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
  padding: 12px !important;
}

.speech-config-grid {
  display: grid !important;
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  gap: 10px !important;
}

.speech-config-grid label {
  display: grid !important;
  gap: 5px !important;
  color: #64748b !important;
  font-size: .82rem !important;
  font-weight: 700 !important;
}

.speech-config-grid label:last-child {
  grid-column: 1 / -1 !important;
}

.speech-config-grid select {
  width: 100% !important;
  border: 1px solid #e5eaf2 !important;
  background: #f8fbff !important;
  border-radius: 12px !important;
  padding: 9px 10px !important;
  color: #172033 !important;
}

.speech-button-row {
  display: flex !important;
  gap: 8px !important;
  flex-wrap: wrap !important;
  margin-top: 10px !important;
}

.speech-button-row .soft-action {
  flex: 1 1 auto !important;
  background: #eef5ff !important;
  color: #1a73e8 !important;
}

.speech-phrase-card {
  display: grid !important;
  gap: 5px !important;
}

.speech-phrase-card strong {
  color: #172033 !important;
  font-size: clamp(1rem, 2.7vw, 1.24rem) !important;
  line-height: 1.25 !important;
}

.speech-record-card {
  display: grid !important;
  grid-template-columns: 74px minmax(0, 1fr) !important;
  gap: 12px !important;
  align-items: center !important;
}

.speech-record-icon {
  width: 74px !important;
  height: 74px !important;
  border-radius: 14px !important;
  overflow: hidden !important;
  background: #eaf8ed !important;
}

.speech-record-icon svg {
  width: 100% !important;
  height: 100% !important;
  display: block !important;
}

.speech-record-icon.active {
  animation: pulseMic 1s ease-in-out infinite;
}

.speech-record-copy {
  min-width: 0 !important;
  display: grid !important;
  gap: 7px !important;
}

.speech-home .status-file {
  color: #64748b !important;
}

.speech-home .audio-player {
  width: 100% !important;
}

.speech-record-controls {
  display: grid !important;
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  gap: 8px !important;
}

.speech-record-controls button {
  width: 100% !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .speech-home .google-home-main {
    position: static !important;
    grid-template-columns: minmax(380px, 470px) minmax(430px, 570px) !important;
    justify-content: center !important;
    align-items: center !important;
    column-gap: clamp(22px, 3.5vw, 46px) !important;
    padding: 0 clamp(28px, 4.5vw, 62px) 8px clamp(18px, 3.2vw, 44px) !important;
  }

  .speech-home .speech-intro-panel {
    justify-self: start !important;
    align-self: center !important;
    width: min(100%, 470px) !important;
    max-width: 470px !important;
    transform: translate(-28px, 34px) !important;
  }

  .speech-home .speech-intro-panel h1,
  .speech-home .speech-intro-panel p {
    transform: none !important;
  }

  .speech-home .speech-intro-panel h1 {
    max-width: 15ch !important;
    font-size: clamp(1.45rem, 2.35vw, 1.95rem) !important;
  }

  .speech-home .speech-intro-panel p {
    max-width: 34ch !important;
    font-size: clamp(.8rem, 1.15vw, .92rem) !important;
  }

  .speech-work-panel {
    justify-self: start !important;
    align-self: center !important;
    width: min(100%, 570px) !important;
    max-width: 570px !important;
    transform: translateY(42px) !important;
    gap: 8px !important;
  }

  .speech-config-card,
  .speech-phrase-card,
  .speech-record-card,
  .speech-record-controls {
    padding: 10px 12px !important;
  }

  .speech-record-card {
    grid-template-columns: 64px minmax(0, 1fr) !important;
  }

  .speech-record-icon {
    width: 64px !important;
    height: 64px !important;
  }

  .speech-record-controls {
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
  }
}

/* ===== SPEECH LANDSCAPE — intro compacta no topo, controlos largos ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .speech-home .google-home-main {
    grid-template-columns: minmax(0, 1080px) !important;
    grid-template-rows: auto minmax(0, 1fr) !important;
    justify-content: center !important;
    align-content: start !important;
    row-gap: 8px !important;
    padding: 0 clamp(34px, 5vw, 70px) 8px !important;
  }

  .speech-home .speech-intro-panel {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    justify-self: center !important;
    align-self: end !important;
    transform: none !important;
    display: grid !important;
    grid-template-columns: minmax(0, auto) minmax(260px, 1fr) !important;
    column-gap: 18px !important;
    align-items: center !important;
    margin-top: -2px !important;
  }

  .speech-home .speech-intro-panel h1 {
    grid-column: 1 !important;
    max-width: none !important;
    font-size: clamp(1rem, 1.45vw, 1.28rem) !important;
    line-height: 1.1 !important;
    margin: 0 !important;
    white-space: nowrap !important;
  }

  .speech-home .speech-intro-panel p {
    grid-column: 2 !important;
    max-width: 60ch !important;
    font-size: clamp(.68rem, .9vw, .8rem) !important;
    line-height: 1.25 !important;
    margin: 0 !important;
  }

  .speech-work-panel {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    justify-self: center !important;
    align-self: start !important;
    transform: none !important;
    display: grid !important;
    grid-template-columns: minmax(260px, .95fr) minmax(280px, 1.05fr) !important;
    grid-template-rows: auto auto !important;
    gap: 10px !important;
  }

  .speech-config-grid {
    gap: 7px !important;
  }

  .speech-config-card {
    grid-column: 1 !important;
    grid-row: 1 / 3 !important;
  }

  .speech-phrase-card {
    grid-column: 2 !important;
    grid-row: 1 !important;
  }

  .speech-record-card {
    grid-column: 2 !important;
    grid-row: 2 !important;
  }

  .speech-record-controls {
    grid-column: 1 / -1 !important;
    grid-row: 3 !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
  }

  .speech-config-card,
  .speech-phrase-card,
  .speech-record-card,
  .speech-record-controls {
    padding: 8px 10px !important;
  }

  .speech-config-grid select {
    padding: 7px 9px !important;
  }

  .speech-button-row {
    margin-top: 8px !important;
  }

  .speech-phrase-card strong {
    font-size: clamp(.95rem, 1.55vw, 1.16rem) !important;
  }

  .speech-record-card {
    grid-template-columns: 56px minmax(0, 1fr) !important;
  }

  .speech-record-icon {
    width: 56px !important;
    height: 56px !important;
  }
}

/* ===== SPEECH LANDSCAPE FINAL FIT — topo fixo e conteúdo visível ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .speech-home .google-home-header {
    padding-top: max(9px, env(safe-area-inset-top)) !important;
    padding-bottom: 0 !important;
  }

  .speech-home .google-home-main {
    grid-template-columns: minmax(0, 1080px) !important;
    grid-template-rows: auto auto !important;
    row-gap: 5px !important;
    align-content: start !important;
    padding: 0 clamp(34px, 5vw, 70px) 2px !important;
  }

  .speech-home .speech-intro-panel {
    min-height: 32px !important;
    margin-top: -3px !important;
    grid-template-columns: auto minmax(250px, 1fr) !important;
    column-gap: 16px !important;
    align-items: center !important;
  }

  .speech-home .speech-intro-panel h1 {
    font-size: clamp(1.08rem, 1.55vw, 1.34rem) !important;
    line-height: 1.05 !important;
  }

  .speech-home .speech-intro-panel p {
    max-width: none !important;
    font-size: clamp(.7rem, .95vw, .84rem) !important;
    line-height: 1.18 !important;
  }

  .speech-work-panel {
    transform: translateY(-8px) !important;
    gap: 7px !important;
    align-content: start !important;
  }

  .speech-config-card,
  .speech-phrase-card,
  .speech-record-card,
  .speech-record-controls {
    padding: 7px 10px !important;
    border-radius: 13px !important;
  }

  .speech-config-grid {
    gap: 6px !important;
  }

  .speech-config-grid label {
    gap: 3px !important;
    font-size: .72rem !important;
  }

  .speech-config-grid select {
    min-height: 34px !important;
    padding: 5px 8px !important;
    border-radius: 10px !important;
    font-size: .86rem !important;
  }

  .speech-button-row {
    margin-top: 6px !important;
  }

  .speech-button-row .soft-action,
  .speech-record-controls button {
    min-height: 42px !important;
    padding: 8px 10px !important;
    font-size: .9rem !important;
  }

  .speech-phrase-card {
    min-height: 82px !important;
    place-content: center !important;
  }

  .speech-phrase-card strong {
    font-size: clamp(.9rem, 1.42vw, 1.08rem) !important;
    line-height: 1.16 !important;
  }

  .speech-record-card {
    min-height: 72px !important;
    grid-template-columns: 50px minmax(0, 1fr) !important;
  }

  .speech-record-icon {
    width: 50px !important;
    height: 50px !important;
  }

  .speech-record-copy {
    gap: 4px !important;
  }

  .speech-home .status-pill {
    min-height: 36px !important;
    padding: 6px 12px !important;
    font-size: .92rem !important;
  }

  .speech-record-controls {
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    min-height: 48px !important;
  }
}

/* ===== SPEECH LANDSCAPE HEADER LINE — título ao nível da marca ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .speech-home {
    position: relative !important;
  }

  .speech-home .google-home-header {
    min-height: 70px !important;
  }

  .speech-home .speech-intro-panel {
    position: absolute !important;
    top: max(18px, env(safe-area-inset-top)) !important;
    left: clamp(360px, 34vw, 500px) !important;
    right: clamp(70px, 6vw, 108px) !important;
    min-height: 0 !important;
    width: auto !important;
    max-width: none !important;
    z-index: 3 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    row-gap: 3px !important;
    align-items: start !important;
    margin: 0 !important;
    padding: 0 !important;
    pointer-events: none !important;
  }

  .speech-home .speech-intro-panel h1 {
    grid-column: 1 !important;
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
    white-space: nowrap !important;
  }

  .speech-home .speech-intro-panel p {
    grid-column: 1 !important;
    max-width: 38ch !important;
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
    white-space: normal !important;
  }

  .speech-home .google-home-main {
    grid-template-rows: minmax(0, 1fr) !important;
    padding-top: 16px !important;
  }

  .speech-work-panel {
    grid-row: 1 !important;
    justify-self: center !important;
    align-self: start !important;
    transform: translateY(34px) !important;
  }
}

/* ===== ALL SCREENS FINAL SYSTEM — revisão, processamento e leitor ===== */
.review-home .google-home-main,
.processing-home .google-home-main {
  grid-template-rows: auto minmax(0, 1fr) !important;
  gap: 14px !important;
  padding-top: 18px !important;
}

.review-intro-panel h1 {
  max-width: 13ch !important;
  font-size: clamp(1.82rem, 5.8vw, 2.25rem) !important;
}

.review-intro-panel p {
  max-width: 34ch !important;
}

.review-panel {
  min-height: 0 !important;
  display: grid !important;
  grid-template-rows: minmax(0, 1fr) auto !important;
  gap: 10px !important;
  align-content: stretch !important;
}

.review-preview-card,
.review-action-card,
.processing-panel {
  border: 1px solid #e8edf5 !important;
  border-radius: 14px !important;
  background: #fff !important;
  box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
}

.review-preview-card {
  min-height: 0 !important;
  display: grid !important;
  place-items: center !important;
  padding: 12px !important;
  overflow: hidden !important;
}

.review-home .preview-image {
  width: 100% !important;
  height: 100% !important;
  max-width: 100% !important;
  max-height: none !important;
  object-fit: contain !important;
  border-radius: 11px !important;
  box-shadow: none !important;
}

.review-action-card {
  display: grid !important;
  gap: 12px !important;
  padding: 12px !important;
}

.review-action-row {
  display: grid !important;
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  gap: 8px !important;
}

.review-action-row button {
  width: 100% !important;
}

.processing-home .google-home-main {
  align-content: center !important;
}

.processing-panel {
  width: min(100%, 560px) !important;
  justify-self: center !important;
  display: grid !important;
  gap: 14px !important;
  place-items: center !important;
  padding: 28px 18px !important;
  text-align: center !important;
}

.processing-panel h2 {
  margin: 0 !important;
  color: #172033 !important;
  font-size: clamp(1.35rem, 4vw, 2rem) !important;
  line-height: 1.08 !important;
  letter-spacing: -0.035em !important;
}

.processing-panel p {
  margin: 0 !important;
  color: #64748b !important;
  font-size: clamp(.9rem, 2.3vw, 1rem) !important;
}

.processing-panel .loader-ring {
  width: 58px !important;
  height: 58px !important;
  margin: 0 !important;
  border-color: #e8f0fe !important;
  border-top-color: #1a73e8 !important;
}

.processing-panel .progress-track {
  height: 8px !important;
  background: #e5eaf2 !important;
}

.processing-panel .progress-bar {
  background: #1a73e8 !important;
}

.reader-card {
  width: 100vw !important;
  height: 100dvh !important;
  max-height: 100dvh !important;
  border: 1px solid #e7ecf4 !important;
  border-radius: 10px !important;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .72) !important;
  background: #fff !important;
  overflow: hidden !important;
  display: grid !important;
  grid-template-rows: minmax(0, 1fr) auto !important;
}

.reader-top {
  height: auto !important;
  min-height: 0 !important;
  padding: 76px 18px 18px !important;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%) !important;
}

.reader-bottom {
  max-height: 48dvh !important;
  background: #fff !important;
  border-top: 1px solid #e6ebf2 !important;
  padding: 12px 16px max(12px, env(safe-area-inset-bottom)) !important;
}

.mini-brand {
  color: #1a73e8 !important;
  font-size: clamp(1.18rem, 4.2vw, 1.55rem) !important;
  letter-spacing: -0.018em !important;
}

.line-focus {
  color: #172033 !important;
  letter-spacing: -0.025em !important;
}

.speech-summary-box,
.settings-panel {
  background: #f8fbff !important;
  border: 1px solid #e8edf5 !important;
  box-shadow: 0 4px 12px rgba(15, 23, 42, .04) !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .review-home .google-home-main {
    grid-template-columns: minmax(300px, 420px) minmax(430px, 620px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    justify-content: center !important;
    align-items: center !important;
    column-gap: clamp(22px, 4vw, 54px) !important;
    padding: 2px clamp(34px, 5vw, 70px) 8px !important;
  }

  .review-intro-panel {
    justify-self: start !important;
    align-self: center !important;
    max-width: 420px !important;
  }

  .review-intro-panel h1 {
    max-width: 14ch !important;
    font-size: clamp(1.45rem, 2.35vw, 1.95rem) !important;
  }

  .review-intro-panel p {
    max-width: 34ch !important;
    font-size: clamp(.78rem, 1.35vw, .98rem) !important;
  }

  .review-panel {
    width: min(100%, 620px) !important;
    max-height: calc(100dvh - 138px) !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    align-self: center !important;
  }

  .review-action-card {
    grid-template-columns: minmax(0, 1fr) minmax(220px, auto) !important;
    align-items: center !important;
  }

  .review-action-row {
    grid-template-columns: repeat(2, minmax(100px, 1fr)) !important;
  }

  .processing-home .google-home-main {
    grid-template-columns: minmax(0, 720px) !important;
    justify-content: center !important;
    align-content: center !important;
    padding: 0 clamp(34px, 5vw, 70px) 8px !important;
  }

  .processing-panel {
    padding: 24px !important;
  }

  .reader-card {
    width: 100vw !important;
    height: 100dvh !important;
  }

  .reader-top {
    padding: 64px 24px 16px !important;
  }

  .reader-bottom {
    max-height: 46dvh !important;
    padding: 10px 18px max(10px, env(safe-area-inset-bottom)) !important;
  }

  .controls-compact {
    gap: 7px !important;
  }

  .pill-btn {
    padding: 8px 12px !important;
    font-size: .82rem !important;
  }

  .settings-panel {
    padding: 10px !important;
    gap: 10px !important;
  }
}

/* ===== REVIEW IMAGE LANDSCAPE FIX — imagem visível e texto legível ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .review-home .google-home-main {
    grid-template-columns: minmax(250px, 340px) minmax(480px, 620px) !important;
    column-gap: clamp(22px, 4vw, 48px) !important;
    align-items: center !important;
    padding-top: 0 !important;
    padding-bottom: 8px !important;
  }

  .review-home .review-intro-panel {
    transform: translateY(18px) !important;
  }

  .review-home .review-intro-panel h1 {
    font-size: clamp(1.65rem, 3vw, 2.28rem) !important;
    line-height: 1.05 !important;
  }

  .review-home .review-intro-panel p {
    font-size: clamp(.84rem, 1.42vw, 1.02rem) !important;
    line-height: 1.35 !important;
  }

  .review-home .review-panel {
    width: min(100%, 620px) !important;
    height: min(430px, calc(100dvh - 128px)) !important;
    max-height: none !important;
    display: grid !important;
    grid-template-rows: minmax(170px, 1fr) auto !important;
    gap: 10px !important;
    align-self: center !important;
    overflow: visible !important;
  }

  .review-home .review-preview-card {
    min-height: 170px !important;
    padding: 10px !important;
  }

  .review-home .preview-image {
    display: block !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 150px !important;
    object-fit: contain !important;
  }

  .review-home .review-action-card {
    display: grid !important;
    grid-template-columns: 1fr !important;
    gap: 8px !important;
    padding: 11px 12px !important;
  }

  .review-home .confirm-text {
    display: grid !important;
    gap: 4px !important;
    min-width: 0 !important;
    max-width: none !important;
    text-align: left !important;
  }

  .review-home .confirm-text strong {
    font-size: clamp(1rem, 1.7vw, 1.18rem) !important;
    line-height: 1.12 !important;
    white-space: normal !important;
  }

  .review-home .confirm-text span {
    font-size: clamp(.78rem, 1.25vw, .92rem) !important;
    line-height: 1.25 !important;
    white-space: normal !important;
  }

  .review-home .review-action-row {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
  }

  .review-home .review-action-row button {
    min-height: 42px !important;
    padding: 8px 12px !important;
    font-size: .9rem !important;
  }
}

/* ===== REVIEW IMAGE FINAL TUNING — titulo numa linha e painel equilibrado ===== */
.review-home .review-intro-panel h1 {
  max-width: none !important;
  white-space: nowrap !important;
  font-size: clamp(1.7rem, 5vw, 2.15rem) !important;
  line-height: 1.02 !important;
}

.review-home .review-intro-panel p {
  font-size: clamp(.9rem, 2.55vw, 1rem) !important;
  line-height: 1.32 !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .review-home .google-home-main {
    grid-template-columns: minmax(390px, 470px) minmax(520px, 660px) !important;
    column-gap: clamp(18px, 3vw, 36px) !important;
  }

  .review-home .review-intro-panel {
    max-width: 470px !important;
    transform: translateY(10px) !important;
  }

  .review-home .review-intro-panel h1 {
    font-size: clamp(1.45rem, 2.25vw, 1.9rem) !important;
  }

  .review-home .review-intro-panel p {
    max-width: 36ch !important;
    font-size: clamp(.74rem, 1.2vw, .9rem) !important;
  }

  .review-home .review-panel {
    width: min(100%, 660px) !important;
    height: min(420px, calc(100dvh - 126px)) !important;
    grid-template-rows: minmax(128px, .62fr) minmax(112px, auto) !important;
    gap: 8px !important;
  }

  .review-home .review-preview-card {
    min-height: 128px !important;
    padding: 8px !important;
  }

  .review-home .preview-image {
    min-height: 110px !important;
    max-height: 100% !important;
  }

  .review-home .review-action-card {
    padding: 12px 14px !important;
    gap: 10px !important;
  }

  .review-home .confirm-text strong {
    font-size: clamp(.92rem, 1.38vw, 1.05rem) !important;
  }

  .review-home .confirm-text span {
    max-width: 48ch !important;
    font-size: clamp(.72rem, 1.05vw, .82rem) !important;
    line-height: 1.2 !important;
  }
}

/* ===== REVIEW IMAGE HEADER LAYOUT — igual ao ecrã da fala ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .review-home {
    position: relative !important;
  }

  .review-home .google-home-header {
    min-height: 70px !important;
    padding-top: max(9px, env(safe-area-inset-top)) !important;
    padding-bottom: 0 !important;
  }

  .review-home .review-intro-panel {
    position: absolute !important;
    top: max(18px, env(safe-area-inset-top)) !important;
    left: clamp(360px, 34vw, 500px) !important;
    right: clamp(70px, 6vw, 108px) !important;
    z-index: 3 !important;
    width: auto !important;
    max-width: none !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    row-gap: 3px !important;
    align-items: start !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
    pointer-events: none !important;
  }

  .review-home .review-intro-panel h1 {
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
    white-space: nowrap !important;
  }

  .review-home .review-intro-panel p {
    max-width: 38ch !important;
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
    white-space: normal !important;
  }

  .review-home .google-home-main {
    grid-template-columns: minmax(0, 820px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    justify-content: center !important;
    align-content: start !important;
    padding: 6px clamp(34px, 5vw, 70px) 8px !important;
  }

  .review-home .review-panel {
    width: min(100%, 820px) !important;
    height: min(390px, calc(100dvh - 128px)) !important;
    justify-self: center !important;
    align-self: start !important;
    display: grid !important;
    grid-template-columns: minmax(0, .92fr) minmax(340px, 1.08fr) !important;
    grid-template-rows: minmax(92px, 130px) minmax(128px, auto) !important;
    gap: 10px !important;
    transform: translateY(8px) !important;
  }

  .review-home .review-preview-card {
    grid-column: 1 / -1 !important;
    grid-row: 1 !important;
    min-height: 92px !important;
    padding: 7px !important;
  }

  .review-home .preview-image {
    min-height: 78px !important;
    object-fit: contain !important;
  }

  .review-home .review-action-card {
    grid-column: 2 !important;
    grid-row: 2 !important;
    align-self: start !important;
    min-height: 126px !important;
    padding: 14px !important;
    gap: 12px !important;
  }

  .review-home .review-action-row button {
    min-height: 44px !important;
    font-size: .92rem !important;
  }
}

/* ===== REVIEW IMAGE LANDSCAPE FINAL — cabeçalho e zona inferior cheia ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .review-home .review-intro-panel {
    top: max(14px, env(safe-area-inset-top)) !important;
    left: clamp(360px, 33vw, 470px) !important;
    right: clamp(72px, 7vw, 120px) !important;
    row-gap: 2px !important;
  }

  .review-home .review-intro-panel h1 {
    font-size: clamp(1.05rem, 1.55vw, 1.32rem) !important;
  }

  .review-home .review-intro-panel p {
    max-width: 44ch !important;
    font-size: clamp(.68rem, .9vw, .8rem) !important;
  }

  .review-home .google-home-main {
    grid-template-columns: minmax(0, 900px) !important;
    padding: 4px clamp(34px, 5vw, 70px) 8px !important;
  }

  .review-home .review-panel {
    width: min(100%, 900px) !important;
    height: min(405px, calc(100dvh - 126px)) !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(88px, 112px) minmax(0, 1fr) !important;
    gap: 10px !important;
    transform: translateY(4px) !important;
  }

  .review-home .review-preview-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
    min-height: 88px !important;
    max-height: 112px !important;
  }

  .review-home .preview-image {
    min-height: 74px !important;
  }

  .review-home .review-action-card {
    grid-column: 1 !important;
    grid-row: 2 !important;
    width: 100% !important;
    min-height: 0 !important;
    height: 100% !important;
    align-self: stretch !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) minmax(280px, .8fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    column-gap: clamp(16px, 3vw, 36px) !important;
    padding: clamp(18px, 3vh, 28px) clamp(20px, 4vw, 38px) !important;
  }

  .review-home .confirm-text {
    align-self: center !important;
    gap: 8px !important;
  }

  .review-home .confirm-text strong {
    font-size: clamp(1.18rem, 2vw, 1.45rem) !important;
  }

  .review-home .confirm-text span {
    max-width: 48ch !important;
    font-size: clamp(.9rem, 1.35vw, 1.05rem) !important;
    line-height: 1.32 !important;
  }

  .review-home .review-action-row {
    align-self: center !important;
    grid-template-columns: 1fr !important;
    gap: 10px !important;
  }

  .review-home .review-action-row button {
    min-height: 50px !important;
    font-size: clamp(.95rem, 1.45vw, 1.08rem) !important;
  }
}

/* ===== REVIEW IMAGE MATCH SPEECH SCREEN — mesma abordagem de fala ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .review-home {
    position: relative !important;
  }

  .review-home .google-home-header {
    min-height: 70px !important;
    padding-top: max(9px, env(safe-area-inset-top)) !important;
    padding-bottom: 0 !important;
  }

  .review-home .review-intro-panel {
    position: absolute !important;
    top: max(18px, env(safe-area-inset-top)) !important;
    left: clamp(360px, 34vw, 500px) !important;
    right: clamp(70px, 6vw, 108px) !important;
    width: auto !important;
    max-width: none !important;
    min-height: 0 !important;
    z-index: 3 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    row-gap: 3px !important;
    align-items: start !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
    pointer-events: none !important;
  }

  .review-home .review-intro-panel h1 {
    grid-column: 1 !important;
    max-width: none !important;
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
    white-space: nowrap !important;
    margin: 0 !important;
  }

  .review-home .review-intro-panel p {
    grid-column: 1 !important;
    max-width: 38ch !important;
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
    white-space: normal !important;
    margin: 0 !important;
  }

  .review-home .google-home-main {
    grid-template-columns: minmax(0, 1080px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    justify-content: center !important;
    align-content: start !important;
    padding: 16px clamp(34px, 5vw, 70px) 8px !important;
  }

  .review-home .review-panel {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    height: auto !important;
    max-height: none !important;
    justify-self: center !important;
    align-self: start !important;
    transform: translateY(34px) !important;
    display: grid !important;
    grid-template-columns: minmax(280px, .95fr) minmax(320px, 1.05fr) !important;
    grid-template-rows: auto auto !important;
    gap: 10px !important;
  }

  .review-home .review-preview-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
    min-height: 170px !important;
    max-height: 220px !important;
    padding: 8px !important;
  }

  .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    min-height: 150px !important;
    object-fit: contain !important;
  }

  .review-home .review-action-card {
    grid-column: 2 !important;
    grid-row: 1 !important;
    min-height: 170px !important;
    height: auto !important;
    align-self: stretch !important;
    display: grid !important;
    grid-template-columns: 1fr !important;
    grid-template-rows: minmax(0, 1fr) 74px !important;
    gap: 10px !important;
    padding: 12px !important;
  }

  .review-home .confirm-text {
    align-self: center !important;
    gap: 6px !important;
    text-align: left !important;
  }

  .review-home .confirm-text strong {
    font-size: clamp(1rem, 1.65vw, 1.22rem) !important;
    line-height: 1.12 !important;
  }

  .review-home .confirm-text span {
    max-width: 44ch !important;
    font-size: clamp(.78rem, 1.22vw, .92rem) !important;
    line-height: 1.25 !important;
  }

  .review-home .review-action-row {
    grid-column: 1 / -1 !important;
    grid-row: 2 !important;
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    align-self: end !important;
  }

  .review-home .review-action-row button {
    width: 100% !important;
    min-height: 48px !important;
    padding: 8px 10px !important;
    font-size: .9rem !important;
  }
}

/* ===== REVIEW IMAGE TRUE SPEECH PATTERN — cartões + barra inferior ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .review-home .review-intro-panel {
    position: absolute !important;
    top: max(18px, env(safe-area-inset-top)) !important;
    left: clamp(360px, 34vw, 500px) !important;
    right: clamp(70px, 6vw, 108px) !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    row-gap: 3px !important;
    align-items: start !important;
    transform: none !important;
    pointer-events: none !important;
    z-index: 3 !important;
  }

  .review-home .review-intro-panel h1 {
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
    white-space: nowrap !important;
    margin: 0 !important;
  }

  .review-home .review-intro-panel p {
    max-width: 38ch !important;
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
    margin: 0 !important;
  }

  .review-home .google-home-main {
    grid-template-columns: minmax(0, 1080px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    justify-content: center !important;
    align-content: start !important;
    padding: 16px clamp(34px, 5vw, 70px) 8px !important;
  }

  .review-home .review-panel {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    justify-self: center !important;
    align-self: start !important;
    transform: translateY(34px) !important;
    display: grid !important;
    grid-template-columns: minmax(280px, .95fr) minmax(320px, 1.05fr) !important;
    grid-template-rows: minmax(150px, auto) auto !important;
    gap: 10px !important;
  }

  .review-home .review-preview-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
    min-height: 150px !important;
    max-height: 210px !important;
    padding: 8px !important;
  }

  .review-home .preview-image {
    min-height: 132px !important;
    width: 100% !important;
    height: 100% !important;
    object-fit: contain !important;
  }

  .review-home .review-action-card {
    display: contents !important;
  }

  .review-home .confirm-text {
    grid-column: 2 !important;
    grid-row: 1 !important;
    align-self: stretch !important;
    display: grid !important;
    align-content: center !important;
    gap: 6px !important;
    min-width: 0 !important;
    padding: 12px !important;
    border: 1px solid #e8edf5 !important;
    border-radius: 13px !important;
    background: #fff !important;
    box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
    text-align: left !important;
  }

  .review-home .confirm-text strong {
    font-size: clamp(1rem, 1.65vw, 1.22rem) !important;
    line-height: 1.12 !important;
  }

  .review-home .confirm-text span {
    max-width: 44ch !important;
    font-size: clamp(.78rem, 1.22vw, .92rem) !important;
    line-height: 1.25 !important;
  }

  .review-home .review-action-row {
    grid-column: 1 / -1 !important;
    grid-row: 2 !important;
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    padding: 7px 10px !important;
    border: 1px solid #e8edf5 !important;
    border-radius: 13px !important;
    background: #fff !important;
    box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
  }

  .review-home .review-action-row button {
    width: 100% !important;
    min-height: 48px !important;
    padding: 8px 10px !important;
    font-size: .9rem !important;
  }
}

/* ===== REVIEW IMAGE FINAL MATCH — imagem no centro, ações em baixo ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .review-home .google-home-header {
    min-height: 70px !important;
    padding-top: max(9px, env(safe-area-inset-top)) !important;
    padding-bottom: 0 !important;
  }

  .review-home .review-intro-panel {
    position: absolute !important;
    top: max(18px, env(safe-area-inset-top)) !important;
    left: clamp(360px, 34vw, 500px) !important;
    right: clamp(70px, 6vw, 108px) !important;
    width: auto !important;
    max-width: none !important;
    z-index: 3 !important;
    display: grid !important;
    row-gap: 3px !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
    pointer-events: none !important;
  }

  .review-home .review-intro-panel h1 {
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
    white-space: nowrap !important;
    margin: 0 !important;
  }

  .review-home .review-intro-panel p {
    max-width: 38ch !important;
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
    margin: 0 !important;
  }

  .review-home .google-home-main {
    grid-template-columns: minmax(0, 1080px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    justify-content: center !important;
    align-content: start !important;
    padding: 16px clamp(34px, 5vw, 70px) 8px !important;
  }

  .review-home .review-panel {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    height: min(410px, calc(100dvh - 128px)) !important;
    justify-self: center !important;
    align-self: start !important;
    transform: translateY(34px) !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(210px, 1fr) auto !important;
    gap: 10px !important;
  }

  .review-home .review-preview-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 210px !important;
    max-height: none !important;
    padding: 8px !important;
  }

  .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    min-height: 190px !important;
    object-fit: contain !important;
  }

  .review-home .review-action-card {
    grid-column: 1 !important;
    grid-row: 2 !important;
    width: 100% !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-columns: minmax(220px, .72fr) minmax(360px, 1fr) !important;
    align-items: center !important;
    column-gap: 12px !important;
    padding: 7px 10px !important;
    border: 1px solid #e8edf5 !important;
    border-radius: 13px !important;
    background: #fff !important;
    box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
  }

  .review-home .confirm-text {
    display: grid !important;
    gap: 3px !important;
    min-width: 0 !important;
    padding: 0 !important;
    border: 0 !important;
    box-shadow: none !important;
    background: transparent !important;
  }

  .review-home .confirm-text strong {
    font-size: clamp(.86rem, 1.25vw, 1rem) !important;
    line-height: 1.12 !important;
  }

  .review-home .confirm-text span {
    max-width: 36ch !important;
    font-size: clamp(.68rem, .95vw, .78rem) !important;
    line-height: 1.18 !important;
  }

  .review-home .review-action-row {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    padding: 0 !important;
    border: 0 !important;
    box-shadow: none !important;
    background: transparent !important;
  }

  .review-home .review-action-row button {
    width: 100% !important;
    min-height: 48px !important;
    padding: 8px 10px !important;
    font-size: .9rem !important;
  }
}

/* ===== REVIEW IMAGE ACTION BAR SPLIT — texto à esquerda, botões à direita ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .review-home .review-intro-panel {
    left: clamp(420px, 40vw, 620px) !important;
    right: clamp(70px, 6vw, 108px) !important;
  }

  .review-home .review-action-card {
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) !important;
    column-gap: 14px !important;
    align-items: center !important;
  }

  .review-home .confirm-text {
    justify-self: stretch !important;
    align-self: center !important;
    text-align: left !important;
    padding-left: 2px !important;
  }

  .review-home .confirm-text strong {
    font-size: clamp(.92rem, 1.34vw, 1.08rem) !important;
  }

  .review-home .confirm-text span {
    max-width: 36ch !important;
    font-size: clamp(.72rem, 1vw, .84rem) !important;
  }

  .review-home .review-action-row {
    justify-self: stretch !important;
    align-self: center !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }
}

/* ===== REVIEW IMAGE TARGET — imagem dominante e barra baixa ===== */
@media (orientation: landscape) and (min-width: 700px) {
  .review-home .review-intro-panel {
    left: clamp(390px, 36vw, 560px) !important;
    right: clamp(82px, 7vw, 122px) !important;
  }

  .review-home .google-home-main {
    grid-template-columns: minmax(0, 1060px) !important;
    padding: 8px clamp(40px, 6vw, 88px) 8px !important;
  }

  .review-home .review-panel {
    width: min(100%, 1060px) !important;
    height: min(420px, calc(100dvh - 124px)) !important;
    transform: translateY(28px) !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(230px, 1fr) 64px !important;
    gap: 8px !important;
  }

  .review-home .review-preview-card {
    width: 100% !important;
    min-height: 230px !important;
    max-height: none !important;
    padding: 6px !important;
    display: grid !important;
    place-items: center !important;
  }

  .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    min-height: 218px !important;
    object-fit: contain !important;
  }

  .review-home .review-action-card {
    width: 100% !important;
    min-height: 64px !important;
    height: 64px !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) !important;
    column-gap: 18px !important;
    align-items: center !important;
    padding: 8px 14px !important;
  }

  .review-home .confirm-text {
    gap: 1px !important;
    align-content: center !important;
  }

  .review-home .confirm-text strong {
    font-size: clamp(.7rem, 1vw, .82rem) !important;
    line-height: 1.05 !important;
  }

  .review-home .confirm-text span {
    max-width: 42ch !important;
    font-size: clamp(.58rem, .82vw, .68rem) !important;
    line-height: 1.08 !important;
  }

  .review-home .review-action-row {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    align-items: center !important;
    justify-self: stretch !important;
  }

  .review-home .review-action-row button {
    min-height: 34px !important;
    padding: 6px 10px !important;
    font-size: clamp(.68rem, .95vw, .82rem) !important;
  }
}

@media (orientation: landscape), (max-height: 620px) and (min-width: 520px) {
  .review-home .review-action-card {
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) !important;
  }

  .review-home .confirm-text {
    grid-column: 1 !important;
    justify-self: stretch !important;
    text-align: left !important;
  }

  .review-home .review-action-row {
    grid-column: 2 !important;
    justify-self: stretch !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }
}

/* ===== REVIEW HEADER FINAL — igual ao ecrã de fala em horizontal ===== */
@media (orientation: landscape), (max-height: 620px) and (min-width: 520px) {
  .review-home {
    position: relative !important;
  }

  .review-home .google-home-header {
    min-height: 70px !important;
  }

  .review-home .review-intro-panel {
    position: absolute !important;
    top: max(18px, env(safe-area-inset-top)) !important;
    left: clamp(360px, 34vw, 500px) !important;
    right: clamp(70px, 6vw, 108px) !important;
    z-index: 4 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    row-gap: 3px !important;
    align-items: start !important;
    width: auto !important;
    max-width: none !important;
    min-height: 0 !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
    pointer-events: none !important;
  }

  .review-home .review-intro-panel h1 {
    grid-column: 1 !important;
    max-width: none !important;
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
    white-space: nowrap !important;
    margin: 0 !important;
  }

  .review-home .review-intro-panel p {
    grid-column: 1 !important;
    max-width: 38ch !important;
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
    white-space: normal !important;
    margin: 0 !important;
  }
}

/* ===== PORTRAIT ACTION SPLIT — duas caixas a ocupar a area util ===== */
@media (orientation: portrait) {
  .google-home-main {
    grid-template-rows: auto minmax(0, 1fr) !important;
    align-content: stretch !important;
  }

  .home-actions-panel,
  .assisted-home .assisted-actions-panel {
    height: 100% !important;
    min-height: 0 !important;
    align-content: stretch !important;
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
  }

  .home-option-card,
  .assisted-home .assisted-option-card {
    min-height: 0 !important;
    height: 100% !important;
    grid-template-columns: clamp(58px, 18vw, 82px) minmax(0, 1fr) clamp(32px, 9vw, 40px) !important;
    gap: clamp(9px, 3vw, 13px) !important;
    padding: clamp(10px, 2.3vh, 14px) !important;
    border-radius: 18px !important;
  }

  .home-option-icon,
  .assisted-home .assisted-option-icon {
    width: clamp(58px, 18vw, 82px) !important;
    height: clamp(58px, 18vw, 82px) !important;
    min-width: clamp(58px, 18vw, 82px) !important;
  }

  .home-option-arrow {
    width: clamp(32px, 9vw, 40px) !important;
    height: clamp(32px, 9vw, 40px) !important;
    min-width: clamp(32px, 9vw, 40px) !important;
    font-size: clamp(1.05rem, 4vw, 1.28rem) !important;
  }

  .home-option-text {
    gap: 4px !important;
  }

  .home-option-text strong {
    font-size: clamp(.96rem, 4vw, 1.16rem) !important;
    line-height: 1.12 !important;
    letter-spacing: 0 !important;
  }

  .home-option-text span {
    font-size: clamp(.72rem, 3vw, .86rem) !important;
    line-height: 1.24 !important;
  }
}

@media (orientation: portrait) and (max-height: 700px) {
  .google-home-main {
    gap: 10px !important;
    padding-top: 4px !important;
    padding-bottom: 6px !important;
  }

  .home-actions-panel,
  .assisted-home .assisted-actions-panel {
    gap: 9px !important;
  }

  .home-option-card,
  .assisted-home .assisted-option-card {
    grid-template-columns: clamp(50px, 15vw, 68px) minmax(0, 1fr) 32px !important;
    padding: 9px 10px !important;
  }

  .home-option-icon,
  .assisted-home .assisted-option-icon {
    width: clamp(50px, 15vw, 68px) !important;
    height: clamp(50px, 15vw, 68px) !important;
    min-width: clamp(50px, 15vw, 68px) !important;
  }

  .home-option-text strong {
    font-size: clamp(.88rem, 3.5vw, 1.04rem) !important;
  }

  .home-option-text span {
    font-size: clamp(.66rem, 2.55vw, .78rem) !important;
  }
}

/* ===== ANDROID PORTRAIT FINAL FIT ===== */
@media (orientation: portrait) {
  .home-intro-panel h1,
  .assisted-home .assisted-intro-panel h1,
  .speech-home .speech-intro-panel h1,
  .review-intro-panel h1,
  .review-home .review-intro-panel h1 {
    max-width: 18ch !important;
    font-size: clamp(1.18rem, 4.4vw, 1.46rem) !important;
    line-height: 1.12 !important;
    letter-spacing: 0 !important;
  }

  .home-intro-panel p,
  .assisted-home .assisted-intro-panel p,
  .speech-home .speech-intro-panel p,
  .review-intro-panel p,
  .review-home .review-intro-panel p {
    max-width: 42ch !important;
    font-size: clamp(.72rem, 2.7vw, .84rem) !important;
    line-height: 1.24 !important;
  }

  .speech-home .google-home-main {
    grid-template-rows: auto minmax(0, 1fr) !important;
    gap: 8px !important;
    padding: 4px clamp(14px, 4vw, 20px) 6px !important;
    overflow: hidden !important;
  }

  .speech-home .speech-intro-panel {
    min-height: 0 !important;
    gap: 4px 8px !important;
  }

  .speech-work-panel {
    height: 100% !important;
    min-height: 0 !important;
    overflow: hidden !important;
    align-content: stretch !important;
    gap: 7px !important;
    grid-template-rows: auto minmax(110px, 1.35fr) minmax(38px, .38fr) auto !important;
  }

  .speech-config-card,
  .speech-phrase-card,
  .speech-record-card,
  .speech-record-controls {
    min-height: 0 !important;
    padding: 8px 10px !important;
    border-radius: 12px !important;
  }

  .speech-config-grid {
    gap: 6px !important;
  }

  .speech-config-grid label {
    gap: 3px !important;
    font-size: .68rem !important;
  }

  .speech-config-grid select {
    min-height: 30px !important;
    padding: 4px 8px !important;
    border-radius: 9px !important;
    font-size: .78rem !important;
  }

  .speech-button-row {
    margin-top: 6px !important;
    gap: 6px !important;
  }

  .speech-button-row .soft-action,
  .speech-record-controls button {
    min-height: 36px !important;
    padding: 7px 8px !important;
    font-size: .8rem !important;
  }

  .speech-phrase-card {
    align-content: center !important;
    gap: 4px !important;
    min-height: 96px !important;
  }

  .speech-phrase-card strong {
    font-size: clamp(.84rem, 3.2vw, .96rem) !important;
    line-height: 1.16 !important;
  }

  .speech-record-card {
    grid-template-columns: 32px minmax(0, 1fr) !important;
    gap: 7px !important;
    align-items: center !important;
    overflow: hidden !important;
    min-height: 38px !important;
  }

  .speech-record-icon {
    width: 32px !important;
    height: 32px !important;
    border-radius: 9px !important;
  }

  .speech-record-copy {
    min-height: 0 !important;
    gap: 3px !important;
    overflow: hidden !important;
  }

  .speech-home .status-pill {
    min-height: 24px !important;
    width: fit-content !important;
    max-width: 100% !important;
    padding: 3px 9px !important;
    font-size: .7rem !important;
    line-height: 1.1 !important;
  }

  .speech-home .status-file {
    max-width: 100% !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    white-space: nowrap !important;
    font-size: .68rem !important;
    line-height: 1.1 !important;
  }

  .speech-home .audio-player {
    width: 100% !important;
    height: 30px !important;
    max-height: 30px !important;
  }

  .speech-record-controls {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 6px !important;
  }
}

@media (orientation: portrait) and (max-height: 700px) {
  .speech-home .google-home-header {
    padding-top: max(6px, env(safe-area-inset-top)) !important;
    padding-bottom: 0 !important;
  }

  .speech-home .google-home-main {
    gap: 6px !important;
    padding-top: 2px !important;
    padding-bottom: 4px !important;
  }

  .speech-work-panel {
    gap: 5px !important;
    grid-template-rows: auto minmax(96px, 1.25fr) minmax(34px, .34fr) auto !important;
  }

  .speech-config-card,
  .speech-phrase-card,
  .speech-record-card,
  .speech-record-controls {
    padding: 6px 8px !important;
  }

  .speech-record-icon {
    width: 28px !important;
    height: 28px !important;
  }

  .speech-record-card {
    grid-template-columns: 28px minmax(0, 1fr) !important;
    min-height: 34px !important;
  }

  .speech-phrase-card {
    min-height: 82px !important;
  }
}

/* ===== SPEECH ANDROID LANDSCAPE FINAL — mesma logica da vertical ===== */
@media (orientation: landscape) and (min-width: 520px) {
  .speech-home:not(.review-home) {
    position: static !important;
  }

  .speech-home:not(.review-home) .google-home-header {
    min-height: 0 !important;
    padding: max(8px, env(safe-area-inset-top)) clamp(18px, 3.2vw, 32px) 2px !important;
  }

  .speech-home:not(.review-home) .google-home-main {
    position: static !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: auto minmax(0, 1fr) !important;
    align-content: stretch !important;
    align-items: stretch !important;
    justify-content: stretch !important;
    gap: 6px !important;
    padding: 2px clamp(16px, 3.2vw, 28px) 6px !important;
    overflow: hidden !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel {
    position: static !important;
    grid-row: 1 !important;
    width: 100% !important;
    max-width: none !important;
    min-height: 0 !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
    pointer-events: auto !important;
    display: grid !important;
    grid-template-columns: minmax(0, auto) minmax(0, 1fr) !important;
    align-items: center !important;
    column-gap: 12px !important;
    row-gap: 0 !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel h1 {
    grid-column: 1 !important;
    max-width: none !important;
    margin: 0 !important;
    transform: none !important;
    white-space: nowrap !important;
    font-size: clamp(.95rem, 1.75vw, 1.16rem) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel p {
    grid-column: 2 !important;
    max-width: none !important;
    margin: 0 !important;
    transform: none !important;
    white-space: nowrap !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    font-size: clamp(.66rem, 1.15vw, .78rem) !important;
    line-height: 1.15 !important;
  }

  .speech-home:not(.review-home) .speech-work-panel {
    grid-row: 2 !important;
    width: 100% !important;
    max-width: none !important;
    height: 100% !important;
    min-height: 0 !important;
    justify-self: stretch !important;
    align-self: stretch !important;
    transform: none !important;
    overflow: hidden !important;
    display: grid !important;
    grid-template-columns: minmax(220px, .95fr) minmax(260px, 1.05fr) !important;
    grid-template-rows: minmax(0, 1fr) 64px !important;
    gap: 7px !important;
    align-content: stretch !important;
  }

  .speech-home:not(.review-home) .speech-config-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
  }

  .speech-home:not(.review-home) .speech-phrase-card,
  .speech-home:not(.review-home) .speech-record-card {
    grid-column: 2 !important;
  }

  .speech-home:not(.review-home) .speech-phrase-card {
    grid-row: 1 !important;
    align-self: start !important;
    min-height: 66px !important;
  }

  .speech-home:not(.review-home) .speech-record-card {
    grid-row: 1 !important;
    align-self: end !important;
    min-height: 74px !important;
  }

  .speech-home:not(.review-home) .speech-record-controls {
    grid-column: 1 / -1 !important;
    grid-row: 2 !important;
    display: grid !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 7px !important;
  }

  .speech-home:not(.review-home) .speech-config-card,
  .speech-home:not(.review-home) .speech-phrase-card,
  .speech-home:not(.review-home) .speech-record-card,
  .speech-home:not(.review-home) .speech-record-controls {
    min-height: 0 !important;
    padding: 7px 9px !important;
    border-radius: 12px !important;
  }

  .speech-home:not(.review-home) .speech-config-grid {
    gap: 5px !important;
  }

  .speech-home:not(.review-home) .speech-config-grid label {
    gap: 2px !important;
    font-size: .66rem !important;
  }

  .speech-home:not(.review-home) .speech-config-grid select {
    min-height: 28px !important;
    padding: 3px 8px !important;
    font-size: .76rem !important;
  }

  .speech-home:not(.review-home) .speech-button-row {
    margin-top: 5px !important;
    gap: 6px !important;
  }

  .speech-home:not(.review-home) .speech-button-row .soft-action,
  .speech-home:not(.review-home) .speech-record-controls button {
    min-height: 34px !important;
    padding: 6px 8px !important;
    font-size: .78rem !important;
  }

  .speech-home:not(.review-home) .speech-phrase-card strong {
    font-size: clamp(.78rem, 1.45vw, .94rem) !important;
    line-height: 1.14 !important;
  }

  .speech-home:not(.review-home) .speech-record-card {
    grid-template-columns: 40px minmax(0, 1fr) !important;
    gap: 7px !important;
    overflow: hidden !important;
  }

  .speech-home:not(.review-home) .speech-record-icon {
    width: 40px !important;
    height: 40px !important;
    border-radius: 10px !important;
  }

  .speech-home:not(.review-home) .speech-record-copy {
    gap: 3px !important;
    overflow: hidden !important;
  }

  .speech-home:not(.review-home) .status-pill {
    min-height: 26px !important;
    width: fit-content !important;
    max-width: 100% !important;
    padding: 4px 9px !important;
    font-size: .72rem !important;
    line-height: 1.1 !important;
  }

  .speech-home:not(.review-home) .status-file {
    max-width: 100% !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    white-space: nowrap !important;
    font-size: .66rem !important;
    line-height: 1.1 !important;
  }

  .speech-home:not(.review-home) .audio-player {
    width: 100% !important;
    height: 28px !important;
    max-height: 28px !important;
  }
}

/* ===== NAVIGATION FINAL — topo com inicio e bottom vazio ===== */
.header-action-row {
  display: flex !important;
  align-items: center !important;
  justify-content: flex-end !important;
  gap: 8px !important;
}

.home-help-btn svg,
.reader-header-btn svg {
  width: 18px !important;
  height: 18px !important;
  display: block !important;
}

.home-bottom-nav--empty {
  display: block !important;
  width: 100% !important;
  height: 0 !important;
  min-height: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  border: 0 !important;
  box-shadow: none !important;
  background: transparent !important;
  overflow: hidden !important;
}

/* ===== REVIEW IMAGE BUTTONS FINAL ===== */
.review-home .review-action-card.speech-record-controls {
  width: 100% !important;
  min-width: 0 !important;
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  padding: 8px !important;
}

.review-home .review-action-row {
  width: 100% !important;
  min-width: 0 !important;
  justify-self: stretch !important;
  display: grid !important;
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  gap: 8px !important;
}

.review-home .review-action-row button {
  width: 100% !important;
  min-width: 0 !important;
  min-height: 48px !important;
  padding: 8px 10px !important;
  font-size: .82rem !important;
}

/* ===== READER CONTROLS FINAL ===== */
.reader-home:not(.speech-result-home) .pill-btn {
  font-size: .78rem !important;
  line-height: 1.08 !important;
}

.reader-home:not(.speech-result-home) .settings-panel {
  font-size: .78rem !important;
}

.reader-home:not(.speech-result-home) .setting-block {
  gap: 5px !important;
  font-size: .78rem !important;
  line-height: 1.08 !important;
}

.reader-home:not(.speech-result-home) .setting-block label {
  font-size: .78rem !important;
  line-height: 1.12 !important;
}

.reader-home:not(.speech-result-home) .setting-block select,
.reader-home:not(.speech-result-home) .setting-block input {
  font-size: .78rem !important;
}

.reader-home:not(.speech-result-home) .check-block {
  grid-column: 1 / -1 !important;
  width: 100% !important;
  justify-content: center !important;
  white-space: nowrap !important;
  font-size: .78rem !important;
  line-height: 1.1 !important;
}

/* ===== TYPOGRAPHY FINAL — mesma escala em todas as orientacoes ===== */
.google-home .home-brand-title {
  font-size: clamp(1.12rem, 3.8vw, 1.42rem) !important;
  line-height: .98 !important;
  letter-spacing: -0.02em !important;
  font-weight: 760 !important;
}

.google-home .home-brand-subtitle,
.speech-result-home .home-brand-subtitle {
  margin-top: 2px !important;
  font-size: clamp(.62rem, 1.9vw, .74rem) !important;
  line-height: 1.1 !important;
  color: #6f7b8f !important;
}

.google-home .home-intro-panel h1,
.google-home .assisted-intro-panel h1,
.google-home .speech-intro-panel h1,
.google-home .review-intro-panel h1 {
  max-width: 18ch !important;
  font-size: clamp(1rem, 3.4vw, 1.2rem) !important;
  line-height: 1.12 !important;
  letter-spacing: 0 !important;
}

.google-home .home-intro-panel p,
.google-home .assisted-intro-panel p,
.google-home .speech-intro-panel p,
.google-home .review-intro-panel p {
  max-width: 42ch !important;
  font-size: clamp(.62rem, 2vw, .74rem) !important;
  line-height: 1.24 !important;
}

.google-home .home-option-text strong,
.google-home .assisted-option-card .home-option-text strong {
  font-size: clamp(.82rem, 2.9vw, .98rem) !important;
  line-height: 1.12 !important;
  letter-spacing: 0 !important;
}

.google-home .home-option-text span,
.google-home .assisted-option-card .home-option-text span {
  font-size: clamp(.62rem, 2.1vw, .74rem) !important;
  line-height: 1.24 !important;
}

/* ===== SPEECH PREP RESTORE — uma coluna, organizado por linhas ===== */
.speech-home:not(.review-home) .google-home-main {
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
  align-content: stretch !important;
  align-items: stretch !important;
  gap: 10px !important;
  overflow: hidden !important;
}

.speech-home:not(.review-home) .speech-intro-panel {
  position: static !important;
  width: 100% !important;
  max-width: none !important;
  min-height: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  transform: none !important;
  pointer-events: auto !important;
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  gap: 4px !important;
  align-items: start !important;
}

.speech-home:not(.review-home) .speech-intro-panel h1 {
  grid-column: 1 !important;
  max-width: 18ch !important;
  margin: 0 !important;
  transform: none !important;
  white-space: normal !important;
  font-size: clamp(1.18rem, 4.4vw, 1.46rem) !important;
  line-height: 1.12 !important;
  letter-spacing: 0 !important;
}

.speech-home:not(.review-home) .speech-intro-panel p {
  grid-column: 1 !important;
  max-width: 42ch !important;
  margin: 0 !important;
  transform: none !important;
  white-space: normal !important;
  overflow: visible !important;
  text-overflow: clip !important;
  font-size: clamp(.72rem, 2.7vw, .84rem) !important;
  line-height: 1.24 !important;
}

.speech-home:not(.review-home) .speech-work-panel {
  width: 100% !important;
  max-width: none !important;
  height: 100% !important;
  min-height: 0 !important;
  justify-self: stretch !important;
  align-self: stretch !important;
  transform: none !important;
  overflow: hidden !important;
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  grid-template-rows: auto minmax(58px, .7fr) minmax(72px, .85fr) auto !important;
  gap: 7px !important;
  align-content: stretch !important;
}

.speech-home:not(.review-home) .speech-config-card,
.speech-home:not(.review-home) .speech-phrase-card,
.speech-home:not(.review-home) .speech-record-card,
.speech-home:not(.review-home) .speech-record-controls {
  grid-column: 1 !important;
  grid-row: auto !important;
  min-height: 0 !important;
}

@media (orientation: landscape) and (min-width: 520px) {
  .speech-home:not(.review-home) .google-home-header {
    min-height: 0 !important;
    padding: max(8px, env(safe-area-inset-top)) clamp(18px, 3.2vw, 32px) 2px !important;
  }

  .speech-home:not(.review-home) .google-home-main {
    gap: 6px !important;
    padding: 2px clamp(16px, 3.2vw, 28px) 6px !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel {
    gap: 2px !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel h1 {
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel p {
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
  }

  .speech-home:not(.review-home) .speech-work-panel {
    grid-template-rows: auto minmax(48px, .62fr) minmax(58px, .7fr) auto !important;
    gap: 5px !important;
  }

  .speech-home:not(.review-home) .speech-record-controls {
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
  }
}

/* ===== SPEECH PREP LANDSCAPE TARGET — titulo no topo e conteudo em duas colunas ===== */
@media (orientation: landscape) and (min-width: 520px) {
  .speech-home:not(.review-home) .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: auto minmax(0, 1fr) !important;
    align-content: stretch !important;
    align-items: stretch !important;
    gap: 8px !important;
    padding: 4px clamp(28px, 6vw, 70px) 8px !important;
    overflow: hidden !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel {
    grid-row: 1 !important;
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    justify-self: center !important;
    position: static !important;
    transform: none !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    align-items: start !important;
    column-gap: 0 !important;
    row-gap: 2px !important;
    margin: 0 !important;
    padding: 0 !important;
    pointer-events: auto !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel h1 {
    grid-column: 1 !important;
    justify-self: start !important;
    max-width: none !important;
    margin: 0 !important;
    white-space: nowrap !important;
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
    font-weight: 760 !important;
    letter-spacing: 0 !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel p {
    grid-column: 1 !important;
    justify-self: start !important;
    max-width: 52ch !important;
    margin: 0 !important;
    white-space: nowrap !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    text-align: left !important;
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
  }

  .speech-home:not(.review-home) .speech-work-panel {
    grid-row: 2 !important;
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    height: 100% !important;
    min-height: 0 !important;
    justify-self: center !important;
    align-self: stretch !important;
    transform: none !important;
    overflow: hidden !important;
    display: grid !important;
    grid-template-columns: minmax(280px, .95fr) minmax(320px, 1.05fr) !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    gap: 10px !important;
    align-content: stretch !important;
  }

  .speech-home:not(.review-home) .speech-config-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: stretch !important;
  }

  .speech-home:not(.review-home) .speech-phrase-card {
    grid-column: 2 !important;
    grid-row: 1 !important;
    align-self: start !important;
    min-height: 0 !important;
    height: calc(50% - 5px) !important;
  }

  .speech-home:not(.review-home) .speech-record-card {
    grid-column: 2 !important;
    grid-row: 1 !important;
    align-self: end !important;
    min-height: 0 !important;
    height: calc(50% - 5px) !important;
  }

  .speech-home:not(.review-home) .speech-record-controls {
    grid-column: 1 / -1 !important;
    grid-row: 2 !important;
    display: grid !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 8px !important;
  }

  .speech-home:not(.review-home) .speech-config-card,
  .speech-home:not(.review-home) .speech-phrase-card,
  .speech-home:not(.review-home) .speech-record-card,
  .speech-home:not(.review-home) .speech-record-controls {
    padding: 10px !important;
    border-radius: 13px !important;
  }
}

/* ===== SPEECH PREP PORTRAIT FINAL PROPORTIONS ===== */
@media (orientation: portrait) {
  .speech-home:not(.review-home) .speech-work-panel {
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: auto minmax(108px, 1.32fr) minmax(44px, .42fr) auto !important;
    gap: 7px !important;
  }

  .speech-home:not(.review-home) .speech-phrase-card {
    min-height: 108px !important;
    height: auto !important;
    align-content: center !important;
  }

  .speech-home:not(.review-home) .speech-record-card {
    min-height: 44px !important;
    height: auto !important;
    grid-template-columns: 30px minmax(0, 1fr) !important;
    gap: 7px !important;
    padding: 7px 9px !important;
    align-items: center !important;
  }

  .speech-home:not(.review-home) .speech-record-icon {
    width: 30px !important;
    height: 30px !important;
    border-radius: 8px !important;
  }

  .speech-home:not(.review-home) .speech-record-copy {
    gap: 2px !important;
  }

  .speech-home:not(.review-home) .status-pill {
    min-height: 24px !important;
    padding: 3px 8px !important;
    font-size: .68rem !important;
    line-height: 1.05 !important;
  }
}

@media (orientation: portrait) and (max-height: 700px) {
  .speech-home:not(.review-home) .speech-work-panel {
    grid-template-rows: auto minmax(92px, 1.18fr) minmax(40px, .38fr) auto !important;
    gap: 5px !important;
  }

  .speech-home:not(.review-home) .speech-phrase-card {
    min-height: 92px !important;
  }

  .speech-home:not(.review-home) .speech-record-card {
    min-height: 40px !important;
    grid-template-columns: 28px minmax(0, 1fr) !important;
    padding: 6px 8px !important;
  }

  .speech-home:not(.review-home) .speech-record-icon {
    width: 28px !important;
    height: 28px !important;
  }
}

/* ===== SPEECH PREP HEADER MATCH REVIEW LANDSCAPE ===== */
@media (orientation: landscape) and (min-width: 520px) {
  .speech-home:not(.review-home) {
    position: relative !important;
  }

  .speech-home:not(.review-home) .google-home-header {
    min-height: 70px !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel {
    position: absolute !important;
    top: max(18px, env(safe-area-inset-top)) !important;
    left: clamp(360px, 34vw, 500px) !important;
    right: clamp(70px, 6vw, 108px) !important;
    z-index: 4 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    row-gap: 3px !important;
    align-items: start !important;
    width: auto !important;
    max-width: none !important;
    min-height: 0 !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
    pointer-events: none !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel h1 {
    grid-column: 1 !important;
    max-width: none !important;
    font-size: clamp(1rem, 1.45vw, 1.24rem) !important;
    line-height: 1.08 !important;
    white-space: nowrap !important;
    margin: 0 !important;
    font-weight: 760 !important;
  }

  .speech-home:not(.review-home) .speech-intro-panel p {
    grid-column: 1 !important;
    max-width: 38ch !important;
    font-size: clamp(.68rem, .9vw, .82rem) !important;
    line-height: 1.15 !important;
    white-space: normal !important;
    margin: 0 !important;
    overflow: visible !important;
    text-overflow: clip !important;
  }

  .speech-home:not(.review-home) .google-home-main {
    grid-template-rows: minmax(0, 1fr) !important;
    padding-top: 16px !important;
  }

  .speech-home:not(.review-home) .speech-work-panel {
    grid-row: 1 !important;
    transform: translateY(34px) !important;
  }
}

/* ===== SPEECH PREP LANDSCAPE ROW LAYOUT ===== */
@media (orientation: landscape) and (min-width: 520px) {
  .speech-home:not(.review-home) .speech-work-panel {
    width: 100% !important;
    max-width: none !important;
    height: calc(100% - 34px) !important;
    justify-self: center !important;
    align-self: start !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(92px, 1fr) auto auto auto !important;
    gap: 8px !important;
    transform: translateY(34px) !important;
    overflow: hidden !important;
  }

  .speech-home:not(.review-home) .speech-phrase-card,
  .speech-home:not(.review-home) .speech-config-card,
  .speech-home:not(.review-home) .speech-record-card,
  .speech-home:not(.review-home) .speech-record-controls {
    grid-column: 1 !important;
    width: 100% !important;
    min-height: 0 !important;
    height: auto !important;
    align-self: stretch !important;
    padding: 8px 10px !important;
    border-radius: 13px !important;
  }

  .speech-home:not(.review-home) .speech-phrase-card {
    grid-row: 1 !important;
    min-height: 92px !important;
    align-content: center !important;
  }

  .speech-home:not(.review-home) .speech-config-card {
    grid-row: 2 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) minmax(180px, auto) !important;
    align-items: end !important;
    gap: 8px !important;
  }

  .speech-home:not(.review-home) .speech-config-grid {
    display: grid !important;
    grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
    gap: 8px !important;
  }

  .speech-home:not(.review-home) .speech-config-grid label:last-child {
    grid-column: auto !important;
  }

  .speech-home:not(.review-home) .speech-config-grid label {
    gap: 3px !important;
    font-size: .7rem !important;
  }

  .speech-home:not(.review-home) .speech-config-grid select {
    min-height: 30px !important;
    padding: 4px 8px !important;
    font-size: .78rem !important;
  }

  .speech-home:not(.review-home) .speech-button-row {
    margin: 0 !important;
    display: flex !important;
    flex-wrap: nowrap !important;
    gap: 6px !important;
    align-items: end !important;
  }

  .speech-home:not(.review-home) .speech-button-row .soft-action {
    min-width: 112px !important;
    min-height: 32px !important;
    padding: 6px 10px !important;
    font-size: .78rem !important;
    white-space: nowrap !important;
  }

  .speech-home:not(.review-home) .speech-record-card {
    grid-row: 3 !important;
    grid-template-columns: 34px minmax(0, 1fr) !important;
    gap: 8px !important;
    align-items: center !important;
    min-height: 44px !important;
  }

  .speech-home:not(.review-home) .speech-record-icon {
    width: 34px !important;
    height: 34px !important;
  }

  .speech-home:not(.review-home) .speech-record-controls {
    grid-row: 4 !important;
    display: grid !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 8px !important;
  }

  .speech-home:not(.review-home) .speech-record-controls button {
    min-height: 34px !important;
    padding: 6px 10px !important;
    font-size: .78rem !important;
  }
}

/* ===== SPEECH PREP LANDSCAPE WIDTH MATCH ===== */
@media (orientation: landscape) and (min-width: 520px) {
  .speech-home:not(.review-home) .google-home-main {
    padding: 8px clamp(18px, 3vw, 30px) 10px !important;
  }

  .speech-home:not(.review-home) .speech-work-panel {
    width: 100% !important;
    max-width: none !important;
    justify-self: stretch !important;
  }
}

/* ===== REVIEW IMAGE LANDSCAPE FILL BOTTOM ===== */
@media (orientation: landscape) and (min-width: 520px) {
  .review-home .google-home-main {
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    padding: 8px clamp(18px, 3vw, 30px) 10px !important;
    overflow: hidden !important;
  }

  .review-home .review-panel {
    width: 100% !important;
    max-width: none !important;
    height: 100% !important;
    max-height: none !important;
    justify-self: stretch !important;
    align-self: stretch !important;
    transform: none !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    gap: 8px !important;
  }

  .review-home .review-preview-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    max-height: none !important;
  }

  .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    object-fit: contain !important;
  }

  .review-home .review-action-card.speech-record-controls,
  .review-home .review-action-card {
    grid-column: 1 !important;
    grid-row: 2 !important;
    width: 100% !important;
    min-height: 64px !important;
    height: 64px !important;
    align-self: end !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    padding: 8px 10px !important;
    overflow: hidden !important;
  }

  .review-home .review-action-row {
    width: 100% !important;
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    align-items: center !important;
  }

  .review-home .review-action-row button {
    min-height: 44px !important;
    height: 44px !important;
    padding: 7px 10px !important;
    font-size: .78rem !important;
  }
}

/* ===== REVIEW IMAGE PORTRAIT BUTTON BOX FIT ===== */
@media (orientation: portrait) {
  .review-home .google-home-main {
    grid-template-rows: auto minmax(0, 1fr) !important;
    gap: 8px !important;
    padding: 4px clamp(12px, 3.5vw, 18px) 8px !important;
    overflow: hidden !important;
  }

  .review-home .review-panel {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) 78px !important;
    gap: 8px !important;
    overflow: hidden !important;
  }

  .review-home .review-preview-card {
    min-height: 0 !important;
    height: 100% !important;
    padding: 8px !important;
    overflow: hidden !important;
  }

  .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    object-fit: contain !important;
  }

  .review-home .review-action-card.speech-record-controls,
  .review-home .review-action-card {
    min-height: 78px !important;
    height: 78px !important;
    padding: 9px 10px !important;
    display: grid !important;
    align-items: center !important;
    overflow: hidden !important;
  }

  .review-home .review-action-row {
    width: 100% !important;
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    align-items: center !important;
  }

  .review-home .review-action-row button {
    min-height: 58px !important;
    height: 58px !important;
    padding: 8px 10px !important;
    font-size: .82rem !important;
    line-height: 1.12 !important;
  }
}

@media (orientation: portrait) and (max-height: 700px) {
  .review-home .review-panel {
    grid-template-rows: minmax(0, 1fr) 70px !important;
    gap: 6px !important;
  }

  .review-home .review-action-card.speech-record-controls,
  .review-home .review-action-card {
    min-height: 70px !important;
    height: 70px !important;
    padding: 8px 9px !important;
  }

  .review-home .review-action-row button {
    min-height: 52px !important;
    height: 52px !important;
    font-size: .78rem !important;
  }
}

/* ===== SHARED RECORD CONTROLS FINAL — voz e confirma imagem iguais ===== */
.speech-home:not(.review-home) .speech-record-controls,
.review-home .image-confirm-controls {
  width: 100% !important;
  min-width: 0 !important;
  min-height: 0 !important;
  height: auto !important;
  display: grid !important;
  gap: 8px !important;
  align-items: center !important;
  padding: 8px 10px !important;
  border: 1px solid #e8edf5 !important;
  border-radius: 13px !important;
  background: #fff !important;
  box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
  box-sizing: border-box !important;
  overflow: hidden !important;
}

.speech-home:not(.review-home) .speech-record-controls button,
.review-home .image-confirm-controls button {
  width: 100% !important;
  min-width: 0 !important;
  min-height: 34px !important;
  height: auto !important;
  padding: 6px 10px !important;
  font-size: .78rem !important;
  line-height: 1.08 !important;
  box-sizing: border-box !important;
}

.speech-home:not(.review-home) .speech-record-controls {
  grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
}

.review-home .image-confirm-controls {
  grid-column: 1 !important;
  grid-row: 2 !important;
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  justify-self: stretch !important;
  align-self: stretch !important;
}

@media (orientation: landscape), (max-height: 620px) and (min-width: 520px) {
  .review-home .google-home-main {
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    padding: 8px clamp(18px, 3vw, 30px) 8px !important;
    overflow: hidden !important;
  }

  .review-home .review-panel {
    width: 100% !important;
    max-width: none !important;
    height: 100% !important;
    max-height: none !important;
    min-height: 0 !important;
    justify-self: stretch !important;
    align-self: stretch !important;
    transform: none !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    gap: 8px !important;
    overflow: hidden !important;
  }

  .review-home .review-preview-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 6px !important;
    overflow: hidden !important;
  }

  .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    object-fit: contain !important;
  }

  .review-home .image-confirm-controls {
    min-height: 0 !important;
    height: auto !important;
  }
}

@media (orientation: portrait) {
  .review-home .review-panel {
    grid-template-rows: minmax(0, 1fr) auto !important;
  }

  .review-home .image-confirm-controls {
    min-height: 0 !important;
    height: auto !important;
  }
}

/* ===== HOME / IMAGE SOURCE FINAL CENTERING ===== */
@media (orientation: portrait) {
  .home-screen .home-actions-panel,
  .assisted-home .assisted-actions-panel {
    height: calc(100% - clamp(10px, 2vh, 16px)) !important;
    transform: translateY(clamp(10px, 2vh, 16px)) !important;
  }
}

@media (orientation: landscape) and (min-width: 700px) {
  .home-screen .home-actions-panel,
  .assisted-home .assisted-actions-panel {
    transform: translateY(66px) !important;
  }
}

/* ===== READER TRANSPORT FINAL — sem bolas, cores Google ===== */
.reader-home .transport-row .round-btn,
.reader-home .transport-row .play-btn,
.reader-home.settings-open:not(.speech-result-home) .transport-row .round-btn,
.reader-home.settings-open:not(.speech-result-home) .transport-row .play-btn {
  width: 34px !important;
  height: 34px !important;
  min-width: 34px !important;
  min-height: 34px !important;
  padding: 0 !important;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  display: grid !important;
  place-items: center !important;
  font-size: 1.18rem !important;
  line-height: 1 !important;
}

.reader-home .transport-row .prev-btn {
  color: #fbbc04 !important;
}

.reader-home .transport-row .play-btn {
  color: #1a73e8 !important;
}

.reader-home .transport-row .play-btn.active {
  color: #ea4335 !important;
}

.reader-home .transport-row .next-btn {
  color: #34a853 !important;
}

.reader-home .transport-row .round-btn:disabled,
.reader-home .transport-row .play-btn:disabled {
  color: #dadce0 !important;
  opacity: 1 !important;
}

/* ===== HEADER ICONS FINAL — sem circulos ===== */
.google-home .home-help-btn,
.google-home .reader-header-btn {
  width: 38px !important;
  height: 38px !important;
  min-width: 38px !important;
  min-height: 38px !important;
  padding: 0 !important;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  color: #172033 !important;
  display: grid !important;
  place-items: center !important;
}

.google-home .home-help-btn svg,
.google-home .reader-header-btn svg {
  width: 23px !important;
  height: 23px !important;
}

.google-home .reader-text-btn {
  font-size: 1.38rem !important;
  line-height: 1 !important;
  font-weight: 760 !important;
}

/* ===== READER BUTTONS BALANCE =====
   Os controlos do leitor devem parecer botoes normais, nao chips circulares.
   Para 8 acoes, a grelha fica equilibrada em 4 + 4. */
.reader-home.settings-open:not(.speech-result-home) .controls-compact {
  display: grid !important;
  grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
  gap: 8px !important;
  width: 100% !important;
  align-items: stretch !important;
}

.reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
  width: 100% !important;
  min-width: 0 !important;
  height: auto !important;
  min-height: 42px !important;
  aspect-ratio: auto !important;
  padding: 8px 10px !important;
  border-radius: 12px !important;
  border: 1px solid #e5eaf2 !important;
  background: #f3f6fb !important;
  color: #344054 !important;
  box-shadow: none !important;
  font-size: clamp(.78rem, 1.9vw, .96rem) !important;
  font-weight: 720 !important;
  line-height: 1.12 !important;
  text-align: center !important;
  white-space: normal !important;
  overflow-wrap: normal !important;
  word-break: normal !important;
  display: grid !important;
  place-items: center !important;
}

.reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn.active {
  background: #e8f0fe !important;
  color: #1a73e8 !important;
  border-color: #bcd4ff !important;
}

@media (orientation: portrait) and (max-width: 620px) {
  .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 6px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-height: 38px !important;
    border-radius: 11px !important;
    padding: 7px 8px !important;
    font-size: .78rem !important;
  }
}

@media (orientation: landscape) and (max-height: 620px) {
  .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 6px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-height: 34px !important;
    border-radius: 10px !important;
    padding: 5px 7px !important;
    font-size: clamp(.7rem, 1.35vw, .82rem) !important;
  }
}

/* ===== RESPONSIVE SAFETY PASS — Android landscape/small screens =====
   Mantem a UI dentro do viewport e evita que controlos fixos roubem espaco
   aos ecras finais. */
.home-bottom-nav--empty {
  display: none !important;
}

.reader-home,
.speech-home,
.review-home {
  grid-template-rows: auto minmax(0, 1fr) !important;
}

.reader-home .google-home-main,
.speech-home .google-home-main,
.review-home .google-home-main {
  min-height: 0 !important;
  overflow: hidden !important;
}

.reader-home.settings-open:not(.speech-result-home) .reader-main {
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  grid-template-rows: minmax(96px, 1fr) minmax(0, auto) !important;
}

.reader-home.settings-open:not(.speech-result-home) .reader-top {
  min-height: 0 !important;
  height: auto !important;
  overflow: auto !important;
  -webkit-overflow-scrolling: touch !important;
}

.reader-home.settings-open:not(.speech-result-home) .reader-bottom {
  align-self: end !important;
  max-height: min(50dvh, 260px) !important;
  overflow-y: auto !important;
  overflow-x: hidden !important;
  -webkit-overflow-scrolling: touch !important;
}

.reader-home.settings-open:not(.speech-result-home) .controls-compact {
  display: grid !important;
  grid-template-columns: repeat(auto-fit, minmax(88px, 1fr)) !important;
  gap: clamp(4px, 1.1vw, 8px) !important;
}

.reader-home.settings-open:not(.speech-result-home) .settings-panel {
  display: grid !important;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)) !important;
}

.reader-home.settings-open:not(.speech-result-home) .check-block {
  grid-column: 1 / -1 !important;
  justify-content: center !important;
  white-space: normal !important;
  text-align: center !important;
}

.speech-result-home .reader-main {
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  grid-template-rows: minmax(0, 1fr) !important;
}

.speech-result-home .speech-result-panel {
  width: 100% !important;
  max-width: none !important;
  min-height: 0 !important;
  height: 100% !important;
  align-self: stretch !important;
  justify-self: stretch !important;
  overflow: hidden !important;
}

.speech-result-home .result-phrase-card {
  min-width: 0 !important;
  min-height: 0 !important;
  overflow: hidden !important;
}

.speech-result-home .result-phrase-copy strong {
  overflow-wrap: anywhere !important;
}

@media (orientation: landscape) and (max-height: 620px) {
  .google-home-header {
    padding-top: max(4px, env(safe-area-inset-top)) !important;
    padding-bottom: 2px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-main {
    grid-template-rows: minmax(72px, 1fr) minmax(0, auto) !important;
    gap: 4px !important;
    padding-top: 2px !important;
    padding-bottom: 4px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    max-height: min(58dvh, 230px) !important;
    padding-top: 4px !important;
    padding-bottom: max(4px, env(safe-area-inset-bottom)) !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .line-focus,
  .reader-home.settings-open:not(.speech-result-home) .word-context {
    line-height: 1.18 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .pill-btn {
    min-height: 28px !important;
    padding: 4px 7px !important;
    font-size: clamp(.66rem, 1.35vw, .78rem) !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    gap: 4px !important;
    padding: 5px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .setting-block,
  .reader-home.settings-open:not(.speech-result-home) .setting-block label,
  .reader-home.settings-open:not(.speech-result-home) .setting-block select,
  .reader-home.settings-open:not(.speech-result-home) .setting-block input {
    font-size: clamp(.64rem, 1.2vw, .72rem) !important;
  }

  .speech-result-home .reader-main {
    padding: 4px clamp(12px, 2.4vw, 24px) 6px !important;
  }

  .speech-result-home .speech-result-panel {
    grid-template-rows: repeat(2, minmax(0, 1fr)) auto !important;
    gap: 8px !important;
  }

  .speech-result-home .result-phrase-card {
    padding: 10px 12px !important;
  }
}

@media (orientation: landscape) and (max-height: 460px) {
  .reader-home.settings-open:not(.speech-result-home) .reader-main {
    grid-template-rows: minmax(56px, 1fr) minmax(0, auto) !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    max-height: 64dvh !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    grid-template-columns: repeat(auto-fit, minmax(76px, 1fr)) !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr)) !important;
  }
}

/* ===== FINAL READER BUTTON OVERRIDE =====
   Tem de ficar no fim para vencer as regras antigas com !important. */
.reader-home.settings-open:not(.speech-result-home) .controls-compact {
  display: grid !important;
  grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
  gap: 8px !important;
  width: 100% !important;
  align-items: stretch !important;
}

.reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
  width: 100% !important;
  min-width: 0 !important;
  height: auto !important;
  min-height: 42px !important;
  aspect-ratio: auto !important;
  padding: 8px 10px !important;
  border-radius: 12px !important;
  border: 1px solid #e5eaf2 !important;
  background: #f3f6fb !important;
  color: #344054 !important;
  box-shadow: none !important;
  font-size: clamp(.78rem, 1.9vw, .96rem) !important;
  font-weight: 720 !important;
  line-height: 1.12 !important;
  text-align: center !important;
  white-space: normal !important;
  overflow-wrap: normal !important;
  word-break: normal !important;
  display: grid !important;
  place-items: center !important;
}

.reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn.active {
  background: #e8f0fe !important;
  color: #1a73e8 !important;
  border-color: #bcd4ff !important;
}

@media (orientation: portrait) and (max-width: 620px) {
  .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 6px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-height: 38px !important;
    border-radius: 11px !important;
    padding: 7px 8px !important;
    font-size: .78rem !important;
  }
}

@media (orientation: landscape) and (max-height: 620px) {
  .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 6px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-height: 34px !important;
    border-radius: 10px !important;
    padding: 5px 7px !important;
    font-size: clamp(.7rem, 1.35vw, .82rem) !important;
  }
}

/* ===== HOME RESPONSIVE FIT =====
   Esta camada fica no fim de propósito: o Home tinha mínimos rígidos
   em landscape que rebentavam em telemóveis pequenos. */
.home-screen,
.home-screen .google-home,
.home-screen .google-home-main,
.home-screen .home-intro-panel,
.home-screen .home-actions-panel,
.home-screen .home-option-card,
.home-screen .home-option-text {
  min-width: 0 !important;
}

.home-screen .google-home {
  inline-size: 100vw !important;
  max-inline-size: 100vw !important;
}

.home-screen .google-home-main {
  inline-size: 100% !important;
  max-inline-size: 100% !important;
}

.home-screen .home-option-card {
  max-inline-size: 100% !important;
  overflow: hidden !important;
}

.home-screen .home-option-text strong,
.home-screen .home-option-text span,
.home-screen .home-intro-panel h1,
.home-screen .home-intro-panel p {
  overflow-wrap: break-word !important;
}

.home-screen .assisted-intro-panel,
.home-screen .speech-intro-panel,
.home-screen .review-intro-panel,
.home-screen .processing-panel,
.home-screen .assisted-actions-panel,
.home-screen .speech-work-panel,
.home-screen .review-panel,
.home-screen .assisted-option-card,
.home-screen .speech-config-card,
.home-screen .speech-phrase-card,
.home-screen .speech-record-card,
.home-screen .speech-record-controls,
.home-screen .review-preview-card,
.home-screen .image-confirm-controls {
  min-width: 0 !important;
  max-width: 100% !important;
}

@media (orientation: portrait) {
  .home-screen .google-home-header {
    padding: max(14px, env(safe-area-inset-top)) clamp(18px, 5vw, 24px) 6px !important;
  }

  .home-screen .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: auto minmax(0, 1fr) !important;
    align-content: stretch !important;
    gap: clamp(12px, 2.2vh, 18px) !important;
    padding: 8px clamp(18px, 5vw, 24px) 8px !important;
    overflow: hidden !important;
  }

  .home-screen .home-intro-panel {
    position: static !important;
    max-width: none !important;
    justify-self: stretch !important;
    justify-items: start !important;
    gap: 8px !important;
    text-align: left !important;
  }

  .home-screen .home-badge {
    position: static !important;
    inset: auto !important;
    justify-self: start !important;
    max-width: 100% !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel h1 {
    max-width: 16ch !important;
    font-size: clamp(1.35rem, 5.4vw, 2rem) !important;
    line-height: 1.1 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-intro-panel p {
    max-width: 40ch !important;
    font-size: clamp(.78rem, 3vw, .98rem) !important;
    line-height: 1.28 !important;
  }

  .home-screen .home-actions-panel {
    width: calc(100vw - clamp(36px, 10vw, 48px)) !important;
    max-width: calc(100vw - clamp(36px, 10vw, 48px)) !important;
    height: 100% !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    align-content: stretch !important;
    gap: clamp(12px, 2.1vh, 18px) !important;
    overflow: hidden !important;
  }

  .home-screen .home-option-card {
    width: 100% !important;
    max-width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    grid-template-columns: clamp(54px, 16vw, 72px) minmax(0, 1fr) !important;
    gap: clamp(10px, 3vw, 14px) !important;
    padding: clamp(12px, 2.2vh, 16px) !important;
    border-radius: 18px !important;
  }

  .home-screen .home-option-icon {
    width: clamp(54px, 16vw, 72px) !important;
    height: clamp(54px, 16vw, 72px) !important;
    min-width: clamp(54px, 16vw, 72px) !important;
  }

  .home-screen .home-option-text strong {
    font-size: clamp(.98rem, 4vw, 1.24rem) !important;
    line-height: 1.12 !important;
    letter-spacing: 0 !important;
    white-space: normal !important;
  }

  .home-screen .home-option-text span {
    font-size: clamp(.72rem, 2.7vw, .88rem) !important;
    line-height: 1.24 !important;
    white-space: normal !important;
  }

  .home-screen .home-option-arrow {
    display: none !important;
  }
}

@media (orientation: landscape) {
  .home-screen .home-intro-panel,
  .home-screen .home-intro-panel h1,
  .home-screen .home-intro-panel p,
  .home-screen .home-actions-panel,
  .home-screen .assisted-intro-panel,
  .home-screen .assisted-intro-panel h1,
  .home-screen .assisted-intro-panel p,
  .home-screen .assisted-actions-panel,
  .home-screen .speech-intro-panel,
  .home-screen .speech-intro-panel h1,
  .home-screen .speech-intro-panel p,
  .home-screen .speech-work-panel,
  .home-screen .review-intro-panel,
  .home-screen .review-intro-panel h1,
  .home-screen .review-intro-panel p,
  .home-screen .review-panel {
    transform: none !important;
  }

  .home-screen .google-home {
    height: 100dvh !important;
    min-height: 100dvh !important;
    margin: 0 !important;
    border-radius: 0 !important;
  }

  .home-screen .google-home-header {
    padding: max(6px, env(safe-area-inset-top)) clamp(10px, 2.6vw, 28px) 0 !important;
  }

  .home-screen .home-logo-mark {
    width: clamp(24px, 5.4vh, 32px) !important;
    height: clamp(24px, 5.4vh, 32px) !important;
    min-width: clamp(24px, 5.4vh, 32px) !important;
  }

  .home-screen .home-brand-title {
    font-size: clamp(1.05rem, 4.6vh, 1.5rem) !important;
    line-height: 1 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-brand-subtitle {
    font-size: clamp(.62rem, 2.5vh, .78rem) !important;
    margin-top: 1px !important;
  }

  .home-screen .home-help-btn {
    width: clamp(28px, 7.2vh, 34px) !important;
    height: clamp(28px, 7.2vh, 34px) !important;
    min-width: clamp(28px, 7.2vh, 34px) !important;
  }

  .home-screen .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, .68fr) minmax(0, 1.32fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    gap: clamp(8px, 2.6vw, 24px) !important;
    padding: clamp(2px, 1.2vh, 6px) clamp(10px, 2.8vw, 28px) clamp(4px, 1.4vh, 8px) !important;
    overflow: hidden !important;
  }

  .home-screen .home-intro-panel {
    align-self: center !important;
    justify-self: start !important;
    max-width: none !important;
    gap: clamp(3px, 1.4vh, 7px) !important;
    padding: 0 !important;
    width: 100% !important;
  }

  .home-screen .assisted-intro-panel,
  .home-screen .speech-intro-panel,
  .home-screen .review-intro-panel {
    align-self: center !important;
    justify-self: start !important;
    max-width: none !important;
    width: 100% !important;
    padding: 0 !important;
    gap: clamp(3px, 1.4vh, 7px) !important;
  }

  .home-screen .home-badge {
    min-height: 0 !important;
    padding: 3px 8px !important;
    font-size: clamp(.62rem, 2.55vh, .78rem) !important;
    line-height: 1.1 !important;
  }

  .home-screen .home-intro-panel h1 {
    max-width: 15ch !important;
    font-size: clamp(1rem, 6.5vh, 1.72rem) !important;
    line-height: 1.06 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .assisted-intro-panel h1,
  .home-screen .speech-intro-panel h1,
  .home-screen .review-intro-panel h1 {
    max-width: 15ch !important;
    font-size: clamp(1rem, 6.5vh, 1.72rem) !important;
    line-height: 1.06 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-intro-panel p {
    max-width: 32ch !important;
    font-size: clamp(.56rem, 2.55vh, .76rem) !important;
    line-height: 1.22 !important;
  }

  .home-screen .assisted-intro-panel p,
  .home-screen .speech-intro-panel p,
  .home-screen .review-intro-panel p {
    max-width: 32ch !important;
    font-size: clamp(.56rem, 2.55vh, .76rem) !important;
    line-height: 1.22 !important;
  }

  .home-screen .home-actions-panel {
    align-self: center !important;
    justify-self: stretch !important;
    max-width: none !important;
    height: auto !important;
    gap: clamp(6px, 2.2vh, 12px) !important;
    grid-template-rows: repeat(2, minmax(0, auto)) !important;
  }

  .home-screen .assisted-actions-panel {
    align-self: center !important;
    justify-self: stretch !important;
    max-width: none !important;
    height: auto !important;
    gap: clamp(6px, 2.2vh, 12px) !important;
    grid-template-rows: repeat(2, minmax(0, auto)) !important;
  }

  .home-screen .home-option-card {
    height: auto !important;
    min-height: clamp(58px, 28vh, 84px) !important;
    grid-template-columns: clamp(42px, 15vh, 62px) minmax(0, 1fr) clamp(24px, 7vh, 32px) !important;
    gap: clamp(7px, 1.8vw, 12px) !important;
    padding: clamp(6px, 1.7vh, 10px) !important;
    border-radius: clamp(13px, 4vh, 18px) !important;
  }

  .home-screen .assisted-option-card {
    height: auto !important;
    min-height: clamp(58px, 28vh, 84px) !important;
    grid-template-columns: clamp(42px, 15vh, 62px) minmax(0, 1fr) clamp(24px, 7vh, 32px) !important;
    gap: clamp(7px, 1.8vw, 12px) !important;
    padding: clamp(6px, 1.7vh, 10px) !important;
    border-radius: clamp(13px, 4vh, 18px) !important;
    overflow: hidden !important;
  }

  .home-screen .home-option-icon {
    width: clamp(42px, 15vh, 62px) !important;
    height: clamp(42px, 15vh, 62px) !important;
    min-width: clamp(42px, 15vh, 62px) !important;
    border-radius: clamp(11px, 3.8vh, 16px) !important;
  }

  .home-screen .assisted-option-icon {
    width: clamp(42px, 15vh, 62px) !important;
    height: clamp(42px, 15vh, 62px) !important;
    min-width: clamp(42px, 15vh, 62px) !important;
    border-radius: clamp(11px, 3.8vh, 16px) !important;
  }

  .home-screen .home-option-text {
    gap: 3px !important;
  }

  .home-screen .home-option-text strong {
    font-size: clamp(.78rem, 3.9vh, 1.08rem) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-option-text span {
    font-size: clamp(.58rem, 2.65vh, .76rem) !important;
    line-height: 1.18 !important;
  }

  .home-screen .home-option-arrow {
    display: grid !important;
    width: clamp(24px, 7vh, 32px) !important;
    height: clamp(24px, 7vh, 32px) !important;
    min-width: clamp(24px, 7vh, 32px) !important;
    font-size: clamp(.9rem, 4vh, 1.2rem) !important;
  }

  .home-screen .home-bottom-nav {
    width: min(240px, calc(100vw - 24px)) !important;
    margin-bottom: max(4px, env(safe-area-inset-bottom)) !important;
    padding: 3px !important;
  }

  .home-screen .home-bottom-nav button {
    min-height: clamp(32px, 11vh, 44px) !important;
    padding: 3px 6px !important;
  }

  .home-screen .home-bottom-nav button svg {
    width: clamp(16px, 5vh, 20px) !important;
    height: clamp(16px, 5vh, 20px) !important;
  }

  .home-screen .home-bottom-nav button span {
    font-size: clamp(.58rem, 2.4vh, .72rem) !important;
  }

  .home-screen .speech-work-panel {
    grid-column: 2 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    grid-template-rows: repeat(2, minmax(0, auto)) auto !important;
    gap: clamp(5px, 1.6vh, 8px) !important;
    max-width: none !important;
    height: auto !important;
    min-height: 0 !important;
    overflow: hidden !important;
  }

  .home-screen .speech-intro-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
  }

  .home-screen .speech-config-card,
  .home-screen .speech-phrase-card,
  .home-screen .speech-record-card,
  .home-screen .speech-record-controls {
    min-height: 0 !important;
    padding: clamp(6px, 1.8vh, 10px) !important;
    border-radius: 14px !important;
    overflow: hidden !important;
  }

  .home-screen .speech-config-grid {
    gap: 5px !important;
  }

  .home-screen .speech-config-grid label,
  .home-screen .speech-label,
  .home-screen .status-pill,
  .home-screen .status-file {
    font-size: clamp(.52rem, 2.2vh, .68rem) !important;
    line-height: 1.12 !important;
  }

  .home-screen .speech-config-grid select,
  .home-screen .speech-button-row .soft-action,
  .home-screen .speech-record-controls button {
    min-height: clamp(26px, 8vh, 34px) !important;
    padding: 4px 7px !important;
    font-size: clamp(.58rem, 2.3vh, .72rem) !important;
  }

  .home-screen .speech-phrase-card strong {
    font-size: clamp(.72rem, 3.2vh, .98rem) !important;
    line-height: 1.14 !important;
  }

  .home-screen .speech-record-card {
    grid-template-columns: clamp(42px, 15vh, 60px) minmax(0, 1fr) !important;
  }

  .home-screen .speech-record-icon {
    width: clamp(42px, 15vh, 60px) !important;
    height: clamp(42px, 15vh, 60px) !important;
  }

  .home-screen .speech-record-controls {
    grid-column: 1 / -1 !important;
    display: grid !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 6px !important;
  }

  .home-screen .review-panel {
    align-self: center !important;
    justify-self: stretch !important;
    display: grid !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    gap: clamp(6px, 1.8vh, 10px) !important;
    max-width: none !important;
    height: 100% !important;
    min-height: 0 !important;
    overflow: hidden !important;
  }

  .home-screen .review-preview-card {
    min-height: 0 !important;
    height: 100% !important;
    padding: clamp(6px, 1.8vh, 10px) !important;
    overflow: hidden !important;
  }

  .home-screen .preview-image {
    max-height: 100% !important;
    object-fit: contain !important;
  }

  .home-screen .image-confirm-controls {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 6px !important;
    min-height: 0 !important;
  }
}

@media (orientation: landscape) and (max-width: 640px) {
  .home-screen .google-home-main {
    grid-template-columns: minmax(0, .62fr) minmax(0, 1.38fr) !important;
    gap: 8px !important;
    padding-left: 8px !important;
    padding-right: 8px !important;
  }

  .home-screen .home-badge {
    display: none !important;
  }

  .home-screen .home-intro-panel h1 {
    font-size: clamp(1rem, 6.5vh, 1.55rem) !important;
    max-width: 12ch !important;
  }

  .home-screen .home-intro-panel p {
    font-size: clamp(.56rem, 2.45vh, .68rem) !important;
    max-width: 27ch !important;
  }

  .home-screen .home-option-card {
    grid-template-columns: clamp(38px, 14vh, 54px) minmax(0, 1fr) 26px !important;
    min-height: clamp(52px, 27vh, 74px) !important;
    gap: 7px !important;
  }

  .home-screen .home-option-icon {
    width: clamp(38px, 14vh, 54px) !important;
    height: clamp(38px, 14vh, 54px) !important;
    min-width: clamp(38px, 14vh, 54px) !important;
  }

  .home-screen .home-option-text strong {
    font-size: clamp(.72rem, 3.4vh, .95rem) !important;
  }

  .home-screen .home-option-text span {
    font-size: clamp(.52rem, 2.35vh, .66rem) !important;
  }

  .home-screen .home-option-arrow {
    width: 26px !important;
    height: 26px !important;
    min-width: 26px !important;
  }

  .home-screen .assisted-intro-panel h1,
  .home-screen .speech-intro-panel h1,
  .home-screen .review-intro-panel h1 {
    max-width: 12ch !important;
    font-size: clamp(1rem, 6.5vh, 1.55rem) !important;
  }

  .home-screen .assisted-intro-panel p,
  .home-screen .speech-intro-panel p,
  .home-screen .review-intro-panel p {
    max-width: 27ch !important;
    font-size: clamp(.56rem, 2.45vh, .68rem) !important;
  }

  .home-screen .assisted-option-card {
    grid-template-columns: clamp(38px, 14vh, 54px) minmax(0, 1fr) 26px !important;
    min-height: clamp(52px, 27vh, 74px) !important;
    gap: 7px !important;
  }

  .home-screen .assisted-option-icon {
    width: clamp(38px, 14vh, 54px) !important;
    height: clamp(38px, 14vh, 54px) !important;
    min-width: clamp(38px, 14vh, 54px) !important;
  }

  .home-screen .speech-work-panel {
    gap: 5px !important;
  }

  .home-screen .speech-config-card,
  .home-screen .speech-phrase-card,
  .home-screen .speech-record-card,
  .home-screen .speech-record-controls {
    padding: 6px !important;
  }
}

@media (orientation: landscape) {
  .home-screen .speech-home:not(.review-home) .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, .68fr) minmax(0, 1.32fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    gap: clamp(8px, 2.6vw, 24px) !important;
    padding: clamp(2px, 1.2vh, 6px) clamp(10px, 2.8vw, 28px) clamp(4px, 1.4vh, 8px) !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel,
  .home-screen .speech-home:not(.review-home) .speech-intro-panel h1,
  .home-screen .speech-home:not(.review-home) .speech-intro-panel p,
  .home-screen .speech-home:not(.review-home) .speech-work-panel {
    transform: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: start !important;
    width: 100% !important;
    max-width: none !important;
    padding: 0 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel h1 {
    max-width: 15ch !important;
    font-size: clamp(1rem, 6.5vh, 1.72rem) !important;
    line-height: 1.06 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel p {
    max-width: 32ch !important;
    font-size: clamp(.56rem, 2.55vh, .76rem) !important;
    line-height: 1.22 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-work-panel {
    grid-column: 2 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    height: auto !important;
    min-height: 0 !important;
    max-height: 100% !important;
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .home-screen .review-home .google-home-main,
  .home-screen .processing-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    justify-items: stretch !important;
    gap: 0 !important;
    padding: clamp(4px, 1.5vh, 8px) clamp(12px, 3vw, 28px) clamp(4px, 1.5vh, 8px) !important;
    overflow: hidden !important;
  }

  .home-screen .review-home .speech-intro-panel,
  .home-screen .review-home .review-intro-panel {
    display: none !important;
  }

  .home-screen .review-home .review-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    width: 100% !important;
    height: 100% !important;
    max-height: 100% !important;
    display: grid !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    gap: 6px !important;
    align-self: center !important;
    justify-self: stretch !important;
    transform: none !important;
    overflow: hidden !important;
  }

  .home-screen .review-home .review-preview-card {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 8px !important;
    display: grid !important;
    place-items: center !important;
    overflow: hidden !important;
  }

  .home-screen .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    max-height: 100% !important;
    object-fit: contain !important;
  }

  .home-screen .review-home .image-confirm-controls {
    width: 100% !important;
    min-height: 34px !important;
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 6px !important;
    transform: none !important;
  }

  .home-screen .review-home .image-confirm-controls button {
    min-height: 32px !important;
    padding: 6px 8px !important;
    font-size: .68rem !important;
  }

  .home-screen .processing-home .processing-panel {
    width: 100% !important;
    max-height: 100% !important;
    padding: 12px 14px !important;
    transform: none !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .google-home-main {
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    padding: clamp(4px, 1.5vh, 8px) clamp(12px, 3vw, 28px) clamp(4px, 1.5vh, 8px) !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel {
    display: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-work-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    transform: none !important;
    width: 100% !important;
    height: 100% !important;
    max-height: 100% !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) !important;
    grid-template-rows: 52px 44px 44px !important;
    align-content: start !important;
    gap: clamp(5px, 1.8vh, 8px) !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card {
    grid-column: 1 / -1 !important;
    grid-row: 1 !important;
    min-height: 52px !important;
    display: grid !important;
    align-content: center !important;
    justify-items: center !important;
    gap: 2px !important;
    transform: none !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-card {
    grid-row: 2 !important;
    min-height: 0 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-card {
    grid-column: 1 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    align-items: center !important;
    max-height: 44px !important;
    overflow: hidden !important;
    z-index: 2 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-card {
    display: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-controls {
    grid-column: 1 / -1 !important;
    grid-row: 3 !important;
    position: static !important;
    margin: 0 !important;
    transform: none !important;
    z-index: 1 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid {
    display: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-button-row {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 6px !important;
    width: 100% !important;
    margin: 0 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-button-row .soft-action {
    min-width: 0 !important;
    width: 100% !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card .speech-label {
    font-size: .58rem !important;
    line-height: 1 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card strong {
    font-size: clamp(.74rem, 3vh, .92rem) !important;
    line-height: 1.1 !important;
    text-align: center !important;
    max-width: 100% !important;
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-main {
    grid-template-rows: minmax(36px, 1fr) 158px !important;
    gap: 3px !important;
    padding: 4px clamp(12px, 2.6vw, 22px) 6px !important;
    align-content: stretch !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    height: 158px !important;
    max-height: 158px !important;
    gap: 5px !important;
    padding: 0 clamp(12px, 2.6vw, 18px) max(4px, env(safe-area-inset-bottom)) !important;
    overflow: visible !important;
    align-self: end !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-control-stack {
    gap: 5px !important;
    overflow: visible !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .progress-label,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .progress-line {
    display: none !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row {
    display: grid !important;
    grid-template-columns: repeat(3, 30px) !important;
    justify-content: center !important;
    align-items: center !important;
    gap: 8px !important;
    min-height: 32px !important;
    margin: 0 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row .round-btn,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row .play-btn {
    width: 30px !important;
    min-width: 30px !important;
    height: 30px !important;
    min-height: 30px !important;
    display: grid !important;
    place-items: center !important;
    padding: 0 !important;
    font-size: 1rem !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    display: grid !important;
    grid-auto-flow: column !important;
    grid-auto-columns: minmax(0, 1fr) !important;
    grid-template-columns: none !important;
    gap: 5px !important;
    width: 100% !important;
    min-width: 0 !important;
    min-height: 30px !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-width: 0 !important;
    width: 100% !important;
    min-height: 28px !important;
    padding: 4px 6px !important;
    border-radius: 999px !important;
    font-size: clamp(.56rem, 1.45vw, .68rem) !important;
    line-height: 1.05 !important;
    white-space: nowrap !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    display: grid !important;
    grid-template-columns: 1fr 1fr 1.05fr !important;
    gap: 5px !important;
    min-height: 66px !important;
    padding: 5px !important;
    border-radius: 12px !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block {
    min-width: 0 !important;
    gap: 3px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block label {
    font-size: .58rem !important;
    line-height: 1.05 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block input {
    height: 18px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block select {
    min-height: 28px !important;
    padding: 4px 7px !important;
    font-size: .58rem !important;
  }
}

@media (orientation: portrait) {
  .home-screen .google-home {
    width: calc(100vw - 12px) !important;
    max-width: calc(100vw - 12px) !important;
    height: calc(100dvh - 12px) !important;
    min-height: calc(100dvh - 12px) !important;
    margin: 6px auto !important;
    border: 1px solid #d9e2ee !important;
    border-radius: 10px !important;
    box-shadow: 0 6px 18px rgba(15, 23, 42, .06) !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-main {
    grid-template-rows: minmax(150px, 1fr) auto !important;
    gap: 6px !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-top {
    min-height: 0 !important;
    padding-bottom: 8px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    width: calc(100vw - 44px) !important;
    max-width: calc(100vw - 44px) !important;
    justify-self: center !important;
    margin-left: auto !important;
    margin-right: auto !important;
    max-height: none !important;
    overflow: visible !important;
    padding-top: 4px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row {
    min-height: 34px !important;
    margin-top: 0 !important;
    margin-bottom: 8px !important;
    position: relative !important;
    z-index: 3 !important;
    transform: none !important;
    display: flex !important;
    justify-content: center !important;
    align-items: center !important;
    gap: 14px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row .round-btn,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row .play-btn {
    width: 34px !important;
    min-width: 34px !important;
    height: 34px !important;
    min-height: 34px !important;
    display: grid !important;
    place-items: center !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    box-sizing: border-box !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 7px !important;
    margin-inline: auto !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-width: 0 !important;
    max-width: 100% !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn:last-child {
    grid-column: 1 / -1 !important;
    width: 100% !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact button:last-of-type {
    grid-column: 1 / -1 !important;
    width: 100% !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    box-sizing: border-box !important;
    grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
    gap: 6px !important;
    padding: 7px !important;
    margin-inline: auto !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block {
    min-width: 0 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-control-stack {
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block select,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block input {
    min-width: 0 !important;
    width: 100% !important;
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-bottom,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    justify-self: center !important;
    margin-left: auto !important;
    margin-right: auto !important;
    transform: none !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-main {
    grid-template-rows: minmax(30px, 1fr) 170px !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    height: 170px !important;
    max-height: 170px !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row {
    min-height: 30px !important;
    position: relative !important;
    z-index: 3 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    min-height: 30px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    display: grid !important;
    min-height: 72px !important;
    height: 72px !important;
    max-height: 72px !important;
    align-items: center !important;
  }
}

@media (orientation: landscape) {
  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    width: calc(100vw - 52px) !important;
    max-width: calc(100vw - 52px) !important;
    justify-self: center !important;
    margin-inline: auto !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    display: grid !important;
    grid-template-columns: repeat(10, minmax(0, 1fr)) !important;
    grid-auto-flow: row !important;
    gap: 4px !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-width: 0 !important;
    width: 100% !important;
    min-height: 28px !important;
    padding: 4px 3px !important;
    border-radius: 999px !important;
    font-size: clamp(.48rem, .82vw, .66rem) !important;
    line-height: 1.05 !important;
    white-space: nowrap !important;
    overflow-wrap: anywhere !important;
    overflow: hidden !important;
    text-overflow: clip !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    width: 100% !important;
    max-width: 100% !important;
    margin-inline: auto !important;
  }
}

@media (orientation: portrait) {
  .home-screen .google-home:not(.reader-home) {
    width: calc(100vw - 20px) !important;
    max-width: calc(100vw - 20px) !important;
    height: calc(100dvh - 24px) !important;
    min-height: calc(100dvh - 24px) !important;
    margin: 10px auto 14px !important;
    border-radius: 12px !important;
  }

  .home-screen .google-home:not(.reader-home) .google-home-main {
    padding-left: 14px !important;
    padding-right: 14px !important;
    padding-bottom: 14px !important;
  }

  .home-screen .google-home:not(.reader-home) .home-actions-panel {
    width: 100% !important;
    max-width: 100% !important;
    justify-self: stretch !important;
  }

  .home-screen .google-home:not(.reader-home) .home-option-card {
    width: 100% !important;
    max-width: 100% !important;
  }

  .home-screen .google-home:not(.reader-home):not(.review-home) .google-home-main {
    grid-template-rows: auto minmax(0, 1fr) !important;
    align-content: stretch !important;
    gap: clamp(14px, 2.2vh, 18px) !important;
    padding-bottom: clamp(18px, 3vh, 26px) !important;
  }

  .home-screen .google-home:not(.reader-home):not(.review-home) .home-actions-panel,
  .home-screen .assisted-home .assisted-actions-panel {
    height: 100% !important;
    min-height: 0 !important;
    align-content: stretch !important;
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    gap: clamp(14px, 2.4vh, 20px) !important;
    padding-bottom: 0 !important;
    transform: none !important;
  }

  .home-screen .google-home:not(.reader-home):not(.review-home) .home-option-card,
  .home-screen .assisted-home .assisted-option-card {
    height: 100% !important;
    min-height: clamp(150px, 26vh, 190px) !important;
    max-height: none !important;
    align-self: stretch !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn:last-child,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact button:last-of-type {
    grid-column: auto !important;
    width: 100% !important;
  }
}

/* ===== SPEECH LANDSCAPE FINAL FIX =====
   Evita colisao entre frase, silabas e botoes em telemoveis deitados. */
@media (orientation: landscape) and (max-height: 430px) {
  .home-screen .speech-home:not(.review-home) {
    grid-template-rows: auto minmax(0, 1fr) !important;
  }

  .home-screen .speech-home:not(.review-home) .google-home-header {
    min-height: 0 !important;
    padding: max(6px, env(safe-area-inset-top)) clamp(18px, 3vw, 28px) 2px !important;
  }

  .home-screen .speech-home:not(.review-home) .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    padding: 4px clamp(18px, 4vw, 32px) max(8px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel {
    display: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-work-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    width: 100% !important;
    height: 100% !important;
    max-width: none !important;
    max-height: 100% !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) minmax(112px, 128px) !important;
    grid-template-rows: 44px minmax(58px, 1fr) 38px !important;
    gap: 8px !important;
    align-content: stretch !important;
    transform: none !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card {
    grid-column: 1 / -1 !important;
    grid-row: 2 !important;
    min-height: 0 !important;
    height: 100% !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) auto !important;
    grid-template-rows: auto minmax(0, 1fr) !important;
    align-items: center !important;
    gap: 3px 10px !important;
    padding: 8px 12px !important;
    transform: none !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card .speech-label {
    grid-column: 1 / -1 !important;
    font-size: .58rem !important;
    line-height: 1 !important;
    text-align: center !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card strong {
    grid-column: 1 !important;
    min-width: 0 !important;
    max-width: 100% !important;
    font-size: clamp(.68rem, 2.55vh, .86rem) !important;
    line-height: 1.16 !important;
    text-align: center !important;
    overflow: hidden !important;
    display: -webkit-box !important;
    -webkit-line-clamp: 2 !important;
    -webkit-box-orient: vertical !important;
  }

  .home-screen .speech-home:not(.review-home) .syllable-toggle {
    grid-column: 2 !important;
    grid-row: 2 !important;
    justify-self: end !important;
    align-self: center !important;
    min-height: 28px !important;
    padding: 4px 10px !important;
    font-size: .68rem !important;
    white-space: nowrap !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-card {
    grid-column: 1 !important;
    grid-row: 1 !important;
    min-height: 0 !important;
    height: 44px !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    padding: 5px 8px !important;
    background: #fff !important;
    border: 1px solid #e5eaf2 !important;
    border-radius: 14px !important;
    box-shadow: 0 5px 14px rgba(15, 23, 42, .04) !important;
    transform: none !important;
    overflow: visible !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid {
    display: grid !important;
    grid-template-columns: .7fr .55fr minmax(120px, 1fr) !important;
    gap: 6px !important;
    min-width: 0 !important;
    align-items: end !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-button-row {
    display: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid label {
    gap: 2px !important;
    min-width: 0 !important;
    font-size: .54rem !important;
    line-height: 1 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid label:last-child {
    grid-column: auto !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid select {
    min-height: 26px !important;
    padding: 3px 7px !important;
    border-radius: 9px !important;
    font-size: .62rem !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-controls {
    grid-column: 1 / -1 !important;
    grid-row: 3 !important;
    width: 100% !important;
    height: 38px !important;
    display: grid !important;
    grid-template-columns: repeat(6, minmax(0, 1fr)) !important;
    gap: 6px !important;
    padding: 0 !important;
    background: transparent !important;
    border: 0 !important;
    box-shadow: none !important;
    transform: none !important;
    overflow: visible !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-controls button {
    width: 100% !important;
    min-width: 0 !important;
    min-height: 38px !important;
    padding: 6px 7px !important;
    font-size: .68rem !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-controls .speech-inline-generate {
    display: grid !important;
    place-items: center !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-card {
    grid-column: 2 !important;
    grid-row: 1 !important;
    min-height: 0 !important;
    height: 44px !important;
    display: grid !important;
    grid-template-columns: 28px minmax(0, 1fr) !important;
    gap: 6px !important;
    align-items: center !important;
    padding: 5px 7px !important;
    background: #fff !important;
    border: 1px solid #e5eaf2 !important;
    border-radius: 14px !important;
    box-shadow: 0 5px 14px rgba(15, 23, 42, .04) !important;
    overflow: hidden !important;
    transform: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-icon {
    width: 28px !important;
    height: 28px !important;
    border-radius: 9px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-copy {
    min-width: 0 !important;
    gap: 0 !important;
  }

  .home-screen .speech-home:not(.review-home) .recording-status-line {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) auto !important;
    gap: 4px !important;
    align-items: center !important;
    min-width: 0 !important;
  }

  .home-screen .speech-home:not(.review-home) .status-pill {
    min-width: 0 !important;
    padding: 5px 6px !important;
    font-size: .58rem !important;
    line-height: 1 !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    white-space: nowrap !important;
  }

  .home-screen .speech-home:not(.review-home) .recording-timer {
    min-width: 42px !important;
    padding: 5px 6px !important;
    font-size: .62rem !important;
    line-height: 1 !important;
  }

  .home-screen .speech-home:not(.review-home) .status-file,
  .home-screen .speech-home:not(.review-home) .audio-player {
    display: none !important;
  }
}

/* ===== SPEECH PORTRAIT COMPACT FORM =====
   Em telemoveis verticais, os selects sao pequenos; os labels nao devem
   consumir uma linha inteira e roubar altura a frase/gravação. */
@media (orientation: portrait) and (max-width: 620px) {
  .home-screen .speech-home:not(.review-home) .google-home-main {
    grid-template-rows: auto minmax(0, 1fr) !important;
    gap: clamp(7px, 1.4vh, 10px) !important;
    padding-top: 4px !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel {
    gap: 3px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel h1 {
    font-size: clamp(1.14rem, 5.6vw, 1.42rem) !important;
    line-height: 1.05 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel p {
    font-size: .78rem !important;
    line-height: 1.16 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-work-panel {
    height: 100% !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-rows: auto minmax(136px, 1fr) auto auto !important;
    gap: 7px !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-card {
    min-height: 0 !important;
    padding: 7px !important;
    border-radius: 12px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid {
    display: grid !important;
    grid-template-columns: .72fr .56fr minmax(0, 1.12fr) !important;
    gap: 5px !important;
    align-items: end !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid label {
    min-width: 0 !important;
    gap: 2px !important;
    font-size: .56rem !important;
    line-height: 1 !important;
    color: #64748b !important;
    font-weight: 800 !important;
    text-align: center !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid label:last-child {
    grid-column: auto !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid select {
    width: 100% !important;
    min-width: 0 !important;
    min-height: 31px !important;
    padding: 6px 8px !important;
    border-radius: 10px !important;
    font-size: .82rem !important;
    font-weight: 700 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-button-row {
    margin-top: 6px !important;
    gap: 5px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-button-row .soft-action {
    min-height: 34px !important;
    padding: 7px 8px !important;
    font-size: .76rem !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card {
    min-height: 0 !important;
    height: 100% !important;
    padding: 11px 10px !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card strong {
    font-size: clamp(.86rem, 4vw, 1rem) !important;
    line-height: 1.16 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-card {
    grid-template-columns: 36px minmax(0, 1fr) !important;
    gap: 7px !important;
    padding: 7px 8px !important;
    border-radius: 12px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-icon {
    width: 36px !important;
    height: 36px !important;
    border-radius: 10px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-copy {
    gap: 4px !important;
  }

  .home-screen .speech-home:not(.review-home) .status-file {
    display: none !important;
  }

  .home-screen .speech-home:not(.review-home) .recording-status-line {
    flex-wrap: nowrap !important;
    gap: 6px !important;
  }

  .home-screen .speech-home:not(.review-home) .status-pill {
    min-width: 0 !important;
    padding: 5px 8px !important;
    font-size: .66rem !important;
    white-space: nowrap !important;
  }

  .home-screen .speech-home:not(.review-home) .recording-timer {
    min-width: 46px !important;
    padding: 5px 7px !important;
    font-size: .68rem !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-controls {
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 5px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-controls button {
    min-height: 34px !important;
    padding: 7px 5px !important;
    font-size: .72rem !important;
  }
}

/* ===== LANDSCAPE CONSISTENCY FOR TABLETS / LARGE SCREENS =====
   Mantem o mesmo desenho em horizontal sem deixar regras antigas de desktop
   aumentar logo/titulos ate colidirem com o conteudo. */
@media (orientation: landscape) and (min-height: 431px) {
  .home-screen .google-home:not(.reader-home) {
    width: 100vw !important;
    max-width: 100vw !important;
    height: 100dvh !important;
    min-height: 100dvh !important;
    margin: 0 !important;
    border: 0 !important;
    border-radius: 0 !important;
    grid-template-rows: auto minmax(0, 1fr) auto !important;
    overflow: hidden !important;
  }

  .home-screen .google-home:not(.reader-home) .google-home-header {
    min-height: 0 !important;
    padding: max(8px, env(safe-area-inset-top)) clamp(18px, 3vw, 34px) 4px !important;
    align-items: flex-start !important;
  }

  .home-screen .google-home:not(.reader-home) .home-logo-mark {
    width: clamp(30px, 5.8vh, 38px) !important;
    height: clamp(30px, 5.8vh, 38px) !important;
    min-width: clamp(30px, 5.8vh, 38px) !important;
    border-radius: 10px !important;
  }

  .home-screen .google-home:not(.reader-home) .home-brand {
    gap: 10px !important;
    align-items: center !important;
    min-width: 0 !important;
  }

  .home-screen .google-home:not(.reader-home) .home-brand-title {
    font-size: clamp(1.45rem, 5vh, 2rem) !important;
    line-height: 1 !important;
    letter-spacing: 0 !important;
    font-weight: 800 !important;
  }

  .home-screen .google-home:not(.reader-home) .home-brand-subtitle {
    font-size: clamp(.76rem, 2.2vh, .95rem) !important;
    line-height: 1.1 !important;
  }

  .home-screen .google-home:not(.reader-home) .home-help-btn {
    width: clamp(30px, 5.4vh, 38px) !important;
    height: clamp(30px, 5.4vh, 38px) !important;
    min-width: clamp(30px, 5.4vh, 38px) !important;
    padding: 6px !important;
  }

  .home-screen .google-home:not(.reader-home) .google-home-main {
    min-height: 0 !important;
    max-height: 100% !important;
    padding: clamp(4px, 1.2vh, 8px) clamp(22px, 5vw, 54px) max(10px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
  }

  .home-screen .google-home:not(.assisted-home):not(.speech-home):not(.review-home):not(.processing-home) .google-home-main,
  .home-screen .assisted-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(220px, .42fr) minmax(300px, .58fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    justify-content: center !important;
    column-gap: clamp(22px, 5vw, 54px) !important;
  }

  .home-screen .home-intro-panel,
  .home-screen .assisted-intro-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    transform: none !important;
    padding: 0 !important;
    max-width: none !important;
  }

  .home-screen .home-badge {
    position: static !important;
    justify-self: start !important;
    margin: 0 0 10px !important;
    font-size: clamp(.72rem, 2.1vh, .92rem) !important;
    padding: 6px 12px !important;
  }

  .home-screen .home-intro-panel h1,
  .home-screen .assisted-intro-panel h1,
  .home-screen .speech-intro-panel h1,
  .home-screen .review-intro-panel h1 {
    max-width: 14ch !important;
    font-size: clamp(1.55rem, 7vh, 2.45rem) !important;
    line-height: 1.06 !important;
    letter-spacing: 0 !important;
    margin: 0 !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel p,
  .home-screen .assisted-intro-panel p,
  .home-screen .speech-intro-panel p,
  .home-screen .review-intro-panel p {
    max-width: 32ch !important;
    font-size: clamp(.86rem, 2.8vh, 1.08rem) !important;
    line-height: 1.25 !important;
    margin: 10px 0 0 !important;
    transform: none !important;
  }

  .home-screen .home-actions-panel,
  .home-screen .assisted-actions-panel {
    grid-column: 2 !important;
    grid-row: 1 !important;
    width: min(100%, 520px) !important;
    max-width: 520px !important;
    justify-self: start !important;
    align-self: center !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(78px, auto)) !important;
    gap: clamp(10px, 2.4vh, 14px) !important;
    transform: none !important;
    padding: 0 !important;
  }

  .home-screen .home-option-card,
  .home-screen .assisted-option-card {
    width: 100% !important;
    min-height: clamp(78px, 16vh, 104px) !important;
    max-height: none !important;
    display: grid !important;
    grid-template-columns: clamp(48px, 9vh, 66px) minmax(0, 1fr) clamp(28px, 5vh, 34px) !important;
    gap: clamp(10px, 2vw, 16px) !important;
    padding: clamp(10px, 2vh, 14px) !important;
    align-items: center !important;
    transform: none !important;
  }

  .home-screen .home-option-icon,
  .home-screen .assisted-option-icon {
    width: clamp(48px, 9vh, 66px) !important;
    height: clamp(48px, 9vh, 66px) !important;
    min-width: clamp(48px, 9vh, 66px) !important;
  }

  .home-screen .home-option-text strong,
  .home-screen .assisted-option-card .home-option-text strong {
    font-size: clamp(1rem, 3.4vh, 1.35rem) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-option-text span,
  .home-screen .assisted-option-card .home-option-text span {
    font-size: clamp(.8rem, 2.6vh, 1rem) !important;
    line-height: 1.22 !important;
  }

  .home-screen .home-recent-card,
  .home-screen .home-safety-card {
    display: none !important;
  }

  .home-screen .review-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(220px, .34fr) minmax(280px, .66fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    justify-content: center !important;
    column-gap: clamp(18px, 4vw, 48px) !important;
  }

  .home-screen .review-home .speech-intro-panel,
  .home-screen .review-home .review-intro-panel {
    display: block !important;
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    padding: 0 !important;
    transform: none !important;
  }

  .home-screen .review-home .review-panel {
    grid-column: 2 !important;
    grid-row: 1 !important;
    width: min(100%, 520px) !important;
    max-width: 520px !important;
    height: min(100%, calc(100dvh - 98px)) !important;
    min-height: 0 !important;
    justify-self: start !important;
    align-self: center !important;
    display: grid !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    gap: 8px !important;
    transform: none !important;
    overflow: hidden !important;
  }

  .home-screen .review-home .review-preview-card {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 8px !important;
    overflow: hidden !important;
    display: grid !important;
    place-items: center !important;
  }

  .home-screen .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    max-height: 100% !important;
    object-fit: contain !important;
  }

  .home-screen .review-home .image-confirm-controls {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    min-height: 42px !important;
    gap: 8px !important;
    padding: 0 !important;
  }

  .home-screen .processing-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    place-items: center !important;
  }

  .home-screen .processing-home .processing-panel {
    width: min(100%, 520px) !important;
    max-width: 520px !important;
    min-height: 0 !important;
    padding: clamp(18px, 4vh, 30px) !important;
    transform: none !important;
  }

  .home-screen .speech-home:not(.review-home) .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    justify-items: center !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-intro-panel {
    display: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-work-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    width: min(100%, 860px) !important;
    max-width: 860px !important;
    height: auto !important;
    max-height: calc(100dvh - 92px) !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: auto auto auto auto !important;
    gap: 8px !important;
    align-self: center !important;
    justify-self: center !important;
    transform: none !important;
    overflow: hidden !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card,
  .home-screen .speech-home:not(.review-home) .speech-config-card,
  .home-screen .speech-home:not(.review-home) .speech-record-card,
  .home-screen .speech-home:not(.review-home) .speech-record-controls {
    grid-column: 1 !important;
    width: 100% !important;
    max-width: 100% !important;
    transform: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card {
    grid-row: 1 !important;
    min-height: clamp(120px, 26vh, 188px) !important;
    padding: clamp(12px, 2.6vh, 18px) !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-phrase-card strong {
    font-size: clamp(1.05rem, 3.5vh, 1.5rem) !important;
    line-height: 1.18 !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-card {
    grid-row: 2 !important;
    padding: 8px 10px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-config-grid {
    display: grid !important;
    grid-template-columns: .7fr .55fr minmax(0, 1.1fr) !important;
    gap: 8px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-button-row {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    margin-top: 8px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-card {
    grid-row: 3 !important;
    display: grid !important;
    grid-template-columns: 42px minmax(0, 1fr) !important;
    gap: 8px !important;
    padding: 8px 10px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-icon {
    width: 42px !important;
    height: 42px !important;
    border-radius: 11px !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-controls {
    grid-row: 4 !important;
    display: grid !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 8px !important;
    padding: 0 !important;
    background: transparent !important;
    border: 0 !important;
    box-shadow: none !important;
  }

  .home-screen .speech-home:not(.review-home) .speech-record-controls .speech-inline-generate {
    display: none !important;
  }
}

/* ===== FINAL SMALL-LANDSCAPE NORMALIZATION =====
   Regra de fecho para tablets/telemoveis deitados com pouca altura.
   O desenho deve manter proporcoes estaveis, sem header gigante nem
   titulos a passar por cima do conteudo. */
@media (orientation: landscape) and (max-height: 430px) {
  .home-screen .google-home,
  .home-screen .reader-home {
    width: 100vw !important;
    max-width: 100vw !important;
    height: 100dvh !important;
    min-height: 100dvh !important;
    margin: 0 !important;
    border: 0 !important;
    border-radius: 0 !important;
    grid-template-rows: auto minmax(0, 1fr) auto !important;
    overflow: hidden !important;
  }

  .home-screen .google-home-header {
    min-height: 0 !important;
    padding: max(5px, env(safe-area-inset-top)) clamp(14px, 2.8vw, 22px) 2px !important;
    align-items: flex-start !important;
  }

  .home-screen .home-logo-mark {
    width: clamp(22px, 7.2vh, 30px) !important;
    height: clamp(22px, 7.2vh, 30px) !important;
    min-width: clamp(22px, 7.2vh, 30px) !important;
    border-radius: 8px !important;
  }

  .home-screen .home-brand {
    gap: 8px !important;
    align-items: center !important;
    min-width: 0 !important;
  }

  .home-screen .home-brand-title {
    font-size: clamp(1.1rem, 6.3vh, 1.55rem) !important;
    line-height: 1 !important;
    letter-spacing: 0 !important;
    font-weight: 800 !important;
  }

  .home-screen .home-brand-subtitle {
    font-size: clamp(.58rem, 2.7vh, .76rem) !important;
    line-height: 1.05 !important;
  }

  .home-screen .home-help-btn,
  .home-screen .reader-header-btn {
    width: clamp(24px, 7vh, 30px) !important;
    height: clamp(24px, 7vh, 30px) !important;
    min-width: clamp(24px, 7vh, 30px) !important;
    padding: 5px !important;
  }

  .home-screen .reader-text-btn {
    font-size: 1.1rem !important;
  }

  .home-screen .google-home-main {
    min-height: 0 !important;
    max-height: 100% !important;
    padding: 3px clamp(16px, 4vw, 28px) max(5px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
  }

  .home-screen .google-home:not(.assisted-home):not(.speech-home):not(.review-home):not(.processing-home) .google-home-main,
  .home-screen .assisted-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(190px, .42fr) minmax(300px, .58fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    column-gap: clamp(16px, 4vw, 34px) !important;
    align-items: center !important;
    justify-content: center !important;
  }

  .home-screen .home-intro-panel,
  .home-screen .assisted-intro-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    padding: 0 !important;
    transform: none !important;
  }

  .home-screen .home-badge {
    position: static !important;
    justify-self: start !important;
    margin: 0 0 6px !important;
    padding: 4px 9px !important;
    font-size: clamp(.58rem, 2.9vh, .72rem) !important;
    line-height: 1 !important;
  }

  .home-screen .home-intro-panel h1,
  .home-screen .assisted-intro-panel h1 {
    max-width: 12ch !important;
    font-size: clamp(1.32rem, 8vh, 2rem) !important;
    line-height: 1.05 !important;
    letter-spacing: 0 !important;
    margin: 0 !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel p,
  .home-screen .assisted-intro-panel p {
    max-width: 27ch !important;
    font-size: clamp(.68rem, 3.5vh, .92rem) !important;
    line-height: 1.22 !important;
    margin: 8px 0 0 !important;
    transform: none !important;
  }

  .home-screen .home-actions-panel,
  .home-screen .assisted-actions-panel {
    grid-column: 2 !important;
    grid-row: 1 !important;
    width: min(100%, 420px) !important;
    max-width: 420px !important;
    justify-self: start !important;
    align-self: center !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(68px, auto)) !important;
    gap: clamp(8px, 2.3vh, 12px) !important;
    padding: 0 !important;
    transform: none !important;
  }

  .home-screen .home-option-card,
  .home-screen .assisted-option-card {
    width: 100% !important;
    min-height: clamp(68px, 18vh, 86px) !important;
    max-height: none !important;
    display: grid !important;
    grid-template-columns: clamp(38px, 11vh, 50px) minmax(0, 1fr) clamp(24px, 7vh, 30px) !important;
    gap: clamp(8px, 2vw, 12px) !important;
    padding: clamp(8px, 2vh, 11px) !important;
    align-items: center !important;
    transform: none !important;
  }

  .home-screen .home-option-icon,
  .home-screen .assisted-option-icon {
    width: clamp(38px, 11vh, 50px) !important;
    height: clamp(38px, 11vh, 50px) !important;
    min-width: clamp(38px, 11vh, 50px) !important;
  }

  .home-screen .home-option-arrow {
    width: clamp(24px, 7vh, 30px) !important;
    height: clamp(24px, 7vh, 30px) !important;
    min-width: clamp(24px, 7vh, 30px) !important;
    font-size: .9rem !important;
  }

  .home-screen .home-option-text strong,
  .home-screen .assisted-option-card .home-option-text strong {
    font-size: clamp(.9rem, 4.2vh, 1.16rem) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-option-text span,
  .home-screen .assisted-option-card .home-option-text span {
    font-size: clamp(.66rem, 3.2vh, .86rem) !important;
    line-height: 1.18 !important;
  }

  .home-screen .home-recent-card,
  .home-screen .home-safety-card {
    display: none !important;
  }

  .home-screen .review-home .google-home-main,
  .home-screen .processing-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    place-items: center !important;
    padding: 4px clamp(18px, 4vw, 30px) max(7px, env(safe-area-inset-bottom)) !important;
  }

  .home-screen .review-home .speech-intro-panel,
  .home-screen .review-home .review-intro-panel {
    display: none !important;
  }

  .home-screen .review-home .review-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    width: min(100%, 520px) !important;
    max-width: 520px !important;
    height: 100% !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-rows: minmax(0, 1fr) 34px !important;
    gap: 6px !important;
    justify-self: center !important;
    align-self: stretch !important;
    transform: none !important;
    overflow: hidden !important;
  }

  .home-screen .review-home .review-preview-card {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 6px !important;
    display: grid !important;
    place-items: center !important;
    overflow: hidden !important;
  }

  .home-screen .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    max-height: 100% !important;
    object-fit: contain !important;
  }

  .home-screen .review-home .image-confirm-controls {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 6px !important;
    min-height: 34px !important;
    padding: 0 !important;
    transform: none !important;
  }

  .home-screen .review-home .image-confirm-controls button {
    min-height: 34px !important;
    padding: 6px 8px !important;
    font-size: .74rem !important;
  }

  .home-screen .processing-home .processing-panel {
    width: min(100%, 480px) !important;
    max-width: 480px !important;
    padding: clamp(16px, 5vh, 26px) !important;
    transform: none !important;
  }

  .home-screen .reader-home .google-home-main,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    gap: 4px !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    padding: 3px clamp(14px, 3vw, 24px) max(6px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
    align-items: stretch !important;
    justify-items: stretch !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-top {
    grid-column: 1 !important;
    grid-row: 1 !important;
    width: 100% !important;
    max-width: 100% !important;
    min-height: 0 !important;
    height: 100% !important;
    padding: 8px 6px !important;
    display: grid !important;
    place-items: center stretch !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reading-focus,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .line-focus,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .word-focus,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .word-context {
    width: 100% !important;
    max-width: 100% !important;
    text-align: left !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .line-focus {
    font-size: clamp(.86rem, 4vh, 1.05rem) !important;
    line-height: 1.18 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    grid-column: 1 !important;
    grid-row: 2 !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    height: auto !important;
    max-height: 152px !important;
    padding: 0 !important;
    justify-self: stretch !important;
    align-self: end !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-control-stack {
    gap: 4px !important;
    width: 100% !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row {
    min-height: 26px !important;
    gap: 9px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row .round-btn,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .transport-row .play-btn {
    width: 26px !important;
    min-width: 26px !important;
    height: 26px !important;
    min-height: 26px !important;
    padding: 0 !important;
    font-size: .85rem !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .progress-label {
    font-size: .66rem !important;
    line-height: 1 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .progress-line {
    height: 4px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    display: grid !important;
    grid-template-columns: repeat(10, minmax(0, 1fr)) !important;
    gap: 4px !important;
    width: 100% !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-width: 0 !important;
    min-height: 24px !important;
    padding: 3px 4px !important;
    border-radius: 999px !important;
    font-size: clamp(.48rem, 1.8vh, .62rem) !important;
    line-height: 1 !important;
    white-space: nowrap !important;
    overflow: hidden !important;
    text-overflow: clip !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    width: 100% !important;
    height: 54px !important;
    min-height: 54px !important;
    max-height: 54px !important;
    display: grid !important;
    grid-template-columns: 1fr 1fr 1.05fr !important;
    gap: 5px !important;
    padding: 5px !important;
    overflow: hidden !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block {
    gap: 2px !important;
    min-width: 0 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block label {
    font-size: .58rem !important;
    line-height: 1 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block input {
    height: 18px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .setting-block select {
    min-height: 24px !important;
    padding: 3px 6px !important;
    font-size: .58rem !important;
  }
}

/* ===== TABLET / MEDIUM LANDSCAPE RESTORE =====
   Entre 331px e 430px de altura ja ha espaco para o desenho largo.
   O modo ultra-compacto fica reservado para telefones realmente baixos. */
@media (orientation: landscape) and (min-height: 331px) and (max-height: 430px) {
  .home-screen .google-home:not(.reader-home),
  .home-screen .reader-home {
    grid-template-rows: auto minmax(0, 1fr) auto !important;
  }

  .home-screen .google-home-header {
    padding: max(12px, env(safe-area-inset-top)) clamp(24px, 4vw, 42px) 6px !important;
  }

  .home-screen .home-logo-mark {
    width: clamp(36px, 9vh, 52px) !important;
    height: clamp(36px, 9vh, 52px) !important;
    min-width: clamp(36px, 9vh, 52px) !important;
    border-radius: 13px !important;
  }

  .home-screen .home-brand-title {
    font-size: clamp(1.55rem, 7vh, 2.45rem) !important;
    line-height: 1 !important;
  }

  .home-screen .home-brand-subtitle {
    font-size: clamp(.78rem, 3vh, 1rem) !important;
  }

  .home-screen .home-help-btn,
  .home-screen .reader-header-btn {
    width: clamp(34px, 8vh, 44px) !important;
    height: clamp(34px, 8vh, 44px) !important;
    min-width: clamp(34px, 8vh, 44px) !important;
  }

  .home-screen .google-home:not(.assisted-home):not(.speech-home):not(.review-home):not(.processing-home) .google-home-main,
  .home-screen .assisted-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(210px, .36fr) minmax(330px, .64fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    column-gap: clamp(24px, 5vw, 52px) !important;
    align-items: center !important;
    justify-content: center !important;
    padding: 8px clamp(26px, 5vw, 56px) max(12px, env(safe-area-inset-bottom)) !important;
  }

  .home-screen .home-intro-panel,
  .home-screen .assisted-intro-panel {
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    padding: 0 !important;
    transform: none !important;
  }

  .home-screen .home-badge {
    position: static !important;
    justify-self: start !important;
    margin: 0 0 10px !important;
    padding: 6px 12px !important;
    font-size: clamp(.72rem, 2.6vh, .95rem) !important;
  }

  .home-screen .home-intro-panel h1,
  .home-screen .assisted-intro-panel h1 {
    max-width: 14ch !important;
    font-size: clamp(1.65rem, 8.4vh, 2.55rem) !important;
    line-height: 1.06 !important;
    letter-spacing: 0 !important;
    margin: 0 !important;
  }

  .home-screen .home-intro-panel p,
  .home-screen .assisted-intro-panel p {
    max-width: 30ch !important;
    font-size: clamp(.84rem, 3.2vh, 1.05rem) !important;
    line-height: 1.24 !important;
    margin: 10px 0 0 !important;
  }

  .home-screen .home-actions-panel,
  .home-screen .assisted-actions-panel {
    grid-column: 2 !important;
    grid-row: 1 !important;
    width: min(100%, 690px) !important;
    max-width: 690px !important;
    justify-self: stretch !important;
    align-self: center !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(86px, auto)) !important;
    gap: clamp(10px, 2.8vh, 16px) !important;
    padding: 0 !important;
    transform: none !important;
  }

  .home-screen .home-option-card,
  .home-screen .assisted-option-card {
    min-height: clamp(86px, 22vh, 118px) !important;
    grid-template-columns: clamp(52px, 12vh, 76px) minmax(0, 1fr) clamp(30px, 7vh, 40px) !important;
    gap: clamp(12px, 2.4vw, 18px) !important;
    padding: clamp(12px, 2.7vh, 18px) !important;
  }

  .home-screen .home-option-icon,
  .home-screen .assisted-option-icon {
    width: clamp(52px, 12vh, 76px) !important;
    height: clamp(52px, 12vh, 76px) !important;
    min-width: clamp(52px, 12vh, 76px) !important;
  }

  .home-screen .home-option-text strong,
  .home-screen .assisted-option-card .home-option-text strong {
    font-size: clamp(1.05rem, 4.6vh, 1.55rem) !important;
    line-height: 1.08 !important;
  }

  .home-screen .home-option-text span,
  .home-screen .assisted-option-card .home-option-text span {
    font-size: clamp(.82rem, 3.2vh, 1.05rem) !important;
    line-height: 1.2 !important;
  }

  .home-screen .review-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    place-items: stretch center !important;
    padding: 10px clamp(28px, 5vw, 56px) max(12px, env(safe-area-inset-bottom)) !important;
  }

  .home-screen .review-home .speech-intro-panel,
  .home-screen .review-home .review-intro-panel {
    display: none !important;
  }

  .home-screen .review-home .review-panel {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    height: 100% !important;
    grid-template-rows: minmax(0, 1fr) clamp(42px, 9vh, 56px) !important;
    gap: 10px !important;
    justify-self: center !important;
    align-self: stretch !important;
  }

  .home-screen .review-home .review-preview-card {
    padding: 10px !important;
  }

  .home-screen .review-home .image-confirm-controls button {
    min-height: clamp(42px, 9vh, 56px) !important;
    font-size: clamp(.9rem, 3.4vh, 1.16rem) !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    padding: 8px clamp(28px, 5vw, 56px) max(10px, env(safe-area-inset-bottom)) !important;
    gap: 8px !important;
    justify-items: stretch !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-top,
  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    justify-self: center !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .line-focus {
    font-size: clamp(.92rem, 3.6vh, 1.25rem) !important;
    line-height: 1.2 !important;
    text-align: left !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    max-height: 190px !important;
    padding: 0 !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    grid-template-columns: repeat(10, minmax(0, 1fr)) !important;
    gap: 6px !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .controls-compact .pill-btn {
    min-height: 32px !important;
    padding: 6px 8px !important;
    font-size: clamp(.62rem, 2.2vh, .82rem) !important;
  }

  .home-screen .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    height: 74px !important;
    min-height: 74px !important;
    max-height: 74px !important;
    padding: 8px !important;
  }
}

/* ===== VIEWPORT CLASS CONTRACT =====
   Esta e a camada que deve mandar: small/medium/large sao calculados em JS
   com largura+altura reais, evitando que tablet e telefone caiam no mesmo
   comportamento por acidente. */
.layout-landscape.layout-medium .home-screen .google-home,
.layout-landscape.layout-large .home-screen .google-home {
  width: 100vw !important;
  max-width: 100vw !important;
  height: 100dvh !important;
  min-height: 100dvh !important;
  margin: 0 !important;
  border-radius: 0 !important;
  overflow: hidden !important;
  grid-template-rows: auto minmax(0, 1fr) auto !important;
  background: #fff !important;
}

.layout-landscape.layout-medium,
.layout-landscape.layout-large,
.layout-landscape.layout-medium .app-shell,
.layout-landscape.layout-large .app-shell,
.layout-landscape.layout-medium .home-screen,
.layout-landscape.layout-large .home-screen {
  width: 100vw !important;
  max-width: 100vw !important;
  height: 100dvh !important;
  min-height: 100dvh !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
  background: #fff !important;
}

.layout-landscape.layout-medium .home-screen .google-home-header,
.layout-landscape.layout-large .home-screen .google-home-header {
  padding: max(8px, env(safe-area-inset-top)) clamp(24px, 4vw, 46px) 4px !important;
  min-height: 0 !important;
}

.layout-landscape.layout-medium .home-screen .home-logo-mark,
.layout-landscape.layout-large .home-screen .home-logo-mark {
  width: clamp(34px, 8.5vh, 54px) !important;
  height: clamp(34px, 8.5vh, 54px) !important;
  min-width: clamp(34px, 8.5vh, 54px) !important;
}

.layout-landscape.layout-medium .home-screen .home-brand-title,
.layout-landscape.layout-large .home-screen .home-brand-title {
  font-size: clamp(1.45rem, 6.2vh, 2.35rem) !important;
  line-height: 1 !important;
  letter-spacing: 0 !important;
}

.layout-landscape.layout-medium .home-screen .home-brand-subtitle,
.layout-landscape.layout-large .home-screen .home-brand-subtitle {
  font-size: clamp(.76rem, 2.5vh, 1rem) !important;
  line-height: 1.1 !important;
}

.layout-landscape.layout-medium .home-screen .home-help-btn,
.layout-landscape.layout-large .home-screen .home-help-btn,
.layout-landscape.layout-medium .home-screen .reader-header-btn,
.layout-landscape.layout-large .home-screen .reader-header-btn {
  width: clamp(32px, 7.4vh, 44px) !important;
  height: clamp(32px, 7.4vh, 44px) !important;
  min-width: clamp(32px, 7.4vh, 44px) !important;
}

.layout-landscape.layout-medium .home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-main,
.layout-landscape.layout-large .home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-main {
  display: grid !important;
  grid-template-columns: minmax(210px, .36fr) minmax(330px, .64fr) !important;
  grid-template-rows: minmax(0, 1fr) !important;
  gap: clamp(24px, 5vw, 64px) !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 4px clamp(28px, 5vw, 60px) max(8px, env(safe-area-inset-bottom)) !important;
  overflow: hidden !important;
  height: 100% !important;
}

.layout-landscape.layout-large .home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-main {
  grid-template-columns: minmax(300px, .34fr) minmax(560px, .66fr) !important;
}

.layout-landscape.layout-medium .home-screen .home-intro-panel,
.layout-landscape.layout-large .home-screen .home-intro-panel {
  grid-column: 1 !important;
  grid-row: 1 !important;
  align-self: center !important;
  justify-self: stretch !important;
  padding: 0 !important;
  transform: none !important;
}

.layout-landscape.layout-medium .home-screen .home-badge,
.layout-landscape.layout-large .home-screen .home-badge {
  position: static !important;
  justify-self: start !important;
  margin: 0 0 10px !important;
  padding: 5px 11px !important;
}

.layout-landscape.layout-medium .home-screen .home-intro-panel h1,
.layout-landscape.layout-large .home-screen .home-intro-panel h1 {
  max-width: 14ch !important;
  font-size: clamp(1.55rem, 7.2vh, 2.5rem) !important;
  line-height: 1.06 !important;
  letter-spacing: 0 !important;
  margin: 0 !important;
  transform: none !important;
}

.layout-landscape.layout-medium .home-screen .home-intro-panel p,
.layout-landscape.layout-large .home-screen .home-intro-panel p {
  max-width: 31ch !important;
  font-size: clamp(.78rem, 2.8vh, 1.05rem) !important;
  line-height: 1.24 !important;
  margin: 10px 0 0 !important;
  transform: none !important;
}

.layout-landscape.layout-medium .home-screen .home-actions-panel,
.layout-landscape.layout-large .home-screen .home-actions-panel {
  grid-column: 2 !important;
  grid-row: 1 !important;
  width: 100% !important;
  max-width: 720px !important;
  justify-self: stretch !important;
  align-self: center !important;
  display: grid !important;
  grid-template-rows: repeat(2, minmax(72px, auto)) !important;
  gap: clamp(8px, 2vh, 14px) !important;
  padding: 0 !important;
  transform: none !important;
}

.layout-landscape.layout-medium .home-screen .home-option-card,
.layout-landscape.layout-large .home-screen .home-option-card {
  width: 100% !important;
  min-height: clamp(72px, 18vh, 110px) !important;
  grid-template-columns: clamp(44px, 10vh, 70px) minmax(0, 1fr) clamp(28px, 6vh, 38px) !important;
  gap: clamp(12px, 2.2vw, 18px) !important;
  padding: clamp(12px, 2.6vh, 18px) !important;
}

.layout-landscape.layout-medium .home-screen .home-option-icon,
.layout-landscape.layout-large .home-screen .home-option-icon {
  width: clamp(44px, 10vh, 70px) !important;
  height: clamp(44px, 10vh, 70px) !important;
  min-width: clamp(44px, 10vh, 70px) !important;
}

.layout-landscape.layout-medium .home-screen .home-option-text strong,
.layout-landscape.layout-large .home-screen .home-option-text strong {
  font-size: clamp(1rem, 4vh, 1.45rem) !important;
  line-height: 1.08 !important;
}

.layout-landscape.layout-medium .home-screen .home-option-text span,
.layout-landscape.layout-large .home-screen .home-option-text span {
  font-size: clamp(.76rem, 2.9vh, 1.02rem) !important;
  line-height: 1.2 !important;
}

.layout-landscape.layout-medium .home-screen .home-recent-card,
.layout-landscape.layout-large .home-screen .home-recent-card,
.layout-landscape.layout-medium .home-screen .home-safety-card,
.layout-landscape.layout-large .home-screen .home-safety-card {
  display: none !important;
}

.layout-landscape.layout-medium .home-screen .review-home .google-home-main,
.layout-landscape.layout-large .home-screen .review-home .google-home-main,
.layout-landscape.layout-medium .home-screen .processing-home .google-home-main,
.layout-landscape.layout-large .home-screen .processing-home .google-home-main {
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  grid-template-rows: minmax(0, 1fr) !important;
  place-items: stretch center !important;
  padding: 10px clamp(28px, 5vw, 56px) max(12px, env(safe-area-inset-bottom)) !important;
}

.layout-landscape.layout-medium .home-screen .review-home .speech-intro-panel,
.layout-landscape.layout-large .home-screen .review-home .speech-intro-panel,
.layout-landscape.layout-medium .home-screen .review-home .review-intro-panel,
.layout-landscape.layout-large .home-screen .review-home .review-intro-panel {
  display: none !important;
}

.layout-landscape.layout-medium .home-screen .review-home .review-panel,
.layout-landscape.layout-large .home-screen .review-home .review-panel {
  width: min(100%, 1080px) !important;
  max-width: 1080px !important;
  height: 100% !important;
  display: grid !important;
  grid-template-rows: minmax(0, 1fr) clamp(42px, 9vh, 56px) !important;
  gap: 10px !important;
  justify-self: center !important;
  align-self: stretch !important;
  overflow: hidden !important;
}

.layout-landscape.layout-medium .home-screen .review-home .review-preview-card,
.layout-landscape.layout-large .home-screen .review-home .review-preview-card {
  width: 100% !important;
  height: 100% !important;
  min-height: 0 !important;
  padding: 10px !important;
  display: grid !important;
  place-items: center !important;
  overflow: hidden !important;
}

.layout-landscape.layout-medium .home-screen .review-home .preview-image,
.layout-landscape.layout-large .home-screen .review-home .preview-image {
  width: 100% !important;
  height: 100% !important;
  max-height: 100% !important;
  object-fit: contain !important;
}

.layout-landscape.layout-medium .home-screen .processing-home .processing-panel,
.layout-landscape.layout-large .home-screen .processing-home .processing-panel {
  width: min(100%, 540px) !important;
  max-width: 540px !important;
  justify-self: center !important;
  align-self: center !important;
  transform: none !important;
}

.layout-landscape.layout-medium .home-screen .reader-home .reader-main,
.layout-landscape.layout-large .home-screen .reader-home .reader-main {
  width: 100% !important;
  max-width: 100% !important;
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  grid-template-rows: minmax(0, 1fr) auto !important;
  gap: 8px !important;
  padding: 8px clamp(28px, 5vw, 56px) max(10px, env(safe-area-inset-bottom)) !important;
  justify-items: center !important;
  align-items: stretch !important;
  overflow: hidden !important;
}

.layout-landscape.layout-medium .home-screen .reader-home .reader-top,
.layout-landscape.layout-large .home-screen .reader-home .reader-top,
.layout-landscape.layout-medium .home-screen .reader-home .reader-bottom,
.layout-landscape.layout-large .home-screen .reader-home .reader-bottom {
  width: min(100%, 1080px) !important;
  max-width: 1080px !important;
  justify-self: center !important;
}

.layout-landscape.layout-medium .home-screen .reader-home .reader-bottom,
.layout-landscape.layout-large .home-screen .reader-home .reader-bottom {
  max-height: 190px !important;
  padding: 0 !important;
}

.layout-landscape.layout-medium .home-screen .reader-home .controls-compact,
.layout-landscape.layout-large .home-screen .reader-home .controls-compact {
  grid-template-columns: repeat(10, minmax(0, 1fr)) !important;
  gap: 6px !important;
}

/* ===== TABLET LANDSCAPE TYPOGRAPHY LOCK =====
   Em tablets horizontais a altura continua baixa, mas a largura e grande.
   A tipografia nao pode escalar por vh; fica limitada por rem. */
.layout-landscape.layout-medium .home-screen .home-logo-mark,
.layout-landscape.layout-large .home-screen .home-logo-mark {
  width: clamp(30px, 4vw, 44px) !important;
  height: clamp(30px, 4vw, 44px) !important;
  min-width: clamp(30px, 4vw, 44px) !important;
}

.layout-landscape.layout-medium .home-screen .home-brand-title,
.layout-landscape.layout-large .home-screen .home-brand-title {
  font-size: clamp(1.35rem, 3vw, 1.95rem) !important;
  line-height: 1 !important;
  letter-spacing: 0 !important;
}

.layout-landscape.layout-medium .home-screen .home-brand-subtitle,
.layout-landscape.layout-large .home-screen .home-brand-subtitle {
  font-size: clamp(.72rem, 1.6vw, .92rem) !important;
  line-height: 1.1 !important;
}

.layout-landscape.layout-medium .home-screen .google-home-header,
.layout-landscape.layout-large .home-screen .google-home-header {
  padding: max(10px, env(safe-area-inset-top)) clamp(24px, 4vw, 42px) 4px !important;
}

.layout-landscape.layout-medium .home-screen .home-intro-panel h1,
.layout-landscape.layout-large .home-screen .home-intro-panel h1 {
  font-size: clamp(1.45rem, 3.4vw, 2.15rem) !important;
  line-height: 1.08 !important;
  max-width: 15ch !important;
}

.layout-landscape.layout-medium .home-screen .home-intro-panel p,
.layout-landscape.layout-large .home-screen .home-intro-panel p {
  font-size: clamp(.78rem, 1.65vw, .98rem) !important;
  line-height: 1.25 !important;
  max-width: 32ch !important;
}

.layout-landscape.layout-medium .home-screen .home-option-text strong,
.layout-landscape.layout-large .home-screen .home-option-text strong,
.layout-landscape.layout-medium .home-screen .assisted-option-card .home-option-text strong,
.layout-landscape.layout-large .home-screen .assisted-option-card .home-option-text strong {
  font-size: clamp(1rem, 2.35vw, 1.42rem) !important;
  line-height: 1.1 !important;
}

.layout-landscape.layout-medium .home-screen .home-option-text span,
.layout-landscape.layout-large .home-screen .home-option-text span,
.layout-landscape.layout-medium .home-screen .assisted-option-card .home-option-text span,
.layout-landscape.layout-large .home-screen .assisted-option-card .home-option-text span {
  font-size: clamp(.74rem, 1.75vw, .98rem) !important;
  line-height: 1.22 !important;
}

.layout-landscape.layout-medium .home-screen .home-actions-panel,
.layout-landscape.layout-large .home-screen .home-actions-panel,
.layout-landscape.layout-medium .home-screen .assisted-actions-panel,
.layout-landscape.layout-large .home-screen .assisted-actions-panel {
  max-width: min(64vw, 720px) !important;
}

.layout-landscape.layout-medium .home-screen .home-option-card,
.layout-landscape.layout-large .home-screen .home-option-card,
.layout-landscape.layout-medium .home-screen .assisted-option-card,
.layout-landscape.layout-large .home-screen .assisted-option-card {
  min-height: clamp(76px, 16vh, 104px) !important;
  grid-template-columns: clamp(42px, 5vw, 62px) minmax(0, 1fr) clamp(28px, 3.8vw, 36px) !important;
}

.layout-landscape.layout-medium .home-screen .home-option-icon,
.layout-landscape.layout-large .home-screen .home-option-icon,
.layout-landscape.layout-medium .home-screen .assisted-option-icon,
.layout-landscape.layout-large .home-screen .assisted-option-icon {
  width: clamp(42px, 5vw, 62px) !important;
  height: clamp(42px, 5vw, 62px) !important;
  min-width: clamp(42px, 5vw, 62px) !important;
}

.layout-landscape.layout-medium .home-screen .reader-home .reader-main,
.layout-landscape.layout-large .home-screen .reader-home .reader-main {
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) !important;
  grid-template-rows: minmax(0, 1fr) auto !important;
  justify-items: center !important;
  padding-inline: clamp(28px, 5vw, 56px) !important;
}

.layout-landscape.layout-medium .home-screen .reader-home .reader-top,
.layout-landscape.layout-large .home-screen .reader-home .reader-top,
.layout-landscape.layout-medium .home-screen .reader-home .reader-bottom,
.layout-landscape.layout-large .home-screen .reader-home .reader-bottom,
.layout-landscape.layout-medium .home-screen .reader-home .reader-control-stack,
.layout-landscape.layout-large .home-screen .reader-home .reader-control-stack {
  width: min(100%, 1080px) !important;
  max-width: 1080px !important;
  justify-self: center !important;
}

.layout-landscape.layout-medium .home-screen .reader-home .line-focus,
.layout-landscape.layout-large .home-screen .reader-home .line-focus,
.layout-landscape.layout-medium .home-screen .reader-home .reading-focus,
.layout-landscape.layout-large .home-screen .reader-home .reading-focus {
  width: 100% !important;
  max-width: 100% !important;
  font-size: clamp(.9rem, 1.7vw, 1.14rem) !important;
  line-height: 1.22 !important;
  text-align: left !important;
}

@media (orientation: landscape) and (min-width: 900px) {
  .home-screen .google-home {
    width: 100vw !important;
    max-width: 100vw !important;
    height: 100dvh !important;
    min-height: 100dvh !important;
    grid-template-rows: auto minmax(0, 1fr) auto !important;
    overflow: hidden !important;
    background: #fff !important;
  }

  .home-screen .google-home-header {
    padding: max(10px, env(safe-area-inset-top)) clamp(26px, 4vw, 48px) 4px !important;
    min-height: 0 !important;
  }

  .home-screen .home-logo-mark {
    width: clamp(30px, 4vw, 44px) !important;
    height: clamp(30px, 4vw, 44px) !important;
    min-width: clamp(30px, 4vw, 44px) !important;
  }

  .home-screen .home-brand-title {
    font-size: clamp(1.35rem, 3vw, 1.95rem) !important;
    line-height: 1 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-brand-subtitle {
    font-size: clamp(.72rem, 1.6vw, .92rem) !important;
    line-height: 1.1 !important;
  }

  .home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(250px, .36fr) minmax(480px, .64fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    gap: clamp(26px, 5vw, 64px) !important;
    align-items: center !important;
    justify-content: center !important;
    padding: 4px clamp(30px, 5vw, 60px) max(8px, env(safe-area-inset-bottom)) !important;
    height: 100% !important;
    overflow: hidden !important;
  }

  .home-screen .home-intro-panel {
    display: block !important;
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    transform: none !important;
    padding: 0 !important;
  }

  .home-screen .home-badge {
    position: static !important;
    justify-self: start !important;
    margin: 0 0 10px !important;
    padding: 5px 11px !important;
    font-size: clamp(.72rem, 1.5vw, .9rem) !important;
  }

  .home-screen .home-intro-panel h1 {
    max-width: 15ch !important;
    font-size: clamp(1.45rem, 3.4vw, 2.15rem) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
    margin: 0 !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel p {
    max-width: 32ch !important;
    font-size: clamp(.78rem, 1.65vw, .98rem) !important;
    line-height: 1.25 !important;
    margin: 10px 0 0 !important;
    transform: none !important;
  }

  .home-screen .home-actions-panel {
    grid-column: 2 !important;
    grid-row: 1 !important;
    width: 100% !important;
    max-width: 720px !important;
    justify-self: stretch !important;
    align-self: center !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(76px, auto)) !important;
    gap: clamp(8px, 2vh, 14px) !important;
    padding: 0 !important;
    transform: none !important;
  }

  .home-screen .home-option-card {
    width: 100% !important;
    min-height: clamp(76px, 16vh, 104px) !important;
    grid-template-columns: clamp(42px, 5vw, 62px) minmax(0, 1fr) clamp(28px, 3.8vw, 36px) !important;
    gap: clamp(12px, 2.2vw, 18px) !important;
    padding: clamp(10px, 2vh, 16px) !important;
  }

  .home-screen .home-option-icon {
    width: clamp(42px, 5vw, 62px) !important;
    height: clamp(42px, 5vw, 62px) !important;
    min-width: clamp(42px, 5vw, 62px) !important;
  }

  .home-screen .home-option-text strong {
    font-size: clamp(1rem, 2.35vw, 1.42rem) !important;
    line-height: 1.1 !important;
  }

  .home-screen .home-option-text span {
    font-size: clamp(.74rem, 1.75vw, .98rem) !important;
    line-height: 1.22 !important;
  }

  .home-screen .home-recent-card,
  .home-screen .home-safety-card {
    display: none !important;
  }

  .home-screen .review-home .google-home-main,
  .home-screen .processing-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    place-items: stretch center !important;
    padding: 10px clamp(28px, 5vw, 56px) max(12px, env(safe-area-inset-bottom)) !important;
  }

  .home-screen .review-home .speech-intro-panel,
  .home-screen .review-home .review-intro-panel {
    display: none !important;
  }

  .home-screen .review-home .review-panel {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    height: 100% !important;
    grid-template-rows: minmax(0, 1fr) clamp(42px, 9vh, 56px) !important;
    gap: 10px !important;
    justify-self: center !important;
    align-self: stretch !important;
  }
}

/* ===== HARD RESET TABLET LANDSCAPE =====
   Cobre tablets cujo viewport util fica abaixo de 900px. Esta regra
   neutraliza os transforms/positions antigos que empurravam o conteudo
   para fora da esquerda. */
@media (orientation: landscape) and (min-width: 700px) {
  .home-screen .google-home {
    position: relative !important;
    width: 100vw !important;
    max-width: 100vw !important;
    height: 100dvh !important;
    min-height: 100dvh !important;
    margin: 0 !important;
    border: 0 !important;
    border-radius: 0 !important;
    display: grid !important;
    grid-template-rows: auto minmax(0, 1fr) auto !important;
    overflow: hidden !important;
    background: #fff !important;
  }

  .home-screen .google-home-header {
    position: relative !important;
    width: 100% !important;
    min-height: 0 !important;
    display: flex !important;
    align-items: flex-start !important;
    justify-content: space-between !important;
    padding: max(8px, env(safe-area-inset-top)) clamp(22px, 4vw, 42px) 4px !important;
    transform: none !important;
  }

  .home-screen .home-brand {
    display: flex !important;
    align-items: center !important;
    gap: 10px !important;
    position: static !important;
    transform: none !important;
    min-width: 0 !important;
  }

  .home-screen .header-action-row {
    position: static !important;
    transform: none !important;
    margin-left: auto !important;
  }

  .home-screen .home-logo-mark {
    width: clamp(30px, 4vw, 42px) !important;
    height: clamp(30px, 4vw, 42px) !important;
    min-width: clamp(30px, 4vw, 42px) !important;
  }

  .home-screen .home-brand-title {
    font-size: clamp(1.28rem, 2.8vw, 1.86rem) !important;
    line-height: 1 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-brand-subtitle {
    font-size: clamp(.7rem, 1.45vw, .88rem) !important;
    line-height: 1.12 !important;
  }

  .home-screen .home-help-btn,
  .home-screen .reader-header-btn {
    position: static !important;
    width: clamp(30px, 4vw, 40px) !important;
    height: clamp(30px, 4vw, 40px) !important;
    min-width: clamp(30px, 4vw, 40px) !important;
    transform: none !important;
  }

  .home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-main {
    position: relative !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-columns: minmax(210px, .38fr) minmax(340px, .62fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    column-gap: clamp(18px, 4vw, 46px) !important;
    align-items: center !important;
    justify-content: center !important;
    padding: 4px clamp(22px, 4vw, 46px) max(8px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel,
  .home-screen .assisted-intro-panel {
    display: grid !important;
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    gap: 8px !important;
    width: 100% !important;
    max-width: 100% !important;
    padding: 0 !important;
    margin: 0 !important;
    position: static !important;
    transform: none !important;
  }

  .home-screen .home-badge {
    position: static !important;
    grid-column: auto !important;
    grid-row: auto !important;
    justify-self: start !important;
    margin: 0 0 4px !important;
    padding: 5px 10px !important;
    font-size: clamp(.66rem, 1.35vw, .84rem) !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel h1,
  .home-screen .assisted-intro-panel h1 {
    max-width: 14ch !important;
    font-size: clamp(1.35rem, 3vw, 2rem) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
    margin: 0 !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel p,
  .home-screen .assisted-intro-panel p {
    max-width: 31ch !important;
    font-size: clamp(.74rem, 1.55vw, .94rem) !important;
    line-height: 1.25 !important;
    margin: 0 !important;
    transform: none !important;
  }

  .home-screen .home-actions-panel,
  .home-screen .assisted-actions-panel {
    grid-column: 2 !important;
    grid-row: 1 !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(72px, auto)) !important;
    gap: clamp(8px, 2vh, 14px) !important;
    width: 100% !important;
    max-width: min(100%, 620px) !important;
    justify-self: stretch !important;
    align-self: center !important;
    padding: 0 !important;
    margin: 0 !important;
    position: static !important;
    transform: none !important;
  }

  .home-screen .home-option-card,
  .home-screen .assisted-option-card {
    width: 100% !important;
    min-height: clamp(72px, 16vh, 98px) !important;
    display: grid !important;
    grid-template-columns: clamp(40px, 5vw, 58px) minmax(0, 1fr) clamp(26px, 3.5vw, 34px) !important;
    gap: clamp(10px, 2vw, 16px) !important;
    padding: clamp(10px, 2vh, 14px) !important;
    align-items: center !important;
    position: static !important;
    transform: none !important;
  }

  .home-screen .home-option-icon,
  .home-screen .assisted-option-icon {
    width: clamp(40px, 5vw, 58px) !important;
    height: clamp(40px, 5vw, 58px) !important;
    min-width: clamp(40px, 5vw, 58px) !important;
  }

  .home-screen .home-option-text strong,
  .home-screen .assisted-option-card .home-option-text strong {
    font-size: clamp(.95rem, 2.1vw, 1.28rem) !important;
    line-height: 1.1 !important;
  }

  .home-screen .home-option-text span,
  .home-screen .assisted-option-card .home-option-text span {
    font-size: clamp(.7rem, 1.55vw, .9rem) !important;
    line-height: 1.22 !important;
  }

  .home-screen .home-recent-card,
  .home-screen .home-safety-card {
    display: none !important;
  }
}

/* ===== UNIVERSAL LANDSCAPE BASELINE =====
   Regra final: em horizontal todos os ecras usam uma grelha previsivel.
   Neutraliza as camadas antigas que usavam absolute/fixed/translate. */
@media (orientation: landscape) {
  .player-app,
  .app-shell,
  .screen-center.home-screen {
    width: 100vw !important;
    max-width: 100vw !important;
    height: 100dvh !important;
    min-height: 100dvh !important;
    margin: 0 !important;
    padding: 0 !important;
    overflow: hidden !important;
    background: #fff !important;
  }

  .home-screen .google-home {
    position: relative !important;
    width: 100vw !important;
    max-width: 100vw !important;
    height: 100dvh !important;
    min-height: 100dvh !important;
    margin: 0 !important;
    border: 0 !important;
    border-radius: 0 !important;
    display: grid !important;
    grid-template-rows: auto minmax(0, 1fr) auto !important;
    overflow: hidden !important;
    background: #fff !important;
    box-shadow: none !important;
  }

  .home-screen .google-home-header {
    position: static !important;
    width: 100% !important;
    min-height: 0 !important;
    display: flex !important;
    align-items: flex-start !important;
    justify-content: space-between !important;
    gap: 12px !important;
    padding: max(8px, env(safe-area-inset-top)) clamp(16px, 4vw, 38px) 4px !important;
    transform: none !important;
  }

  .home-screen .home-brand,
  .home-screen .header-action-row,
  .home-screen .reader-header-actions {
    position: static !important;
    display: flex !important;
    align-items: center !important;
    transform: none !important;
    inset: auto !important;
  }

  .home-screen .home-brand {
    justify-self: start !important;
    min-width: 0 !important;
    gap: 9px !important;
  }

  .home-screen .header-action-row,
  .home-screen .reader-header-actions {
    justify-content: flex-end !important;
    margin-left: auto !important;
  }

  .home-screen .home-logo-mark {
    width: clamp(26px, 4.2vw, 42px) !important;
    height: clamp(26px, 4.2vw, 42px) !important;
    min-width: clamp(26px, 4.2vw, 42px) !important;
    transform: none !important;
  }

  .home-screen .home-brand-title {
    font-size: clamp(1.15rem, 3vw, 1.85rem) !important;
    line-height: 1 !important;
    letter-spacing: 0 !important;
    transform: none !important;
  }

  .home-screen .home-brand-subtitle {
    font-size: clamp(.62rem, 1.6vw, .88rem) !important;
    line-height: 1.1 !important;
    transform: none !important;
  }

  .home-screen .home-help-btn,
  .home-screen .reader-header-btn {
    position: static !important;
    width: clamp(26px, 4vw, 38px) !important;
    height: clamp(26px, 4vw, 38px) !important;
    min-width: clamp(26px, 4vw, 38px) !important;
    transform: none !important;
  }

  .home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-main {
    position: static !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-columns: minmax(0, .38fr) minmax(0, .62fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    column-gap: clamp(12px, 4vw, 44px) !important;
    align-items: center !important;
    justify-content: center !important;
    padding: 4px clamp(12px, 4vw, 42px) max(8px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel,
  .home-screen .assisted-intro-panel,
  .home-screen .speech-intro-panel {
    position: static !important;
    display: grid !important;
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    gap: clamp(5px, 1.6vh, 10px) !important;
    padding: 0 !important;
    margin: 0 !important;
    transform: none !important;
    pointer-events: auto !important;
  }

  .home-screen .home-badge {
    position: static !important;
    grid-column: auto !important;
    grid-row: auto !important;
    justify-self: start !important;
    margin: 0 0 2px !important;
    padding: 4px 9px !important;
    font-size: clamp(.58rem, 1.6vw, .82rem) !important;
    line-height: 1 !important;
    transform: none !important;
  }

  .home-screen .home-intro-panel h1,
  .home-screen .assisted-intro-panel h1,
  .home-screen .speech-intro-panel h1 {
    max-width: 14ch !important;
    font-size: clamp(1.08rem, 3.8vw, 2rem) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
    margin: 0 !important;
    transform: none !important;
    white-space: normal !important;
  }

  .home-screen .home-intro-panel p,
  .home-screen .assisted-intro-panel p,
  .home-screen .speech-intro-panel p {
    max-width: 31ch !important;
    font-size: clamp(.62rem, 1.8vw, .92rem) !important;
    line-height: 1.24 !important;
    margin: 0 !important;
    transform: none !important;
    white-space: normal !important;
  }

  .home-screen .home-actions-panel,
  .home-screen .assisted-actions-panel {
    position: static !important;
    grid-column: 2 !important;
    grid-row: 1 !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(56px, auto)) !important;
    gap: clamp(6px, 2vh, 12px) !important;
    width: 100% !important;
    max-width: min(100%, 680px) !important;
    justify-self: stretch !important;
    align-self: center !important;
    padding: 0 !important;
    margin: 0 !important;
    transform: none !important;
  }

  .home-screen .home-option-card,
  .home-screen .assisted-option-card {
    position: static !important;
    width: 100% !important;
    min-width: 0 !important;
    min-height: clamp(56px, 18vh, 102px) !important;
    max-height: none !important;
    display: grid !important;
    grid-template-columns: clamp(34px, 6vw, 58px) minmax(0, 1fr) clamp(24px, 4vw, 34px) !important;
    gap: clamp(8px, 2vw, 14px) !important;
    padding: clamp(8px, 2vh, 14px) !important;
    align-items: center !important;
    transform: none !important;
  }

  .home-screen .home-option-icon,
  .home-screen .assisted-option-icon {
    width: clamp(34px, 6vw, 58px) !important;
    height: clamp(34px, 6vw, 58px) !important;
    min-width: clamp(34px, 6vw, 58px) !important;
  }

  .home-screen .home-option-arrow {
    width: clamp(24px, 4vw, 34px) !important;
    height: clamp(24px, 4vw, 34px) !important;
    min-width: clamp(24px, 4vw, 34px) !important;
  }

  .home-screen .home-option-text strong,
  .home-screen .assisted-option-card .home-option-text strong {
    font-size: clamp(.82rem, 2.5vw, 1.28rem) !important;
    line-height: 1.1 !important;
    letter-spacing: 0 !important;
  }

  .home-screen .home-option-text span,
  .home-screen .assisted-option-card .home-option-text span {
    font-size: clamp(.58rem, 1.75vw, .9rem) !important;
    line-height: 1.2 !important;
  }

  .home-screen .home-recent-card,
  .home-screen .home-safety-card {
    display: none !important;
  }

  .home-screen .review-home .google-home-main,
  .home-screen .processing-home .google-home-main {
    position: static !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    place-items: stretch center !important;
    padding: 6px clamp(12px, 4vw, 42px) max(8px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
  }

  .home-screen .review-home .speech-intro-panel,
  .home-screen .review-home .review-intro-panel {
    display: none !important;
  }

  .home-screen .review-home .review-panel {
    position: static !important;
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    height: 100% !important;
    min-height: 0 !important;
    display: grid !important;
    grid-template-rows: minmax(0, 1fr) clamp(34px, 9vh, 54px) !important;
    gap: 8px !important;
    justify-self: center !important;
    align-self: stretch !important;
    overflow: hidden !important;
    transform: none !important;
  }

  .home-screen .review-home .review-preview-card {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 8px !important;
    display: grid !important;
    place-items: center !important;
    overflow: hidden !important;
    transform: none !important;
  }

  .home-screen .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    max-height: 100% !important;
    object-fit: contain !important;
  }

  .home-screen .review-home .image-confirm-controls {
    min-height: clamp(34px, 9vh, 54px) !important;
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    padding: 0 !important;
    transform: none !important;
  }
}

/* ===== LANDSCAPE HARD RESET =====
   Esta regra fica no fim de App.vue de proposito: neutraliza o CSS antigo
   que ainda centrava/recortava o #app e puxava os paineis para fora do ecra. */
@media (orientation: landscape) {
  html,
  body,
  #app {
    width: 100vw !important;
    max-width: 100vw !important;
    min-width: 0 !important;
    height: 100dvh !important;
    min-height: 100dvh !important;
    margin: 0 !important;
    padding: 0 !important;
    border: 0 !important;
    overflow: hidden !important;
    text-align: initial !important;
  }

  #app {
    display: block !important;
  }

  body #app .player-app,
  body #app .app-shell,
  body #app .screen-center.home-screen,
  body #app .screen-center.home-screen > .google-home {
    position: relative !important;
    inset: auto !important;
    left: auto !important;
    right: auto !important;
    top: auto !important;
    bottom: auto !important;
    width: 100vw !important;
    max-width: 100vw !important;
    min-width: 0 !important;
    height: 100dvh !important;
    min-height: 100dvh !important;
    max-height: 100dvh !important;
    margin: 0 !important;
    padding: 0 !important;
    border: 0 !important;
    border-radius: 0 !important;
    box-shadow: none !important;
    overflow: hidden !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .player-app {
    position: fixed !important;
    inset: 0 !important;
    left: 0 !important;
    top: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
  }

  body #app .screen-center.home-screen {
    display: block !important;
    place-items: initial !important;
  }

  body #app .screen-center.home-screen > .google-home {
    display: grid !important;
    grid-template-rows: auto minmax(0, 1fr) auto !important;
    background: #fff !important;
  }

  body #app .screen-center.home-screen .google-home-header {
    position: relative !important;
    inset: auto !important;
    width: 100% !important;
    max-width: 100% !important;
    min-height: 0 !important;
    display: flex !important;
    align-items: flex-start !important;
    justify-content: space-between !important;
    gap: clamp(8px, 2vw, 14px) !important;
    padding: max(8px, env(safe-area-inset-top)) clamp(18px, 4vw, 42px) 4px !important;
    overflow: visible !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .home-brand {
    position: relative !important;
    inset: auto !important;
    display: flex !important;
    align-items: center !important;
    gap: clamp(8px, 1.8vw, 12px) !important;
    min-width: 0 !important;
    opacity: 1 !important;
    visibility: visible !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .home-logo-mark {
    position: relative !important;
    flex: 0 0 auto !important;
    width: clamp(28px, 5.4vh, 42px) !important;
    min-width: clamp(28px, 5.4vh, 42px) !important;
    height: clamp(28px, 5.4vh, 42px) !important;
    opacity: 1 !important;
    visibility: visible !important;
  }

  body #app .screen-center.home-screen .home-brand-title {
    font-size: clamp(1.25rem, 4.7vh, 1.9rem) !important;
    line-height: 1 !important;
    letter-spacing: 0 !important;
    font-weight: 800 !important;
    opacity: 1 !important;
    visibility: visible !important;
  }

  body #app .screen-center.home-screen .home-brand-subtitle {
    margin-top: 2px !important;
    font-size: clamp(.66rem, 2.2vh, .9rem) !important;
    line-height: 1.1 !important;
    opacity: 1 !important;
    visibility: visible !important;
  }

  body #app .screen-center.home-screen .header-action-row,
  body #app .screen-center.home-screen .reader-header-actions {
    position: relative !important;
    inset: auto !important;
    display: flex !important;
    align-items: center !important;
    justify-content: flex-end !important;
    margin-left: auto !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .home-help-btn,
  body #app .screen-center.home-screen .reader-header-btn {
    position: relative !important;
    inset: auto !important;
    width: clamp(30px, 5.2vh, 38px) !important;
    min-width: clamp(30px, 5.2vh, 38px) !important;
    height: clamp(30px, 5.2vh, 38px) !important;
    padding: 6px !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-main {
    position: relative !important;
    inset: auto !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    height: 100% !important;
    min-height: 0 !important;
    max-height: 100% !important;
    display: grid !important;
    grid-template-columns: minmax(0, .38fr) minmax(0, .62fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    column-gap: clamp(16px, 4vw, 54px) !important;
    align-items: center !important;
    justify-content: center !important;
    padding: 4px clamp(18px, 4vw, 46px) max(8px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .home-intro-panel {
    position: relative !important;
    inset: auto !important;
    grid-column: 1 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    display: grid !important;
    gap: clamp(5px, 1.5vh, 10px) !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    margin: 0 !important;
    padding: 0 !important;
    overflow: visible !important;
    opacity: 1 !important;
    visibility: visible !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .home-intro-panel h1 {
    max-width: 14ch !important;
    margin: 0 !important;
    font-size: clamp(1.15rem, 5.5vh, 2.15rem) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
    white-space: normal !important;
    transform: none !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .home-intro-panel p {
    max-width: 32ch !important;
    margin: 0 !important;
    font-size: clamp(.64rem, 2.45vh, .96rem) !important;
    line-height: 1.26 !important;
    white-space: normal !important;
    transform: none !important;
  }

  body #app .screen-center.home-screen .home-badge {
    position: relative !important;
    inset: auto !important;
    justify-self: start !important;
    max-width: 100% !important;
    margin: 0 0 2px !important;
    padding: 4px 10px !important;
    font-size: clamp(.6rem, 2.2vh, .82rem) !important;
    line-height: 1 !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .home-actions-panel,
  body #app .screen-center.home-screen .assisted-actions-panel {
    position: relative !important;
    inset: auto !important;
    grid-column: 2 !important;
    grid-row: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(58px, auto)) !important;
    gap: clamp(7px, 2vh, 13px) !important;
    width: 100% !important;
    max-width: min(100%, 720px) !important;
    min-width: 0 !important;
    height: auto !important;
    margin: 0 !important;
    padding: 0 !important;
    overflow: visible !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .home-option-card,
  body #app .screen-center.home-screen .assisted-option-card {
    position: relative !important;
    inset: auto !important;
    width: 100% !important;
    max-width: 100% !important;
    min-width: 0 !important;
    min-height: clamp(58px, 18vh, 104px) !important;
    display: grid !important;
    grid-template-columns: clamp(34px, 7vh, 58px) minmax(0, 1fr) clamp(24px, 5vh, 34px) !important;
    align-items: center !important;
    gap: clamp(8px, 2vw, 14px) !important;
    padding: clamp(8px, 2vh, 14px) !important;
    overflow: hidden !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .home-option-icon,
  body #app .screen-center.home-screen .assisted-option-icon {
    width: clamp(34px, 7vh, 58px) !important;
    min-width: clamp(34px, 7vh, 58px) !important;
    height: clamp(34px, 7vh, 58px) !important;
  }

  body #app .screen-center.home-screen .home-option-text {
    min-width: 0 !important;
    text-align: left !important;
  }

  body #app .screen-center.home-screen .home-option-text strong {
    font-size: clamp(.84rem, 3.3vh, 1.3rem) !important;
    line-height: 1.1 !important;
    letter-spacing: 0 !important;
  }

  body #app .screen-center.home-screen .home-option-text span {
    font-size: clamp(.58rem, 2.25vh, .9rem) !important;
    line-height: 1.22 !important;
  }

  body #app .screen-center.home-screen .home-option-arrow {
    display: grid !important;
    place-items: center !important;
    width: clamp(24px, 5vh, 34px) !important;
    min-width: clamp(24px, 5vh, 34px) !important;
    height: clamp(24px, 5vh, 34px) !important;
  }

  body #app .screen-center.home-screen .home-recent-card,
  body #app .screen-center.home-screen .home-safety-card {
    display: none !important;
  }

  body #app .screen-center.home-screen .review-home .google-home-main,
  body #app .screen-center.home-screen .processing-home .google-home-main {
    position: relative !important;
    inset: auto !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    place-items: stretch center !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 6px clamp(18px, 4vw, 46px) max(8px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .review-home .speech-intro-panel,
  body #app .screen-center.home-screen .review-home .review-intro-panel {
    display: none !important;
  }

  body #app .screen-center.home-screen .review-home .review-panel {
    position: relative !important;
    inset: auto !important;
    display: grid !important;
    grid-template-rows: minmax(0, 1fr) clamp(36px, 9vh, 54px) !important;
    gap: 8px !important;
    align-self: stretch !important;
    justify-self: center !important;
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    height: 100% !important;
    min-height: 0 !important;
    margin: 0 !important;
    padding: 0 !important;
    overflow: hidden !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .screen-center.home-screen .review-home .review-preview-card {
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 8px !important;
    display: grid !important;
    place-items: center !important;
    overflow: hidden !important;
    transform: none !important;
  }

  body #app .screen-center.home-screen .review-home .preview-image {
    width: 100% !important;
    height: 100% !important;
    max-height: 100% !important;
    object-fit: contain !important;
  }

  body #app .screen-center.home-screen .review-home .image-confirm-controls {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    min-height: clamp(36px, 9vh, 54px) !important;
    padding: 0 !important;
    transform: none !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-header {
    position: fixed !important;
    z-index: 20 !important;
    inset: 0 0 auto 0 !important;
    width: 100vw !important;
    max-width: 100vw !important;
    padding: max(8px, env(safe-area-inset-top)) clamp(18px, 4vw, 42px) 4px !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .home-brand {
    position: fixed !important;
    z-index: 21 !important;
    left: clamp(18px, 4vw, 42px) !important;
    top: max(8px, env(safe-area-inset-top)) !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .header-action-row {
    position: fixed !important;
    z-index: 21 !important;
    right: clamp(18px, 4vw, 42px) !important;
    top: max(8px, env(safe-area-inset-top)) !important;
    margin-left: 0 !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .google-home-main {
    display: block !important;
    padding: 0 !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .home-intro-panel,
  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .assisted-intro-panel {
    position: fixed !important;
    z-index: 10 !important;
    left: clamp(22px, 5vw, 58px) !important;
    top: clamp(86px, 30vh, 170px) !important;
    width: min(34vw, 360px) !important;
    max-width: min(34vw, 360px) !important;
    display: grid !important;
    gap: clamp(6px, 1.5vh, 10px) !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .home-actions-panel,
  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .assisted-actions-panel {
    position: fixed !important;
    z-index: 10 !important;
    right: clamp(28px, 5vw, 64px) !important;
    top: clamp(112px, 31vh, 190px) !important;
    width: min(62vw, 720px) !important;
    max-width: min(62vw, 720px) !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(62px, auto)) !important;
    gap: clamp(8px, 2vh, 14px) !important;
  }

  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .home-option-card,
  body #app .screen-center.home-screen .google-home:not(.reader-home):not(.review-home):not(.processing-home) .assisted-option-card {
    min-height: clamp(62px, 18vh, 108px) !important;
  }

  body #app .screen-center.home-screen .review-home .google-home-header {
    position: fixed !important;
    z-index: 20 !important;
    inset: 0 0 auto 0 !important;
    width: 100vw !important;
    padding: max(8px, env(safe-area-inset-top)) clamp(18px, 4vw, 42px) 4px !important;
  }

  body #app .screen-center.home-screen .review-home .home-brand {
    position: fixed !important;
    z-index: 21 !important;
    left: clamp(18px, 4vw, 42px) !important;
    top: max(8px, env(safe-area-inset-top)) !important;
  }

  body #app .screen-center.home-screen .review-home .header-action-row {
    position: fixed !important;
    z-index: 21 !important;
    right: clamp(18px, 4vw, 42px) !important;
    top: max(8px, env(safe-area-inset-top)) !important;
    margin-left: 0 !important;
  }

  body #app .screen-center.home-screen .review-home .review-panel {
    position: fixed !important;
    left: clamp(28px, 4vw, 48px) !important;
    right: clamp(28px, 4vw, 48px) !important;
    top: clamp(74px, 15vh, 120px) !important;
    bottom: max(8px, env(safe-area-inset-bottom)) !important;
    width: auto !important;
    min-width: calc(100vw - clamp(56px, 8vw, 96px)) !important;
    max-width: none !important;
    height: auto !important;
  }

  body #app .screen-center.home-screen .review-home .review-preview-card,
  body #app .screen-center.home-screen .review-home .image-confirm-controls {
    width: 100% !important;
    max-width: none !important;
  }
}

.app-entry-view,
.app-entry-view * {
  box-sizing: border-box;
}

.player-app.is-entry-screen,
.player-app.is-entry-screen .app-shell {
  position: fixed !important;
  inset: 0 !important;
  left: 0 !important;
  top: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  display: block !important;
  width: 100vw !important;
  max-width: none !important;
  min-width: 100vw !important;
  height: 100dvh !important;
  min-height: 100dvh !important;
  max-height: 100dvh !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
  background: #fff !important;
  transform: none !important;
  translate: none !important;
}

.player-app.is-app-screen,
.player-app.is-app-screen .app-shell {
  position: fixed !important;
  inset: 0 !important;
  left: 0 !important;
  top: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  display: block !important;
  width: 100vw !important;
  max-width: none !important;
  min-width: 100vw !important;
  height: 100dvh !important;
  min-height: 100dvh !important;
  max-height: 100dvh !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
  background: #fff !important;
  transform: none !important;
  translate: none !important;
}

.player-app.is-app-screen .screen-center.home-screen,
.player-app.is-app-screen .screen-center.home-screen > .google-home {
  position: absolute !important;
  inset: 0 !important;
  left: 0 !important;
  top: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  display: block !important;
  width: 100vw !important;
  max-width: none !important;
  min-width: 100vw !important;
  height: 100dvh !important;
  min-height: 100dvh !important;
  max-height: 100dvh !important;
  margin: 0 !important;
  padding: 0 !important;
  border: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  overflow: hidden !important;
  background: #fff !important;
  transform: none !important;
  translate: none !important;
}

.player-app.is-app-screen .screen-center.home-screen > .google-home {
  display: grid !important;
  grid-template-rows: auto minmax(0, 1fr) auto !important;
}

.player-app.is-app-screen .google-home-header {
  position: relative !important;
  z-index: 2 !important;
  display: flex !important;
  width: 100% !important;
  min-height: 0 !important;
  padding: max(12px, env(safe-area-inset-top)) max(22px, env(safe-area-inset-right)) 6px max(22px, env(safe-area-inset-left)) !important;
  transform: none !important;
}

.player-app.is-app-screen .home-brand,
.player-app.is-app-screen .header-action-row,
.player-app.is-app-screen .reader-header-actions {
  position: static !important;
  transform: none !important;
  translate: none !important;
}

.player-app.is-app-screen .home-logo-mark {
  width: clamp(28px, 4.6vh, 42px) !important;
  height: clamp(28px, 4.6vh, 42px) !important;
  min-width: clamp(28px, 4.6vh, 42px) !important;
}

.player-app.is-app-screen .home-brand-title {
  font-size: clamp(20px, 3.1vh, 30px) !important;
  line-height: 1 !important;
  letter-spacing: 0 !important;
}

.player-app.is-app-screen .home-brand-subtitle {
  font-size: clamp(10px, 1.8vh, 14px) !important;
  line-height: 1.15 !important;
}

.player-app.is-app-screen .google-home-main {
  position: relative !important;
  min-width: 0 !important;
  min-height: 0 !important;
  width: 100% !important;
  height: 100% !important;
  overflow: hidden !important;
  transform: none !important;
  translate: none !important;
}

@media (orientation: portrait) {
  .player-app.is-app-screen .google-home-main {
    padding: 8px max(18px, env(safe-area-inset-right)) max(10px, env(safe-area-inset-bottom)) max(18px, env(safe-area-inset-left)) !important;
  }
}

@media (orientation: landscape) {
  .player-app.is-app-screen .google-home-header {
    padding: max(8px, env(safe-area-inset-top)) max(clamp(22px, 4vw, 52px), env(safe-area-inset-right)) 4px max(clamp(22px, 4vw, 52px), env(safe-area-inset-left)) !important;
  }

  .player-app.is-app-screen .google-home-main {
    padding: 4px max(clamp(22px, 4vw, 56px), env(safe-area-inset-right)) max(8px, env(safe-area-inset-bottom)) max(clamp(22px, 4vw, 56px), env(safe-area-inset-left)) !important;
  }

  .player-app.is-app-screen .assisted-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(180px, 360px) minmax(340px, 690px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    justify-content: center !important;
    gap: clamp(22px, 7vw, 92px) !important;
  }

  .player-app.is-app-screen .assisted-intro-panel {
    position: static !important;
    grid-column: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
  }

  .player-app.is-app-screen .assisted-actions-panel {
    position: static !important;
    grid-column: 2 !important;
    display: grid !important;
    grid-template-rows: repeat(2, minmax(54px, auto)) !important;
    gap: clamp(8px, 2.2vh, 14px) !important;
    width: 100% !important;
    max-width: none !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
  }

  .player-app.is-app-screen .assisted-intro-panel h1 {
    max-width: 15ch !important;
    margin: 0 !important;
    font-size: clamp(18px, 4vh, 30px) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
  }

  .player-app.is-app-screen .assisted-intro-panel p {
    max-width: 32ch !important;
    margin: 8px 0 0 !important;
    font-size: clamp(10px, 1.9vh, 14px) !important;
    line-height: 1.25 !important;
  }

  .player-app.is-app-screen .assisted-option-card {
    width: 100% !important;
    min-height: clamp(54px, 12vh, 78px) !important;
    display: grid !important;
    grid-template-columns: clamp(42px, 8vh, 62px) minmax(0, 1fr) clamp(28px, 5vh, 34px) !important;
    gap: clamp(10px, 2vw, 16px) !important;
    padding: clamp(8px, 1.8vh, 13px) !important;
    border-radius: 14px !important;
    transform: none !important;
  }

  .player-app.is-app-screen .assisted-option-icon {
    width: clamp(42px, 8vh, 62px) !important;
    height: clamp(42px, 8vh, 62px) !important;
    min-width: clamp(42px, 8vh, 62px) !important;
  }

  .player-app.is-app-screen .home-option-text strong {
    font-size: clamp(13px, 2.35vh, 18px) !important;
    line-height: 1.1 !important;
    letter-spacing: 0 !important;
  }

  .player-app.is-app-screen .home-option-text span {
    font-size: clamp(9px, 1.65vh, 13px) !important;
    line-height: 1.2 !important;
  }

  .player-app.is-app-screen .speech-home .google-home-main {
    display: grid !important;
    grid-template-columns: minmax(180px, 340px) minmax(420px, 760px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    justify-content: center !important;
    gap: clamp(20px, 6vw, 78px) !important;
  }

  .player-app.is-app-screen .speech-intro-panel {
    position: static !important;
    grid-column: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
  }

  .player-app.is-app-screen .speech-intro-panel h1 {
    margin: 0 !important;
    font-size: clamp(17px, 3.7vh, 28px) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
  }

  .player-app.is-app-screen .speech-intro-panel p {
    margin: 8px 0 0 !important;
    font-size: clamp(10px, 1.75vh, 13px) !important;
    line-height: 1.25 !important;
  }

  .player-app.is-app-screen .speech-work-panel {
    grid-column: 2 !important;
    align-self: center !important;
    width: 100% !important;
    max-width: none !important;
    min-height: 0 !important;
    margin: 0 !important;
    transform: none !important;
  }
}

.app-entry-view {
  position: absolute !important;
  inset: 0 !important;
  z-index: 9999 !important;
  width: 100vw !important;
  height: 100dvh !important;
  min-height: 100dvh !important;
  overflow: hidden !important;
  background: #fff !important;
  color: #172033;
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

.app-entry-header {
  position: absolute;
  inset: 0 0 auto;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: max(14px, env(safe-area-inset-top)) max(18px, env(safe-area-inset-right)) 0 max(18px, env(safe-area-inset-left));
}

.app-entry-brand {
  display: flex;
  align-items: center;
  gap: clamp(8px, 1.6vw, 12px);
  min-width: 0;
}

.app-entry-logo {
  position: relative;
  flex: 0 0 auto;
  width: clamp(24px, 4.2vh, 32px);
  height: clamp(24px, 4.2vh, 32px);
  overflow: hidden;
  border-radius: 12px;
}

.app-entry-logo span {
  position: absolute;
  display: block;
}

.app-entry-logo-blue {
  left: 0;
  top: 0;
  width: 100%;
  height: 68%;
  background: #1a73e8;
  border-radius: 14px 16px 7px 7px;
}

.app-entry-logo-red {
  left: 0;
  top: 36%;
  width: 50%;
  height: 28%;
  background: #ea4335;
}

.app-entry-logo-yellow {
  left: 0;
  bottom: 0;
  width: 50%;
  height: 42%;
  background: #fbbc04;
}

.app-entry-logo-green {
  right: 0;
  bottom: 0;
  width: 58%;
  height: 58%;
  background: #34a853;
}

.app-entry-brand-title {
  color: #1f64d9;
  font-size: clamp(18px, 2.1vw, 24px);
  font-weight: 800;
  line-height: 1;
  letter-spacing: 0;
}

.app-entry-brand-title span {
  color: #1a73e8;
}

.app-entry-brand-subtitle {
  margin-top: 4px;
  color: #667085;
  font-size: clamp(10px, 1vw, 12px);
  line-height: 1.15;
}

.app-entry-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-left: auto;
}

.app-entry-home-button {
  display: grid;
  place-items: center;
  width: clamp(34px, 6vh, 42px);
  height: clamp(34px, 6vh, 42px);
  padding: 7px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #172033;
}

.app-entry-home-button svg {
  width: 100%;
  height: 100%;
}

.app-entry-main {
  position: absolute;
  inset: clamp(82px, 16vh, 118px) max(20px, env(safe-area-inset-right)) max(12px, env(safe-area-inset-bottom)) max(20px, env(safe-area-inset-left));
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  grid-template-rows: auto minmax(0, 1fr);
  align-content: start;
  gap: clamp(18px, 3.2vh, 24px);
  overflow: hidden;
}

.app-entry-intro {
  display: grid;
  justify-items: start;
  gap: 8px;
  min-width: 0;
}

.app-entry-badge {
  display: inline-flex;
  max-width: 100%;
  padding: 6px 12px;
  border-radius: 999px;
  background: #edf4ff;
  color: #1a73e8;
  font-size: clamp(10px, 1.1vw, 12px);
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
}

.app-entry-intro h1 {
  max-width: 13ch;
  margin: 0;
  color: #13213d;
  font-size: clamp(22px, 3.6vw, 28px);
  font-weight: 830;
  line-height: 1.08;
  letter-spacing: 0;
}

.app-entry-intro p {
  max-width: 32ch;
  margin: 0;
  color: #667085;
  font-size: clamp(11px, 1.6vw, 14px);
  line-height: 1.3;
}

.app-entry-options {
  display: grid;
  align-content: stretch;
  gap: 12px;
  width: 100%;
  min-width: 0;
}

.app-entry-option {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) 34px;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-width: 0;
  min-height: 96px;
  padding: 12px;
  border: 1px solid #e4eaf3;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, .08);
  color: inherit;
  text-align: left;
}

.app-entry-option-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  overflow: hidden;
  border-radius: 14px;
}

.app-entry-option-icon svg {
  width: 100%;
  height: 100%;
}

.app-entry-option-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.app-entry-option-copy strong {
  color: #172033;
  font-size: clamp(13px, 1.7vw, 17px);
  font-weight: 820;
  line-height: 1.1;
  letter-spacing: 0;
}

.app-entry-option-copy span {
  color: #667085;
  font-size: clamp(10px, 1.2vw, 13px);
  line-height: 1.24;
}

.app-entry-option-arrow {
  display: grid;
  place-items: center;
  justify-self: end;
  width: 32px;
  height: 32px;
  border-radius: 999px;
  font-size: 1.1rem;
  font-weight: 900;
}

.app-entry-option-blue .app-entry-option-arrow {
  color: #1a73e8;
  background: #edf4ff;
}

.app-entry-option-green .app-entry-option-arrow {
  color: #16a34a;
  background: #eaf8ef;
}

@media (orientation: landscape) {
  .app-entry-header {
    padding: max(10px, env(safe-area-inset-top)) max(clamp(22px, 4vw, 48px), env(safe-area-inset-right)) 0 max(clamp(22px, 4vw, 48px), env(safe-area-inset-left));
  }

  .app-entry-brand-title {
    font-size: clamp(18px, 3.1vh, 24px);
  }

  .app-entry-brand-subtitle {
    font-size: clamp(9px, 1.55vh, 12px);
  }

  .app-entry-main {
    inset: clamp(64px, 13vh, 100px) max(clamp(24px, 4vw, 64px), env(safe-area-inset-right)) max(12px, env(safe-area-inset-bottom)) max(clamp(24px, 4vw, 64px), env(safe-area-inset-left));
    grid-template-columns: minmax(190px, 380px) minmax(340px, 680px);
    grid-template-rows: minmax(0, 1fr);
    align-items: center;
    justify-content: center;
    align-content: stretch;
    gap: clamp(22px, 7vw, 96px);
  }

  .app-entry-intro {
    align-content: center;
    gap: clamp(6px, 1.8vh, 12px);
  }

  .app-entry-badge {
    font-size: clamp(9px, 1.55vh, 12px);
  }

  .app-entry-intro h1 {
    max-width: 15ch;
    font-size: clamp(18px, 4vh, 28px);
  }

  .app-entry-intro p {
    font-size: clamp(10px, 1.85vh, 14px);
    line-height: 1.28;
  }

  .app-entry-options {
    align-self: center;
    gap: clamp(8px, 2.5vh, 16px);
  }

  .app-entry-option {
    grid-template-columns: clamp(42px, 10vh, 68px) minmax(0, 1fr) clamp(28px, 6vh, 38px);
    gap: clamp(10px, 2.2vw, 18px);
    min-height: clamp(54px, 12vh, 72px);
    padding: clamp(8px, 1.7vh, 12px);
  }

  .app-entry-option-icon {
    width: clamp(42px, 10vh, 68px);
    height: clamp(42px, 10vh, 68px);
  }

  .app-entry-option-copy strong {
    font-size: clamp(13px, 2.35vh, 18px);
  }

  .app-entry-option-copy span {
    font-size: clamp(9px, 1.65vh, 13px);
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .app-entry-main {
    inset: clamp(48px, 13vh, 64px) clamp(16px, 3vw, 36px) max(8px, env(safe-area-inset-bottom));
    grid-template-columns: minmax(160px, 260px) minmax(320px, 560px);
    gap: clamp(14px, 5vw, 56px);
  }

  .app-entry-intro h1 {
    font-size: clamp(16px, 4vh, 22px);
  }

  .app-entry-intro p {
    font-size: clamp(9px, 1.8vh, 12px);
  }

  .app-entry-option {
    min-height: clamp(58px, 18vh, 78px);
  }
}

@media (orientation: portrait) and (max-height: 680px) {
  .app-entry-header {
    padding: max(12px, env(safe-area-inset-top)) 18px 0;
  }

  .app-entry-main {
    inset: clamp(78px, 15vh, 96px) 18px max(8px, env(safe-area-inset-bottom));
    gap: 14px;
  }

  .app-entry-options {
    gap: 12px;
  }

  .app-entry-option {
    min-height: min(176px, 24vh);
  }
}

/* ===== UI consistency: entrada ===== */
.app-entry-home-button {
  width: 38px !important;
  height: 38px !important;
  min-width: 38px !important;
  min-height: 38px !important;
  padding: 7px !important;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
}

.app-entry-home-button svg {
  width: 24px !important;
  height: 24px !important;
  display: block !important;
  stroke-width: 2.2px !important;
}

@media (orientation: portrait) {
  .app-entry-main {
    inset: clamp(78px, 13vh, 104px) 14px max(10px, env(safe-area-inset-bottom)) !important;
    gap: 16px !important;
  }

  .app-entry-intro h1 {
    max-width: 18ch !important;
    font-size: clamp(20px, 6.4vw, 26px) !important;
    font-weight: 760 !important;
    line-height: 1.08 !important;
  }

  .app-entry-intro p {
    max-width: 36ch !important;
    font-size: clamp(11px, 3.2vw, 13px) !important;
    line-height: 1.28 !important;
  }

  .app-entry-option {
    min-height: min(178px, 25vh) !important;
  }
}

.app-entry-option,
.app-entry-option-copy,
.app-entry-option-copy strong,
.app-entry-option-copy span {
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif !important;
}

.app-entry-option-copy strong {
  font-size: clamp(14px, 3.4vw, 16px) !important;
  font-weight: 720 !important;
  line-height: 1.14 !important;
}

.app-entry-option-copy span {
  font-size: clamp(10px, 2.65vw, 12px) !important;
  font-weight: 500 !important;
  line-height: 1.25 !important;
}

@media (orientation: landscape) {
  body #app .player-app.is-app-screen .screen-center.home-screen .assisted-home .google-home-main {
    position: relative !important;
    display: grid !important;
    grid-template-columns: minmax(180px, 360px) minmax(340px, 690px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    justify-content: center !important;
    gap: clamp(22px, 7vw, 92px) !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 4px max(clamp(22px, 4vw, 56px), env(safe-area-inset-right)) max(8px, env(safe-area-inset-bottom)) max(clamp(22px, 4vw, 56px), env(safe-area-inset-left)) !important;
    overflow: hidden !important;
    transform: none !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .assisted-home .assisted-intro-panel,
  body #app .player-app.is-app-screen .screen-center.home-screen .assisted-home .assisted-actions-panel {
    position: static !important;
    inset: auto !important;
    left: auto !important;
    top: auto !important;
    right: auto !important;
    bottom: auto !important;
    width: 100% !important;
    max-width: none !important;
    min-width: 0 !important;
    height: auto !important;
    margin: 0 !important;
    padding: 0 !important;
    display: grid !important;
    transform: none !important;
    translate: none !important;
    opacity: 1 !important;
    visibility: visible !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .assisted-home .assisted-intro-panel {
    grid-column: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .assisted-home .assisted-actions-panel {
    grid-column: 2 !important;
    align-self: center !important;
    justify-self: stretch !important;
    grid-template-rows: repeat(2, minmax(54px, auto)) !important;
    gap: clamp(8px, 2.2vh, 14px) !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .assisted-home .assisted-intro-panel h1 {
    max-width: 15ch !important;
    margin: 0 !important;
    font-size: clamp(18px, 4vh, 30px) !important;
    line-height: 1.08 !important;
    letter-spacing: 0 !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .assisted-home .assisted-intro-panel p {
    max-width: 32ch !important;
    margin: 8px 0 0 !important;
    font-size: clamp(10px, 1.9vh, 14px) !important;
    line-height: 1.25 !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .assisted-home .assisted-option-card {
    position: static !important;
    width: 100% !important;
    min-width: 0 !important;
    min-height: clamp(54px, 12vh, 78px) !important;
    display: grid !important;
    grid-template-columns: clamp(42px, 8vh, 62px) minmax(0, 1fr) clamp(28px, 5vh, 34px) !important;
    gap: clamp(10px, 2vw, 16px) !important;
    padding: clamp(8px, 1.8vh, 13px) !important;
    align-items: center !important;
    transform: none !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .speech-home .google-home-main {
    position: relative !important;
    display: grid !important;
    grid-template-columns: minmax(180px, 340px) minmax(420px, 760px) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: center !important;
    justify-content: center !important;
    gap: clamp(20px, 6vw, 78px) !important;
    width: 100% !important;
    height: 100% !important;
    min-height: 0 !important;
    padding: 4px max(clamp(22px, 4vw, 56px), env(safe-area-inset-right)) max(8px, env(safe-area-inset-bottom)) max(clamp(22px, 4vw, 56px), env(safe-area-inset-left)) !important;
    overflow: hidden !important;
    transform: none !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .speech-home .speech-intro-panel,
  body #app .player-app.is-app-screen .screen-center.home-screen .speech-home .speech-work-panel {
    position: static !important;
    inset: auto !important;
    width: 100% !important;
    max-width: none !important;
    min-width: 0 !important;
    margin: 0 !important;
    padding: 0 !important;
    transform: none !important;
    translate: none !important;
    opacity: 1 !important;
    visibility: visible !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .speech-home .speech-intro-panel {
    grid-column: 1 !important;
    align-self: center !important;
    justify-self: stretch !important;
  }

  body #app .player-app.is-app-screen .screen-center.home-screen .speech-home .speech-work-panel {
    grid-column: 2 !important;
    align-self: center !important;
    justify-self: stretch !important;
    display: grid !important;
    gap: clamp(6px, 1.4vh, 10px) !important;
  }
}

/* Legibilidade: caixas que mostram frases para leitura não usam bold pesado. */
body #app .speech-phrase-card strong,
body #app .home-screen .speech-home:not(.review-home) .speech-phrase-card strong,
body #app .result-phrase-copy strong,
body #app .speech-summary-copy strong,
body #app .reading-focus,
body #app .line-focus,
body #app .word-context,
body #app .reader-line-focus,
body #app .reader-word-focus,
body #app .reader-word-context,
body #app .reader-word-chip {
  font-weight: 400 !important;
}

/* Sistema comum de botões: cores iguais em todas as janelas. */
body #app {
  --ui-primary: #1a73e8;
  --ui-primary-pressed: #155ec1;
  --ui-primary-soft: #e8f0fe;
  --ui-primary-border: #c6dafc;
  --ui-secondary-bg: #f3f6fb;
  --ui-secondary-border: #e5eaf2;
  --ui-secondary-text: #344054;
  --ui-disabled-bg: #f6f8fb;
  --ui-disabled-text: #a4acb8;
}

body #app .main-action {
  background: var(--ui-primary) !important;
  color: #fff !important;
  border-color: var(--ui-primary) !important;
  box-shadow: none !important;
}

body #app .main-action:active {
  background: var(--ui-primary-pressed) !important;
  border-color: var(--ui-primary-pressed) !important;
}

body #app .soft-action,
body #app .pill-btn,
body #app .result-listen-btn,
body #app .result-feedback-listen-btn,
body #app .listen-btn,
body #app .syllable-toggle {
  background: var(--ui-secondary-bg) !important;
  color: var(--ui-secondary-text) !important;
  border-color: var(--ui-secondary-border) !important;
  box-shadow: none !important;
}

body #app .result-listen-btn,
body #app .result-feedback-listen-btn,
body #app .listen-btn,
body #app .syllable-toggle {
  font-weight: 500 !important;
}

body #app .soft-action.active,
body #app .pill-btn.active,
body #app .syllable-toggle.active,
body #app .controls-compact .pill-btn.active,
body #app .reader-controls-panel .pill-btn.active {
  background: var(--ui-primary-soft) !important;
  color: var(--ui-primary) !important;
  border-color: var(--ui-primary-border) !important;
}

body #app .main-action:disabled,
body #app .soft-action:disabled,
body #app .pill-btn:disabled {
  background: var(--ui-disabled-bg) !important;
  color: var(--ui-disabled-text) !important;
  border-color: var(--ui-disabled-bg) !important;
  opacity: 1 !important;
}

body #app .app-entry-option-arrow,
body #app .flow-option-arrow {
  background: var(--ui-primary-soft) !important;
  color: var(--ui-primary) !important;
}

/* Última barreira contra o legacy: frases de leitura sempre em peso regular. */
body #app .reader-speech-result-panel .result-phrase-copy strong,
body #app .reader-controls-panel .speech-summary-copy strong,
body #app .speech-phrase-card strong,
body #app .reading-focus,
body #app .line-focus,
body #app .word-context,
body #app .reader-line-focus,
body #app .reader-word-focus,
body #app .reader-word-context,
body #app .reader-word-chip {
  font-weight: 400 !important;
  letter-spacing: 0 !important;
}

body #app .reader-speech-result-panel .result-listen-btn,
body #app .reader-speech-result-panel .result-feedback-listen-btn,
body #app .reader-controls-panel .listen-btn {
  background: var(--ui-secondary-bg) !important;
  color: var(--ui-secondary-text) !important;
  border-color: var(--ui-secondary-border) !important;
  box-shadow: none !important;
}

body #app .reader-word-chip.active,
body #app .reader-text-panel .reader-word-chip.active {
  background: var(--active-word-bg) !important;
  color: var(--active-word-text) !important;
  border-radius: 5px !important;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, .18) !important;
  padding: 0 .12em !important;
}

/* O fluxo novo de áudio é uma view própria; estas regras anulam CSS legacy
   global que usa os mesmos nomes internos e deslocava o painel em Android real. */
@media (orientation: landscape) {
  body #app .player-app.is-app-screen .audio-prepare-view .audio-prepare-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: stretch !important;
    justify-content: stretch !important;
    gap: 0 !important;
    padding: 4px clamp(18px, 3.2vw, 30px) max(14px, env(safe-area-inset-bottom)) !important;
  }

  body #app .player-app.is-app-screen .audio-prepare-view .audio-prepare-main > .flow-intro {
    display: none !important;
    grid-column: auto !important;
    grid-row: auto !important;
  }

  body #app .player-app.is-app-screen .audio-prepare-view .speech-work-panel {
    grid-column: auto !important;
    grid-row: auto !important;
    align-self: stretch !important;
    justify-self: stretch !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) !important;
    grid-template-rows: minmax(108px, 1fr) auto auto !important;
    grid-template-areas:
      "phrase phrase"
      "config record"
      "controls controls" !important;
    gap: 8px !important;
    width: 100% !important;
    max-width: none !important;
    height: 100% !important;
    max-height: none !important;
    min-height: 0 !important;
    margin: 0 !important;
    padding: 0 !important;
    overflow: hidden !important;
    transform: none !important;
    translate: none !important;
  }

  body #app .player-app.is-app-screen .audio-prepare-view .speech-phrase-card {
    grid-area: phrase !important;
  }

  body #app .player-app.is-app-screen .audio-prepare-view .speech-config-card {
    grid-area: config !important;
  }

  body #app .player-app.is-app-screen .audio-prepare-view .speech-record-card {
    grid-area: record !important;
  }

  body #app .player-app.is-app-screen .audio-prepare-view .speech-record-controls {
    grid-area: controls !important;
    display: grid !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 8px !important;
  }
}

</style>
