<template>
  <section class="reader-view-screen">
    <div class="reader-view-shell" :class="{ 'reader-speech-result': isSpeechResult, 'reader-settings-open': !isSpeechResult }">
      <AppHeader :subtitle="isSpeechResult ? t('app.speech') : t('app.assisted')">
        <template #actions>
          <div class="reader-header-actions">
            <LanguageToggle />
            <button class="home-help-btn reader-header-btn" @click="$emit('reset-all')" :title="t('app.home')" :aria-label="t('app.home')">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6h-4v6H5a1 1 0 0 1-1-1z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              </svg>
            </button>
            <button v-if="activeFlow === 'image' && !isSpeechResult" class="home-help-btn reader-header-btn reader-text-btn" @click="$emit('open-validation')" :title="t('app.fullText')" :aria-label="t('app.fullText')">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M4 6h16M4 12h16M4 18h16" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </template>
      </AppHeader>

      <div class="reader-view-main">
        <div v-if="!isSpeechResult" class="reader-text-panel">
        <div v-if="readingMode === 'line'" class="reader-line-focus"
          :style="{ fontSize: `${computedLineFontSize}px`, color: paletteStyles.lineText || '#172033' }">
          {{ currentLines[currentLineIndex] || t('reader.noText') }}
        </div>

        <div v-else class="reader-word-focus" :style="{ fontSize: `${computedWordContextFontSize}px` }">
          <div class="reader-word-context">
            <span v-for="(word, index) in currentWords" :key="`${currentLineIndex}-${index}-${word}`"
              class="reader-word-chip" :class="{ active: index === currentWordIndex }" :style="index === currentWordIndex
                ? { ...activeWordStyle, '--active-word-bg': paletteStyles.activeBg, '--active-word-text': paletteStyles.activeText }
                : { color: paletteStyles.inactiveWord }">
              {{ word }}<template v-if="index < currentWords.length - 1"> </template>
            </span>
          </div>
        </div>
        </div>

        <section v-if="isSpeechResult" class="reader-speech-result-panel" :aria-label="t('reader.speechResultLabel')">
          <article v-if="expectedReadingText" class="result-phrase-card">
            <div class="result-phrase-copy">
              <span class="speech-label">{{ t('reader.expectedPhrase') }}</span>
              <strong>{{ expectedReadingText }}</strong>
            </div>
            <div class="result-action-stack">
              <button class="soft-action result-listen-btn" @click="$emit('speak-text', expectedReadingText)">{{ t('reader.listen') }}</button>
            </div>
          </article>

          <article v-if="hasSpokenText" class="result-phrase-card">
            <div class="result-phrase-copy">
              <span class="speech-label">{{ t('reader.transcribedPhrase') }}</span>
              <strong>{{ spokenText }}</strong>
            </div>
            <div class="result-action-stack">
              <button class="soft-action result-listen-btn" @click="$emit('speak-text', spokenText)">{{ t('reader.listen') }}</button>
              <button
                v-if="audioFeedbackText"
                class="soft-action result-feedback-listen-btn"
                @click="$emit('speak-text', audioFeedbackText)"
              >
                {{ t('reader.listenComment') }}
              </button>
            </div>
          </article>
        </section>

        <div v-else class="reader-controls-panel">
          <div v-if="activeFlow === 'audio' && (expectedReadingText || hasSpokenText)" class="reader-summary-stack">
            <div v-if="expectedReadingText" class="speech-summary-box">
              <div class="speech-summary-copy">
                <span class="speech-label">{{ t('reader.expectedPhrase') }}</span>
                <strong>{{ expectedReadingText }}</strong>
              </div>
              <button class="pill-btn listen-btn" @click="$emit('speak-text', expectedReadingText)">{{ t('reader.listen') }}</button>
            </div>

            <div v-if="hasSpokenText" class="speech-summary-box">
              <div class="speech-summary-copy">
                <span class="speech-label">{{ t('reader.transcribedPhrase') }}</span>
                <strong>{{ spokenText }}</strong>
              </div>
              <button class="pill-btn listen-btn" @click="$emit('speak-text', spokenText)">{{ t('reader.listen') }}</button>
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
              <span>{{ t('reader.line') }} {{ currentLineIndex + 1 }}/{{ currentLines.length || 1 }}</span>
              <span v-if="readingMode === 'word'">· {{ t('reader.word') }} {{ currentWordIndex + 1 }}/{{ currentWords.length || 1
                }}</span>
            </div>

            <div class="progress-line">
              <div class="progress-fill" :style="{ width: `${lineProgressPercent}%` }"></div>
            </div>

            <div class="controls-compact">
              <button class="pill-btn" :class="{ active: currentTextMode === 'original' }"
                @click="$emit('switch-text-mode', 'original')">{{ t('reader.original') }}</button>
              <button class="pill-btn" :class="{ active: currentTextMode === 'simplified' }"
                @click="$emit('switch-text-mode', 'simplified')">{{ t('reader.simplified') }}</button>
              <button v-if="hasSpokenText" class="pill-btn" :class="{ active: currentTextMode === 'spoken' }"
                @click="$emit('switch-text-mode', 'spoken')">{{ t('reader.spoken') }}</button>
              <button class="pill-btn" :class="{ active: showSyllables }"
                @click="$emit('toggle-syllables')">{{ t('reader.syllables') }}</button>
              <button class="pill-btn" :class="{ active: readingMode === 'line' }"
                @click="$emit('set-reading-mode', 'line')">{{ t('reader.line') }}</button>
              <button class="pill-btn" :class="{ active: readingMode === 'word' }"
                @click="$emit('set-reading-mode', 'word')">{{ t('reader.word') }}</button>
              <button class="pill-btn" :class="{ active: wordAudioMode === 'audio' }"
                @click="$emit('set-word-audio-mode', wordAudioMode === 'audio' ? 'silent' : 'audio')">{{ t('reader.sound') }}</button>

              <button v-if="readingMode === 'line'" class="pill-btn" :class="{ active: linePlaybackMode === 'single' }"
                @click="$emit('update:linePlaybackMode', 'single')">{{ t('reader.singleLine') }}</button>
              <button v-if="readingMode === 'line'" class="pill-btn"
                :class="{ active: linePlaybackMode === 'continuous' }" @click="$emit('update:linePlaybackMode', 'continuous')">
                {{ isSpeakingLine && linePlaybackMode === 'continuous' ? t('reader.readingToEnd') : t('reader.toEnd') }}
              </button>

              <button class="pill-btn" @click="$emit('restart-reading')">{{ t('reader.restart') }}</button>
              <button class="pill-btn" @click="$emit('stop-all-audio')">{{ t('reader.stop') }}</button>
            </div>

            <div class="settings-panel">
              <div class="setting-block">
                <label>{{ t('reader.font') }} {{ fontSize }}</label>
                <input :value="fontSize" type="range" min="14" max="24"
                  @input="$emit('update:fontSize', Number($event.target.value))" />
              </div>

              <div class="setting-block">
                <label>{{ t('reader.speed') }} {{ speechRate.toFixed(2) }}</label>
                <input :value="speechRate" type="range" min="0.6" max="1.8" step="0.05"
                  @input="$emit('update:speechRate', Number($event.target.value))" />
              </div>

              <div class="setting-block">
                <label>{{ t('reader.colors') }}</label>
                <select :value="readingPalette" @change="$emit('update:readingPalette', $event.target.value)">
                  <option value="yellow">{{ t('reader.yellow') }}</option>
                  <option value="blue">{{ t('reader.blue') }}</option>
                  <option value="green">{{ t('reader.green') }}</option>
                  <option value="pink">{{ t('reader.pink') }}</option>
                </select>
              </div>

              <label v-if="readingMode === 'word'" class="check-block">
                <input :checked="autoAdvanceLine" type="checkbox"
                  @change="$emit('update:autoAdvanceLine', $event.target.checked)" />
                {{ t('reader.nextLine') }}
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
import LanguageToggle from '../components/common/LanguageToggle.vue'
import { t } from '../i18n'

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
  audioFeedbackText: {
    type: String,
    default: '',
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
  showSyllables: {
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
  'toggle-syllables',
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
/* ===== READER ISOLADO — mantém o desenho aprovado, sem herdar Home/Google ===== */
.reader-view-screen {
  width: 100%;
  height: 100dvh;
  min-height: 0;
  overflow: hidden;
  background: #fff;
  color: #172033;
  box-sizing: border-box;
}

.reader-view-shell {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  overflow: hidden;
  box-sizing: border-box;
}

.reader-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.reader-header-btn {
  display: grid;
  place-items: center;
}

.reader-header-actions .home-help-btn,
.reader-header-actions .reader-header-btn {
  width: 32px !important;
  height: 32px !important;
  min-width: 32px !important;
  min-height: 32px !important;
  padding: 0 !important;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  color: #172033 !important;
  font-size: 1.15rem !important;
  line-height: 1 !important;
}
.reader-header-actions .home-help-btn svg,
.reader-header-actions .reader-header-btn svg {
  width: 18px !important;
  height: 18px !important;
  display: block !important;
}
.reader-header-actions .reader-text-btn {
  font-size: 1.28rem !important;
  font-weight: 760 !important;
}

.reader-view-main {
  min-height: 0;
  width: 100%;
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  gap: 8px;
  padding: 8px clamp(28px, 5vw, 56px) max(10px, env(safe-area-inset-bottom));
  overflow: hidden;
  box-sizing: border-box;
}

.reader-text-panel {
  min-width: 0;
  min-height: 0;
  width: 100%;
  max-width: none;
  justify-self: stretch;
  align-self: stretch;
  display: grid;
  place-items: center stretch;
  overflow: hidden;
  padding: 10px 6px;
  box-sizing: border-box;
}

.reader-line-focus,
.reader-word-focus,
.reader-word-context {
  display: block;
  min-width: 0;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  color: #172033;
  font-weight: 400;
  line-height: 1.22;
  text-align: left;
  overflow-wrap: anywhere;
  word-break: normal;
  hyphens: auto;
}

.reader-word-context {
  line-height: 1.45;
}

.reader-word-chip {
  display: inline;
  padding: 0 .08em;
  border-radius: 3px;
  background: transparent;
  white-space: normal;
  font-weight: 400;
}
.reader-word-chip.active {
  background: var(--active-word-bg);
  color: var(--active-word-text);
}

.reader-controls-panel {
  min-width: 0;
  width: 100%;
  max-width: none;
  justify-self: stretch;
  align-self: end;
  overflow: hidden;
  display: grid;
  gap: 7px;
  padding: 0;
  box-sizing: border-box;
}
.reader-summary-stack,
.reader-control-stack {
  min-width: 0;
  width: 100%;
  display: grid;
  gap: 7px;
}

.speech-summary-box {
  min-width: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border: 1px solid #e5eaf2;
  border-radius: 14px;
  background: #f8fbff;
}
.speech-summary-copy { min-width: 0; display: grid; gap: 3px; }
.speech-label { color: #667085; font-size: .72rem; font-weight: 760; text-transform: uppercase; letter-spacing: .06em; }
.speech-summary-copy strong { font-size: .9rem; line-height: 1.15; overflow-wrap: anywhere; }
.listen-btn { justify-self: end; white-space: nowrap; }

.transport-row {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  min-height: 30px;
}
.round-btn,
.play-btn {
  width: 34px;
  height: 28px;
  min-width: 34px;
  border-radius: 0;
  border: 0;
  background: transparent;
  box-shadow: none;
  color: #8a94a6;
  font-size: 1.18rem;
  font-weight: 800;
  line-height: 1;
  display: inline-grid;
  place-items: center;
  padding: 0;
}
.play-btn {
  color: #1a73e8;
  background: transparent;
  box-shadow: none;
}
.play-btn.active {
  color: #ea4335;
}
.next-btn { color: #34a853; }
.round-btn:disabled { opacity: .35; }

.progress-label {
  display: flex;
  justify-content: center;
  gap: 6px;
  font-size: .78rem;
  font-weight: 700;
  color: #667085;
  line-height: 1;
}
.progress-line {
  width: 100%;
  height: 4px;
  border-radius: 999px;
  background: #e8edf5;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 999px;
  background: #1a73e8;
}

.controls-compact {
  display: grid;
  grid-template-columns: repeat(10, minmax(0, 1fr));
  gap: 6px;
  width: 100%;
}
.controls-compact .pill-btn {
  min-width: 0;
  min-height: 32px;
  height: auto;
  padding: 6px 7px;
  border-radius: 999px;
  border: 1px solid #e5eaf2;
  background: #f3f6fb;
  color: #344054;
  display: grid;
  place-items: center;
  text-align: center;
  font-size: clamp(.62rem, 1.9vh, .82rem);
  font-weight: 740;
  line-height: 1.05;
  white-space: normal;
  box-shadow: 0 3px 8px rgba(15, 23, 42, .04);
}
.controls-compact .pill-btn.active {
  background: #e8f0fe;
  color: #1a73e8;
  border-color: #bcd4ff;
}

.settings-panel {
  min-width: 0;
  width: 100%;
  height: 74px;
  min-height: 74px;
  max-height: 74px;
  display: grid;
  grid-template-columns: 1fr 1fr 1.05fr;
  gap: 8px;
  padding: 8px;
  border: 1px solid #eef2f7;
  border-radius: 14px;
  background: #f8fbff;
  overflow: hidden;
  box-sizing: border-box;
}
.setting-block {
  min-width: 0;
  display: grid;
  gap: 4px;
  font-size: .72rem;
  color: #344054;
}
.setting-block input,
.setting-block select {
  width: 100%;
  min-width: 0;
  min-height: 28px;
  font-size: .72rem;
}
.check-block {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: .72rem;
  white-space: nowrap;
}

.reader-speech-result-panel {
  min-width: 0;
  min-height: 0;
  width: 100%;
  height: 100%;
  justify-self: stretch;
  display: grid;
  grid-template-rows: repeat(2, minmax(0, 1fr)) auto;
  gap: 12px;
  overflow: hidden;
}
.result-phrase-card {
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border: 1px solid #e8edf5;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 5px 16px rgba(15, 23, 42, .08);
  overflow: hidden;
}
.result-phrase-copy { min-width: 0; display: grid; gap: 6px; }
.result-phrase-copy strong { font-size: clamp(1rem, 2.4vw, 1.7rem); line-height: 1.18; overflow-wrap: anywhere; }
.result-action-stack {
  display: grid;
  gap: 8px;
  justify-items: stretch;
  align-content: center;
}
.result-listen-btn,
.result-feedback-listen-btn { white-space: nowrap; }
.result-feedback-listen-btn { justify-self: stretch; }

/* Médio/grande em paisagem: caso do Samsung Tab S6 Lite */
@media (orientation: landscape) and (min-width: 700px) and (min-height: 331px) {
  .reader-view-main {
    grid-template-rows: minmax(0, 1fr) auto;
    gap: 8px;
    padding-inline: clamp(28px, 5vw, 56px);
  }
  .reader-text-panel {
    width: 100%;
    padding: 10px 6px;
    place-items: center stretch;
  }
  .reader-line-focus {
    font-size: clamp(.92rem, 1.7vw, 1.16rem) !important;
    line-height: 1.22;
  }
  .reader-word-focus {
    font-size: clamp(.9rem, 1.65vw, 1.12rem) !important;
  }
  .reader-controls-panel {
    width: 100%;
    max-height: 190px;
  }
}

/* Paisagem baixa: reduz controlos sem quebrar largura */
@media (orientation: landscape) and (max-height: 430px) {
  .reader-view-main {
    padding-top: 4px;
    gap: 5px;
  }
  .reader-text-panel {
    padding: 6px 6px;
  }
  .reader-line-focus {
    font-size: clamp(.86rem, 3.4vh, 1.08rem) !important;
    line-height: 1.18;
  }
  .reader-controls-panel {
    max-height: 152px;
    gap: 5px;
  }
  .transport-row { min-height: 26px; }
  .round-btn,
  .play-btn {
    width: 30px;
    height: 24px;
    min-width: 30px;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
  }
  .progress-label { font-size: .72rem; }
  .controls-compact { gap: 4px; }
  .controls-compact .pill-btn {
    min-height: 25px;
    padding: 3px 4px;
    font-size: clamp(.48rem, 1.8vh, .62rem);
    white-space: normal;
    overflow: hidden;
  }
  .settings-panel {
    height: 54px;
    min-height: 54px;
    max-height: 54px;
    gap: 5px;
    padding: 5px;
  }
  .setting-block,
  .setting-block input,
  .setting-block select,
  .check-block {
    font-size: .62rem;
  }
}

/* Retrato ou ecrãs estreitos */
@media (orientation: portrait), (max-width: 699px) {
  .reader-view-main {
    padding: 8px 12px max(8px, env(safe-area-inset-bottom));
    gap: 7px;
  }
  .reader-line-focus {
    font-size: clamp(1.05rem, 5.2vw, 1.35rem) !important;
    line-height: 1.24;
  }
  .reader-word-focus {
    font-size: clamp(1rem, 5vw, 1.3rem) !important;
  }
  .reader-controls-panel { max-height: 52dvh; overflow: auto; }
  .controls-compact { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 6px; }
  .controls-compact .pill-btn { min-height: 38px; font-size: .78rem; }
  .settings-panel {
    height: auto;
    min-height: 0;
    max-height: none;
    grid-template-columns: 1fr;
    gap: 7px;
  }
  .check-block { justify-content: flex-start; white-space: normal; }
  .result-phrase-card { grid-template-columns: minmax(0, 1fr); gap: 8px; padding: 12px; }
  .result-listen-btn { justify-self: start; }
}

/* Integração com as classes calculadas no App.vue */
:global(.layout-landscape.layout-medium) .reader-view-main,
:global(.layout-landscape.layout-large) .reader-view-main {
  padding-inline: clamp(28px, 5vw, 56px);
}
:global(.layout-landscape.layout-medium) .reader-controls-panel,
:global(.layout-landscape.layout-large) .reader-controls-panel,
:global(.layout-landscape.layout-medium) .reader-text-panel,
:global(.layout-landscape.layout-large) .reader-text-panel {
  width: 100%;
  max-width: none;
}


/* ===== FIX FINAL — Samsung Tab S6 Lite / paisagem média =====
   Objetivos: texto sempre visível, botões compactos, comandos dentro do ecrã. */
.reader-view-shell {
  height: 100dvh !important;
  max-height: 100dvh !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
}

.reader-view-shell > :last-child {
  display: none !important;
}

.reader-view-main {
  height: 100% !important;
  min-height: 0 !important;
  grid-template-rows: minmax(0, 1fr) auto !important;
  overflow: hidden !important;
}

.reader-text-panel {
  min-height: 0 !important;
  height: 100% !important;
  align-self: stretch !important;
  display: grid !important;
  align-items: center !important;
  justify-items: stretch !important;
  overflow: hidden !important;
}

.reader-line-focus,
.reader-word-focus,
.reader-word-context {
  color: #172033 !important;
  opacity: 1 !important;
  visibility: visible !important;
  display: block !important;
  width: 100% !important;
  max-width: 100% !important;
  text-align: left !important;
  overflow: hidden !important;
}

.reader-line-focus {
  font-size: clamp(1rem, 2.05vw, 1.34rem) !important;
  line-height: 1.2 !important;
  font-weight: 800 !important;
  max-height: 100% !important;
}

.reader-controls-panel {
  width: 100% !important;
  min-height: 0 !important;
  max-height: none !important;
  overflow: hidden !important;
  align-self: end !important;
  gap: 4px !important;
}

.reader-control-stack {
  width: 100% !important;
  gap: 4px !important;
}

.transport-row {
  min-height: 20px !important;
  height: 20px !important;
  gap: 6px !important;
}

.round-btn,
.play-btn {
  width: 22px !important;
  height: 20px !important;
  min-width: 22px !important;
  min-height: 20px !important;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  padding: 0 !important;
  font-size: .9rem !important;
  line-height: 1 !important;
}

.progress-label {
  min-height: 13px !important;
  height: 13px !important;
  font-size: .68rem !important;
  line-height: 1 !important;
}

.progress-line {
  height: 3px !important;
  min-height: 3px !important;
}

.controls-compact {
  width: 100% !important;
  display: grid !important;
  grid-template-columns: repeat(10, minmax(0, 1fr)) !important;
  gap: 4px !important;
}

.controls-compact .pill-btn {
  width: 100% !important;
  min-width: 0 !important;
  min-height: 28px !important;
  height: 28px !important;
  padding: 3px 4px !important;
  border-radius: 999px !important;
  border: 1px solid #e5eaf2 !important;
  background: #f3f6fb !important;
  color: #344054 !important;
  box-shadow: 0 2px 6px rgba(15, 23, 42, .04) !important;
  display: grid !important;
  place-items: center !important;
  text-align: center !important;
  font-size: clamp(.52rem, 1.6vh, .68rem) !important;
  font-weight: 740 !important;
  line-height: .98 !important;
  white-space: normal !important;
  overflow: hidden !important;
  overflow-wrap: anywhere !important;
}

.controls-compact .pill-btn.active {
  background: #e8f0fe !important;
  color: #1a73e8 !important;
  border-color: #bcd4ff !important;
}

.settings-panel {
  width: 100% !important;
  height: 52px !important;
  min-height: 52px !important;
  max-height: 52px !important;
  grid-template-columns: 1fr 1fr 1.05fr !important;
  gap: 5px !important;
  padding: 5px 6px !important;
  border-radius: 12px !important;
  overflow: hidden !important;
}

.setting-block {
  gap: 2px !important;
  font-size: .58rem !important;
  line-height: 1 !important;
}

.setting-block label {
  font-size: .58rem !important;
  line-height: 1 !important;
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
}

.setting-block input,
.setting-block select {
  min-height: 22px !important;
  height: 22px !important;
  font-size: .58rem !important;
  padding: 2px 4px !important;
}

.check-block {
  min-height: 18px !important;
  font-size: .58rem !important;
  line-height: 1 !important;
}

@media (orientation: landscape) and (min-width: 700px) and (max-height: 460px) {
  .reader-view-main {
    padding: 2px 24px max(5px, env(safe-area-inset-bottom)) !important;
    gap: 4px !important;
  }

  .reader-text-panel {
    padding: 4px 8px !important;
  }

  .reader-line-focus {
    font-size: clamp(.98rem, 3.2vh, 1.18rem) !important;
    line-height: 1.18 !important;
  }

  .reader-controls-panel {
    gap: 3px !important;
  }

  .controls-compact {
    gap: 3px !important;
  }

  .controls-compact .pill-btn {
    height: 27px !important;
    min-height: 27px !important;
    padding: 3px 3px !important;
    font-size: clamp(.5rem, 1.55vh, .64rem) !important;
  }

  .settings-panel {
    height: 50px !important;
    min-height: 50px !important;
    max-height: 50px !important;
  }
}

@media (orientation: portrait), (max-width: 699px) {
  .reader-view-main {
    padding: 8px 12px max(8px, env(safe-area-inset-bottom)) !important;
  }

  .controls-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }

  .controls-compact .pill-btn {
    min-height: 36px !important;
    height: 36px !important;
    font-size: .76rem !important;
  }

  .settings-panel {
    height: auto !important;
    min-height: 0 !important;
    max-height: none !important;
    grid-template-columns: 1fr !important;
  }
}



/* ===== FIX HARMONIA FINAL — ReaderView isolado e responsivo =====
   Mantém a lógica do componente, mas aproxima visualmente este ecrã dos restantes:
   - header sem círculos nos ícones;
   - texto mais contido;
   - botões em pills harmonizados;
   - controlos com respiro em retrato e paisagem;
   - combo/select totalmente visível. */
.reader-view-screen,
.reader-view-shell,
.reader-view-main,
.reader-text-panel,
.reader-controls-panel,
.reader-control-stack,
.controls-compact,
.settings-panel {
  box-sizing: border-box !important;
}

.reader-view-screen {
  position: fixed !important;
  inset: 0 !important;
  width: 100vw !important;
  height: 100dvh !important;
  overflow: hidden !important;
  background: #fff !important;
  color: #172033 !important;
}

.reader-view-shell {
  width: 100% !important;
  height: 100% !important;
  min-height: 0 !important;
  display: grid !important;
  grid-template-rows: auto minmax(0, 1fr) !important;
  overflow: hidden !important;
}

.reader-view-shell > :last-child {
  display: none !important;
}

.reader-view-shell :deep(.google-home-header) {
  padding: max(8px, env(safe-area-inset-top)) clamp(18px, 4vw, 32px) 4px !important;
  align-items: flex-start !important;
}

.reader-view-shell :deep(.home-help-btn),
.reader-header-btn {
  width: 30px !important;
  height: 30px !important;
  min-width: 30px !important;
  padding: 5px !important;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  color: #172033 !important;
  display: grid !important;
  place-items: center !important;
}

.reader-view-shell :deep(.home-help-btn svg),
.reader-header-btn svg {
  width: 17px !important;
  height: 17px !important;
}

.reader-text-btn {
  font-size: 1.35rem !important;
  line-height: 1 !important;
  font-weight: 700 !important;
  padding: 0 !important;
}

.reader-view-main {
  width: 100% !important;
  height: 100% !important;
  min-height: 0 !important;
  display: grid !important;
  grid-template-rows: minmax(0, 1fr) auto !important;
  overflow: hidden !important;
  gap: clamp(8px, 1.8vh, 14px) !important;
  padding: clamp(6px, 1.6vh, 12px) clamp(18px, 4vw, 36px) max(14px, env(safe-area-inset-bottom)) !important;
}

.reader-text-panel {
  min-height: 0 !important;
  width: 100% !important;
  display: grid !important;
  align-items: center !important;
  justify-items: stretch !important;
  overflow: hidden !important;
  padding: clamp(8px, 2vh, 18px) clamp(4px, 1vw, 10px) !important;
}

.reader-line-focus,
.reader-word-focus,
.reader-word-context {
  width: 100% !important;
  max-width: 100% !important;
  color: #172033 !important;
  opacity: 1 !important;
  visibility: visible !important;
  overflow: hidden !important;
  text-align: left !important;
}

.reader-line-focus {
  font-size: clamp(1rem, 2.15vw, 1.32rem) !important;
  line-height: 1.22 !important;
  font-weight: 780 !important;
  overflow-wrap: break-word !important;
  word-break: normal !important;
  hyphens: none !important;
}

.reader-word-focus,
.reader-word-context {
  font-size: clamp(1rem, 2vw, 1.28rem) !important;
  line-height: 1.35 !important;
}

.reader-word-chip {
  display: inline !important;
  margin: 0 !important;
  padding: 0 .08em !important;
  border: 0 !important;
  border-radius: 4px !important;
  background: transparent !important;
  box-shadow: none !important;
}

.reader-controls-panel {
  width: 100% !important;
  min-height: 0 !important;
  max-height: 46dvh !important;
  display: grid !important;
  align-content: end !important;
  overflow: hidden !important;
  padding: 0 !important;
}

.reader-control-stack {
  width: 100% !important;
  display: grid !important;
  gap: 8px !important;
}

.transport-row {
  height: 22px !important;
  min-height: 22px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 10px !important;
}

.round-btn,
.play-btn {
  width: 24px !important;
  height: 22px !important;
  min-width: 24px !important;
  min-height: 22px !important;
  border: 0 !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  padding: 0 !important;
  display: grid !important;
  place-items: center !important;
  font-size: .88rem !important;
  line-height: 1 !important;
  color: #1a73e8 !important;
}

.round-btn:disabled {
  opacity: .28 !important;
}

.progress-label {
  min-height: 16px !important;
  height: 16px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  color: #667085 !important;
  font-size: .78rem !important;
  font-weight: 720 !important;
  line-height: 1 !important;
}

.progress-line {
  height: 3px !important;
  min-height: 3px !important;
  border-radius: 999px !important;
  background: #d8e6fb !important;
  overflow: hidden !important;
}

.progress-fill {
  height: 100% !important;
  border-radius: inherit !important;
  background: #1a73e8 !important;
}

.controls-compact {
  width: 100% !important;
  display: grid !important;
  align-items: stretch !important;
  gap: 7px !important;
}

.controls-compact .pill-btn,
.reader-controls-panel .pill-btn {
  width: 100% !important;
  min-width: 0 !important;
  min-height: 32px !important;
  height: 32px !important;
  padding: 6px 8px !important;
  border-radius: 999px !important;
  border: 1px solid #e5eaf2 !important;
  background: #f3f6fb !important;
  color: #344054 !important;
  box-shadow: 0 2px 8px rgba(15, 23, 42, .035) !important;
  display: grid !important;
  place-items: center !important;
  text-align: center !important;
  font-size: clamp(.68rem, 1.55vw, .82rem) !important;
  font-weight: 740 !important;
  line-height: 1.06 !important;
  white-space: normal !important;
  overflow: hidden !important;
  text-overflow: clip !important;
}

.controls-compact .pill-btn.active,
.reader-controls-panel .pill-btn.active {
  background: #e8f0fe !important;
  color: #1a73e8 !important;
  border-color: #c6dafc !important;
}

.settings-panel {
  width: 100% !important;
  min-width: 0 !important;
  display: grid !important;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) minmax(120px, 1.1fr) !important;
  gap: 10px !important;
  padding: 10px 12px !important;
  border: 1px solid #edf1f7 !important;
  border-radius: 14px !important;
  background: #f8fbff !important;
  overflow: visible !important;
}

.setting-block {
  min-width: 0 !important;
  display: grid !important;
  gap: 5px !important;
  color: #344054 !important;
}

.setting-block label {
  min-width: 0 !important;
  font-size: .72rem !important;
  line-height: 1.1 !important;
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
}

.setting-block input,
.setting-block select {
  width: 100% !important;
  min-width: 0 !important;
  min-height: 28px !important;
  height: 28px !important;
  font-size: .72rem !important;
}

.setting-block select {
  padding: 3px 26px 3px 8px !important;
  border-radius: 9px !important;
  border: 1px solid #dfe6ef !important;
  background: #fff !important;
}

.check-block {
  grid-column: 1 / -1 !important;
  min-height: 28px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  gap: 8px !important;
  color: #344054 !important;
  font-size: .75rem !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .reader-view-main {
    padding-inline: clamp(28px, 5vw, 56px) !important;
    padding-bottom: max(18px, env(safe-area-inset-bottom)) !important;
    gap: 10px !important;
  }

  .reader-text-panel,
  .reader-controls-panel,
  .reader-control-stack {
    width: min(100%, 1080px) !important;
    max-width: 1080px !important;
    justify-self: center !important;
  }

  .reader-text-panel {
    padding-block: clamp(10px, 3vh, 24px) !important;
  }

  .controls-compact {
    grid-template-columns: repeat(10, minmax(0, 1fr)) !important;
    gap: 6px !important;
  }
}

@media (orientation: landscape) and (min-width: 700px) and (max-height: 460px) {
  .reader-view-shell :deep(.google-home-header) {
    padding-top: 6px !important;
    padding-bottom: 2px !important;
  }

  .reader-view-main {
    padding-top: 4px !important;
    padding-bottom: max(12px, env(safe-area-inset-bottom)) !important;
    gap: 8px !important;
  }

  .reader-line-focus {
    font-size: clamp(.98rem, 3.2vh, 1.18rem) !important;
    line-height: 1.2 !important;
  }

  .reader-word-focus,
  .reader-word-context {
    font-size: clamp(.96rem, 3vh, 1.12rem) !important;
    line-height: 1.28 !important;
  }

  .reader-control-stack {
    gap: 6px !important;
  }

  .controls-compact .pill-btn,
  .reader-controls-panel .pill-btn {
    height: 30px !important;
    min-height: 30px !important;
    padding: 5px 6px !important;
    font-size: clamp(.58rem, 2vh, .72rem) !important;
  }

  .settings-panel {
    padding: 8px 10px !important;
    gap: 8px !important;
  }
}

@media (orientation: portrait), (max-width: 699px) {
  .reader-view-shell :deep(.google-home-header) {
    padding-inline: clamp(12px, 4vw, 22px) !important;
  }

  .reader-view-main {
    padding: 8px clamp(10px, 3vw, 16px) max(16px, env(safe-area-inset-bottom)) !important;
    gap: 12px !important;
  }

  .reader-text-panel {
    align-items: center !important;
    padding: 8px 2px !important;
  }

  .reader-line-focus {
    font-size: clamp(.94rem, 4.1vw, 1.12rem) !important;
    line-height: 1.22 !important;
  }

  .reader-word-focus,
  .reader-word-context {
    font-size: clamp(.92rem, 4vw, 1.08rem) !important;
    line-height: 1.3 !important;
  }

  .reader-controls-panel {
    max-height: 52dvh !important;
    overflow-y: auto !important;
    overflow-x: hidden !important;
    padding-bottom: 4px !important;
  }

  .reader-control-stack {
    gap: 9px !important;
  }

  .controls-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 7px !important;
  }

  .controls-compact .pill-btn,
  .reader-controls-panel .pill-btn {
    height: 34px !important;
    min-height: 34px !important;
    font-size: .74rem !important;
  }

  .settings-panel {
    grid-template-columns: 1fr !important;
    gap: 8px !important;
    padding: 10px !important;
    margin-top: 2px !important;
  }

  .setting-block label,
  .setting-block input,
  .setting-block select {
    font-size: .72rem !important;
  }

  .setting-block input,
  .setting-block select {
    min-height: 28px !important;
    height: 28px !important;
  }
}

/* ===== Tablet/paisagem: camada final deliberada =====
   Este bloco fica no fim de propósito: a vista tinha várias tentativas antigas
   para o mesmo layout. Aqui o ecrã é tratado como um painel horizontal curto:
   texto compacto em cima, comandos a ocupar a largura útil, botões em 2 linhas
   e definições sempre visíveis. */
@media (orientation: landscape) and (min-width: 700px) {
  .reader-view-shell {
    grid-template-rows: auto minmax(0, 1fr) !important;
  }

  .reader-view-shell :deep(.google-home-header) {
    padding: max(6px, env(safe-area-inset-top)) clamp(18px, 3vw, 28px) 2px !important;
  }

  .reader-view-shell :deep(.home-logo-mark) {
    width: clamp(24px, 3vw, 34px) !important;
    height: clamp(24px, 3vw, 34px) !important;
    min-width: clamp(24px, 3vw, 34px) !important;
  }

  .reader-view-shell :deep(.home-brand-title) {
    font-size: clamp(1.05rem, 2.35vw, 1.55rem) !important;
    line-height: 1 !important;
  }

  .reader-view-shell :deep(.home-brand-subtitle) {
    font-size: clamp(.58rem, 1.25vw, .78rem) !important;
    line-height: 1.1 !important;
  }

  .reader-header-actions {
    gap: 10px !important;
  }

  .reader-view-shell :deep(.home-help-btn),
  .reader-header-btn {
    width: 32px !important;
    height: 32px !important;
    min-width: 32px !important;
    min-height: 32px !important;
    padding: 4px !important;
  }

  .reader-view-shell :deep(.home-help-btn svg),
  .reader-header-btn svg {
    width: 22px !important;
    height: 22px !important;
    stroke-width: 2.4px !important;
  }

  .reader-text-btn {
    font-size: 1.45rem !important;
  }

  .reader-view-main {
    grid-template-rows: minmax(72px, 1fr) auto !important;
    gap: 6px !important;
    padding: 4px clamp(18px, 3.4vw, 32px) max(8px, env(safe-area-inset-bottom)) !important;
  }

  .reader-text-panel,
  .reader-controls-panel,
  .reader-control-stack {
    width: 100% !important;
    max-width: none !important;
    justify-self: stretch !important;
  }

  .reader-text-panel {
    align-items: center !important;
    padding: 4px clamp(18px, 5vw, 48px) !important;
  }

  .reader-line-focus {
    font-size: clamp(.86rem, 1.55vw, 1.08rem) !important;
    line-height: 1.18 !important;
    font-weight: 620 !important;
  }

  .reader-word-focus,
  .reader-word-context {
    font-size: clamp(.84rem, 1.5vw, 1.04rem) !important;
    line-height: 1.24 !important;
  }

  .reader-controls-panel {
    max-height: none !important;
    overflow: visible !important;
    align-self: end !important;
  }

  .reader-control-stack {
    gap: 4px !important;
  }

  .transport-row {
    height: 18px !important;
    min-height: 18px !important;
    gap: 8px !important;
  }

  .round-btn,
  .play-btn {
    width: 22px !important;
    height: 18px !important;
    min-width: 22px !important;
    min-height: 18px !important;
    font-size: .78rem !important;
  }

  .progress-label {
    height: 13px !important;
    min-height: 13px !important;
    font-size: .66rem !important;
  }

  .progress-line {
    height: 3px !important;
    min-height: 3px !important;
  }

  .controls-compact {
    grid-template-columns: repeat(5, minmax(0, 1fr)) !important;
    gap: 4px 6px !important;
  }

  .controls-compact .pill-btn,
  .reader-controls-panel .pill-btn {
    height: 24px !important;
    min-height: 24px !important;
    padding: 3px 5px !important;
    font-size: clamp(.54rem, 1.28vw, .66rem) !important;
    line-height: 1 !important;
  }

  .settings-panel {
    height: 48px !important;
    min-height: 48px !important;
    max-height: 48px !important;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) minmax(150px, 1.1fr) !important;
    gap: 8px !important;
    padding: 5px 8px !important;
    overflow: hidden !important;
  }

  .setting-block {
    gap: 2px !important;
    text-align: center !important;
  }

  .setting-block label {
    font-size: .62rem !important;
    line-height: 1 !important;
  }

  .setting-block input,
  .setting-block select {
    height: 21px !important;
    min-height: 21px !important;
    font-size: .62rem !important;
  }
}

