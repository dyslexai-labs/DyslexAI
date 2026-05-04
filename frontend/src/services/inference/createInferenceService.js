import { createWebInferenceProvider } from './webInferenceProvider'
import { createMobileInferenceProvider } from './mobileInferenceProvider'
import { createMockInferenceProvider } from './mockInferenceProvider'
import { isNativeMobile } from './capacitorDyslexAI'
import { MOCK_INFERENCE_ENABLED } from '../../config'

export function createInferenceService() {
  if (MOCK_INFERENCE_ENABLED) {
    console.info('[DyslexAI] Inferência mock ativa.')
    return createMockInferenceProvider()
  }

  if (isNativeMobile()) {
    return createMobileInferenceProvider()
  }

  return createWebInferenceProvider()
}
