<template>
  <section class="screen-center">
    <div class="google-home reader-home" :class="{ 'speech-result-home': isSpeechResult, 'settings-open': !isSpeechResult }">
      <AppHeader :subtitle="isSpeechResult ? 'Leitura a partir da fala' : 'Leitura assistida'">
        <template #actions>
          <div class="reader-header-actions">
            <button class="home-help-btn reader-header-btn" @click="$emit('reset-all')" title="Início" aria-label="Início">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6h-4v6H5a1 1 0 0 1-1-1z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              </svg>
            </button>
            <button v-if="activeFlow === 'image' && !isSpeechResult" class="home-help-btn reader-header-btn reader-text-btn" @click="$emit('open-validation')" title="Texto completo" aria-label="Texto completo">
              ☰
            </button>
          </div>
        </template>
      </AppHeader>

      <div class="google-home-main reader-main">
        <div v-if="!isSpeechResult" class="player-top reader-top">
        <div v-if="readingMode === 'line'" class="reading-focus line-focus"
          :style="{ fontSize: `${computedLineFontSize}px`, color: paletteStyles.lineText }">
          {{ currentLines[currentLineIndex] || 'Sem texto disponível.' }}
        </div>

        <div v-else class="reading-focus word-focus" :style="{ fontSize: `${computedWordContextFontSize}px` }">
          <div class="word-context">
            <span v-for="(word, index) in currentWords" :key="`${currentLineIndex}-${index}-${word}`"
              class="word-chip" :class="{ active: index === currentWordIndex }" :style="index === currentWordIndex
                ? { ...activeWordStyle, '--active-word-bg': paletteStyles.activeBg, '--active-word-text': paletteStyles.activeText }
                : { color: paletteStyles.inactiveWord }">
              {{ word }}
            </span>
          </div>
        </div>
        </div>

        <section v-if="isSpeechResult" class="speech-result-panel" aria-label="Resultado da leitura por voz">
          <article v-if="expectedReadingText" class="result-phrase-card">
            <div class="result-phrase-copy">
              <span class="speech-label">Frase esperada</span>
              <strong>{{ expectedReadingText }}</strong>
            </div>
            <button class="soft-action result-listen-btn" @click="$emit('speak-text', expectedReadingText)">Ouvir</button>
          </article>

          <article v-if="hasSpokenText" class="result-phrase-card">
            <div class="result-phrase-copy">
              <span class="speech-label">Frase transcrita</span>
              <strong>{{ spokenText }}</strong>
            </div>
            <button class="soft-action result-listen-btn" @click="$emit('speak-text', spokenText)">Ouvir</button>
          </article>
        </section>

        <div v-else class="player-bottom reader-bottom">
          <div v-if="activeFlow === 'audio' && (expectedReadingText || hasSpokenText)" class="reader-summary-stack">
            <div v-if="expectedReadingText" class="speech-summary-box">
              <div class="speech-summary-copy">
                <span class="speech-label">Frase esperada</span>
                <strong>{{ expectedReadingText }}</strong>
              </div>
              <button class="pill-btn listen-btn" @click="$emit('speak-text', expectedReadingText)">Ouvir</button>
            </div>

            <div v-if="hasSpokenText" class="speech-summary-box">
              <div class="speech-summary-copy">
                <span class="speech-label">Frase transcrita</span>
                <strong>{{ spokenText }}</strong>
              </div>
              <button class="pill-btn listen-btn" @click="$emit('speak-text', spokenText)">Ouvir</button>
            </div>
          </div>

          <div class="reader-control-stack">
            <div class="transport-row">
              <template v-if="readingMode === 'line'">
                <button class="round-btn prev-btn" @click="$emit('prev-line')" :disabled="currentLineIndex === 0">◀</button>
                <button class="play-btn" :class="{ active: isSpeakingLine }" @click="$emit('toggle-line-reading')">{{ isSpeakingLine ? '❚❚' : '▶' }}</button>
                <button class="round-btn next-btn" @click="$emit('next-line')"
                  :disabled="currentLineIndex === currentLines.length - 1">▶</button>
              </template>

              <template v-else>
                <button class="round-btn prev-btn" @click="$emit('prev-word')">◀</button>
                <button class="play-btn" :class="{ active: isPlayingWords }" @click="$emit('toggle-word-play')">{{ isPlayingWords ? '❚❚' : '▶' }}</button>
                <button class="round-btn next-btn" @click="$emit('next-word')">▶</button>
              </template>
            </div>

            <div class="progress-label">
              <span>Linha {{ currentLineIndex + 1 }}/{{ currentLines.length || 1 }}</span>
              <span v-if="readingMode === 'word'">· Palavra {{ currentWordIndex + 1 }}/{{ currentWords.length || 1
                }}</span>
            </div>

            <div class="progress-line">
              <div class="progress-fill" :style="{ width: `${lineProgressPercent}%` }"></div>
            </div>

            <div class="controls-compact">
              <button class="pill-btn" :class="{ active: currentTextMode === 'original' }"
                @click="$emit('switch-text-mode', 'original')">Original</button>
              <button class="pill-btn" :class="{ active: currentTextMode === 'simplified' }"
                @click="$emit('switch-text-mode', 'simplified')">Simplificado</button>
              <button v-if="hasSpokenText" class="pill-btn" :class="{ active: currentTextMode === 'spoken' }"
                @click="$emit('switch-text-mode', 'spoken')">Falado</button>
              <button class="pill-btn" :class="{ active: readingMode === 'line' }"
                @click="$emit('set-reading-mode', 'line')">Linha</button>
              <button class="pill-btn" :class="{ active: readingMode === 'word' }"
                @click="$emit('set-reading-mode', 'word')">Palavra</button>

              <button v-if="readingMode === 'line'" class="pill-btn" :class="{ active: linePlaybackMode === 'single' }"
                @click="$emit('update:linePlaybackMode', 'single')">Linha única</button>
              <button v-if="readingMode === 'line'" class="pill-btn"
                :class="{ active: linePlaybackMode === 'continuous' }" @click="$emit('update:linePlaybackMode', 'continuous')">
                {{ isSpeakingLine && linePlaybackMode === 'continuous' ? 'A ler até ao fim' : 'Até ao fim' }}
              </button>

              <button v-if="readingMode === 'word'" class="pill-btn" :class="{ active: wordAudioMode === 'silent' }"
                @click="$emit('set-word-audio-mode', 'silent')">Sem som</button>
              <button v-if="readingMode === 'word'" class="pill-btn" :class="{ active: wordAudioMode === 'audio' }"
                @click="$emit('set-word-audio-mode', 'audio')">Com som</button>

              <button class="pill-btn" @click="$emit('restart-reading')">Reiniciar</button>
              <button class="pill-btn" @click="$emit('stop-all-audio')">Parar</button>
            </div>

            <div class="settings-panel">
              <div class="setting-block">
                <label>Fonte {{ fontSize }}</label>
                <input :value="fontSize" type="range" min="18" max="34"
                  @input="$emit('update:fontSize', Number($event.target.value))" />
              </div>

              <div class="setting-block">
                <label>Velocidade {{ speechRate.toFixed(2) }}</label>
                <input :value="speechRate" type="range" min="0.6" max="1.8" step="0.05"
                  @input="$emit('update:speechRate', Number($event.target.value))" />
              </div>

              <div class="setting-block">
                <label>Cores de leitura</label>
                <select :value="readingPalette" @change="$emit('update:readingPalette', $event.target.value)">
                  <option value="yellow">Amarelo</option>
                  <option value="blue">Azul</option>
                  <option value="green">Verde</option>
                  <option value="pink">Rosa suave</option>
                </select>
              </div>

              <label v-if="readingMode === 'word'" class="check-block">
                <input :checked="autoAdvanceLine" type="checkbox"
                  @change="$emit('update:autoAdvanceLine', $event.target.checked)" />
                Passar à linha seguinte
              </label>
            </div>
          </div>
        </div>
      </div>

      <BottomNav @home="$emit('reset-all')" />
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import AppHeader from '../components/common/AppHeader.vue'
import BottomNav from '../components/common/BottomNav.vue'