/* ===== ReaderView harmonizado com os restantes ecrãs =====
   Última camada: normaliza header, ícones, espaçamentos e controlos em tablet
   horizontal sem mexer no fluxo nem na lógica de leitura. */
@media (orientation: landscape) and (min-width: 700px) {
  .reader-view-screen {
    background: #fff !important;
  }

  .reader-view-shell {
    grid-template-rows: auto minmax(0, 1fr) !important;
  }

  .reader-view-shell :deep(.google-home-header) {
    padding: max(10px, env(safe-area-inset-top)) clamp(26px, 4vw, 46px) 4px !important;
  }

  .reader-view-shell :deep(.home-brand) {
    gap: 10px !important;
  }

  .reader-view-shell :deep(.home-logo-mark) {
    width: 34px !important;
    height: 34px !important;
    min-width: 34px !important;
  }

  .reader-view-shell :deep(.home-brand-title) {
    font-size: 1.55rem !important;
    line-height: 1 !important;
  }

  .reader-view-shell :deep(.home-brand-subtitle) {
    margin-top: 3px !important;
    font-size: .78rem !important;
    line-height: 1.1 !important;
  }

  .reader-header-actions {
    align-items: center !important;
    gap: 12px !important;
  }

  .reader-header-actions .home-help-btn,
  .reader-header-actions .reader-header-btn {
    width: 34px !important;
    height: 34px !important;
    min-width: 34px !important;
    min-height: 34px !important;
    padding: 4px !important;
    border: 0 !important;
    border-radius: 0 !important;
    background: transparent !important;
    color: #172033 !important;
    box-shadow: none !important;
  }

  .reader-header-actions .reader-header-btn > svg,
  .reader-view-shell :deep(.home-help-btn > svg) {
    width: 24px !important;
    height: 24px !important;
    display: block !important;
    stroke-width: 2.35px !important;
  }

  .reader-view-main {
    grid-template-rows: minmax(96px, 1fr) auto !important;
    gap: 10px !important;
    padding: 2px clamp(28px, 3.8vw, 48px) max(38px, env(safe-area-inset-bottom)) !important;
  }

  .reader-text-panel {
    width: 100% !important;
    max-width: none !important;
    align-items: center !important;
    padding: 10px clamp(18px, 3.5vw, 42px) !important;
  }

  .reader-line-focus {
    font-size: clamp(.96rem, 1.55vw, 1.08rem) !important;
    line-height: 1.22 !important;
    font-weight: 620 !important;
  }

  .reader-controls-panel {
    width: 100% !important;
    max-width: none !important;
    justify-self: stretch !important;
    align-self: end !important;
    overflow: visible !important;
  }

  .reader-control-stack {
    width: 100% !important;
    max-width: none !important;
    gap: 6px !important;
    padding: 0 !important;
  }

  .transport-row {
    height: 22px !important;
    min-height: 22px !important;
    gap: 10px !important;
  }

  .round-btn,
  .play-btn {
    width: 24px !important;
    height: 22px !important;
    min-width: 24px !important;
    min-height: 22px !important;
    color: #aab4c3 !important;
    font-size: .9rem !important;
  }

  .play-btn {
    color: #1a73e8 !important;
  }

  .progress-label {
    height: 15px !important;
    min-height: 15px !important;
    font-size: .72rem !important;
    color: #667085 !important;
  }

  .progress-line {
    height: 4px !important;
    min-height: 4px !important;
  }

  .controls-compact {
    grid-template-columns: repeat(5, minmax(0, 1fr)) !important;
    gap: 6px !important;
    width: 100% !important;
  }

  .controls-compact .pill-btn,
  .reader-controls-panel .pill-btn {
    height: 29px !important;
    min-height: 29px !important;
    padding: 5px 8px !important;
    border-radius: 999px !important;
    border: 1px solid #e5eaf2 !important;
    background: #f3f6fb !important;
    color: #344054 !important;
    font-size: clamp(.6rem, 1.15vw, .72rem) !important;
    font-weight: 740 !important;
    line-height: 1.05 !important;
  }

  .controls-compact .pill-btn.active,
  .reader-controls-panel .pill-btn.active {
    background: #e8f0fe !important;
    color: #1a73e8 !important;
    border-color: #c6dafc !important;
  }

  .settings-panel {
    height: 58px !important;
    min-height: 58px !important;
    max-height: 58px !important;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) minmax(160px, 1.1fr) !important;
    gap: 10px !important;
    padding: 7px 10px !important;
    border: 1px solid #edf1f7 !important;
    border-radius: 12px !important;
    background: #f8fbff !important;
    overflow: hidden !important;
  }

  .setting-block {
    gap: 4px !important;
    text-align: center !important;
  }

  .setting-block label {
    font-size: .7rem !important;
    line-height: 1.05 !important;
    color: #344054 !important;
  }

  .setting-block input,
  .setting-block select {
    height: 24px !important;
    min-height: 24px !important;
    font-size: .68rem !important;
  }
}

