<template>
  <section class="screen-center home-screen">
    <div class="google-home">
      <AppHeader subtitle="Leitor guiado" @home="$emit('go-home')" />

      <div class="google-home-main">
        <section class="home-intro-panel">
          <div class="home-badge">👋 Bem-vindo(a)!</div>
          <h1>Escolhe como queres começar</h1>
          <p>Escolhe uma imagem ou grava a tua voz. Depois lê com ajuda, passo a passo.</p>
        </section>

        <section class="home-actions-panel" aria-label="Modos de leitura">
          <button class="home-option-card home-option-card--blue" @click="$emit('start-image')">
            <div class="home-option-icon image-icon" aria-hidden="true">
              <svg viewBox="0 0 96 96" role="img" focusable="false">
                <rect x="6" y="8" width="84" height="80" rx="18" fill="#dff1ff"/>
                <circle cx="68" cy="30" r="9" fill="#fbbc04"/>
                <path d="M12 70l23-27 18 20 11-13 20 20v10H12z" fill="#34a853"/>
                <path d="M12 69l26-31 24 31z" fill="#2f80ed" opacity=".9"/>
                <path d="M42 69l19-23 24 23z" fill="#1a73e8" opacity=".78"/>
                <path d="M16 34c7-11 21-11 28 0 6-5 15-3 19 4H16z" fill="#fff" opacity=".92"/>
              </svg>
            </div>
            <div class="home-option-text">
              <strong>Leitura assistida</strong>
              <span>Usa uma imagem ou texto para preparar a leitura.</span>
            </div>
            <div class="home-option-arrow" aria-hidden="true">&gt;</div>
          </button>

          <button class="home-option-card home-option-card--green" @click="$emit('start-audio')">
            <div class="home-option-icon mic-icon" aria-hidden="true">
              <svg viewBox="0 0 96 96" role="img" focusable="false">
                <rect x="6" y="8" width="84" height="80" rx="18" fill="#e5f6e8"/>
                <rect x="37" y="18" width="22" height="42" rx="11" fill="#34a853"/>
                <path d="M26 44c0 14 9 25 22 25s22-11 22-25" fill="none" stroke="#34a853" stroke-width="7" stroke-linecap="round"/>
                <path d="M48 69v13M34 82h28" stroke="#34a853" stroke-width="7" stroke-linecap="round"/>
                <path d="M61 31h8M61 43h8" stroke="#34a853" stroke-width="5" stroke-linecap="round" opacity=".75"/>
              </svg>
            </div>
            <div class="home-option-text">
              <strong>Leitura a partir da fala</strong>
              <span>Grava a fala do aluno e gera uma frase para leitura guiada.</span>
            </div>
            <div class="home-option-arrow" aria-hidden="true">&gt;</div>
          </button>
        </section>

        <section class="home-recent-card" aria-label="Atividade recente">
          <div class="recent-title">Recente</div>
          <div class="recent-row">
            <div class="recent-icon">📄</div>
            <div><strong>Lição – Animais da Quinta</strong><span>30/04/2025 · 2 min</span></div>
            <div class="recent-arrow">›</div>
          </div>
        </section>

        <section class="home-safety-card" aria-label="Privacidade e segurança">
          <div class="safety-icon">🛡️</div>
          <div><strong>Privacidade e segurança</strong><span>Os dados são utilizados apenas para melhorar a experiência de leitura guiada.</span></div>
          <div class="recent-arrow">›</div>
        </section>
      </div>

      <BottomNav active="home" />
    </div>
  </section>
</template>

<script setup>
import AppHeader from '../components/common/AppHeader.vue'
import BottomNav from '../components/common/BottomNav.vue'

defineEmits(['start-image', 'start-audio', 'go-home'])
</script>
