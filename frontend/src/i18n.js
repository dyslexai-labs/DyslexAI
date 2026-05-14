import { computed, ref, watch } from 'vue'

const STORAGE_KEY = 'dyslexai.locale'
const supportedLocales = ['pt', 'en']

const dictionary = {
  pt: {
    app: {
      subtitle: 'Leitor guiado',
      home: 'Início',
      assisted: 'Leitura assistida',
      speech: 'Leitura a partir da fala',
      validation: 'Validação',
      fullText: 'Texto completo',
      close: 'Fechar',
    },
    language: {
      label: 'Idioma',
      pt: 'PT',
      en: 'EN',
    },
    model: {
      preparing: 'A preparar o modelo local de IA...',
      firstLaunch: 'Isto pode demorar vários minutos no primeiro arranque.',
      wifi: 'Recomendado: ligação Wi-Fi.',
      downloading: 'A descarregar modelo...',
      installed: 'Modelo instalado com sucesso',
      failed: 'Falha ao preparar o modelo.',
      downloadFailed: 'Download falhou.',
      retry: 'Tentar novamente',
      checkingRuntime: 'A iniciar runtime local de IA...',
      connectionHint: 'Verifica a ligação e tenta novamente.',
      calculating: 'a calcular...',
    },
    home: {
      badge: '👋 Bem-vindo(a)!',
      title: 'Escolhe como queres começar',
      description: 'Escolhe uma imagem ou grava a tua voz. Depois lê com ajuda, passo a passo.',
      assistedTitle: 'Leitura assistida',
      assistedDescription: 'Usa uma imagem ou texto para preparar a leitura.',
      speechTitle: 'Leitura a partir da fala',
      speechDescription: 'Grava a fala do aluno e gera uma frase para leitura guiada.',
      modesLabel: 'Modos de leitura',
    },
    imageSource: {
      title: 'Escolhe a origem da imagem',
      description: 'Tira uma fotografia da página ou escolhe uma imagem guardada.',
      label: 'Origem da imagem',
      cameraTitle: 'Tirar fotografia',
      cameraDescription: 'Usa a câmara para fotografar a página.',
      galleryTitle: 'Escolher da galeria',
      galleryDescription: 'Escolhe uma imagem já guardada.',
    },
    imageConfirm: {
      title: 'Confirma a imagem',
      description: 'Verifica se a imagem está legível antes de processar.',
      label: 'Confirmar imagem',
      previewAlt: 'Pré-visualização',
      empty: 'Sem imagem selecionada.',
      process: 'Processar',
      chooseOther: 'Outra imagem',
    },
    audio: {
      headerTitle: 'Prepara a leitura com a tua voz',
      recordingRunning: 'A gravação está a decorrer.',
      recordingReady: 'A gravação está pronta.',
      intro: 'Gera uma frase. Depois grava a tua leitura.',
      recordingHelp: 'A gravação está a decorrer. Quando acabares, carrega em Parar.',
      readyHelp: 'A gravação está pronta. Podes ouvir e depois processar.',
      age: 'Idade',
      level: 'Nível',
      type: 'Tipo',
      typeSimple: 'Frase simples',
      typeRhyme: 'Lengalenga',
      typeTongueTwister: 'Trava-línguas',
      generate: 'Gerar frase',
      generating: 'A gerar...',
      newPhrase: 'Nova frase',
      preparation: 'Preparação',
      phraseToRead: 'Frase a ler',
      generatingPhrase: 'A gerar frase...',
      startHint: 'Gera uma frase para começar.',
      syllables: 'Sílabas',
      recording: 'A gravar',
      recorded: 'Gravação pronta',
      waiting: 'À espera de gravação',
      record: 'Gravar',
      recordingButton: 'A gravar...',
      stop: 'Parar',
      clear: 'Limpar',
      process: 'Processar',
    },
    processing: {
      preparing: 'A preparar...',
      titleImage: 'A preparar a leitura',
      titleAudio: 'A preparar a leitura a partir da fala',
      sendingImage: 'A enviar imagem...',
      processingText: 'A processar texto...',
      sendingAudio: 'A enviar áudio...',
      transcribing: 'A transcrever a fala...',
      done: 'Concluído.',
    },
    reader: {
      noText: 'Sem texto disponível.',
      speechResultLabel: 'Resultado da leitura por voz',
      expectedPhrase: 'Frase esperada',
      transcribedPhrase: 'Frase transcrita',
      listen: 'Ouvir',
      listenComment: 'Ouvir comentário',
      line: 'Linha',
      word: 'Palavra',
      original: 'Original',
      simplified: 'Simplificado',
      spoken: 'Falado',
      syllables: 'Sílabas',
      sound: 'Som',
      singleLine: 'Linha única',
      readingToEnd: 'A ler até ao fim',
      toEnd: 'Até ao fim',
      restart: 'Reiniciar',
      stop: 'Parar',
      font: 'Fonte',
      speed: 'Velocidade',
      colors: 'Cores de leitura',
      yellow: 'Amarelo',
      blue: 'Azul',
      green: 'Verde',
      pink: 'Rosa suave',
      nextLine: 'Passar à linha seguinte',
    },
    errors: {
      photoEmpty: 'A fotografia não devolveu dados.',
      photoFailed: 'Não foi possível tirar fotografia.',
      imageEmpty: 'A imagem não devolveu dados.',
      pickImageFailed: 'Não foi possível escolher imagem.',
      phraseFirst: 'Gera primeiro uma frase para o aluno ler.',
      recordingStartFailed: 'Não foi possível iniciar a gravação de áudio.',
      recordingUnsupported: 'A gravação de áudio não é suportada neste navegador.',
      recordingFailed: 'Não foi possível gravar o áudio.',
      microphoneFailed: 'Não foi possível aceder ao microfone.',
      nativeAudioEmpty: 'A gravação WAV nativa não devolveu áudio.',
      recordingStopFailed: 'Não foi possível terminar a gravação de áudio.',
      invalidDataUrl: 'Data URL inválido.',
      phraseFailed: 'Não foi possível gerar a frase.',
      phraseGeneric: 'Erro ao gerar frase.',
      readImageFailed: 'Não foi possível ler a imagem selecionada.',
      noPreparedImage: 'Não existe imagem preparada.',
      noSpeech: 'Não ouvi fala suficiente na gravação. Tenta gravar novamente.',
      noImageSelected: 'Nenhuma imagem selecionada.',
      processImageFailed: 'Ocorreu um erro ao processar a imagem.',
      noRecording: 'Ainda não existe uma gravação pronta.',
      expectedPhraseFirst: 'Gera primeiro a frase que o aluno deve ler.',
      processAudioFailed: 'Ocorreu um erro ao processar o áudio.',
    },
  },
  en: {
    app: {
      subtitle: 'Guided reader',
      home: 'Home',
      assisted: 'Assisted reading',
      speech: 'Reading from speech',
      validation: 'Validation',
      fullText: 'Full text',
      close: 'Close',
    },
    language: {
      label: 'Language',
      pt: 'PT',
      en: 'EN',
    },
    model: {
      preparing: 'Preparing local AI model...',
      firstLaunch: 'This may take several minutes on first launch.',
      wifi: 'Recommended: Wi-Fi connection.',
      downloading: 'Downloading model...',
      installed: 'Model installed successfully',
      failed: 'Local runtime failed to start.',
      downloadFailed: 'Download failed.',
      retry: 'Retry download',
      checkingRuntime: 'Starting local AI runtime...',
      connectionHint: 'Please check your connection and try again.',
      calculating: 'calculating...',
    },
    home: {
      badge: '👋 Welcome!',
      title: 'Choose how to start',
      description: 'Choose an image or record your voice. Then read with guided support, step by step.',
      assistedTitle: 'Assisted reading',
      assistedDescription: 'Use an image or text to prepare the reading.',
      speechTitle: 'Reading from speech',
      speechDescription: 'Record the student voice and generate a phrase for guided reading.',
      modesLabel: 'Reading modes',
    },
    imageSource: {
      title: 'Choose image source',
      description: 'Take a photo of the page or choose a saved image.',
      label: 'Image source',
      cameraTitle: 'Take photo',
      cameraDescription: 'Use the camera to photograph the page.',
      galleryTitle: 'Choose from gallery',
      galleryDescription: 'Choose an image already saved.',
    },
    imageConfirm: {
      title: 'Confirm image',
      description: 'Check that the image is readable before processing.',
      label: 'Confirm image',
      previewAlt: 'Preview',
      empty: 'No image selected.',
      process: 'Process',
      chooseOther: 'Another image',
    },
    audio: {
      headerTitle: 'Prepare reading with your voice',
      recordingRunning: 'Recording is in progress.',
      recordingReady: 'The recording is ready.',
      intro: 'Generate a phrase. Then record your reading.',
      recordingHelp: 'Recording is in progress. Press Stop when finished.',
      readyHelp: 'The recording is ready. You can listen and then process it.',
      age: 'Age',
      level: 'Level',
      type: 'Type',
      typeSimple: 'Simple sentence',
      typeRhyme: 'Rhyme',
      typeTongueTwister: 'Tongue twister',
      generate: 'Generate phrase',
      generating: 'Generating...',
      newPhrase: 'New phrase',
      preparation: 'Preparation',
      phraseToRead: 'Phrase to read',
      generatingPhrase: 'Generating phrase...',
      startHint: 'Generate a phrase to start.',
      syllables: 'Syllables',
      recording: 'Recording',
      recorded: 'Recording ready',
      waiting: 'Waiting for recording',
      record: 'Record',
      recordingButton: 'Recording...',
      stop: 'Stop',
      clear: 'Clear',
      process: 'Process',
    },
    processing: {
      preparing: 'Preparing...',
      titleImage: 'Preparing the reading',
      titleAudio: 'Preparing reading from speech',
      sendingImage: 'Sending image...',
      processingText: 'Processing text...',
      sendingAudio: 'Sending audio...',
      transcribing: 'Transcribing speech...',
      done: 'Done.',
    },
    reader: {
      noText: 'No text available.',
      speechResultLabel: 'Speech reading result',
      expectedPhrase: 'Expected phrase',
      transcribedPhrase: 'Transcribed phrase',
      listen: 'Listen',
      listenComment: 'Listen to comment',
      line: 'Line',
      word: 'Word',
      original: 'Original',
      simplified: 'Simplified',
      spoken: 'Spoken',
      syllables: 'Syllables',
      sound: 'Sound',
      singleLine: 'Single line',
      readingToEnd: 'Reading to the end',
      toEnd: 'To the end',
      restart: 'Restart',
      stop: 'Stop',
      font: 'Font',
      speed: 'Speed',
      colors: 'Reading colors',
      yellow: 'Yellow',
      blue: 'Blue',
      green: 'Green',
      pink: 'Soft pink',
      nextLine: 'Move to the next line',
    },
    errors: {
      photoEmpty: 'The photo did not return data.',
      photoFailed: 'Could not take photo.',
      imageEmpty: 'The image did not return data.',
      pickImageFailed: 'Could not choose image.',
      phraseFirst: 'Generate a phrase for the student to read first.',
      recordingStartFailed: 'Could not start audio recording.',
      recordingUnsupported: 'Audio recording is not supported in this browser.',
      recordingFailed: 'Could not record audio.',
      microphoneFailed: 'Could not access the microphone.',
      nativeAudioEmpty: 'Native WAV recording did not return audio.',
      recordingStopFailed: 'Could not finish audio recording.',
      invalidDataUrl: 'Invalid data URL.',
      phraseFailed: 'Could not generate the phrase.',
      phraseGeneric: 'Error generating phrase.',
      readImageFailed: 'Could not read the selected image.',
      noPreparedImage: 'There is no prepared image.',
      noSpeech: 'I did not hear enough speech in the recording. Try recording again.',
      noImageSelected: 'No image selected.',
      processImageFailed: 'An error occurred while processing the image.',
      noRecording: 'There is no recording ready yet.',
      expectedPhraseFirst: 'Generate the phrase the student should read first.',
      processAudioFailed: 'An error occurred while processing the audio.',
    },
  },
}