/* ===== ReaderView paisagem: escala visual final =====
   O tablet em paisagem precisa de controlos suficientemente grandes para tocar,
   mas com o conjunto inteiro afastado da barra do sistema. */
@media (orientation: landscape) and (min-width: 700px) {
  .reader-view-shell :deep(.google-home-header) {
    padding: max(10px, env(safe-area-inset-top)) 28px 4px !important;
  }

  .reader-view-shell :deep(.home-help-btn),
  .reader-header-actions .reader-header-btn {
    width: 42px !important;
    height: 42px !important;
    min-width: 42px !important;
    min-height: 42px !important;
    padding: 6px !important;
    display: grid !important;
    place-items: center !important;
  }

  .reader-view-shell :deep(.home-help-btn > svg),
  .reader-header-actions .reader-header-btn > svg {
    width: 30px !important;
    height: 30px !important;
    stroke-width: 2.25px !important;
  }

  .reader-header-actions {
    gap: 10px !important;
  }

  .reader-view-main {
    grid-template-rows: minmax(72px, 1fr) auto !important;
    gap: 8px !important;
    padding: 0 32px max(36px, env(safe-area-inset-bottom)) !important;
  }

  .reader-text-panel {
    align-items: center !important;
    padding: 8px 24px 4px !important;
  }

  .reader-line-focus {
    font-size: clamp(1rem, 1.65vw, 1.14rem) !important;
    line-height: 1.22 !important;
    font-weight: 620 !important;
  }

  .reader-control-stack {
    gap: 7px !important;
    padding: 0 0 4px !important;
  }

  .transport-row {
    height: 44px !important;
    min-height: 44px !important;
    gap: 18px !important;
    align-items: flex-start !important;
  }

  .round-btn,
  .play-btn {
    width: 48px !important;
    height: 42px !important;
    min-width: 48px !important;
    min-height: 42px !important;
    font-size: 1.46rem !important;
  }

  .play-btn {
    font-size: 1.58rem !important;
  }

  .progress-label {
    height: 16px !important;
    min-height: 16px !important;
    font-size: .76rem !important;
  }

  .progress-line {
    height: 4px !important;
    min-height: 4px !important;
  }

  .controls-compact {
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 7px 8px !important;
  }

  .controls-compact:has(> :nth-child(10)) {
    grid-template-columns: repeat(5, minmax(0, 1fr)) !important;
  }

  .controls-compact .pill-btn,
  .reader-controls-panel .pill-btn {
    height: 36px !important;
    min-height: 36px !important;
    padding: 7px 10px !important;
    font-size: clamp(.7rem, 1.25vw, .82rem) !important;
    font-weight: 600 !important;
    line-height: 1.14 !important;
  }

  .settings-panel {
    height: 78px !important;
    min-height: 78px !important;
    max-height: 78px !important;
    padding: 10px 12px 12px !important;
    margin-bottom: 0 !important;
    gap: 12px !important;
    border-radius: 14px !important;
    overflow: hidden !important;
  }

  .setting-block {
    gap: 6px !important;
  }

  .setting-block label {
    font-size: .76rem !important;
  }

  .setting-block input,
  .setting-block select {
    height: 32px !important;
    min-height: 32px !important;
    font-size: .72rem !important;
  }
}

