<template>
  <section class="flow-view image-source-view">
    <div class="flow-shell image-source-shell">
      <AppHeader :subtitle="t('app.assisted')" @home="$emit('go-home')" />

      <div class="flow-main image-source-main">
        <section class="flow-intro image-source-intro">
          <h1>{{ t('imageSource.title') }}</h1>
          <p>{{ t('imageSource.description') }}</p>
        </section>

        <section class="flow-options image-source-options" :aria-label="t('imageSource.label')">
          <button class="flow-option flow-option-blue" @click="$emit('take-photo')">
            <div class="flow-option-icon" aria-hidden="true">
              <svg viewBox="0 0 96 96" role="img" focusable="false">
                <rect x="10" y="28" width="76" height="52" rx="16" fill="#eaf4ff"/>
                <path d="M32 28l7-10h18l7 10" fill="#1a73e8" opacity=".9"/>
                <circle cx="48" cy="54" r="18" fill="#1a73e8"/>
                <circle cx="48" cy="54" r="10" fill="#8fd3ff"/>
                <circle cx="72" cy="40" r="5" fill="#fbbc04"/>
                <path d="M18 73l17-18 13 14 9-9 19 13" fill="none" stroke="#34a853" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
            <div class="flow-option-text"><strong>{{ t('imageSource.cameraTitle') }}</strong><span>{{ t('imageSource.cameraDescription') }}</span></div>
            <div class="flow-option-arrow" aria-hidden="true">&gt;</div>
          </button>

          <button class="flow-option flow-option-green" @click="$emit('pick-gallery')">
            <div class="flow-option-icon" aria-hidden="true">
              <svg viewBox="0 0 96 96" role="img" focusable="false">
                <rect x="12" y="14" width="66" height="58" rx="14" fill="#eaf4ff"/>
                <rect x="20" y="24" width="66" height="58" rx="14" fill="#dff1ff"/>
                <circle cx="66" cy="38" r="7" fill="#fbbc04"/>
                <path d="M27 72l18-22 14 16 9-11 12 17" fill="#34a853"/>
                <path d="M27 72l23-28 22 28" fill="#1a73e8" opacity=".78"/>
              </svg>
            </div>
            <div class="flow-option-text"><strong>{{ t('imageSource.galleryTitle') }}</strong><span>{{ t('imageSource.galleryDescription') }}</span></div>
            <div class="flow-option-arrow" aria-hidden="true">&gt;</div>
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
import { t } from '../i18n'

defineEmits(['take-photo', 'pick-gallery', 'go-home'])
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

.flow-options {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.flow-option {
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

.flow-option-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  overflow: hidden;
  border-radius: 14px;
}

.flow-option-icon svg {
  width: 100%;
  height: 100%;
}

.flow-option-text {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.flow-option-text strong {
  color: #172033;
  font-size: clamp(15px, 3.2vw, 19px);
  font-weight: 820;
  line-height: 1.1;
  letter-spacing: 0;
}

.flow-option-text span {
  color: #667085;
  font-size: clamp(10px, 2vw, 13px);
  line-height: 1.22;
}

.flow-option-arrow {
  display: grid;
  place-items: center;
  justify-self: end;
  width: 32px;
  height: 32px;
  border-radius: 999px;
  font-size: 1.1rem;
  font-weight: 900;
}

.flow-option-blue .flow-option-arrow {
  color: #1a73e8;
  background: #edf4ff;
}

.flow-option-green .flow-option-arrow {
  color: #16a34a;
  background: #eaf8ef;
}

@media (orientation: portrait) {
  .flow-main {
    display: grid;
    grid-template-rows: auto minmax(0, 1fr);
    gap: 18px;
    padding: 16px 20px max(10px, env(safe-area-inset-bottom));
  }

  .flow-options {
    align-content: stretch;
  }

  .flow-option {
    min-height: min(180px, 26vh);
  }
}

@media (orientation: landscape) {
  .flow-main {
    display: grid;
    grid-template-columns: minmax(180px, 360px) minmax(340px, 690px);
    grid-template-rows: minmax(0, 1fr);
    align-items: center;
    justify-content: center;
    gap: clamp(22px, 7vw, 92px);
    padding: 4px max(clamp(22px, 4vw, 56px), env(safe-area-inset-right)) max(8px, env(safe-area-inset-bottom)) max(clamp(22px, 4vw, 56px), env(safe-area-inset-left));
  }

  .flow-option {
    grid-template-columns: clamp(42px, 8vh, 62px) minmax(0, 1fr) clamp(28px, 5vh, 34px);
    min-height: clamp(54px, 12vh, 78px);
    padding: clamp(8px, 1.8vh, 13px);
  }

  .flow-option-icon {
    width: clamp(42px, 8vh, 62px);
    height: clamp(42px, 8vh, 62px);
  }

  .flow-intro h1 {
    font-size: clamp(18px, 4vh, 30px);
  }

  .flow-intro p {
    font-size: clamp(10px, 1.9vh, 14px);
  }

  .flow-option-text strong {
    font-size: clamp(13px, 2.35vh, 18px);
  }

  .flow-option-text span {
    font-size: clamp(9px, 1.65vh, 13px);
  }
}

/* ===== UI consistency: origem da imagem ===== */
.image-source-view :deep(.home-help-btn) {
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

.image-source-view :deep(.home-help-btn svg) {
  width: 24px !important;
  height: 24px !important;
  display: block !important;
  stroke-width: 2.2px !important;
}

.flow-option,
.flow-option-text,
.flow-option-text strong,
.flow-option-text span {
  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif !important;
}

.flow-option-text strong {
  font-size: clamp(14px, 3.4vw, 16px) !important;
  font-weight: 720 !important;
  line-height: 1.14 !important;
}

.flow-option-text span {
  font-size: clamp(10px, 2.65vw, 12px) !important;
  font-weight: 500 !important;
  line-height: 1.25 !important;
}

@media (orientation: portrait) {
  .flow-main {
    padding: 14px 16px max(10px, env(safe-area-inset-bottom)) !important;
    gap: 16px !important;
  }

  .flow-intro h1 {
    max-width: 18ch !important;
    font-size: clamp(21px, 7vw, 28px) !important;
    font-weight: 760 !important;
    line-height: 1.08 !important;
  }

  .flow-intro p {
    max-width: 36ch !important;
    font-size: clamp(12px, 3.4vw, 14px) !important;
    line-height: 1.28 !important;
  }

  .flow-option {
    min-height: min(178px, 25vh) !important;
  }
}

/* Setas dos cartões com a mesma cor de ação dos restantes ecrãs. */
.flow-option-arrow,
.flow-option-blue .flow-option-arrow,
.flow-option-green .flow-option-arrow {
  background: #e8f0fe !important;
  color: #1a73e8 !important;
}
</style>