function readInitialLocale() {
  const saved = window.localStorage?.getItem(STORAGE_KEY)
  if (supportedLocales.includes(saved)) return saved
  const browserLocale = String(window.navigator?.language || '').toLowerCase()
  return browserLocale.startsWith('en') ? 'en' : 'pt'
}

export const locale = ref(readInitialLocale())

export const currentLanguageLabel = computed(() => dictionary[locale.value]?.language?.[locale.value] || locale.value.toUpperCase())

watch(locale, value => {
  if (supportedLocales.includes(value)) {
    window.localStorage?.setItem(STORAGE_KEY, value)
    document.documentElement.lang = value === 'en' ? 'en' : 'pt-PT'
  }
}, { immediate: true })

export function setLocale(value) {
  if (supportedLocales.includes(value)) {
    locale.value = value
  }
}

export function toggleLocale() {
  locale.value = locale.value === 'en' ? 'pt' : 'en'
}

export function t(key) {
  const parts = String(key).split('.')
  let value = dictionary[locale.value]
  for (const part of parts) {
    value = value?.[part]
  }

  if (typeof value === 'string') return value

  let fallback = dictionary.pt
  for (const part of parts) {
    fallback = fallback?.[part]
  }
  return typeof fallback === 'string' ? fallback : key
}
