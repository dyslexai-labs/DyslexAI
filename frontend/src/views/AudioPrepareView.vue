<template>
  <section class="flow-view audio-prepare-view">
    <div class="flow-shell audio-prepare-shell">
      <AppHeader subtitle="Leitura a partir da fala" @home="$emit('go-home')">
        <template #actions>
          <div class="audio-header-actions">
            <div class="audio-header-copy">
              <strong>Prepara a leitura com a tua voz</strong>
              <span>
                {{ isRecording
                  ? 'A gravação está a decorrer.'
                  : hasRecordedAudio
                    ? 'A gravação está pronta.'
                    : 'Gera uma frase. Depois grava a tua leitura.' }}
              </span>
            </div>
            <button class="home-help-btn audio-header-home" @click="$emit('go-home')" title="Início" aria-label="Início">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6h-4v6H5a1 1 0 0 1-1-1z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </template>
      </AppHeader>

      <div class="flow-main audio-prepare-main">
        <section class="flow-intro speech-intro-panel">
          <h1>Prepara a leitura com a tua voz</h1>
          <p>
            {{ isRecording
              ? 'A gravação está a decorrer. Quando acabares, carrega em Parar.'
              : hasRecordedAudio
                ? 'A gravação está pronta. Podes ouvir e depois processar.'
                : 'Gera uma frase. Depois grava a tua leitura.' }}
          </p>
        </section>

        <section class="speech-work-panel" aria-label="Preparar leitura por voz">
          <div class="speech-config-card">
            <div class="speech-config-grid">
              <label>Idade
                <select :value="readingAgeGroup" @change="$emit('update:readingAgeGroup', $event.target.value)">
                  <option value="6-7">6-7</option>
                  <option value="8-10">8-10</option>
                  <option value="11-13">11-13</option>
                </select>
              </label>
              <label>Nível
                <select :value="readingLevel" @change="$emit('update:readingLevel', $event.target.value)">
                  <option value="1">1</option>
                  <option value="2">2</option>
                  <option value="3">3</option>
                </select>
              </label>
              <label>Tipo
                <select :value="readingType" @change="$emit('update:readingType', $event.target.value)">
                  <option value="simple_sentence">Frase simples</option>
                  <option value="rhyme">Lengalenga</option>
                  <option value="tongue_twister">Trava-línguas</option>
                </select>
              </label>
            </div>

            <div class="speech-button-row">
              <button class="soft-action" @click="$emit('generate-phrase')"
                :disabled="isGeneratingPhrase || isRecording || isRecorderBusy">
                {{ isGeneratingPhrase ? 'A gerar...' : 'Gerar frase' }}
              </button>
              <button v-if="expectedReadingText" class="soft-action" @click="$emit('generate-phrase')"
                :disabled="isGeneratingPhrase || isRecording || isRecorderBusy">
                Nova frase
              </button>
            </div>
          </div>

          <div class="speech-phrase-card">
            <span class="speech-label">{{ isGeneratingPhrase ? 'Preparação' : 'Frase a ler' }}</span>
            <strong>{{ isGeneratingPhrase ? 'A gerar frase...' : (displayReadingText || 'Gera uma frase para começar.') }}</strong>
            <button
              v-if="expectedReadingText"
              class="soft-action syllable-toggle"
              :class="{ active: showSyllables }"
              @click="$emit('update:showSyllables', !showSyllables)"
            >
              Sílabas
            </button>
          </div>

          <div class="speech-record-card">
            <div class="speech-record-icon" :class="{ active: isRecording }" aria-hidden="true">
              <svg viewBox="0 0 96 96" role="img" focusable="false">
                <rect x="6" y="8" width="84" height="80" rx="18" fill="#e5f6e8"/>
                <rect x="37" y="18" width="22" height="42" rx="11" fill="#34a853"/>
                <path d="M26 44c0 14 9 25 22 25s22-11 22-25" fill="none" stroke="#34a853" stroke-width="7" stroke-linecap="round"/>
                <path d="M48 69v13M34 82h28" stroke="#34a853" stroke-width="7" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="speech-record-copy">
              <div class="recording-status-line">
                <span class="status-pill" :class="{ live: isRecording, ready: hasRecordedAudio && !isRecording }">
                  {{ isRecording ? 'A gravar' : (hasRecordedAudio ? 'Gravação pronta' : 'À espera de gravação') }}
                </span>
                <span class="recording-timer" :class="{ live: isRecording }">{{ recordingElapsedLabel }}</span>
              </div>
              <span v-if="selectedAudioName" class="status-file">{{ selectedAudioName }}</span>
              <audio v-if="audioPreviewUrl" :src="audioPreviewUrl" controls class="audio-player"></audio>
            </div>
          </div>

          <div class="record-controls speech-record-controls">
            <button class="soft-action speech-inline-generate" @click="$emit('generate-phrase')"
              :disabled="isGeneratingPhrase || isRecording || isRecorderBusy">
              {{ isGeneratingPhrase ? 'A gerar...' : 'Gerar frase' }}
            </button>
            <button class="soft-action speech-inline-generate" @click="$emit('generate-phrase')"
              :disabled="isGeneratingPhrase || isRecording || isRecorderBusy || !expectedReadingText">
              Nova frase
            </button>
            <button class="main-action" @click="$emit('start-recording')"
              :disabled="isRecording || isRecorderBusy || !expectedReadingText || isGeneratingPhrase">
              {{ isRecording ? 'A gravar...' : 'Gravar' }}
            </button>
            <button class="soft-action" @click="$emit('stop-recording')" :disabled="!isRecording">Parar</button>
            <button class="soft-action" @click="$emit('clear-recorded-audio')"
              :disabled="isRecording || !hasRecordedAudio">Limpar</button>
            <button class="soft-action" @click="$emit('process-audio')"
              :disabled="isRecording || !hasRecordedAudio || !expectedReadingText">Processar</button>
          </div>
        </section>
      </div>

      <BottomNav @home="$emit('go-home')" />
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import AppHeader from '../components/common/AppHeader.vue'
import BottomNav from '../components/common/BottomNav.vue'

