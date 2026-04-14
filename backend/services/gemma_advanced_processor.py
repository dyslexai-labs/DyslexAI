import time
from typing import Any, Dict

import PIL.Image
from google import genai
from google.genai import types

from services.base_processor import BaseProcessor
from services.logger_utils import get_logger
from services.pipeline_adaptive_utils import (
    build_classification_prompt,
    build_cleanup_prompt,
    build_reading_lines_prompt,
    build_simplification_prompt,
    build_transcription_prompt,
    compress_image_for_api,
    ensure_adaptive_result_shape,
    extract_json_object,
    lines_or_fallback,
    normalize_classification,
    preprocess_for_reading,
)

logger = get_logger("gemma_advanced_processor")


class GemmaAdvancedProcessor(BaseProcessor):
    def __init__(self, api_key: str, model_id: str = "gemma-4-26b-a4b-it"):
        if not api_key:
            raise ValueError("GEMINI_API_KEY não está configurada.")

        self.client = genai.Client(api_key=api_key)
        self.model_id = model_id
        logger.info("Inicializar GemmaAdvancedProcessor | model_id=%s | api_key_definida=SIM", model_id)

    def _call_model(self, prompt: str, image=None, temperature: float = 0.2, timeout_note: str = "") -> Any:
        logger.info(
            "A chamar Gemini API%s | model=%s | prompt_chars=%s | has_image=%s",
            timeout_note,
            self.model_id,
            len(prompt),
            "SIM" if image is not None else "NAO",
        )
        config = types.GenerateContentConfig(
            temperature=temperature,
            response_mime_type="application/json",
        )
        start = time.time()
        response = self.client.models.generate_content(
            model=self.model_id,
            contents=[prompt, image] if image is not None else prompt,
            config=config,
        )
        text = getattr(response, "text", "") or ""
        logger.info("Resposta recebida da API | elapsed=%.2fs | chars=%s", time.time() - start, len(text))
        return extract_json_object(text)

    def process_image(self, image_path: str) -> Dict[str, Any]:
        logger.info("Início de process_image | image_path=%s", image_path)
        total_start = time.time()

        logger.info("Etapa: abrir imagem")
        image = PIL.Image.open(image_path).convert("RGB")
        logger.info("Imagem aberta | size=%s", image.size)

        logger.info("Etapa: preprocess_for_reading")
        processed_image = preprocess_for_reading(image)
        api_image = compress_image_for_api(processed_image)
        logger.info("Imagem preparada para API | size=%s", api_image.size)

        logger.info("Etapa: transcribe_image")
        transcribed = self._call_model(build_transcription_prompt(), image=api_image, timeout_note=" [transcription]")
        raw_extracted_text = str(transcribed.get("raw_extracted_text") or transcribed.get("text") or "").strip()
        logger.info("Transcrição obtida | chars=%s", len(raw_extracted_text))

        logger.info("Etapa: cleanup_text")
        cleaned = self._call_model(build_cleanup_prompt(raw_extracted_text), timeout_note=" [cleanup]")
        corrected_text = str(cleaned.get("corrected_text") or raw_extracted_text).strip()
        logger.info("Texto corrigido | chars=%s", len(corrected_text))

        logger.info("Etapa: classify_content")
        classification_raw = self._call_model(build_classification_prompt(corrected_text), timeout_note=" [classification]")
        classification = normalize_classification(classification_raw if isinstance(classification_raw, dict) else {})
        content_type = classification["content_type"]
        logger.info(
            "Classificação concluída | content_type=%s | confidence=%s | reason=%s",
            classification["content_type"],
            classification["confidence"],
            classification["reason"],
        )

        logger.info("Etapa: build_reading_lines original")
        original_lines_raw = self._call_model(build_reading_lines_prompt(corrected_text, simplified=False), timeout_note=" [reading_original]")
        original_lines = lines_or_fallback(original_lines_raw.get("reading_lines") if isinstance(original_lines_raw, dict) else None, corrected_text)
        logger.info("Linhas originais geradas | count=%s", len(original_lines))

        logger.info("Etapa: simplify_text adaptive")
        simplified_raw = self._call_model(build_simplification_prompt(corrected_text, content_type), timeout_note=" [simplify]")
        simplified_text = str(simplified_raw.get("simplified_text") or "").strip()
        logger.info("Texto simplificado | chars=%s", len(simplified_text))

        logger.info("Etapa: build_reading_lines simplified")
        simplified_lines_raw = self._call_model(build_reading_lines_prompt(simplified_text, simplified=True), timeout_note=" [reading_simplified]")
        simplified_lines = lines_or_fallback(simplified_lines_raw.get("reading_lines") if isinstance(simplified_lines_raw, dict) else None, simplified_text)
        logger.info("Linhas simplificadas geradas | count=%s", len(simplified_lines))

        logger.info("Etapa: ensure_result_shape")
        result = ensure_adaptive_result_shape(
            original_text=corrected_text,
            simplified_text=simplified_text,
            provider="gemma_advanced_api",
            content_type=content_type,
            classification=classification,
        )
        result["original_lines"] = original_lines or result["original_lines"]
        result["simplified_lines"] = simplified_lines or result["simplified_lines"]
        result["meta"]["model_id"] = self.model_id

        logger.info(
            "Processamento avançado concluído | success=%s | elapsed_total=%.2fs | original_lines=%s | simplified_lines=%s",
            result.get("success"),
            time.time() - total_start,
            len(result.get("original_lines", []) or []),
            len(result.get("simplified_lines", []) or []),
        )
        return result
