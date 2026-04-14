import json
import re
from io import BytesIO
from typing import Any, Dict

from PIL import Image, ImageEnhance, ImageOps

from services.text_utils import ensure_result_shape, normalize_text, split_into_lines

CONTENT_TYPES = {"expositivo", "exercicio", "matematico_cientifico"}


def preprocess_for_reading(
    image: Image.Image,
    grayscale: bool = True,
    contrast_factor: float = 1.35,
    sharpness_factor: float = 1.2,
    upscale: float = 1.8,
    max_size: int = 1600,
) -> Image.Image:
    img = image.convert("RGB")
    if grayscale:
        img = ImageOps.grayscale(img).convert("RGB")
    if upscale and upscale > 1.0:
        w, h = img.size
        img = img.resize((int(w * upscale), int(h * upscale)), Image.Resampling.LANCZOS)
    w, h = img.size
    if max(w, h) > max_size:
        ratio = max_size / float(max(w, h))
        img = img.resize((int(w * ratio), int(h * ratio)), Image.Resampling.LANCZOS)
    if contrast_factor and contrast_factor != 1.0:
        img = ImageEnhance.Contrast(img).enhance(contrast_factor)
    if sharpness_factor and sharpness_factor != 1.0:
        img = ImageEnhance.Sharpness(img).enhance(sharpness_factor)
    return img


def compress_image_for_api(image: Image.Image, max_side: int = 1400, quality: int = 85) -> Image.Image:
    img = image.convert("RGB")
    w, h = img.size
    if max(w, h) > max_side:
        ratio = max_side / float(max(w, h))
        img = img.resize((int(w * ratio), int(h * ratio)), Image.Resampling.LANCZOS)
    buffer = BytesIO()
    img.save(buffer, format="JPEG", quality=quality, optimize=True)
    buffer.seek(0)
    return Image.open(buffer).convert("RGB")


def extract_json_object(text: str) -> Any:
    if not text:
        raise ValueError("Resposta vazia do modelo.")
    cleaned = text.strip().replace("```json", "").replace("```", "").strip()
    try:
        return json.loads(cleaned)
    except Exception:
        pass
    match = re.search(r"(\{.*\}|\[.*\])", cleaned, flags=re.DOTALL)
    if not match:
        raise ValueError("Não foi possível extrair JSON da resposta do modelo.")
    return json.loads(match.group(1))


def build_transcription_prompt() -> str:
    return (
        "Transcreve fielmente o texto presente na imagem em português europeu. "
        "Mantém a ordem, a estrutura e a pontuação sempre que possível. "
        "Não inventes conteúdo. Se alguma parte não estiver legível, usa [ilegível]. "
        "Responde apenas em JSON válido no formato: {\"raw_extracted_text\": \"...\"}."
    )


def build_cleanup_prompt(raw_text: str) -> str:
    return (
        "Corrige apenas erros óbvios de OCR no texto seguinte, sem alterar o significado. "
        "Mantém fórmulas, números, símbolos e estrutura sempre que possível. "
        "Remove ruído evidente e normaliza espaços/quebras de linha. "
        "Responde apenas em JSON válido no formato: {\"corrected_text\": \"...\"}.\n\n"
        f"TEXTO:\n{raw_text}"
    )


def build_classification_prompt(corrected_text: str) -> str:
    return (
        "Analisa o texto seguinte e classifica-o numa destas categorias: "
        "expositivo, exercicio, matematico_cientifico. "
        "Usa 'expositivo' para texto corrido explicativo ou narrativo; "
        "'exercicio' para enunciados, perguntas ou instruções; "
        "'matematico_cientifico' para conteúdo com fórmulas, cálculos, símbolos, relações quantitativas ou estrutura técnica densa. "
        "Responde apenas em JSON válido no formato: {\"content_type\": \"expositivo|exercicio|matematico_cientifico\", \"confidence\": \"baixa|media|alta\", \"reason\": \"...\"}.\n\n"
        f"TEXTO:\n{corrected_text}"
    )


def build_simplification_prompt(corrected_text: str, content_type: str) -> str:
    if content_type == "exercicio":
        instruction = (
            "Reescreve o enunciado de forma mais clara e orientadora. "
            "Mantém exatamente o que é pedido. "
            "Separa, quando fizer sentido, o contexto, o que é pedido e os dados importantes. "
            "Não resolvas o exercício e não acrescentes resultados."
        )
    elif content_type == "matematico_cientifico":
        instruction = (
            "Reescreve o conteúdo de forma mais clara sem alterar valores, símbolos, fórmulas ou relações científicas. "
            "Mantém a precisão técnica. "
            "Explica em linguagem simples o que cada parte quer dizer. "
            "Se houver um problema, organiza por dados, objetivo e relações importantes. "
            "Não resolvas o exercício, exceto se isso estiver explicitamente pedido no texto original."
        )
    else:
        instruction = (
            "Reescreve o texto em português europeu claro, simples e acessível. "
            "Mantém toda a informação importante. "
            "Usa frases curtas e ordem lógica das ideias. "
            "Não acrescentes informação nova."
        )
    return (
        f"{instruction} "
        "Responde apenas em JSON válido no formato: {\"simplified_text\": \"...\"}.\n\n"
        f"TEXTO:\n{corrected_text}"
    )


def build_reading_lines_prompt(text: str, simplified: bool = False) -> str:
    mode = "simplificado" if simplified else "original"
    return (
        f"Divide o texto {mode} em linhas curtas e fáceis de ler, adequadas para apoio à leitura. "
        "Mantém a ordem do conteúdo. "
        "Cada linha deve ser curta, natural e não excessivamente fragmentada. "
        "Responde apenas em JSON válido no formato: {\"reading_lines\": [\"...\", \"...\"]}.\n\n"
        f"TEXTO:\n{text}"
    )


def normalize_classification(data: Dict[str, Any]) -> Dict[str, str]:
    content_type = str(data.get("content_type") or data.get("tipo") or "expositivo").strip().lower()
    if content_type not in CONTENT_TYPES:
        content_type = "expositivo"
    confidence = str(data.get("confidence") or "media").strip().lower()
    if confidence not in {"baixa", "media", "alta"}:
        confidence = "media"
    reason = str(data.get("reason") or data.get("justificacao") or "").strip()
    return {
        "content_type": content_type,
        "confidence": confidence,
        "reason": reason,
    }


def ensure_adaptive_result_shape(
    original_text: str,
    simplified_text: str,
    provider: str,
    content_type: str,
    classification: Dict[str, Any],
) -> Dict[str, Any]:
    result = ensure_result_shape(original_text, simplified_text, provider)
    result["meta"]["content_type"] = content_type
    result["meta"]["classification"] = classification
    result["meta"]["adaptive_simplification"] = True
    return result


def lines_or_fallback(lines: Any, text: str):
    if isinstance(lines, list):
        normalized = [normalize_text(str(x)) for x in lines if str(x).strip()]
        normalized = [x for x in normalized if x]
        if normalized:
            return normalized
    return split_into_lines(text)
