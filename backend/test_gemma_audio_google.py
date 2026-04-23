import argparse
import json
import os
import re
import sys
import time
from pathlib import Path

from google import genai
from google.genai import types


def extract_json_object(text: str):
    if not text:
        return None

    fenced = re.search(r"```json\s*(\{.*?\})\s*```", text, re.DOTALL)
    if fenced:
        candidate = fenced.group(1)
        try:
            return json.loads(candidate)
        except Exception:
            pass

    fenced_generic = re.search(r"```\s*(\{.*?\})\s*```", text, re.DOTALL)
    if fenced_generic:
        candidate = fenced_generic.group(1)
        try:
            return json.loads(candidate)
        except Exception:
            pass

    stack = []
    start = None
    objects = []

    for i, ch in enumerate(text):
        if ch == "{":
            if not stack:
                start = i
            stack.append(ch)
        elif ch == "}":
            if stack:
                stack.pop()
                if not stack and start is not None:
                    objects.append(text[start:i + 1])

    for candidate in reversed(objects):
        try:
            return json.loads(candidate)
        except Exception:
            continue

    return None


def main():
    parser = argparse.ArgumentParser(
        description="Testar áudio com Gemma hospedado na Google GenAI API."
    )
    parser.add_argument(
        "--audio",
        required=True,
        help="Caminho para o ficheiro de áudio (.wav, .mp3, .m4a, etc.)",
    )
    parser.add_argument(
        "--model",
        default=os.getenv("GEMMA_MODEL_ID", "gemma-4-26b-a4b-it"),
        help="Modelo a testar. Ex.: gemma-4-26b-a4b-it ou gemma-4-31b-it",
    )
    parser.add_argument(
        "--api-key-env",
        default="GEMINI_API_KEY",
        help="Nome da variável de ambiente com a chave da API",
    )
    args = parser.parse_args()

    api_key = os.getenv(args.api_key_env) or os.getenv("GOOGLE_API_KEY")
    if not api_key:
        print(
            f"Erro: define a variável {args.api_key_env} ou GOOGLE_API_KEY com a tua chave.",
            file=sys.stderr,
        )
        sys.exit(1)

    audio_path = Path(args.audio)
    if not audio_path.exists():
        print(f"Erro: ficheiro não encontrado: {audio_path}", file=sys.stderr)
        sys.exit(1)

    client = genai.Client(api_key=api_key)

    prompt = """
És um assistente educativo para alunos com dislexia.

Tarefas:
1. Transcreve exatamente o que está a ser dito no áudio.
2. Reescreve a frase de forma clara e correta em português europeu, sem inventar conteúdo.
3. Segmenta a frase em palavras para leitura guiada.

Responde apenas em JSON válido com este formato:
{
  "transcription": "...",
  "clean_text": "...",
  "words": ["...", "..."],
  "language": "pt-PT"
}
""".strip()

    print(f"[1/3] Upload do áudio: {audio_path}")
    started = time.time()
    uploaded = client.files.upload(file=str(audio_path))
    print(f"Upload concluído em {time.time() - started:.2f}s")

    print(f"[2/3] Pedido ao modelo: {args.model}")
    config = types.GenerateContentConfig(
        temperature=0.2,
        response_mime_type="application/json",
    )

    started = time.time()
    response = client.models.generate_content(
        model=args.model,
        contents=[prompt, uploaded],
        config=config,
    )
    elapsed = time.time() - started

    text = getattr(response, "text", "") or ""
    print(f"Resposta recebida em {elapsed:.2f}s")

    print("\n===== RAW RESPONSE =====\n")
    print(text)

    parsed = extract_json_object(text)
    print("\n===== JSON PARSED =====\n")
    if parsed is None:
        print("Não foi possível extrair JSON válido.")
    else:
        print(json.dumps(parsed, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()