/* Área de toque do transporte em retrato: os ícones ficam iguais e tocáveis. */
@media (orientation: portrait), (max-width: 699px) {
  .reader-view-shell :deep(.google-home-header) {
    padding: max(14px, env(safe-area-inset-top)) clamp(18px, 5vw, 24px) 6px !important;
  }

  .reader-header-actions .home-help-btn,
  .reader-header-actions .reader-header-btn {
    width: 38px !important;
    height: 38px !important;
    min-width: 38px !important;
    min-height: 38px !important;
    padding: 7px !important;
    border: 0 !important;
    border-radius: 0 !important;
    background: transparent !important;
    box-shadow: none !important;
    color: #172033 !important;
    display: grid !important;
    place-items: center !important;
  }

  .reader-header-actions .home-help-btn > svg,
  .reader-header-actions .reader-header-btn > svg {
    width: 24px !important;
    height: 24px !important;
    display: block !important;
    stroke-width: 2.2px !important;
  }

  .transport-row {
    height: 52px !important;
    min-height: 52px !important;
    gap: 18px !important;
    align-items: center !important;
  }

  .round-btn,
  .play-btn {
    width: 52px !important;
    height: 46px !important;
    min-width: 52px !important;
    min-height: 46px !important;
    border: 0 !important;
    border-radius: 0 !important;
    background: transparent !important;
    box-shadow: none !important;
    padding: 0 !important;
    display: grid !important;
    place-items: center !important;
    font-size: 1.48rem !important;
    line-height: 1 !important;
  }

  .play-btn {
    color: #1a73e8 !important;
    font-size: 1.64rem !important;
  }

  .round-btn:disabled {
    opacity: .28 !important;
  }
}

