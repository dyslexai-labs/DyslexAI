<template>
  <section class="screen-center">
    <div class="google-home speech-home">
      <AppHeader subtitle="Leitura a partir da fala" @home="$emit('go-home')" />

      <div class="google-home-main">
        <section class="home-intro-panel speech-intro-panel">
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
            <strong>{{ isGeneratingPhrase ? 'A gerar frase...' : (expectedReadingText || 'Gera uma frase para começar.') }}</strong>
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
              <span class="status-pill" :class="{ live: isRecording, ready: hasRecordedAudio && !isRecording }">
                {{ isRecording ? 'A gravar' : (hasRecordedAudio ? 'Gravação pronta' : 'À espera de gravação') }}
              </span>
              <span v-if="selectedAudioName" class="status-file">{{ selectedAudioName }}</span>
              <audio v-if="audioPreviewUrl" :src="audioPreviewUrl" controls class="audio-player"></audio>
            </div>
          </div>

          <div class="record-controls speech-record-controls">
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
import AppHeader from '../components/common/AppHeader.vue'
import BottomNav from '../components/common/BottomNav.vue'

defineProps({
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
  selectedAudioName: {
    type: String,
    default: '',
  },
})

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
])
</script>
