import json
import requests

from services.base_processor import BaseProcessor
from services.text_utils import ensure_result_shape


class OllamaProcessor(BaseProcessor):
    def __init__(self, base_url: str, model: str):
        self.base_url = base_url.rstrip("/")
        self.model = model

    def process_image(self, image_path: str):
        # Nesta fase, placeholder textual.
        # Mais tarde podes:
        # 1) fazer OCR antes
        # 2) enviar texto ao Ollama
        # 3) pedir simplificação e reorganização

        prompt = """
Transforma o texto escolar em duas versões:
1. original corrigido
2. simplificado para criança com dificuldade de leitura

Responde apenas em JSON com:
{
  "original_text": "...",
  "simplified_text": "..."
}
"""

        payload = {
            "model": self.model,
            "prompt": prompt,
            "stream": False
        }

        response = requests.post(
            f"{self.base_url}/api/generate",
            json=payload,
            timeout=120
        )
        response.raise_for_status()
        data = response.json()

        raw_text = data.get("response", "").strip()

        try:
            parsed = json.loads(raw_text)
            original_text = parsed.get("original_text", "")
            simplified_text = parsed.get("simplified_text", "")
        except json.JSONDecodeError:
            original_text = raw_text
            simplified_text = raw_text

        return ensure_result_shape(
            original_text=original_text,
            simplified_text=simplified_text,
            provider="ollama"
        )