const props = defineProps({
  audioPreviewUrl: {
    type: String,
    default: '',
  },
  expectedReadingText: {
    type: String,
    default: '',
  },
  hasRecordedAudio: {
    type: Boolean,
    default: false,
  },
  isGeneratingPhrase: {
    type: Boolean,
    default: false,
  },
  isRecorderBusy: {
    type: Boolean,
    default: false,
  },
  isRecording: {
    type: Boolean,
    default: false,
  },
  recordingElapsedLabel: {
    type: String,
    default: '0:00',
  },
  readingAgeGroup: {
    type: String,
    required: true,
  },
  readingLevel: {
    type: String,
    required: true,
  },
  readingType: {
    type: String,
    required: true,
  },
  showSyllables: {
    type: Boolean,
    default: false,
  },
  selectedAudioName: {
    type: String,
    default: '',
  },
})

const displayReadingText = computed(() => (
  props.showSyllables ? syllabifyText(props.expectedReadingText) : props.expectedReadingText
))

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
    current += char

    if (index === chars.length - 1) {
      chunks.push(current)
      break
    }

    const isVowel = vowels.includes(char)
    const nextIsVowel = vowels.includes(next)
    const nextAfterIsVowel = vowels.includes(nextAfter)
    const keepDiphthong = isVowel && nextIsVowel && /[iuoãeõáéíóúâêôãõ]/i.test(next)

    if (isVowel && !keepDiphthong && next && !nextIsVowel && nextAfterIsVowel) {
      chunks.push(current)
      current = ''
    }
  }

  const result = chunks.filter(Boolean).join('-')
  return result.includes('-') ? `${prefix}${result}${suffix}` : value
}

defineEmits([
  'clear-recorded-audio',
  'generate-phrase',
  'go-home',
  'process-audio',
  'start-recording',
  'stop-recording',
  'update:readingAgeGroup',
  'update:readingLevel',
  'update:readingType',
  'update:showSyllables',
])
</script>

