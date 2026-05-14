<template>
  <section class="entry-view" :aria-label="t('home.modesLabel')">
    <header class="entry-header">
      <div class="entry-brand">
        <div class="entry-logo" aria-hidden="true">
          <span class="entry-logo-blue"></span>
          <span class="entry-logo-red"></span>
          <span class="entry-logo-yellow"></span>
          <span class="entry-logo-green"></span>
        </div>
        <div class="entry-brand-copy">
          <div class="entry-brand-title">Dyslex<span>AI</span></div>
          <div class="entry-brand-subtitle">{{ t('app.subtitle') }}</div>
        </div>
      </div>

      <button class="entry-home-button" :title="t('app.home')" :aria-label="t('app.home')" @click="$emit('go-home')">
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path d="M4 10.5 12 4l8 6.5V20a1 1 0 0 1-1 1h-5v-6h-4v6H5a1 1 0 0 1-1-1z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
        </svg>
      </button>
    </header>

    <main class="entry-main">
      <section class="entry-intro">
        <div class="entry-badge">{{ t('home.badge') }}</div>
        <h1>{{ t('home.title') }}</h1>
        <p>{{ t('home.description') }}</p>
      </section>

      <section class="entry-options" :aria-label="t('home.modesLabel')">
        <button class="entry-option entry-option-blue" @click="$emit('start-image')">
          <div class="entry-option-icon" aria-hidden="true">
            <svg viewBox="0 0 96 96" role="img" focusable="false">
              <rect x="6" y="8" width="84" height="80" rx="18" fill="#dff1ff"/>
              <circle cx="68" cy="30" r="9" fill="#fbbc04"/>
              <path d="M12 70l23-27 18 20 11-13 20 20v10H12z" fill="#34a853"/>
              <path d="M12 69l26-31 24 31z" fill="#2f80ed" opacity=".9"/>
              <path d="M42 69l19-23 24 23z" fill="#1a73e8" opacity=".78"/>
              <path d="M16 34c7-11 21-11 28 0 6-5 15-3 19 4H16z" fill="#fff" opacity=".92"/>
            </svg>
          </div>
          <div class="entry-option-copy">
            <strong>{{ t('home.assistedTitle') }}</strong>
            <span>{{ t('home.assistedDescription') }}</span>
          </div>
          <div class="entry-option-arrow" aria-hidden="true">&gt;</div>
        </button>

        <button class="entry-option entry-option-green" @click="$emit('start-audio')">
          <div class="entry-option-icon" aria-hidden="true">
            <svg viewBox="0 0 96 96" role="img" focusable="false">
              <rect x="6" y="8" width="84" height="80" rx="18" fill="#e5f6e8"/>
              <rect x="37" y="18" width="22" height="42" rx="11" fill="#34a853"/>
              <path d="M26 44c0 14 9 25 22 25s22-11 22-25" fill="none" stroke="#34a853" stroke-width="7" stroke-linecap="round"/>
              <path d="M48 69v13M34 82h28" stroke="#34a853" stroke-width="7" stroke-linecap="round"/>
              <path d="M61 31h8M61 43h8" stroke="#34a853" stroke-width="5" stroke-linecap="round" opacity=".75"/>
            </svg>
          </div>
          <div class="entry-option-copy">
            <strong>{{ t('home.speechTitle') }}</strong>
            <span>{{ t('home.speechDescription') }}</span>
          </div>
          <div class="entry-option-arrow" aria-hidden="true">&gt;</div>
        </button>
      </section>
    </main>
  </section>
</template>

<script setup>
import { t } from '../i18n'

defineEmits(['start-image', 'start-audio', 'go-home'])
</script>

<style scoped>
.entry-view {
  position: fixed;
  inset: 0;
  z-index: 300;
  width: 100vw;
  height: 100dvh;
  min-height: 100dvh;
  overflow: hidden;
  background: #fff;
  color: #172033;
}

.entry-header {
  position: absolute;
  inset: 0 0 auto 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: max(18px, env(safe-area-inset-top)) clamp(18px, 5vw, 32px) 0;
}

