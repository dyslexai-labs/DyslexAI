import { createWebInferenceProvider } from './webInferenceProvider'
import { createMobileInferenceProvider } from './mobileInferenceProvider'
import { isNativeMobile } from './capacitorDyslexAI'

export function createInferenceService() {
  if (isNativeMobile()) {
    return createMobileInferenceProvider()
  }

  return createWebInferenceProvider()
}