const props = defineProps({
  activeFlow: {
    type: String,
    default: 'image',
  },
  activeWordStyle: {
    type: Object,
    required: true,
  },
  audioIssues: {
    type: Array,
    default: () => [],
  },
  autoAdvanceLine: {
    type: Boolean,
    default: true,
  },
  computedLineFontSize: {
    type: Number,
    required: true,
  },
  computedWordContextFontSize: {
    type: Number,
    required: true,
  },
  currentLineIndex: {
    type: Number,
    required: true,
  },
  currentLines: {
    type: Array,
    default: () => [],
  },
  currentTextMode: {
    type: String,
    required: true,
  },
  currentWordIndex: {
    type: Number,
    required: true,
  },
  currentWords: {
    type: Array,
    default: () => [],
  },
  expectedReadingText: {
    type: String,
    default: '',
  },
  fontSize: {
    type: Number,
    required: true,
  },
  hasSpokenText: {
    type: Boolean,
    default: false,
  },
  isFullscreen: {
    type: Boolean,
    default: false,
  },
  isPlayingWords: {
    type: Boolean,
    default: false,
  },
  isSpeakingLine: {
    type: Boolean,
    default: false,
  },
  linePlaybackMode: {
    type: String,
    required: true,
  },
  lineProgressPercent: {
    type: Number,
    required: true,
  },
  paletteStyles: {
    type: Object,
    required: true,
  },
  readingMode: {
    type: String,
    required: true,
  },
  readingPalette: {
    type: String,
    required: true,
  },
  showSettings: {
    type: Boolean,
    default: false,
  },
  speechRate: {
    type: Number,
    required: true,
  },
  spokenText: {
    type: String,
    default: '',
  },
  spokenTranscription: {
    type: String,
    default: '',
  },
  wordAudioMode: {
    type: String,
    required: true,
  },
})

