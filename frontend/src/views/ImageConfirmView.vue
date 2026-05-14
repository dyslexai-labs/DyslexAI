<template>
  <section class="flow-view image-confirm-view">
    <div class="flow-shell image-confirm-shell">
      <AppHeader :subtitle="t('app.assisted')" @home="$emit('go-home')" />

      <div class="flow-main image-confirm-main">
        <section class="flow-intro image-confirm-intro">
          <h1>{{ t('imageConfirm.title') }}</h1>
          <p>{{ t('imageConfirm.description') }}</p>
        </section>

        <section class="confirm-panel" :aria-label="t('imageConfirm.label')">
          <div class="confirm-preview-card">
            <img v-if="previewUrl" :src="previewUrl" :alt="t('imageConfirm.previewAlt')" class="confirm-preview-image" />
            <div v-else class="confirm-empty">{{ t('imageConfirm.empty') }}</div>
          </div>

          <div class="confirm-controls">
            <button class="main-action" @click="$emit('process-image')">{{ t('imageConfirm.process') }}</button>
            <button class="soft-action" @click="$emit('choose-other')">{{ t('imageConfirm.chooseOther') }}</button>
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
import { t } from '../i18n'

defineProps({
  previewUrl: {
    type: String,
    required: true,
  },
})

defineEmits(['process-image', 'choose-other', 'go-home'])
</script>

<style scoped>
.flow-view,
.flow-view * { box-sizing: border-box; }

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
  max-width: 18ch;
  margin: 0;
  color: #13213d;
  font-size: clamp(21px, 6.4vw, 28px);
  font-weight: 760;
  line-height: 1.08;
  letter-spacing: 0;
}

.flow-intro p {
  max-width: 36ch;
  margin: 0;
  color: #667085;
  font-size: clamp(11px, 3.2vw, 13px);
  line-height: 1.28;
}

.confirm-panel {
  min-width: 0;
  min-height: 0;
  display: grid;
  gap: 10px;
}

.confirm-preview-card {
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  display: grid;
  place-items: center;
  border: 1px solid #e4eaf3;
  border-radius: 18px;
  background: #f8fbff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, .08);
}

.confirm-preview-image {
  display: block;
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain;
}

.confirm-empty {
  color: #667085;
  font-weight: 700;
}

.confirm-controls {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  min-width: 0;
}

.main-action,
.soft-action {
  min-width: 0;
  min-height: 44px;
  border-radius: 999px;
  border: 1px solid transparent;
  padding: 10px 16px;
  font-size: clamp(13px, 2.2vw, 15px);
  font-weight: 800;
}

.main-action {
  background: #1a73e8;
  color: #fff;
}

.soft-action {
  background: #fff;
  color: #172033;
  border-color: #dbe5f3;
  box-shadow: 0 6px 16px rgba(15, 23, 42, .06);
}

@media (orientation: portrait) {
  .flow-main {
    display: grid;
    grid-template-rows: auto minmax(0, 1fr);
    gap: 12px;
    padding: 12px 18px max(10px, env(safe-area-inset-bottom));
  }

  .confirm-panel { grid-template-rows: minmax(0, 1fr) auto; }
}

@media (orientation: landscape) {
  .flow-main {
    display: grid;
    grid-template-columns: minmax(170px, 330px) minmax(360px, 720px);
    grid-template-rows: minmax(0, 1fr);
    align-items: center;
    justify-content: center;
    gap: clamp(18px, 6vw, 78px);
    padding: 4px max(clamp(22px, 4vw, 56px), env(safe-area-inset-right)) max(8px, env(safe-area-inset-bottom)) max(clamp(22px, 4vw, 56px), env(safe-area-inset-left));
  }

  .flow-intro h1 {
    max-width: 15ch;
    font-size: clamp(18px, 4vh, 30px);
    font-weight: 760;
  }

  .flow-intro p { font-size: clamp(10px, 1.9vh, 13px); }
  .confirm-panel { grid-template-rows: minmax(0, 1fr) auto; max-height: min(72vh, 430px); }
}

@media (orientation: landscape) and (max-height: 420px) {
  .confirm-panel { max-height: calc(100dvh - 72px); }
  .main-action, .soft-action { min-height: 38px; padding: 8px 12px; }
}

/* Botões alinhados com o sistema visual comum. */
.main-action {
  background: #1a73e8 !important;
  color: #fff !important;
  border-color: #1a73e8 !important;
  box-shadow: none !important;
}

.soft-action {
  background: #f3f6fb !important;
  color: #344054 !important;
  border-color: #e5eaf2 !important;
  box-shadow: none !important;
}
</style>
