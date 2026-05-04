# Vue 3 + Vite

This template should help get you started developing with Vue 3 in Vite. The template uses Vue 3 `<script setup>` SFCs, check out the [script setup docs](https://v3.vuejs.org/api/sfc-script-setup.html#sfc-script-setup) to learn more.

Learn more about IDE Support for Vue in the [Vue Docs Scaling up Guide](https://vuejs.org/guide/scaling-up/tooling.html#ide-support).


## Configuração da API

Cria um ficheiro `.env` no frontend com:

```env
VITE_API_BASE_URL=http://localhost:5000
```

Em produção, aponta para o URL do teu backend Flask.

## Modo mock para desenvolvimento da interface

As variáveis `VITE_*` são lidas pelo Vite em tempo de desenvolvimento/build. No Android, estas variáveis ficam embutidas nos assets gerados; a app instalada não volta a ler o `.env` em runtime.

Para criar a configuração local:

```bash
cp .env.example .env
```

Para usar a inferência real:

```env
VITE_DYSLEXAI_MOCK=false
```

Para usar respostas simuladas rápidas:

```env
VITE_DYSLEXAI_MOCK=true
```

Depois de alterar `.env`, para Android é necessário reconstruir e sincronizar:

```bash
npm run build
npx cap sync android
```

Também podes ativar mock só para um comando:

```bash
VITE_DYSLEXAI_MOCK=true npm run dev
```

ou:

```bash
VITE_DYSLEXAI_MOCK=true npm run build
```

O timeout de inferência de imagem é configurado em milissegundos:

```env
VITE_IMAGE_INFERENCE_TIMEOUT_MS=900000
```

`900000` equivale a 15 minutos.
