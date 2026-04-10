<template>
  <div class="player-app" :class="themeClass">

    <main class="page">

      <!-- PLAYER PRINCIPAL -->
      <section class="reader-view">

        <!-- TEXTO (FOCO TOTAL) -->
        <div class="focus-panel" :style="{ fontSize: fontSize + 'px' }">
          {{ currentLines[currentLineIndex] }}
        </div>

        <!-- CONTROLOS TIPO MEDIA PLAYER -->
        <div class="player-controls">
          <button @click="prevLine">⏮</button>
          <button class="play-btn" @click="speakCurrentLine">▶</button>
          <button @click="nextLine">⏭</button>
        </div>

        <!-- CONTROLOS MINIMOS -->
        <div class="secondary-controls">
          <div class="slider-block">
            <label>Fonte {{ fontSize }}</label>
            <input v-model.number="fontSize" type="range" min="24" max="48" />
          </div>

          <div class="slider-block">
            <label>Velocidade {{ speechRate.toFixed(2) }}</label>
            <input v-model.number="speechRate" type="range" min="0.6" max="1.8" step="0.05" />
          </div>
        </div>

      </section>
    </main>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const fontSize = ref(34)
const speechRate = ref(1.0)
const currentLineIndex = ref(0)

const lines = ref([
  'Salazar e o Estado Novo.',
  'A ditadura travou a agitação e a desordem.',
  'Mas ainda faltava resolver o problema das finanças.',
  'Óscar Carmona convidou Salazar.',
  'Salazar foi ministro das Finanças.',
  'Houve sacrifícios para os portugueses.',
  'Os apoios sociais foram cortados.',
  'Os impostos aumentaram.',
  'Educação e Saúde receberam menos dinheiro.'
])

const currentLines = computed(() => lines.value)
const themeClass = computed(() => 'theme-cream')

function prevLine() {
  if (currentLineIndex.value > 0) currentLineIndex.value--
}

function nextLine() {
  if (currentLineIndex.value < currentLines.value.length - 1) currentLineIndex.value++
}

// 🔊 VOZ CORRETA (FIX VOGAIS)
function getPortugueseVoice() {
  const voices = window.speechSynthesis.getVoices()
  return voices.find(v => v.lang === 'pt-PT')
      || voices.find(v => v.lang.startsWith('pt'))
      || voices[0]
}

function speakCurrentLine() {
  if (!('speechSynthesis' in window)) return

  window.speechSynthesis.cancel()

  const utterance = new SpeechSynthesisUtterance(currentLines.value[currentLineIndex.value])

  const voice = getPortugueseVoice()
  utterance.voice = voice
  utterance.lang = voice.lang
  utterance.rate = Number(speechRate.value)

  window.speechSynthesis.speak(utterance)
}

// ⚠️ necessário para carregar vozes no Chrome
onMounted(() => {
  speechSynthesis.onvoiceschanged = () => {
    speechSynthesis.getVoices()
  }
})
</script>

<style scoped>

:global(body) {
  margin: 0;
  font-family: Inter, system-ui;
}

.player-app {
  min-height: 100vh;
  background: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page {
  width: 100%;
  max-width: 700px;
  padding: 20px;
}

/* TEXTO PRINCIPAL */
.focus-panel {
  border-radius: 24px;
  padding: 40px;
  background: linear-gradient(135deg, #7c3aed, #1e293b);
  color: white;
  text-align: center;
  line-height: 1.6;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* PLAYER */
.player-controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  margin-top: 20px;
}

.player-controls button {
  border: none;
  background: #e2e8f0;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  font-size: 20px;
}

.play-btn {
  width: 72px;
  height: 72px;
  background: #2563eb;
  color: white;
  font-size: 28px;
}

/* CONTROLOS MINIMOS */
.secondary-controls {
  margin-top: 20px;
  display: flex;
  gap: 20px;
  justify-content: center;
}

.slider-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: white;
}

/* MOBILE HORIZONTAL */
@media (max-width: 760px) and (orientation: landscape) {
  .focus-panel {
    font-size: 26px;
    padding: 20px;
  }
}

</style>
