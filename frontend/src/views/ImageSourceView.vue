<template>
  <section class="screen-center">
    <div class="google-home assisted-home">
      <AppHeader subtitle="Leitura assistida" @home="$emit('go-home')" />

      <div class="google-home-main">
        <section class="home-intro-panel assisted-intro-panel">
          <h1>Escolhe a origem da imagem</h1>
          <p>Tira uma fotografia da página ou escolhe uma imagem guardada.</p>
        </section>

        <section class="home-actions-panel assisted-actions-panel" aria-label="Origem da imagem">
          <button class="home-option-card home-option-card--blue assisted-option-card" @click="$emit('take-photo')">
            <div class="home-option-icon assisted-option-icon" aria-hidden="true">
              <svg viewBox="0 0 96 96" role="img" focusable="false">
                <rect x="10" y="28" width="76" height="52" rx="16" fill="#eaf4ff"/>
                <path d="M32 28l7-10h18l7 10" fill="#1a73e8" opacity=".9"/>
                <circle cx="48" cy="54" r="18" fill="#1a73e8"/>
                <circle cx="48" cy="54" r="10" fill="#8fd3ff"/>
                <circle cx="72" cy="40" r="5" fill="#fbbc04"/>
                <path d="M18 73l17-18 13 14 9-9 19 13" fill="none" stroke="#34a853" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="home-option-text"><strong>Tirar fotografia</strong><span>Usa a câmara para fotografar a página.</span></div>
            <div class="home-option-arrow" aria-hidden="true">&gt;</div>
          </button>

          <button class="home-option-card home-option-card--green assisted-option-card" @click="$emit('pick-gallery')">
            <div class="home-option-icon assisted-option-icon" aria-hidden="true">
              <svg viewBox="0 0 96 96" role="img" focusable="false">
                <rect x="12" y="14" width="66" height="58" rx="14" fill="#eaf4ff"/>
                <rect x="20" y="24" width="66" height="58" rx="14" fill="#dff1ff"/>
                <circle cx="66" cy="38" r="7" fill="#fbbc04"/>
                <path d="M27 72l18-22 14 16 9-11 12 17" fill="#34a853"/>
                <path d="M27 72l23-28 22 28" fill="#1a73e8" opacity=".78"/>
              </svg>
            </div>
            <div class="home-option-text"><strong>Escolher da galeria</strong><span>Escolhe uma imagem já guardada.</span></div>
            <div class="home-option-arrow" aria-hidden="true">&gt;</div>
          </button>
        </section>
      </div>

      <BottomNav @home="$emit('go-home')" />
    </div>
  </section>
</template>

<script setup>
import AppHeader from '../components/common/AppHeader.vue'
import BottomNav from '../components/common/BottomNav.vue'

defineEmits(['take-photo', 'pick-gallery', 'go-home'])
</script>