/* Resultado da leitura por voz: cartões iguais aos restantes e texto legível. */
.reader-speech-result-panel {
  padding: 0 clamp(12px, 2vw, 18px) max(12px, env(safe-area-inset-bottom)) !important;
  gap: 10px !important;
}

.result-phrase-card {
  grid-template-columns: minmax(0, 1fr) auto !important;
  align-items: center !important;
  gap: 14px !important;
  padding: 14px 16px !important;
  border-radius: 14px !important;
  box-shadow: 0 5px 14px rgba(20, 35, 65, .06) !important;
}

.result-phrase-copy {
  gap: 6px !important;
  min-height: 0 !important;
  overflow: hidden !important;
}

.result-phrase-copy strong {
  color: #172033 !important;
  font-size: clamp(1rem, 2.05vw, 1.22rem) !important;
  font-weight: 400 !important;
  line-height: 1.24 !important;
  overflow: auto !important;
}

.result-listen-btn,
.result-feedback-listen-btn {
  min-height: 36px !important;
  border-radius: 999px !important;
  background: #f3f6fb !important;
  color: #344054 !important;
  border: 1px solid #e5eaf2 !important;
  box-shadow: none !important;
  font-size: .82rem !important;
  font-weight: 500 !important;
  line-height: 1.1 !important;
}

