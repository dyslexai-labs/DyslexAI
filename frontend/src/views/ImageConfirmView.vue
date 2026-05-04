<template>
  <section class="screen-center">
    <div class="google-home review-home speech-home">
      <AppHeader subtitle="Leitura assistida" @home="$emit('go-home')" />

      <div class="google-home-main">
        <section class="home-intro-panel speech-intro-panel">
          <h1>Confirma a imagem</h1>
          <p>Verifica se a imagem está legível.</p>
        </section>

        <section class="review-panel" aria-label="Confirmar imagem">
          <div class="review-preview-card">
            <img :src="previewUrl" alt="Pré-visualização" class="preview-image" />
          </div>

          <div class="record-controls speech-record-controls image-confirm-controls">
            <button class="main-action" @click="$emit('process-image')">Processar</button>
            <button class="soft-action" @click="$emit('choose-other')">Outra imagem</button>
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
  previewUrl: {
    type: String,
    required: true,
  },
})

defineEmits(['process-image', 'choose-other', 'go-home'])
</script>

<style scoped>
.review-home .image-confirm-controls {
  grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
}
</style>
