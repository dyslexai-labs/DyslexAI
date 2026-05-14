<template>
  <header class="google-home-header">
    <div class="home-brand">
      <div class="home-logo-mark" aria-hidden="true">
        <span class="logo-blue"></span>
        <span class="logo-red"></span>
        <span class="logo-yellow"></span>
        <span class="logo-green"></span>
      </div>
      <div class="home-brand-copy">
        <div class="home-brand-title">Dyslex<span>AI</span></div>
        <div class="home-brand-subtitle">{{ subtitle || t('app.subtitle') }}</div>
      </div>
    </div>
    <slot name="actions">
      <div class="header-action-row">
        <LanguageToggle />
        <button class="home-help-btn" :title="t('app.home')" :aria-label="t('app.home')" @click="$emit('home')">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6h-4v6H5a1 1 0 0 1-1-1z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </slot>
  </header>
</template>

<script setup>
import LanguageToggle from './LanguageToggle.vue'
import { t } from '../../i18n'

defineProps({
  subtitle: {
    type: String,
    default: '',
  },
})

defineEmits(['home'])
</script>

<style scoped>
.google-home-header {
  width: 100% !important;
  min-width: 0 !important;
  display: flex !important;
  align-items: flex-start !important;
  justify-content: space-between !important;
  gap: 12px !important;
  transform: none !important;
}

.home-brand {
  position: static !important;
  display: flex !important;
  align-items: center !important;
  gap: 10px !important;
  min-width: 0 !important;
  transform: none !important;
}

.home-brand-copy {
  min-width: 0 !important;
}

.header-action-row {
  position: static !important;
  display: flex !important;
  align-items: center !important;
  justify-content: flex-end !important;
  gap: 8px !important;
  margin-left: auto !important;
  transform: none !important;
}

.home-help-btn {
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

.home-help-btn svg {
  width: 24px !important;
  height: 24px !important;
  display: block !important;
  stroke-width: 2.2px !important;
}

@media (orientation: landscape) {
  .google-home-header {
    padding: max(8px, env(safe-area-inset-top)) clamp(16px, 4vw, 38px) 4px !important;
  }

  .home-logo-mark {
    width: clamp(26px, 4.2vw, 42px) !important;
    height: clamp(26px, 4.2vw, 42px) !important;
    min-width: clamp(26px, 4.2vw, 42px) !important;
    transform: none !important;
  }

  .home-brand-title {
    font-size: clamp(1.15rem, 3vw, 1.85rem) !important;
    line-height: 1 !important;
    letter-spacing: 0 !important;
    transform: none !important;
  }

  .home-brand-subtitle {
    margin-top: 2px !important;
    font-size: clamp(.62rem, 1.6vw, .88rem) !important;
    line-height: 1.1 !important;
    transform: none !important;
  }

  .home-help-btn {
    width: 38px !important;
    height: 38px !important;
    min-width: 38px !important;
    min-height: 38px !important;
    padding: 7px !important;
  }
}
</style>