const isSpeechResult = computed(() => props.activeFlow === 'audio' && (props.expectedReadingText || props.hasSpokenText))

defineEmits([
  'next-line',
  'next-word',
  'open-validation',
  'prev-line',
  'prev-word',
  'reset-all',
  'restart-reading',
  'set-reading-mode',
  'set-word-audio-mode',
  'speak-text',
  'stop-all-audio',
  'switch-text-mode',
  'toggle-fullscreen',
  'toggle-line-reading',
  'toggle-word-play',
  'update:autoAdvanceLine',
  'update:fontSize',
  'update:linePlaybackMode',
  'update:readingPalette',
  'update:showSettings',
  'update:speechRate',
])
</script>

<style scoped>
.reader-home {
  grid-template-rows: auto minmax(0, 1fr) auto !important;
}

.reader-main {
  grid-template-rows: minmax(0, 1fr) auto !important;
  justify-items: stretch !important;
  gap: 10px !important;
  padding-top: 8px !important;
}

.reader-header-actions {
  display: flex !important;
  align-items: center !important;
  gap: 8px !important;
}

.reader-header-btn {
  display: grid !important;
  place-items: center !important;
}

.reader-top {
  min-height: 0 !important;
}

.reader-bottom {
  display: grid !important;
  align-content: start !important;
  gap: 12px !important;
  min-height: 0 !important;
}

.speech-result-home .reader-main {
  grid-template-rows: minmax(0, 1fr) !important;
  align-items: stretch !important;
  justify-items: stretch !important;
}

.speech-result-panel {
  min-width: 0 !important;
  min-height: 0 !important;
  width: min(100%, 900px) !important;
  justify-self: center !important;
  align-self: start !important;
  display: grid !important;
  grid-template-columns: 1fr !important;
  grid-template-rows: repeat(2, auto) !important;
  gap: 12px !important;
}

.result-phrase-card {
  min-width: 0 !important;
  min-height: 112px !important;
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) auto !important;
  align-items: center !important;
  gap: 12px !important;
  padding: 14px !important;
  border: 1px solid #e8edf5 !important;
  border-radius: 14px !important;
  background: #fff !important;
  box-shadow: 0 5px 16px rgba(15, 23, 42, .09) !important;
}

.result-phrase-copy {
  min-width: 0 !important;
  display: grid !important;
  gap: 6px !important;
}

.result-phrase-copy strong {
  color: #172033 !important;
  font-size: clamp(1.08rem, 3.4vw, 1.42rem) !important;
  line-height: 1.2 !important;
  font-weight: 760 !important;
  overflow-wrap: anywhere !important;
}