.entry-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.entry-logo {
  position: relative;
  width: 42px;
  height: 42px;
  min-width: 42px;
  overflow: hidden;
  border-radius: 12px;
}

.entry-logo span {
  position: absolute;
  display: block;
}

.entry-logo-blue {
  left: 0;
  top: 0;
  width: 100%;
  height: 68%;
  background: #1a73e8;
  border-radius: 12px 14px 6px 6px;
}

.entry-logo-red {
  left: 0;
  top: 36%;
  width: 50%;
  height: 28%;
  background: #ea4335;
}

.entry-logo-yellow {
  left: 0;
  bottom: 0;
  width: 50%;
  height: 42%;
  background: #fbbc04;
}

.entry-logo-green {
  right: 0;
  bottom: 0;
  width: 58%;
  height: 58%;
  background: #34a853;
}

.entry-brand-copy {
  min-width: 0;
}

.entry-brand-title {
  color: #1f64d9;
  font-size: 1.55rem;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 0;
}

.entry-brand-title span {
  color: #1a73e8;
}

.entry-brand-subtitle {
  margin-top: 3px;
  color: #667085;
  font-size: .78rem;
  line-height: 1.15;
}

.entry-home-button {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  padding: 7px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #172033;
}

.entry-home-button svg {
  width: 100%;
  height: 100%;
}

.entry-main {
  position: absolute;
  inset: clamp(96px, 18vh, 132px) clamp(18px, 5vw, 32px) max(10px, env(safe-area-inset-bottom));
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  align-content: start;
  gap: 20px;
  overflow: hidden;
}

.entry-intro {
  display: grid;
  justify-items: start;
  gap: 8px;
  min-width: 0;
}

.entry-badge {
  display: inline-flex;
  max-width: 100%;
  padding: 5px 11px;
  border-radius: 999px;
  background: #edf4ff;
  color: #1a73e8;
  font-size: .78rem;
  font-weight: 800;
  line-height: 1;
  white-space: nowrap;
}

.entry-intro h1 {
  max-width: 13ch;
  margin: 0;
  color: #13213d;
  font-size: clamp(1.85rem, 8vw, 2.45rem);
  font-weight: 830;
  line-height: 1.08;
  letter-spacing: 0;
}

.entry-intro p {
  max-width: 32ch;
  margin: 0;
  color: #667085;
  font-size: clamp(.9rem, 3.5vw, 1.05rem);
  line-height: 1.3;
}

.entry-options {
  display: grid;
  gap: 12px;
  width: 100%;
  min-width: 0;
}

.entry-option {
  display: grid;
  grid-template-columns: 58px minmax(0, 1fr) 34px;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 96px;
  padding: 12px;
  border: 1px solid #e4eaf3;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, .08);
  color: inherit;
  text-align: left;
}

.entry-option-icon {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  overflow: hidden;
  border-radius: 14px;
}

.entry-option-icon svg {
  width: 100%;
  height: 100%;
}

.entry-option-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.entry-option-copy strong {
  color: #172033;
  font-size: 1rem;
  font-weight: 820;
  line-height: 1.1;
  letter-spacing: 0;
}

.entry-option-copy span {
  color: #667085;
  font-size: .76rem;
  line-height: 1.24;
}

.entry-option-arrow {
  display: grid;
  place-items: center;
  justify-self: end;
  width: 32px;
  height: 32px;
  border-radius: 999px;
  font-size: 1.1rem;
  font-weight: 900;
}

.entry-option-blue .entry-option-arrow {
  color: #1a73e8;
  background: #edf4ff;
}

.entry-option-green .entry-option-arrow {
  color: #16a34a;
  background: #eaf8ef;
}

