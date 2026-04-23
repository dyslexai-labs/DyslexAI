import { Capacitor, registerPlugin } from '@capacitor/core'

const DyslexAIPlugin = registerPlugin('DyslexAI')

export function isNativeMobile() {
  return Capacitor.isNativePlatform()
}

export { DyslexAIPlugin }