.result-listen-btn {
  min-width: 74px !important;
  min-height: 42px !important;
  justify-self: end !important;
  border-radius: 999px !important;
  background: #eef5ff !important;
  color: #1a73e8 !important;
  font-weight: 760 !important;
  white-space: nowrap !important;
}

.speech-result-home .reader-bottom {
  width: 100% !important;
  height: 100% !important;
  justify-self: center !important;
  align-self: stretch !important;
  align-content: stretch !important;
  background: transparent !important;
  border-top: 0 !important;
  max-height: none !important;
  overflow: hidden !important;
}

.reader-summary-stack,
.reader-control-stack {
  display: grid !important;
  gap: 10px !important;
  width: 100% !important;
  min-width: 0 !important;
}

.speech-summary-box {
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) auto !important;
  align-items: center !important;
  gap: 10px !important;
}

.speech-summary-copy {
  min-width: 0 !important;
  display: grid !important;
  gap: 4px !important;
}

.speech-summary-copy strong {
  overflow-wrap: anywhere !important;
}

.listen-btn {
  justify-self: end !important;
  white-space: nowrap !important;
}

.speech-result-home .settings-panel {
  display: none !important;
}

.speech-result-home .reader-summary-stack {
  height: 100% !important;
  width: 100% !important;
  grid-template-columns: 1fr !important;
  grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
}

.speech-result-home .speech-summary-box {
  width: 100% !important;
  min-height: 0 !important;
  grid-template-columns: minmax(0, 1fr) auto !important;
  gap: 14px !important;
  background: #f8fbff !important;
  border: 1px solid #e5eaf2 !important;
  box-shadow: 0 7px 18px rgba(15, 23, 42, .045) !important;
}

.speech-result-home .speech-summary-copy {
  height: 100% !important;
  align-content: center !important;
  justify-items: start !important;
  text-align: left !important;
}

.speech-result-home .speech-label {
  color: #667085 !important;
}

@media (orientation: landscape), (max-height: 620px) and (min-width: 520px) {
  .reader-home {
    height: 100dvh !important;
    min-height: 100dvh !important;
  }

  .reader-main {
    grid-template-rows: minmax(0, 34dvh) minmax(0, 1fr) !important;
    gap: 8px !important;
    padding: 4px clamp(22px, 4vw, 46px) 6px !important;
  }

  .speech-result-home .reader-main {
    grid-template-rows: minmax(0, 1fr) !important;
    padding: 4px clamp(34px, 6vw, 72px) 8px !important;
  }

  .speech-result-panel {
    width: min(100%, 1080px) !important;
    height: 100% !important;
    align-self: stretch !important;
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    gap: 10px !important;
  }

  .result-phrase-card {
    min-height: 0 !important;
    padding: clamp(10px, 2vh, 16px) clamp(14px, 2.4vw, 22px) !important;
    border-radius: 13px !important;
  }

  .result-phrase-copy {
    gap: 5px !important;
  }

  .result-phrase-copy strong {
    font-size: clamp(1.15rem, 2.35vw, 2rem) !important;
    line-height: 1.16 !important;
  }

  .result-listen-btn {
    min-width: 76px !important;
    min-height: 40px !important;
    padding: 8px 14px !important;
    font-size: .9rem !important;
  }

  .reader-top {
    height: auto !important;
    min-height: 0 !important;
    padding: 18px 22px 12px !important;
  }

  .reader-bottom {
    max-height: none !important;
    min-height: 0 !important;
    overflow: auto !important;
    gap: 8px !important;
    padding: 8px 16px max(8px, env(safe-area-inset-bottom)) !important;
  }

  .speech-result-home .reader-bottom {
    overflow: hidden !important;
    align-content: stretch !important;
    gap: 0 !important;
    padding: 0 !important;
  }

  .line-focus {
    max-width: 720px !important;
    line-height: 1.2 !important;
  }

  .word-focus {
    width: 100% !important;
    max-width: none !important;
  }

  .word-context {
    width: 100% !important;
    gap: 6px !important;
  }

  .word-chip {
    background: #fff !important;
    border: 1px solid #d8e2ef !important;
    color: #172033 !important;
    padding: 8px 12px !important;
    box-shadow: 0 3px 10px rgba(15, 23, 42, .05) !important;
  }

  .word-chip.active {
    background: var(--active-word-bg) !important;
    color: var(--active-word-text) !important;
    border-color: transparent !important;
  }

  .speech-result-home .reader-summary-stack {
    grid-template-columns: 1fr !important;
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    gap: clamp(10px, 2.8vh, 18px) !important;
  }

  .speech-result-home .reader-control-stack {
    gap: 6px !important;
  }

  .speech-result-home .speech-summary-box {
    min-width: 0 !important;
    padding: clamp(12px, 2.4vh, 18px) clamp(18px, 3vw, 28px) !important;
    border-radius: 16px !important;
    min-height: 0 !important;
  }

  .speech-result-home .speech-label {
    font-size: clamp(.74rem, 1.35vw, .92rem) !important;
    line-height: 1.1 !important;
    letter-spacing: .08em !important;
  }

  .speech-result-home .speech-summary-copy strong {
    width: 100% !important;
    font-size: clamp(1.35rem, 3.2vw, 2.7rem) !important;
    line-height: 1.14 !important;
    font-weight: 760 !important;
  }

  .speech-result-home .listen-btn {
    min-width: 76px !important;
    min-height: 40px !important;
    padding: 8px 14px !important;
    font-size: clamp(.86rem, 1.45vw, 1rem) !important;
  }

  .transport-row {
    gap: 10px !important;
  }

  .round-btn,
  .play-btn {
    width: 40px !important;
    height: 40px !important;
  }

  .controls-compact {
    display: grid !important;
    grid-template-columns: repeat(5, minmax(0, 1fr)) !important;
    gap: 8px !important;
    width: 100% !important;
    justify-self: center !important;
  }

  .pill-btn {
    min-height: 38px !important;
    padding: 8px 10px !important;
    font-size: .84rem !important;
  }

  .settings-panel {
    padding: 8px !important;
    gap: 8px !important;
  }

  .speech-result-home .settings-panel {
    display: none !important;
  }
}

