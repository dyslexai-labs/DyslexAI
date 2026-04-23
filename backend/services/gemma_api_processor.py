import json
import mimetypes
import time
from pathlib import Path
from typing import Any, Dict

import PIL.Image
from google import genai
from google.genai import types

from services.base_processor import BaseProcessor
from services.logger_utils import get_logger

logger = get_logger("gemma_api_processor")


class GemmaAPIProcessor(BaseProcessor):
    def __init__(self, api_key: str, model_id: str = "gemma-4-26b-a4b-it", audio_model_id: str = "gemini-2.5-flash"):
        if not api_key:
            raise ValueError("GOOGLE_API_KEY / GEMINI_API_KEY não está configurada.")

        logger.info("Inicializar GemmaAPIProcessor")
        logger.info("Modelo imagem configurado: %s", model_id)
        logger.info("Modelo áudio configurado: %s", audio_model_id)
        logger.info("API key definida: SIM")

        self.client = genai.Client(api_key=api_key)
        self.model_id = model_id
        self.audio_model_id = audio_model_id

    def _build_image_prompt(self) -> str:
        return (
            "Analisa a imagem e responde apenas em JSON válido com os campos: "
            "success, original_text, simplified_text, original_lines, simplified_lines, meta. "
            "Extrai o texto da imagem com a maior fidelidade possível. "
            "Depois cria uma versão simplificada e divide ambos em linhas de leitura. "
            "Mantém o português europeu quando possível. "
            "Não incluas markdown nem texto fora do JSON."
        )

    def _build_audio_prompt(self) -> str:
        return (
            "Analisa o áudio e responde apenas em JSON válido com os campos: "
            "success, transcription, clean_text, words, original_text, simplified_text, original_lines, simplified_lines, meta. "
            "Tarefas: "
            "1) Transcreve com a maior fidelidade possível o que o aluno disse em português. "
            "2) Cria clean_text como versão ligeiramente corrigida e legível da frase, preservando o sentido do que foi dito. "
            "3) Define words como lista de palavras de clean_text. "
            "4) Copia clean_text para original_text. "
            "5) Cria simplified_text, se útil, com formulação mais clara mas sem alterar demasiado o significado. "
            "6) Divide original_text e simplified_text em original_lines e simplified_lines adequadas a leitura guiada. "
            "7) Em meta inclui language e source='audio'. "
            "Não incluas markdown nem texto fora do JSON."
        )

    def _parse_json_response(self, response_text: str) -> Dict[str, Any]:
        raw = (response_text or "").strip()
        if not raw:
            raise ValueError("Resposta vazia da Google API.")

        candidates = [raw]
        if "```json" in raw:
            candidates.append(raw.replace("```json", "").replace("```", "").strip())
        elif "```" in raw:
            candidates.append(raw.replace("```", "").strip())

        start = raw.find("{")
        end = raw.rfind("}")
        if start != -1 and end != -1 and end > start:
            candidates.append(raw[start:end+1])

        for candidate in candidates:
            try:
                return json.loads(candidate)
            except Exception:
                continue

        raise ValueError("Não foi possível interpretar JSON válido da resposta da Google API.")

    def _normalize_lines(self, text_value: str, provided_lines: Any) -> list[str]:
        if isinstance(provided_lines, list) and provided_lines:
            normalized = [str(line).strip() for line in provided_lines if str(line).strip()]
            if normalized:
                return normalized
        return [line.strip() for line in str(text_value or "").splitlines() if line.strip()]

    def _normalize_image_result(self, result):
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
        original_lines = self._normalize_lines(original_text, result.get("original_lines"))
        simplified_lines = self._normalize_lines(simplified_text, result.get("simplified_lines"))
        meta = result.get("meta") or {}
        if not isinstance(meta, dict):
            meta = {"raw_meta": meta}
        meta.setdefault("provider", "google_api")
        meta.setdefault("source", "image")
        meta.setdefault("model_id", self.model_id)

        return {
            "success": True,
            "original_text": original_text,
            "simplified_text": simplified_text,
            "original_lines": original_lines,
            "simplified_lines": simplified_lines,
            "meta": meta,
        }

    def _normalize_audio_result(self, result: Dict[str, Any]) -> Dict[str, Any]:
        transcription = str(result.get("transcription") or "").strip()
        clean_text = str(result.get("clean_text") or transcription).strip()
        simplified_text = str(result.get("simplified_text") or clean_text).strip()
        words = result.get("words") or []
        if not isinstance(words, list) or not words:
            words = [word for word in clean_text.split() if word]
        else:
            words = [str(word).strip() for word in words if str(word).strip()]

        original_text = str(result.get("original_text") or clean_text).strip()
        original_lines = self._normalize_lines(original_text, result.get("original_lines"))
        simplified_lines = self._normalize_lines(simplified_text, result.get("simplified_lines"))

        meta = result.get("meta") or {}
        if not isinstance(meta, dict):
            meta = {"raw_meta": meta}
        meta.setdefault("provider", "google_api")
        meta.setdefault("source", "audio")
        meta.setdefault("model_id", self.audio_model_id)
        meta.setdefault("language", result.get("language") or "pt")

        return {
            "success": bool(result.get("success", True)),
            "transcription": transcription,
            "clean_text": clean_text,
            "words": words,
            "original_text": original_text,
            "simplified_text": simplified_text,
            "original_lines": original_lines,
            "simplified_lines": simplified_lines,
            "meta": meta,
        }

    def process_image(self, image_path: str) -> Dict[str, Any]:
        logger.info("Início de process_image | image_path=%s", image_path)
        start_total = time.time()
        img = PIL.Image.open(image_path).convert("RGB")
        prompt = self._build_image_prompt()

        config = types.GenerateContentConfig(
            temperature=0.2,
            response_mime_type="application/json"
        )

        response = self.client.models.generate_content(
            model=self.model_id,
            contents=[prompt, img],
            config=config
        )

        parsed = self._parse_json_response(getattr(response, "text", "") or "")
        result = self._normalize_image_result(parsed)
        logger.info(
            "Processamento imagem concluído | elapsed_total=%.2fs | original_lines=%s | simplified_lines=%s",
            time.time() - start_total,
            len(result.get("original_lines", []) or []),
            len(result.get("simplified_lines", []) or []),
        )
        return result

    def process_audio(self, audio_path: str) -> Dict[str, Any]:
        logger.info("Início de process_audio | audio_path=%s", audio_path)
        start_total = time.time()
        prompt = self._build_audio_prompt()
        mime_type = mimetypes.guess_type(audio_path)[0] or "audio/wav"

        uploaded_file = None
        try:
            uploaded_file = self.client.files.upload(file=audio_path)
            logger.info("Áudio enviado para Files API | mime=%s | uri=%s", mime_type, getattr(uploaded_file, "uri", None))

            config = types.GenerateContentConfig(
                temperature=0.1,
                response_mime_type="application/json"
            )
            response = self.client.models.generate_content(
                model=self.audio_model_id,
                contents=[prompt, uploaded_file],
                config=config,
            )
            parsed = self._parse_json_response(getattr(response, "text", "") or "")
            result = self._normalize_audio_result(parsed)
            logger.info(
                "Processamento áudio concluído | elapsed_total=%.2fs | transcription_len=%s",
                time.time() - start_total,
                len(result.get("transcription", "") or ""),
            )
            return result
        finally:
            file_name = getattr(uploaded_file, "name", None)
            if file_name:
                try:
                    self.client.files.delete(name=file_name)
                    logger.info("Ficheiro remoto removido da Files API | name=%s", file_name)
                except Exception:
                    logger.warning("Falha ao remover ficheiro remoto da Files API | name=%s", file_name, exc_info=True)
