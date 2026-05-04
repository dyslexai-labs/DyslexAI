<template>
  <section class="screen-center">
    <div class="google-home processing-home">
      <AppHeader :subtitle="activeFlow === 'audio' ? 'Leitura a partir da fala' : 'Leitura assistida'" @home="$emit('go-home')" />

      <div class="google-home-main">
        <section class="processing-panel" aria-live="polite">
          <div class="loader-ring"></div>
          <h2>{{ processingTitle }}</h2>
          <p>{{ processingMessage }}</p>

          <div class="progress-track">
            <div class="progress-bar" :style="{ width: `${processingProgress}%` }"></div>
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
  activeFlow: {
    type: String,
    default: 'image',
  },
  processingTitle: {
    type: String,
    required: true,
  },
  processingMessage: {
    type: String,
    required: true,
  },
  processingProgress: {
    type: Number,
    required: true,
  },
})

defineEmits(['go-home'])
</script>