@media (orientation: portrait), (max-width: 620px) {
  .speech-result-home .reader-main {
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: stretch !important;
    padding: 8px clamp(12px, 3.5vw, 18px) 12px !important;
  }

  .speech-result-panel {
    width: 100% !important;
    height: 100% !important;
    align-self: stretch !important;
    justify-self: stretch !important;
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    gap: 10px !important;
  }

  .result-phrase-card {
    min-height: 0 !important;
    height: 100% !important;
    padding: 12px 10px 12px 12px !important;
    border-radius: 14px !important;
    align-content: start !important;
    align-items: start !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    gap: 0 !important;
    position: relative !important;
  }

  .result-phrase-copy {
    height: 100% !important;
    align-self: stretch !important;
    display: grid !important;
    grid-template-rows: auto minmax(0, 1fr) !important;
    align-content: stretch !important;
    justify-items: center !important;
    gap: 6px !important;
  }

  .result-phrase-copy .speech-label {
    justify-self: center !important;
    text-align: center !important;
    font-size: clamp(.72rem, 2.8vw, .82rem) !important;
    line-height: 1.1 !important;
    letter-spacing: .08em !important;
  }

  .result-phrase-copy strong {
    align-self: center !important;
    justify-self: center !important;
    text-align: center !important;
    max-width: calc(100% - 78px) !important;
    font-size: clamp(.9rem, 3.55vw, 1.06rem) !important;
    line-height: 1.16 !important;
    letter-spacing: 0 !important;
    font-weight: 760 !important;
  }

  .result-listen-btn {
    position: absolute !important;
    right: 10px !important;
    top: 50% !important;
    transform: translateY(-50%) !important;
    min-width: 58px !important;
    min-height: 34px !important;
    padding: 7px 10px !important;
    font-size: .78rem !important;
  }

  .speech-result-home .reader-bottom {
    align-content: stretch !important;
    gap: 0 !important;
    padding: 0 !important;
  }

  .speech-result-home .reader-summary-stack {
    grid-template-columns: 1fr !important;
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    gap: 12px !important;
  }

  .speech-result-home .speech-summary-box {
    min-height: 0 !important;
    padding: clamp(14px, 3vh, 20px) 16px !important;
    border-radius: 18px !important;
    grid-template-columns: minmax(0, 1fr) auto !important;
  }

  .speech-result-home .speech-summary-copy strong {
    width: 100% !important;
    font-size: clamp(1.45rem, 6.2vw, 2.25rem) !important;
    line-height: 1.15 !important;
    font-weight: 760 !important;
  }

  .speech-result-home .speech-label {
    font-size: clamp(.78rem, 2.6vw, .95rem) !important;
    line-height: 1.1 !important;
  }

  .speech-result-home .controls-compact {
    display: grid !important;
    grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
    gap: 7px !important;
  }

  .speech-result-home .pill-btn {
    min-height: 48px !important;
    padding: 10px 12px !important;
    font-size: .95rem !important;
  }

  .speech-result-home .listen-btn {
    min-width: 68px !important;
    min-height: 42px !important;
    padding: 8px 12px !important;
    font-size: .92rem !important;
  }
}

