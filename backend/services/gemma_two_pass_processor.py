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
        logger.info("Modelo: %s", self.model_id)
        logger.info("API key definida: %s", "SIM" if self.api_key else "NAO")

        self.client = genai.Client(api_key=self.api_key)

    def process_image(self, image_path):
        start_total = time.time()

        logger.info("Início processamento: %s", image_path)

        image = Image.open(image_path).convert("RGB")
        processed = preprocess_for_reading(image)

        logger.info("Passo 1: leitura + classificação + linhas")
        analysis = self._analyze_image(processed)

        raw_text = (
            analysis.get("original_text")
            or analysis.get("raw_extracted_text")
            or ""
        )
        content_type = analysis.get("content_type", "expositivo")
        original_lines = analysis.get("original_lines") or []

        if not original_lines and raw_text:
            original_lines = [
                line.strip()
                for line in raw_text.splitlines()
                if line.strip()
            ]

        logger.info("Tipo identificado: %s", content_type)
        logger.info("Chars extraídos: %s", len(raw_text))
        logger.info("Linhas originais: %s", len(original_lines))

        logger.info("Passo 2: simplificação adaptativa")
        simplified = self._simplify_text(raw_text, content_type)

        simplified_text = simplified.get("simplified_text", "") or ""
        simplified_lines = simplified.get("simplified_lines") or []

        if not simplified_lines and simplified_text:
            simplified_lines = [
                line.strip()
                for line in simplified_text.splitlines()
                if line.strip()
            ]

        logger.info("Chars simplificados: %s", len(simplified_text))
        logger.info("Linhas simplificadas: %s", len(simplified_lines))

        elapsed_total = time.time() - start_total
        logger.info("Processamento concluído em %.2fs", elapsed_total)

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
                "elapsed_total": elapsed_total,
            },
        }

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

        logger.info("Resposta análise recebida em %.2fs", elapsed)
        logger.debug("Resposta análise (início): %s", text[:500])

        parsed = self._safe_json_parse(text, stage="analysis")
        return parsed

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

        logger.info("Simplificação concluída em %.2fs", elapsed)
        logger.debug("Resposta simplificação (início): %s", text[:500])

        parsed = self._safe_json_parse(text, stage="simplify")
        return parsed

    def _safe_json_parse(self, text, stage="unknown"):
        cleaned = (text or "").strip()

        if cleaned.startswith("```json"):
            cleaned = cleaned[len("```json"):].strip()
        elif cleaned.startswith("```"):
            cleaned = cleaned[len("```"):].strip()

        if cleaned.endswith("```"):
            cleaned = cleaned[:-3].strip()

        try:
            parsed = json.loads(cleaned)

            if isinstance(parsed, list):
                if parsed and isinstance(parsed[0], dict):
                    logger.info("JSON parsed com sucesso (%s) a partir de lista", stage)
                    return parsed[0]

            if isinstance(parsed, dict):
                logger.info("JSON parsed com sucesso (%s)", stage)
                return parsed

        except Exception as e:
            logger.warning("Falha ao fazer parse JSON (%s): %s", stage, e)
            logger.debug("Texto bruto (%s): %s", stage, cleaned[:1000])

        if stage == "analysis":
            return {
                "content_type": "expositivo",
                "raw_extracted_text": cleaned,
                "original_lines": [],
            }

        return {
            "simplified_text": cleaned,
            "simplified_lines": [],
        }