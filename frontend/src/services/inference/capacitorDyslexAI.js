import { Capacitor, registerPlugin } from '@capacitor/core'

export const DyslexAIPlugin = registerPlugin('DyslexAI')

export function isNativeMobile() {
  return Capacitor.isNativePlatform()
}