@media (orientation: portrait) and (max-height: 720px) {
  .speech-result-home .reader-main {
    padding-top: 6px !important;
    padding-bottom: 10px !important;
  }

  .speech-result-panel {
    gap: 8px !important;
  }

  .result-phrase-card {
    padding: 10px !important;
  }

  .result-phrase-copy strong {
    font-size: clamp(.86rem, 3.3vw, .98rem) !important;
    line-height: 1.14 !important;
  }

  .result-phrase-copy .speech-label {
    font-size: clamp(.66rem, 2.45vw, .76rem) !important;
  }

  .result-listen-btn {
    min-width: 54px !important;
    min-height: 32px !important;
    padding: 6px 9px !important;
    font-size: .74rem !important;
  }
}

@media (orientation: landscape) and (min-width: 520px) {
  .speech-result-home :deep(.home-brand-subtitle) {
    margin-top: 2px !important;
    font-size: .92rem !important;
    line-height: 1.1 !important;
  }

  .speech-result-home .reader-main {
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: stretch !important;
    justify-items: stretch !important;
    padding: 8px clamp(18px, 3vw, 30px) 10px !important;
  }

  .speech-result-panel {
    width: 100% !important;
    max-width: none !important;
    height: 100% !important;
    align-self: stretch !important;
    justify-self: stretch !important;
    grid-column: 1 / -1 !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    gap: 12px !important;
  }

  .result-phrase-card {
    min-width: 0 !important;
    min-height: 0 !important;
    height: 100% !important;
    position: relative !important;
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-content: stretch !important;
    align-items: stretch !important;
    padding: 14px 16px !important;
    border-radius: 14px !important;
    gap: 0 !important;
  }

  .result-phrase-copy {
    height: 100% !important;
    align-self: stretch !important;
    display: grid !important;
    grid-template-rows: auto minmax(0, 1fr) !important;
    align-content: stretch !important;
    justify-items: center !important;
    gap: 6px !important;
    text-align: center !important;
  }

  .result-phrase-copy .speech-label {
    justify-self: center !important;
    text-align: center !important;
    font-size: clamp(.72rem, 2.8vw, .82rem) !important;
    line-height: 1.1 !important;
    letter-spacing: .08em !important;
  }

  .result-phrase-copy strong {
    align-self: center !important;
    justify-self: center !important;
    max-width: calc(100% - 92px) !important;
    text-align: center !important;
    font-size: clamp(.9rem, 3.55vw, 1.06rem) !important;
    line-height: 1.16 !important;
    letter-spacing: 0 !important;
    font-weight: 760 !important;
  }

  .result-listen-btn {
    position: absolute !important;
    right: 16px !important;
    top: 50% !important;
    transform: translateY(-50%) !important;
    min-width: 66px !important;
    min-height: 36px !important;
    padding: 7px 10px !important;
    font-size: .78rem !important;
  }
}

