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
        <div class="home-brand-subtitle">{{ subtitle }}</div>
      </div>
    </div>
    <slot name="actions">
      <div class="header-action-row">
        <button class="home-help-btn" title="Início" aria-label="Início" @click="$emit('home')">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6h-4v6H5a1 1 0 0 1-1-1z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </slot>
  </header>
</template>

<script setup>
defineProps({
  subtitle: {
    type: String,
    default: 'Leitor guiado',
  },
})

defineEmits(['home'])
</script>