.result-listen-btn {
  min-width: 76px !important;
  padding: 8px 14px !important;
}

.result-feedback-listen-btn {
  justify-self: stretch !important;
  align-self: center !important;
  min-width: 136px !important;
  padding: 8px 14px !important;
}

@media (orientation: landscape) and (min-width: 700px) {
  .reader-speech-result-panel {
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    padding-inline: clamp(18px, 3vw, 34px) !important;
    padding-bottom: max(18px, env(safe-area-inset-bottom)) !important;
    gap: 10px !important;
  }

  .result-phrase-card {
    padding: 14px 18px !important;
  }

  .result-action-stack {
    width: 126px !important;
    min-width: 126px !important;
    grid-template-columns: 1fr !important;
    gap: 8px !important;
    align-self: center !important;
  }

  .result-phrase-copy strong {
    font-size: clamp(.92rem, 1.55vw, 1.04rem) !important;
    font-weight: 400 !important;
    line-height: 1.3 !important;
  }

  .result-listen-btn,
  .result-feedback-listen-btn {
    width: 126px !important;
    min-width: 126px !important;
    max-width: 126px !important;
    min-height: 32px !important;
    padding: 7px 9px !important;
    font-size: .7rem !important;
    font-weight: 500 !important;
    white-space: normal !important;
  }
}