@media (orientation: landscape) {
  .entry-header {
    padding: max(10px, env(safe-area-inset-top)) clamp(22px, 4vw, 48px) 0;
  }

  .entry-logo {
    width: clamp(30px, 6vh, 46px);
    height: clamp(30px, 6vh, 46px);
    min-width: clamp(30px, 6vh, 46px);
  }

  .entry-brand-title {
    font-size: clamp(1.45rem, 5.6vh, 2rem);
  }

  .entry-brand-subtitle {
    font-size: clamp(.72rem, 2.35vh, .92rem);
  }

  .entry-main {
    inset: clamp(68px, 14vh, 96px) clamp(24px, 4vw, 56px) max(12px, env(safe-area-inset-bottom));
    grid-template-columns: minmax(190px, .36fr) minmax(340px, .64fr);
    grid-template-rows: minmax(0, 1fr);
    align-items: center;
    align-content: stretch;
    gap: clamp(18px, 5vw, 70px);
  }

  .entry-intro {
    align-content: center;
    gap: clamp(6px, 1.8vh, 12px);
  }

  .entry-badge {
    font-size: clamp(.62rem, 2.4vh, .84rem);
  }

  .entry-intro h1 {
    font-size: clamp(1.45rem, 6.5vh, 2.45rem);
  }

  .entry-intro p {
    font-size: clamp(.72rem, 2.8vh, 1rem);
    line-height: 1.28;
  }

  .entry-options {
    align-self: center;
    gap: clamp(8px, 2.6vh, 16px);
  }

  .entry-option {
    grid-template-columns: clamp(42px, 10vh, 68px) minmax(0, 1fr) clamp(28px, 6vh, 38px);
    gap: clamp(10px, 2.2vw, 18px);
    min-height: clamp(72px, 18vh, 116px);
    padding: clamp(10px, 2.4vh, 18px);
  }

  .entry-option-icon {
    width: clamp(42px, 10vh, 68px);
    height: clamp(42px, 10vh, 68px);
  }

  .entry-option-copy strong {
    font-size: clamp(.95rem, 3.8vh, 1.45rem);
  }

  .entry-option-copy span {
    font-size: clamp(.64rem, 2.45vh, .94rem);
  }
}

@media (orientation: landscape) and (max-height: 380px) {
  .entry-main {
    inset: clamp(48px, 13vh, 64px) clamp(16px, 3vw, 36px) max(8px, env(safe-area-inset-bottom));
    grid-template-columns: minmax(170px, .34fr) minmax(320px, .66fr);
    gap: clamp(14px, 4vw, 44px);
  }

  .entry-intro h1 {
    font-size: clamp(1.2rem, 6.2vh, 1.75rem);
  }

  .entry-intro p {
    font-size: clamp(.64rem, 2.6vh, .82rem);
  }

  .entry-option {
    min-height: clamp(58px, 19vh, 78px);
  }
}

@media (orientation: portrait) and (max-height: 680px) {
  .entry-header {
    padding: max(12px, env(safe-area-inset-top)) 18px 0;
  }

  .entry-logo {
    width: 40px;
    height: 40px;
    min-width: 40px;
  }

  .entry-brand-title {
    font-size: 1.42rem;
  }

  .entry-brand-subtitle {
    font-size: .72rem;
  }

  .entry-main {
    inset: clamp(78px, 15vh, 96px) 18px max(8px, env(safe-area-inset-bottom));
    gap: 14px;
  }

  .entry-intro {
    gap: 6px;
  }

  .entry-intro h1 {
    font-size: clamp(1.6rem, 7.2vw, 2rem);
  }

  .entry-intro p {
    font-size: .86rem;
    line-height: 1.24;
  }

  .entry-options {
    gap: 10px;
  }

  .entry-option {
    grid-template-columns: 52px minmax(0, 1fr) 30px;
    gap: 10px;
    min-height: 104px;
    padding: 10px;
    border-radius: 15px;
  }

  .entry-option-icon {
    width: 52px;
    height: 52px;
  }

  .entry-option-copy strong {
    font-size: .92rem;
  }

  .entry-option-copy span {
    font-size: .68rem;
    line-height: 1.18;
  }

  .entry-option-arrow {
    width: 30px;
    height: 30px;
  }
}
</style>
