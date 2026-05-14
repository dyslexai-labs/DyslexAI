<template>
  <div class="language-toggle" :aria-label="t('language.label')">
    <button
      type="button"
      :class="{ active: locale === 'pt' }"
      @click="setLocale('pt')"
    >
      {{ t('language.pt') }}
    </button>
    <button
      type="button"
      :class="{ active: locale === 'en' }"
      @click="setLocale('en')"
    >
      {{ t('language.en') }}
    </button>
  </div>
</template>

<script setup>
import { locale, setLocale, t } from '../../i18n'
</script>

<style scoped>
.language-toggle {
  display: inline-grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 2px;
  min-width: 76px;
  padding: 3px;
  border: 1px solid #dfe7f2;
  border-radius: 999px;
  background: #f7faff;
}

.language-toggle button {
  min-width: 0;
  min-height: 30px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #667085;
  font-size: .72rem;
  font-weight: 800;
  line-height: 1;
}

.language-toggle button.active {
  background: #1a73e8;
  color: #fff;
}
</style>