<style scoped>
.flow-view,
.flow-view * {
  box-sizing: border-box;
}

.flow-view {
  position: fixed;
  inset: 0;
  z-index: 1000;
  width: 100vw;
  height: 100dvh;
  overflow: hidden;
  background: #fff;
  color: #172033;
}

.flow-shell {
  position: absolute;
  inset: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.flow-main {
  min-width: 0;
  min-height: 0;
  overflow: hidden;
}

.flow-intro {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.flow-intro h1 {
  max-width: 15ch;
  margin: 0;
  color: #13213d;
  font-size: clamp(22px, 5vw, 34px);
  font-weight: 830;
  line-height: 1.08;
  letter-spacing: 0;
}

.flow-intro p {
  max-width: 34ch;
  margin: 0;
  color: #667085;
  font-size: clamp(12px, 2.4vw, 16px);
  line-height: 1.28;
}

.speech-work-panel {
  min-width: 0;
  min-height: 0;
}

.speech-config-card,
.speech-phrase-card,
.speech-record-card {
  min-width: 0;
  border: 1px solid #edf1f7;
  border-radius: 14px;
  background: rgba(255, 255, 255, .96);
  box-shadow: 0 8px 24px rgba(20, 35, 65, .07);
}

.speech-config-card {
  padding: 10px;
}

.speech-config-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.speech-config-grid label {
  min-width: 0;
  color: #59657a;
  font-size: 12px;
  font-weight: 800;
  line-height: 1.1;
}

.speech-config-grid label:nth-child(3) {
  grid-column: 1 / -1;
}

.speech-config-grid select {
  width: 100%;
  min-height: 34px;
  margin-top: 4px;
  border: 1px solid #dde6f3;
  border-radius: 9px;
  background: #f8fbff;
  color: #172033;
  font-size: 13px;
  font-weight: 760;
}

.speech-button-row,
.speech-record-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.speech-button-row {
  margin-top: 8px;
}

.soft-action,
.main-action {
  min-height: 34px;
  border: 0;
  border-radius: 999px;
  padding: 7px 13px;
  font-size: 13px;
  font-weight: 820;
  line-height: 1;
  box-shadow: 0 6px 14px rgba(20, 35, 65, .08);
}

.soft-action {
  background: #fff;
  color: #1a73e8;
}

.main-action {
  background: #1a73e8;
  color: #fff;
}

.soft-action:disabled,
.main-action:disabled {
  opacity: .42;
  box-shadow: none;
}

.speech-phrase-card {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  gap: 7px;
  padding: 12px;
  overflow: hidden;
}

.speech-label {
  color: #68758d;
  font-size: 11px;
  font-weight: 900;
  letter-spacing: .08em;
  text-transform: uppercase;
}

.speech-phrase-card strong {
  min-height: 0;
  overflow: auto;
  color: #14213d;
  font-size: clamp(18px, 4.4vw, 25px);
  font-weight: 400;
  line-height: 1.16;
}

.syllable-toggle {
  justify-self: start;
  min-height: 30px;
  padding: 6px 12px;
  font-size: 12px;
}

.syllable-toggle.active {
  background: #e8f0fe;
  color: #1a73e8;
  border-color: #bcd4ff;
}

.speech-record-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  padding: 10px;
}

.speech-record-icon {
  width: 46px;
  height: 46px;
}

.speech-record-icon svg {
  display: block;
  width: 100%;
  height: 100%;
}

.speech-record-copy {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.recording-status-line {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  flex-wrap: wrap;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  border-radius: 999px;
  background: #eaf2ff;
  color: #1a73e8;
  padding: 5px 12px;
  font-size: 13px;
  font-weight: 860;
  line-height: 1.05;
}

.status-pill.live {
  background: #fff1f2;
  color: #dc2626;
}

.status-pill.ready {
  background: #e5f6e8;
  color: #188038;
}

.recording-timer {
  min-width: 52px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #dbe5f3;
  background: #f8fbff;
  color: #172033;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  text-align: center;
  line-height: 1;
}

.recording-timer.live {
  border-color: #fecaca;
  background: #fff1f2;
  color: #dc2626;
}

.status-file {
  overflow: hidden;
  color: #667085;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.audio-player {
  width: 100%;
  height: 32px;
}

.speech-inline-generate {
  display: none;
}

@media (orientation: portrait) {
  .flow-main {
    display: grid;
    grid-template-rows: auto minmax(0, 1fr);
    gap: 10px;
    padding: 8px 18px max(10px, env(safe-area-inset-bottom));
  }

  .speech-work-panel {
    display: grid;
    grid-template-rows: auto minmax(120px, 1fr) auto auto;
    gap: 10px;
    overflow: hidden;
  }

  .speech-record-controls {
    justify-content: center;
  }
}

@media (orientation: landscape) {
  .flow-main {
    display: grid;
    grid-template-columns: minmax(140px, 230px) minmax(0, 1fr);
    grid-template-rows: minmax(0, 1fr);
    align-items: center;
    gap: clamp(14px, 4vw, 44px);
    padding: 4px clamp(14px, 4vw, 34px) max(8px, env(safe-area-inset-bottom));
  }

  .flow-intro h1 {
    font-size: clamp(18px, 4.2vh, 28px);
  }

  .flow-intro p {
    font-size: clamp(11px, 2vh, 13px);
  }

  .speech-work-panel {
    align-self: center;
    display: grid;
    grid-template-columns: minmax(150px, .9fr) minmax(190px, 1.1fr);
    grid-template-rows: minmax(0, 1fr) auto auto;
    grid-template-areas:
      "config phrase"
      "record phrase"
      "controls controls";
    gap: 8px;
    width: 100%;
    height: min(100%, 260px);
    overflow: hidden;
  }

  .speech-config-card { grid-area: config; }
  .speech-phrase-card { grid-area: phrase; }
  .speech-record-card { grid-area: record; }
  .speech-record-controls { grid-area: controls; }

  .speech-record-controls {
    justify-content: center;
    overflow: hidden;
  }
}

/* Samsung Tab S6 Lite e tablets médios em paisagem: o título passa para cima,
   e os controlos ficam numa grelha compacta para não rebentar o ecrã. */
@media (orientation: landscape) and (max-height: 430px) and (max-width: 900px) {
  .flow-main {
    grid-template-columns: 1fr;
    grid-template-rows: auto minmax(0, 1fr);
    align-items: stretch;
    justify-content: stretch;
    gap: 8px;
    padding: 0 clamp(18px, 5vw, 32px) max(8px, env(safe-area-inset-bottom));
  }

  .flow-intro {
    align-self: end;
    gap: 3px;
  }

  .flow-intro h1 {
    max-width: none;
    font-size: clamp(20px, 5.2vh, 26px);
    line-height: 1.04;
  }

  .flow-intro p {
    max-width: none;
    font-size: clamp(10px, 2.4vh, 12px);
    line-height: 1.15;
  }

  .speech-work-panel {
    align-self: start;
    grid-template-columns: minmax(152px, 190px) minmax(0, 1fr);
    grid-template-rows: auto minmax(58px, 1fr) auto;
    grid-template-areas:
      "config phrase"
      "record phrase"
      "controls controls";
    gap: 7px;
    height: 100%;
    max-height: 220px;
  }

  .speech-config-card,
  .speech-record-card,
  .speech-phrase-card {
    border-radius: 11px;
    box-shadow: 0 5px 14px rgba(20, 35, 65, .06);
  }

  .speech-config-card {
    padding: 7px;
  }

  .speech-config-grid {
    gap: 5px;
  }

  .speech-config-grid label {
    font-size: 10px;
  }

  .speech-config-grid select {
    min-height: 28px;
    margin-top: 2px;
    border-radius: 7px;
    font-size: 11px;
  }

  .speech-button-row {
    display: none;
  }

  .speech-phrase-card {
    padding: 9px;
    gap: 5px;
  }

  .speech-label {
    font-size: 9px;
  }

  .speech-phrase-card strong {
    font-size: clamp(17px, 5vh, 22px);
    line-height: 1.12;
  }

  .syllable-toggle {
    min-height: 24px;
    padding: 5px 10px;
    font-size: 11px;
  }

  .speech-record-card {
    gap: 7px;
    padding: 7px;
  }

  .speech-record-icon {
    width: 36px;
    height: 36px;
  }

  .status-pill {
    min-height: 26px;
    padding: 5px 10px;
    font-size: 11px;
  }

  .recording-timer {
    min-width: 45px;
    padding: 5px 8px;
    font-size: 12px;
  }

  .speech-record-controls {
    flex-wrap: nowrap;
    justify-content: center;
    gap: 6px;
  }

  .speech-inline-generate {
    display: inline-flex;
  }

  .soft-action,
  .main-action {
    min-height: 28px;
    padding: 6px 10px;
    font-size: 11px;
  }
}

@media (orientation: landscape) and (max-width: 560px) {
  .flow-main {
    padding-inline: 12px;
  }

  .flow-intro p {
    display: none;
  }

  .speech-work-panel {
    grid-template-columns: 132px minmax(0, 1fr);
  }

  .speech-record-controls {
    gap: 4px;
  }

  .soft-action,
  .main-action {
    padding-inline: 8px;
    font-size: 10px;
  }
}

/* ===== Audio prepare: paisagem equilibrada ===== */
.audio-header-actions {
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  gap: 12px;
  min-width: 0;
  margin-left: auto;
}

.audio-header-copy {
  display: none;
  min-width: 0;
  text-align: right;
}

.audio-header-copy strong {
  display: block;
  color: #13213d;
  font-size: 1rem;
  font-weight: 760;
  line-height: 1.08;
}

.audio-header-copy span {
  display: block;
  margin-top: 3px;
  color: #667085;
  font-size: .72rem;
  line-height: 1.15;
}

.audio-header-home {
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

.audio-header-home svg {
  width: 24px !important;
  height: 24px !important;
  display: block !important;
}

@media (orientation: landscape) {
  .audio-prepare-shell :deep(.google-home-header) {
    padding: max(8px, env(safe-area-inset-top)) clamp(20px, 4vw, 34px) 4px !important;
  }

  .audio-header-copy {
    display: block;
    max-width: min(46vw, 440px);
  }

  .audio-prepare-main {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) !important;
    grid-template-rows: minmax(0, 1fr) !important;
    align-items: stretch !important;
    gap: 0 !important;
    padding: 4px clamp(20px, 4vw, 36px) max(18px, env(safe-area-inset-bottom)) !important;
  }

  .audio-prepare-main > .flow-intro {
    display: none !important;
  }

  .speech-work-panel {
    align-self: stretch !important;
    display: grid !important;
    grid-template-columns: minmax(260px, .9fr) minmax(360px, 1.35fr) !important;
    grid-template-rows: auto minmax(72px, 1fr) auto !important;
    grid-template-areas:
      "config phrase"
      "record phrase"
      "controls controls" !important;
    gap: 10px !important;
    width: 100% !important;
    height: 100% !important;
    max-height: none !important;
    overflow: hidden !important;
  }

  .speech-config-card,
  .speech-phrase-card,
  .speech-record-card {
    border-radius: 14px !important;
    box-shadow: 0 6px 16px rgba(20, 35, 65, .06) !important;
  }

  .speech-config-card {
    padding: 10px !important;
  }

  .speech-config-grid {
    grid-template-columns: 1fr 1fr 1.15fr !important;
    gap: 8px !important;
  }

  .speech-config-grid label:nth-child(3) {
    grid-column: auto !important;
  }

  .speech-config-grid label {
    font-size: .72rem !important;
    font-weight: 700 !important;
  }

  .speech-config-grid select {
    min-height: 34px !important;
    font-size: .82rem !important;
    font-weight: 620 !important;
  }

  .speech-button-row {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    margin-top: 10px !important;
  }

  .speech-phrase-card {
    padding: 12px 14px !important;
    gap: 8px !important;
  }

  .speech-phrase-card strong {
    font-size: clamp(1rem, 2.4vw, 1.32rem) !important;
    font-weight: 400 !important;
    line-height: 1.22 !important;
  }

  .speech-record-card {
    padding: 10px 12px !important;
  }

  .speech-record-controls {
    display: grid !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
    gap: 9px !important;
    justify-content: stretch !important;
    overflow: visible !important;
  }

  .speech-record-controls .speech-inline-generate {
    display: none !important;
  }

  .soft-action,
  .main-action {
    min-height: 38px !important;
    padding: 8px 12px !important;
    font-size: .82rem !important;
    font-weight: 650 !important;
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .audio-header-copy strong {
    font-size: .9rem;
  }

  .audio-header-copy span {
    font-size: .66rem;
  }

  .audio-prepare-main {
    padding-bottom: max(12px, env(safe-area-inset-bottom)) !important;
  }

  .speech-work-panel {
    grid-template-rows: auto minmax(56px, 1fr) auto !important;
    gap: 8px !important;
  }

  .speech-phrase-card strong {
    font-size: clamp(.9rem, 3.6vh, 1.08rem) !important;
  }

  .soft-action,
  .main-action {
    min-height: 34px !important;
    font-size: .76rem !important;
  }
}

/* ===== Audio prepare: frase em largura total, controlos em baixo ===== */
@media (orientation: landscape) {
  .audio-header-copy {
    max-width: min(58vw, 520px) !important;
  }

  .audio-header-copy strong {
    white-space: nowrap !important;
    font-size: clamp(.82rem, 1.85vw, 1rem) !important;
    line-height: 1.05 !important;
  }

  .audio-header-copy span {
    white-space: nowrap !important;
    font-size: clamp(.58rem, 1.25vw, .72rem) !important;
  }

  .audio-prepare-main {
    padding: 4px clamp(18px, 3.2vw, 30px) max(14px, env(safe-area-inset-bottom)) !important;
  }

  .speech-work-panel {
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) !important;
    grid-template-rows: minmax(108px, 1fr) auto auto !important;
    grid-template-areas:
      "phrase phrase"
      "config record"
      "controls controls" !important;
    gap: 8px !important;
    height: 100% !important;
    max-height: none !important;
  }

  .speech-phrase-card {
    grid-area: phrase !important;
    width: 100% !important;
    min-width: 0 !important;
    padding: 12px 14px !important;
  }

  .speech-phrase-card strong {
    font-size: clamp(1rem, 2.2vw, 1.22rem) !important;
    font-weight: 400 !important;
    line-height: 1.22 !important;
    overflow: auto !important;
  }

  .speech-config-card {
    grid-area: config !important;
    align-self: stretch !important;
    display: grid !important;
    grid-template-rows: auto auto !important;
    align-content: center !important;
    gap: 8px !important;
  }

  .speech-config-grid {
    grid-template-columns: .75fr .75fr 1.35fr !important;
    gap: 7px !important;
  }

  .speech-button-row {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 8px !important;
    margin-top: 0 !important;
  }

  .speech-record-card {
    grid-area: record !important;
    align-self: stretch !important;
  }

  .speech-record-controls {
    grid-area: controls !important;
    grid-template-columns: repeat(4, minmax(0, 1fr)) !important;
  }
}

@media (orientation: landscape) and (max-height: 430px) {
  .audio-header-copy {
    max-width: 60vw !important;
  }

  .audio-header-copy strong {
    font-size: clamp(.72rem, 2.05vh, .86rem) !important;
  }

  .audio-header-copy span {
    font-size: clamp(.54rem, 1.55vh, .64rem) !important;
  }

  .speech-work-panel {
    grid-template-rows: minmax(104px, 1fr) minmax(58px, auto) auto !important;
    gap: 7px !important;
  }

  .speech-config-card {
    padding: 7px !important;
    gap: 6px !important;
  }

  .speech-config-grid select {
    min-height: 28px !important;
  }

  .speech-record-card {
    padding: 7px 10px !important;
  }
}

/* ===== Audio prepare: harmonia retrato + gravação sem sobreposição ===== */
@media (orientation: portrait) {
  .audio-prepare-main {
    grid-template-rows: auto minmax(0, 1fr) !important;
    gap: 7px !important;
    padding: 6px 18px max(8px, env(safe-area-inset-bottom)) !important;
  }

  .speech-intro-panel {
    gap: 4px !important;
  }

  .speech-intro-panel h1 {
    max-width: 17ch !important;
    font-size: clamp(1.28rem, 7vw, 1.62rem) !important;
    line-height: 1.05 !important;
  }

  .speech-intro-panel p {
    font-size: .78rem !important;
    line-height: 1.18 !important;
  }

  .speech-work-panel {
    grid-template-rows: auto minmax(170px, 1fr) minmax(76px, auto) auto !important;
    gap: 7px !important;
    overflow: hidden !important;
  }

  .speech-config-card {
    padding: 7px !important;
    border-radius: 12px !important;
  }

  .speech-config-grid {
    grid-template-columns: .72fr .62fr 1.36fr !important;
    gap: 6px !important;
  }

  .speech-config-grid label,
  .speech-config-grid label:nth-child(3) {
    grid-column: auto !important;
    font-size: .62rem !important;
    font-weight: 680 !important;
    line-height: 1 !important;
  }

  .speech-config-grid select {
    min-height: 29px !important;
    height: 29px !important;
    margin-top: 3px !important;
    font-size: .72rem !important;
    font-weight: 620 !important;
    padding-inline: 8px !important;
  }

  .speech-button-row {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 7px !important;
    margin-top: 7px !important;
  }

  .speech-button-row .soft-action {
    min-height: 31px !important;
    font-size: .72rem !important;
    font-weight: 650 !important;
  }

  .speech-phrase-card {
    padding: 10px 12px !important;
    gap: 7px !important;
    border-radius: 13px !important;
  }

  .speech-label {
    font-size: .66rem !important;
    font-weight: 720 !important;
    letter-spacing: .05em !important;
  }

  .speech-phrase-card strong {
    font-size: clamp(.88rem, 4.1vw, 1.02rem) !important;
    font-weight: 400 !important;
    line-height: 1.22 !important;
  }

  .syllable-toggle {
    min-height: 30px !important;
    padding: 6px 12px !important;
    font-size: .72rem !important;
    font-weight: 650 !important;
    justify-self: start !important;
  }

  .speech-record-card {
    min-height: 76px !important;
    grid-template-columns: 36px minmax(0, 1fr) !important;
    align-items: center !important;
    gap: 8px !important;
    padding: 8px 10px !important;
    border-radius: 13px !important;
    overflow: hidden !important;
  }

  .speech-record-icon {
    width: 36px !important;
    height: 36px !important;
  }

  .speech-record-copy {
    gap: 4px !important;
    min-width: 0 !important;
    overflow: hidden !important;
  }

  .recording-status-line {
    display: grid !important;
    grid-template-columns: minmax(0, 1fr) auto !important;
    gap: 6px !important;
    width: 100% !important;
  }

  .status-pill {
    min-width: 0 !important;
    min-height: 24px !important;
    padding: 4px 8px !important;
    font-size: .64rem !important;
    font-weight: 650 !important;
    overflow: hidden !important;
    text-overflow: ellipsis !important;
    white-space: nowrap !important;
  }

  .recording-timer {
    min-width: 42px !important;
    padding: 4px 7px !important;
    font-size: .72rem !important;
  }

  .status-file {
    font-size: .64rem !important;
    line-height: 1 !important;
  }

  .audio-player {
    height: 26px !important;
    max-width: 100% !important;
  }

  .speech-record-controls {
    display: grid !important;
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    gap: 7px !important;
    justify-content: stretch !important;
  }

  .speech-record-controls .soft-action,
  .speech-record-controls .main-action {
    min-height: 34px !important;
    font-size: .74rem !important;
    font-weight: 650 !important;
  }
}

@media (orientation: portrait) and (max-height: 700px) {
  .speech-intro-panel p {
    display: none !important;
  }

  .speech-work-panel {
    grid-template-rows: auto minmax(160px, 1fr) minmax(72px, auto) auto !important;
    gap: 6px !important;
  }
}

/* ===== Audio prepare: pequenos acertos de paisagem ===== */
@media (orientation: landscape) {
  .syllable-toggle {
    min-height: 30px !important;
    padding: 6px 12px !important;
    font-size: .72rem !important;
    font-weight: 650 !important;
    justify-self: start !important;
    align-self: end !important;
  }

  .speech-phrase-card {
    min-height: 0 !important;
    overflow: hidden !important;
  }

  .speech-phrase-card strong {
    min-height: 0 !important;
    overflow: auto !important;
  }
}

/* ===== Audio prepare: afinação paisagem pequena ===== */
@media (orientation: landscape) and (max-height: 430px) {
  .audio-prepare-main {
    padding-inline: clamp(14px, 2.8vw, 22px) !important;
  }

  .speech-work-panel {
    grid-template-columns: minmax(260px, .94fr) minmax(270px, 1.06fr) !important;
    grid-template-rows: minmax(102px, 1fr) minmax(68px, auto) auto !important;
    gap: 8px !important;
  }

  .speech-config-card {
    padding: 8px !important;
  }

  .speech-config-grid {
    grid-template-columns: .72fr .72fr 1.56fr !important;
    gap: 8px !important;
  }

  .speech-config-grid label,
  .speech-config-grid label:nth-child(3) {
    min-width: 0 !important;
    overflow: visible !important;
    font-size: .68rem !important;
    line-height: 1.05 !important;
  }

  .speech-config-grid select {
    height: 30px !important;
    min-height: 30px !important;
    font-size: .74rem !important;
  }

  .speech-button-row .soft-action,
  .soft-action,
  .main-action {
    min-height: 32px !important;
    padding: 6px 10px !important;
    font-size: .74rem !important;
  }

  .speech-record-controls {
    gap: 8px !important;
  }

  .speech-phrase-card {
    grid-template-columns: minmax(0, 1fr) auto !important;
    grid-template-rows: auto minmax(0, 1fr) !important;
    padding: 10px 12px !important;
    column-gap: 12px !important;
    row-gap: 5px !important;
  }

  .speech-phrase-card .speech-label {
    grid-column: 1 / -1 !important;
  }

  .speech-phrase-card strong {
    grid-column: 1 !important;
    grid-row: 2 !important;
    font-size: clamp(.88rem, 3.5vh, 1.02rem) !important;
    font-weight: 400 !important;
    line-height: 1.2 !important;
  }

  .syllable-toggle {
    grid-column: 2 !important;
    grid-row: 2 !important;
    width: max-content !important;
    max-width: 110px !important;
    min-height: 28px !important;
    padding: 5px 11px !important;
    align-self: center !important;
    justify-self: end !important;
    font-size: .68rem !important;
    line-height: 1 !important;
  }

  .speech-record-card {
    min-height: 68px !important;
    padding: 8px 10px !important;
  }
}

/* Frases para leitura: peso regular melhora a legibilidade em dislexia. */
.speech-phrase-card strong {
  font-weight: 400 !important;
}

/* Botões alinhados com o sistema visual comum. */
.main-action {
  background: #1a73e8 !important;
  color: #fff !important;
  border-color: #1a73e8 !important;
  box-shadow: none !important;
}

.soft-action,
.syllable-toggle {
  background: #f3f6fb !important;
  color: #344054 !important;
  border-color: #e5eaf2 !important;
  box-shadow: none !important;
  font-weight: 500 !important;
}

.soft-action.active,
.syllable-toggle.active {
  background: #e8f0fe !important;
  color: #1a73e8 !important;
  border-color: #c6dafc !important;
}

.main-action:disabled,
.soft-action:disabled {
  background: #f6f8fb !important;
  color: #a4acb8 !important;
  border-color: #f6f8fb !important;
  opacity: 1 !important;
}
</style>
