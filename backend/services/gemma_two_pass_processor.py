import json
import time
from PIL import Image

from google import genai

from config import Config
from services.logger_utils import get_logger
from services.pipeline_adaptive_utils import preprocess_for_reading


logger = get_logger("gemma_two_pass_processor")


class GemmaTwoPassProcessor:

    def __init__(self, api_key=None, model_id=None):
        self.api_key = api_key or Config.GEMINI_API_KEY
        self.model_id = model_id or Config.GEMMA_MODEL_ID

        logger.info("Inicializar GemmaTwoPassProcessor")
        logger.info(f"Modelo: {self.model_id}")
        logger.info(f"API key definida: {'SIM' if self.api_key else 'NAO'}")

        self.client = genai.Client(api_key=self.api_key)

    # =========================
    # ENTRY POINT
    # =========================
    def process_image(self, image_path):
        start_total = time.time()

        logger.info(f"Início processamento: {image_path}")

        image = Image.open(image_path).convert("RGB")

        processed = preprocess_for_reading(image)

        # PASSO 1
        logger.info("Passo 1: leitura + classificação + linhas")
        analysis = self._analyze_image(processed)

        raw_text = analysis.get("raw_extracted_text", "")
        content_type = analysis.get("content_type", "expositivo")
        original_lines = analysis.get("original_lines", [])

        logger.info(f"Tipo identificado: {content_type}")
        logger.info(f"Chars extraídos: {len(raw_text)}")

        # PASSO 2
        logger.info("Passo 2: simplificação adaptativa")
        simplified = self._simplify_text(raw_text, content_type)

        simplified_text = simplified.get("simplified_text", "")
        simplified_lines = simplified.get("simplified_lines", [])

        elapsed_total = time.time() - start_total

        logger.info(f"Processamento concluído em {elapsed_total:.2f}s")

        return {
            "success": True,
            "original_text": raw_text,
            "simplified_text": simplified_text,
            "original_lines": original_lines,
            "simplified_lines": simplified_lines,
            "meta": {
                "provider": "gemma_two_pass",
                "model_id": self.model_id,
                "content_type": content_type,
                "elapsed_total": elapsed_total
            }
        }

    # =========================
    # PASSO 1
    # =========================
    def _analyze_image(self, image):

        prompt = """
Analisa a imagem e responde apenas em JSON válido.

Objetivos:
1. Transcrever fielmente o texto presente na imagem.
2. Identificar o tipo de conteúdo:
   - expositivo
   - exercicio
   - matematico_cientifico
3. Dividir o conteúdo em linhas curtas e claras para leitura assistida.

Regras:
- Não simplifiques.
- Não resumas.
- Não inventes conteúdo.
- Mantém a ordem do texto.
- Usa [ilegível] se necessário.

Formato:
{
  "content_type": "...",
  "raw_extracted_text": "...",
  "original_lines": ["...", "..."]
}
"""

        start = time.time()

        response = self.client.models.generate_content(
            model=self.model_id,
            contents=[prompt, image],
            config={"temperature": 0}
        )

        elapsed = time.time() - start

        text = response.text or ""

        logger.info(f"Resposta análise recebida em {elapsed:.2f}s")
        logger.debug(f"Resposta (inicio): {text[:500]}")

        parsed = self._safe_json_parse(text)

        return parsed

    # =========================
    # PASSO 2
    # =========================
    def _simplify_text(self, text, content_type):

        base_prompt = f"""
Tens de simplificar o texto para leitura acessível em português europeu.

Tipo de conteúdo: {content_type}

Texto:
{text}

Responde apenas em JSON:
{{
  "simplified_text": "...",
  "simplified_lines": ["...", "..."]
}}
"""

        if content_type == "exercicio":
            base_prompt += """
Regras:
- Não resolvas o exercício.
- Clarifica o que é pedido.
- Separa instruções.
"""

        elif content_type == "matematico_cientifico":
            base_prompt += """
Regras:
- Mantém valores, símbolos e fórmulas.
- Explica de forma simples.
- Não alteres dados.
"""

        else:
            base_prompt += """
Regras:
- Usa frases curtas.
- Simplifica linguagem.
- Mantém todas as ideias importantes.
"""

        start = time.time()

        response = self.client.models.generate_content(
            model=self.model_id,
            contents=[base_prompt],
            config={"temperature": 0.2}
        )

        elapsed = time.time() - start

        text = response.text or ""

        logger.info(f"Simplificação concluída em {elapsed:.2f}s")
        logger.debug(f"Simplificado (inicio): {text[:500]}")

        parsed = self._safe_json_parse(text)

        return parsed

    # =========================
    # JSON SAFE
    # =========================
    def _safe_json_parse(self, text):

        try:
            parsed = json.loads(text)

            if isinstance(parsed, list):
                if parsed and isinstance(parsed[0], dict):
                    return parsed[0]

            if isinstance(parsed, dict):
                return parsed

        except Exception:
            logger.warning("Falha ao fazer parse JSON, fallback")

        return {
            "raw_extracted_text": text,
            "simplified_text": text,
            "original_lines": [],
            "simplified_lines": []
        }