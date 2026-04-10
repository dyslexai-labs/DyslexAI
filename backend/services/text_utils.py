import re
from typing import List


def normalize_text(text: str) -> str:
    if not text:
        return ""
    text = text.replace("\r", "\n")
    text = re.sub(r"\n{3,}", "\n\n", text)
    text = re.sub(r"[ \t]+", " ", text)
    return text.strip()


def split_into_lines(text: str) -> List[str]:
    text = normalize_text(text)
    if not text:
        return []

    explicit_lines = [line.strip() for line in text.split("\n") if line.strip()]
    if len(explicit_lines) > 1:
        return explicit_lines

    sentence_lines = re.findall(r"[^.!?]+[.!?]?", text)
    sentence_lines = [line.strip() for line in sentence_lines if line.strip()]
    return sentence_lines if sentence_lines else [text]


def ensure_result_shape(original_text: str, simplified_text: str, provider: str):
    original_text = normalize_text(original_text)
    simplified_text = normalize_text(simplified_text)

    return {
        "success": True,
        "original_text": original_text,
        "simplified_text": simplified_text,
        "original_lines": split_into_lines(original_text),
        "simplified_lines": split_into_lines(simplified_text),
        "meta": {
            "provider": provider
        }
    }