@media (orientation: portrait) and (max-width: 620px) {
  .reader-home.settings-open:not(.speech-result-home) .reader-main {
    grid-template-rows: minmax(0, .72fr) auto !important;
    gap: 6px !important;
    padding: 4px 12px 6px !important;
    overflow: hidden !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-top {
    min-height: 0 !important;
    height: 100% !important;
    padding: 10px 10px 8px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .line-focus {
    max-width: 100% !important;
    width: 100% !important;
    line-height: 1.2 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .word-focus {
    width: 100% !important;
    max-width: 100% !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    max-height: none !important;
    min-height: 0 !important;
    overflow: hidden !important;
    gap: 5px !important;
    padding: 6px 6px max(4px, env(safe-area-inset-bottom)) !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-summary-stack {
    gap: 5px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .speech-summary-box {
    min-height: 0 !important;
    padding: 8px 10px !important;
    border-radius: 12px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .speech-summary-copy {
    gap: 2px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .speech-summary-copy strong {
    font-size: .78rem !important;
    line-height: 1.18 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .speech-label {
    font-size: .66rem !important;
    line-height: 1.1 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .listen-btn {
    min-width: 54px !important;
    min-height: 30px !important;
    padding: 5px 9px !important;
    font-size: .76rem !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .transport-row {
    gap: 8px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .round-btn,
  .reader-home.settings-open:not(.speech-result-home) .play-btn {
    width: 34px !important;
    height: 34px !important;
    min-width: 34px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .progress-label {
    font-size: .82rem !important;
    line-height: 1.1 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .progress-line {
    height: 4px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    display: grid !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 5px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .pill-btn {
    min-height: 30px !important;
    padding: 5px 7px !important;
    border-radius: 999px !important;
    font-size: .72rem !important;
    line-height: 1.08 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
    gap: 6px !important;
    padding: 8px !important;
    border-radius: 12px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .setting-block {
    gap: 4px !important;
    font-size: .72rem !important;
    line-height: 1.12 !important;
    text-align: center !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .setting-block input,
  .reader-home.settings-open:not(.speech-result-home) .setting-block select {
    min-height: 26px !important;
    padding: 3px 6px !important;
    border-radius: 8px !important;
    font-size: .72rem !important;
  }
}

@media (orientation: portrait) and (max-width: 620px) and (max-height: 700px) {
  .reader-home.settings-open:not(.speech-result-home) .reader-main {
    grid-template-rows: minmax(0, .58fr) auto !important;
    gap: 4px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-top {
    padding: 6px 8px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    gap: 4px !important;
    padding-top: 4px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    gap: 4px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .pill-btn {
    min-height: 28px !important;
    padding: 4px 6px !important;
    font-size: .68rem !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    padding: 6px !important;
  }
}

@media (orientation: landscape) and (min-width: 520px) {
  .reader-home.settings-open:not(.speech-result-home) .reader-main {
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    gap: 6px !important;
    padding: 4px clamp(16px, 3vw, 28px) 6px !important;
    overflow: hidden !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-top {
    grid-column: 1 !important;
    grid-row: 1 !important;
    min-height: 0 !important;
    height: 100% !important;
    padding: 8px 10px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .line-focus {
    width: 100% !important;
    max-width: 100% !important;
    line-height: 1.2 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .word-focus,
  .reader-home.settings-open:not(.speech-result-home) .word-context {
    width: 100% !important;
    max-width: none !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .word-context {
    gap: 6px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .word-chip {
    padding: 6px 9px !important;
    font-weight: 760 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .reader-bottom {
    grid-column: 1 !important;
    grid-row: 2 !important;
    max-height: none !important;
    min-height: 0 !important;
    overflow: hidden !important;
    gap: 5px !important;
    padding: 5px 6px max(4px, env(safe-area-inset-bottom)) !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .transport-row {
    gap: 8px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .round-btn,
  .reader-home.settings-open:not(.speech-result-home) .play-btn {
    width: 34px !important;
    height: 34px !important;
    min-width: 34px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .progress-label {
    font-size: .82rem !important;
    line-height: 1.1 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .progress-line {
    height: 4px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .controls-compact {
    display: grid !important;
    grid-template-columns: repeat(7, minmax(0, 1fr)) !important;
    gap: 5px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .pill-btn {
    min-height: 30px !important;
    padding: 5px 7px !important;
    font-size: .72rem !important;
    line-height: 1.08 !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .settings-panel {
    grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
    gap: 6px !important;
    padding: 7px 8px !important;
    border-radius: 12px !important;
  }

  .reader-home.settings-open:not(.speech-result-home) .setting-block,
  .reader-home.settings-open:not(.speech-result-home) .setting-block label,
  .reader-home.settings-open:not(.speech-result-home) .setting-block select,
  .reader-home.settings-open:not(.speech-result-home) .setting-block input {
    font-size: .72rem !important;
    line-height: 1.1 !important;
  }
}
</style>
