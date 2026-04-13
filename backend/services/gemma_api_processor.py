import json
import time
from typing import Any, Dict

import PIL.Image
from google import genai
from google.genai import types

from services.base_processor import BaseProcessor
from services.logger_utils import get_logger

logger = get_logger("gemma_api_processor")


class GemmaAPIProcessor(BaseProcessor):
    def __init__(self, api_key: str, model_id: str = "gemma-4-26b-a4b-it"):
        if not api_key:
            raise ValueError("GEMINI_API_KEY não está configurada.")

        logger.info("Inicializar GemmaAPIProcessor")
        logger.info("Modelo configurado: %s", model_id)
        logger.info("API key definida: SIM")

        self.client = genai.Client(api_key=api_key)
        self.model_id = model_id

    def _build_prompt(self) -> str:
        return (
            "Analisa a imagem e responde apenas em JSON válido com os campos: "
            "success, original_text, simplified_text, original_lines, simplified_lines, meta. "
            "Extrai o texto da imagem com a maior fidelidade possível. "
            "Depois cria uma versão simplificada e divide ambos em linhas de leitura. "
            "Não incluas markdown nem texto fora do JSON."
        )

    def _normalize_result(self, result):
        # Caso a API devolva uma lista em vez de dict
        if isinstance(result, list):
            if not result:
                result = {}
            elif isinstance(result[0], dict):
                result = result[0]
            else:
                result = {"original_text": "\n".join(str(x) for x in result)}

        if not isinstance(result, dict):
            result = {"original_text": str(result), "simplified_text": ""}

        original_text = result.get("original_text") or result.get("corrected_text") or ""
        simplified_text = result.get("simplified_text") or ""
        original_lines = result.get("original_lines") or []
        simplified_lines = result.get("simplified_lines") or []
        meta = result.get("meta") or {}

        # fallback: construir linhas a partir do texto se não vierem prontas
        if not original_lines and original_text:
            original_lines = [line.strip() for line in original_text.splitlines() if line.strip()]

        if not simplified_lines and simplified_text:
            simplified_lines = [line.strip() for line in simplified_text.splitlines() if line.strip()]

        return {
            "success": True,
            "original_text": original_text,
            "simplified_text": simplified_text,
            "original_lines": original_lines,
            "simplified_lines": simplified_lines,
            "meta": meta,
        }

    def process_image(self, image_path: str) -> Dict[str, Any]:
        logger.info("Início de process_image | image_path=%s", image_path)

        start_total = time.time()

        logger.info("Etapa: abrir imagem")
        img = PIL.Image.open(image_path).convert("RGB")
        logger.info("Imagem aberta | size=%s", img.size)

        prompt = self._build_prompt()
        logger.info("Prompt preparado | chars=%s", len(prompt))

        config = types.GenerateContentConfig(
            temperature=0.2,
            response_mime_type="application/json"
        )

        try:
            logger.info("A chamar Gemini API | model=%s", self.model_id)
            start_api = time.time()
            response = self.client.models.generate_content(
                model=self.model_id,
                contents=[prompt, img],
                config=config
            )
            logger.info(
                "Resposta recebida da API | elapsed=%.2fs | chars=%s",
                time.time() - start_api,
                len(getattr(response, "text", "") or ""),
            )
        except Exception:
            logger.exception("Falha na chamada à Gemini API")
            raise

        try:
            logger.info("A interpretar JSON da resposta")
            parsed = json.loads(response.text)
        except Exception:
            logger.warning("Falha a ler JSON direto. A tentar limpeza manual.")
            clean_text = (response.text or "").replace("```json", "").replace("```", "").strip()
            logger.info("Texto limpo preparado | chars=%s", len(clean_text))
            parsed = json.loads(clean_text)

        result = self._normalize_result(parsed)
        logger.info(
            "Processamento API concluído | success=%s | elapsed_total=%.2fs | original_lines=%s | simplified_lines=%s",
            result.get("success"),
            time.time() - start_total,
            len(result.get("original_lines", []) or []),
            len(result.get("simplified_lines", []) or []),
        )
        return result