/* Resultado por voz: botões em baixo no retrato para não estrangular o texto. */
@media (orientation: portrait), (max-width: 699px) {
  .reader-speech-result-panel {
    grid-template-rows: repeat(2, minmax(0, 1fr)) !important;
    gap: 10px !important;
  }

  .result-phrase-card {
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) auto !important;
    align-items: stretch !important;
    gap: 10px !important;
  }

  .result-action-stack {
    width: 100% !important;
    min-width: 0 !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    align-self: end !important;
  }

  .result-action-stack .result-feedback-listen-btn:only-child,
  .result-action-stack .result-listen-btn:only-child {
    grid-column: 1 / -1 !important;
  }

  .result-listen-btn,
  .result-feedback-listen-btn {
    width: 100% !important;
    min-width: 0 !important;
    max-width: none !important;
    min-height: 34px !important;
    padding: 8px 10px !important;
    white-space: normal !important;
  }
}

/* Caixas de leitura: texto regular, sem bold, para preservar legibilidade. */
.reader-line-focus,
.reader-word-focus,
.reader-word-context,
.reader-word-chip,
.result-phrase-copy strong,
.speech-summary-copy strong {
  font-weight: 400 !important;
}

/* Botões alinhados com o sistema visual comum. */
.controls-compact .pill-btn,
.reader-controls-panel .pill-btn,
.result-listen-btn,
.result-feedback-listen-btn,
.listen-btn {
  background: #f3f6fb !important;
  color: #344054 !important;
  border-color: #e5eaf2 !important;
  box-shadow: none !important;
  font-weight: 500 !important;
}

.controls-compact .pill-btn.active,
.reader-controls-panel .pill-btn.active {
  background: #e8f0fe !important;
  color: #1a73e8 !important;
  border-color: #c6dafc !important;
}

.controls-compact .pill-btn:disabled,
.reader-controls-panel .pill-btn:disabled,
.result-listen-btn:disabled,
.result-feedback-listen-btn:disabled {
  background: #f6f8fb !important;
  color: #a4acb8 !important;
  border-color: #f6f8fb !important;
  opacity: 1 !important;
}

.prev-btn,
.next-btn,
.round-btn {
  color: #a4acb8 !important;
}

.play-btn {
  color: #1a73e8 !important;
}

/* Afinação final das caixas de frases: texto normal e ações neutras. */
.reader-speech-result-panel .result-phrase-copy strong,
.reader-controls-panel .speech-summary-copy strong,
.reader-text-panel .reader-line-focus,
.reader-text-panel .reader-word-focus,
.reader-text-panel .reader-word-context,
.reader-text-panel .reader-word-chip {
  font-weight: 400 !important;
  letter-spacing: 0 !important;
}

.reader-speech-result-panel .result-listen-btn,
.reader-speech-result-panel .result-feedback-listen-btn,
.reader-controls-panel .listen-btn {
  background: #f3f6fb !important;
  color: #344054 !important;
  border: 1px solid #e5eaf2 !important;
  box-shadow: none !important;
  font-weight: 500 !important;
}

.reader-word-chip.active,
.reader-text-panel .reader-word-chip.active {
  background: var(--active-word-bg) !important;
  color: var(--active-word-text) !important;
  border-radius: 5px !important;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, .18) !important;
  padding: 0 .12em !important;
}

/* Xiaomi/telefones compactos: não herdar layout de tablet. */
@media (orientation: portrait) and (max-width: 480px) {
  .reader-view-shell :deep(.google-home-header) {
    padding: max(10px, env(safe-area-inset-top)) 16px 4px !important;
  }

  .reader-view-main {
    grid-template-rows: minmax(72px, 1fr) auto !important;
    gap: 6px !important;
    padding: 4px 10px max(8px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
  }

  .reader-text-panel {
    min-height: 0 !important;
    padding: 4px 0 !important;
    align-items: center !important;
    overflow: hidden !important;
  }

  .reader-line-focus {
    font-size: clamp(.92rem, 4vw, 1.05rem) !important;
    line-height: 1.24 !important;
    font-weight: 400 !important;
  }

  .reader-controls-panel {
    max-height: none !important;
    overflow: visible !important;
    align-self: end !important;
  }

  .reader-control-stack {
    gap: 4px !important;
  }

  .transport-row {
    height: 42px !important;
    min-height: 42px !important;
    gap: 16px !important;
    align-items: center !important;
  }

  .round-btn,
  .play-btn {
    width: 44px !important;
    height: 38px !important;
    min-width: 44px !important;
    min-height: 38px !important;
    font-size: 1.32rem !important;
  }

  .play-btn {
    font-size: 1.46rem !important;
  }

  .progress-label {
    height: 14px !important;
    min-height: 14px !important;
    font-size: .7rem !important;
  }

  .progress-line {
    height: 3px !important;
    min-height: 3px !important;
  }

  .controls-compact {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 4px !important;
  }

  .controls-compact .pill-btn,
  .reader-controls-panel .pill-btn {
    height: 27px !important;
    min-height: 27px !important;
    padding: 3px 7px !important;
    font-size: .68rem !important;
    line-height: 1 !important;
  }

  .settings-panel {
    height: 58px !important;
    min-height: 58px !important;
    max-height: 58px !important;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) minmax(112px, 1.05fr) !important;
    gap: 6px !important;
    padding: 6px 8px !important;
    overflow: hidden !important;
  }

  .setting-block {
    gap: 3px !important;
    text-align: center !important;
  }

  .setting-block label {
    font-size: .62rem !important;
    line-height: 1 !important;
  }

  .setting-block input,
  .setting-block select {
    height: 22px !important;
    min-height: 22px !important;
    font-size: .62rem !important;
  }

  .check-block {
    display: none !important;
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .reader-view-shell :deep(.google-home-header) {
    padding: max(4px, env(safe-area-inset-top)) 28px 0 !important;
  }

  .reader-view-shell :deep(.home-logo-mark) {
    width: 28px !important;
    height: 28px !important;
    min-width: 28px !important;
  }

  .reader-view-shell :deep(.home-brand-title) {
    font-size: 1.25rem !important;
    line-height: 1 !important;
  }

  .reader-view-shell :deep(.home-brand-subtitle) {
    font-size: .66rem !important;
    line-height: 1 !important;
  }

  .reader-header-actions .home-help-btn,
  .reader-header-actions .reader-header-btn,
  .reader-view-shell :deep(.home-help-btn) {
    width: 32px !important;
    height: 32px !important;
    min-width: 32px !important;
    min-height: 32px !important;
    padding: 5px !important;
  }

  .reader-header-actions .home-help-btn > svg,
  .reader-header-actions .reader-header-btn > svg,
  .reader-view-shell :deep(.home-help-btn > svg) {
    width: 22px !important;
    height: 22px !important;
  }

  .reader-view-main {
    grid-template-rows: minmax(46px, 1fr) auto !important;
    gap: 3px !important;
    padding: 0 28px max(5px, env(safe-area-inset-bottom)) !important;
    overflow: hidden !important;
  }

  .reader-text-panel {
    padding: 0 18px !important;
    align-items: center !important;
    overflow: hidden !important;
  }

  .reader-line-focus {
    font-size: clamp(.78rem, 3.2vh, .96rem) !important;
    line-height: 1.18 !important;
    font-weight: 400 !important;
  }

  .reader-controls-panel {
    max-height: none !important;
    overflow: visible !important;
    align-self: end !important;
  }

  .reader-control-stack {
    gap: 3px !important;
    padding: 0 !important;
  }

  .transport-row {
    height: 28px !important;
    min-height: 28px !important;
    gap: 14px !important;
    align-items: center !important;
  }

  .round-btn,
  .play-btn {
    width: 38px !important;
    height: 28px !important;
    min-width: 38px !important;
    min-height: 28px !important;
    font-size: 1.08rem !important;
  }

  .play-btn {
    font-size: 1.22rem !important;
  }

  .progress-label {
    height: 12px !important;
    min-height: 12px !important;
    font-size: .62rem !important;
  }

  .progress-line {
    height: 3px !important;
    min-height: 3px !important;
  }

  .controls-compact {
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 4px 6px !important;
  }

  .controls-compact .pill-btn,
  .reader-controls-panel .pill-btn {
    height: 25px !important;
    min-height: 25px !important;
    padding: 3px 6px !important;
    font-size: .62rem !important;
    line-height: 1 !important;
  }

  .settings-panel {
    height: 54px !important;
    min-height: 54px !important;
    max-height: 54px !important;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) minmax(150px, 1.05fr) !important;
    gap: 8px !important;
    padding: 5px 8px !important;
    overflow: hidden !important;
  }

  .setting-block {
    gap: 3px !important;
    text-align: center !important;
  }

  .setting-block label {
    font-size: .62rem !important;
    line-height: 1 !important;
  }

  .setting-block input,
  .setting-block select {
    height: 22px !important;
    min-height: 22px !important;
    font-size: .62rem !important;
  }
}

</style>
