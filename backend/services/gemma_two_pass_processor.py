import json
import time
from io import BytesIO

from PIL import Image, ImageEnhance

from google import genai

from config import Config
from services.logger_utils import get_logger


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
        processed = self._prepare_image_for_fast_reading(image)

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
            original_lines = self._split_lines_fallback(raw_text)

        logger.info("Tipo identificado: %s", content_type)
        logger.info("Chars extraídos: %s", len(raw_text))
        logger.info("Linhas originais: %s", len(original_lines))

        logger.info("Passo 2: simplificação adaptativa orientada para dislexia")
        simplified = self._simplify_text_for_dyslexia(raw_text, content_type)

        simplified_text = simplified.get("simplified_text", "") or ""
        simplified_lines = simplified.get("simplified_lines") or []

        if not simplified_lines and simplified_text:
            simplified_lines = self._split_lines_fallback(simplified_text)

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
                "adaptive_simplification": True,
                "reading_support_target": "dyslexia",
            },
        }

    def _prepare_image_for_fast_reading(self, image: Image.Image) -> Image.Image:
        """
        Reduz o custo da imagem mantendo boa legibilidade para OCR/reading.
        Estratégia:
        - RGB
        - ligeiro aumento de contraste e nitidez
        - resize para janela útil
        - recompressão leve para imagem mais simples
        """
        width, height = image.size
        longest = max(width, height)

        logger.info("Imagem original | size=%sx%s", width, height)

        # Heurística simples para equilibrar qualidade e latência
        target_longest = 1300
        min_longest = 1000

        if longest > target_longest:
            scale = target_longest / float(longest)
            new_size = (max(1, int(width * scale)), max(1, int(height * scale)))
            image = image.resize(new_size, Image.LANCZOS)
            logger.info("Imagem reduzida | new_size=%s", image.size)

        elif longest < min_longest:
            scale = min_longest / float(longest)
            new_size = (max(1, int(width * scale)), max(1, int(height * scale)))
            image = image.resize(new_size, Image.LANCZOS)
            logger.info("Imagem aumentada moderadamente | new_size=%s", image.size)

        # Ajustes leves para texto
        image = ImageEnhance.Contrast(image).enhance(1.35)
        image = ImageEnhance.Sharpness(image).enhance(1.25)

        # Recompressão leve para simplificar payload
        buffer = BytesIO()
        image.save(buffer, format="JPEG", quality=82, optimize=True)
        buffer.seek(0)
        optimized = Image.open(buffer).convert("RGB")

        logger.info("Imagem otimizada | final_size=%s", optimized.size)
        return optimized

    def _analyze_image(self, image: Image.Image) -> dict:
        prompt = """
Lê o texto presente na imagem e responde apenas em JSON válido.

Tarefas:
1. Transcreve fielmente o texto da imagem.
2. Identifica o tipo de conteúdo:
   - expositivo
   - exercicio
   - matematico_cientifico
3. Divide o texto em linhas curtas para leitura assistida.

Regras:
- Não simplifiques.
- Não resumas.
- Não inventes conteúdo.
- Mantém a ordem do texto.
- Mantém números, símbolos, percentagens e unidades.
- Se alguma parte estiver ilegível, usa [ilegível].
- Não uses markdown.
- Não uses blocos de código.
- As linhas devem ser curtas, naturais e fáceis de seguir.
- Evita partir números, unidades ou expressões importantes.

Formato:
{
  "content_type": "expositivo|exercicio|matematico_cientifico",
  "raw_extracted_text": "texto completo",
  "original_lines": ["linha 1", "linha 2"]
}
""".strip()

        start = time.time()
        logger.info(
            "Passo 1: a chamar Gemini API | model=%s | image_size=%s",
            self.model_id,
            image.size,
        )

        response = self.client.models.generate_content(
            model=self.model_id,
            contents=[prompt, image],
            config={"temperature": 0}
        )

        elapsed = time.time() - start
        text = response.text or ""

        logger.info("Passo 1: resposta Gemini recebida em %.2fs", elapsed)
        logger.debug("Resposta análise (início): %s", text[:800])

        parsed = self._safe_json_parse(text, stage="analysis")
        return parsed

    def _simplify_text_for_dyslexia(self, text: str, content_type: str) -> dict:
        if content_type == "exercicio":
            task_instructions = """
Reescreve este enunciado em português europeu para apoio à leitura de uma pessoa com dislexia.

Objetivo:
- tornar o enunciado mais claro e mais fácil de seguir
- manter exatamente o que é pedido

Regras:
- não resolvas o exercício
- separa os dados importantes do que é pedido
- usa frases curtas
- torna as instruções mais claras
- mantém números, valores, unidades e condições
- não acrescentes informação nova
- não elimines informação importante
- divide o texto em linhas curtas para leitura assistida
"""
        elif content_type == "matematico_cientifico":
            task_instructions = """
Reescreve este conteúdo em português europeu para apoio à leitura de uma pessoa com dislexia.

Objetivo:
- tornar o conteúdo mais claro e mais fácil de interpretar
- manter o rigor técnico

Regras:
- mantém números, símbolos, fórmulas, unidades e relações
- não alteres dados
- explica com frases curtas e claras
- organiza a informação de forma mais legível
- se houver um problema, separa dados e objetivo
- não resolvas o exercício, exceto se isso for explicitamente pedido
- divide o texto em linhas curtas para leitura assistida
"""
        else:
            task_instructions = """
Reescreve o texto em português europeu para apoio à leitura de uma pessoa com dislexia.

Objetivo:
- tornar o texto mais claro, mais direto e mais fácil de ler
- manter toda a informação importante

Regras:
- usa frases curtas
- usa linguagem simples
- evita construções longas ou ambíguas
- mantém a ordem das ideias
- não acrescentes informação nova
- não elimines informação importante
- divide o texto em linhas curtas para leitura assistida
"""

        prompt = f"""
{task_instructions}

Texto:
{text}

Responde apenas em JSON válido.
Não uses markdown.
Não uses blocos de código.

Formato:
{{
  "simplified_text": "texto simplificado completo",
  "simplified_lines": ["linha 1", "linha 2"]
}}
""".strip()

        start = time.time()
        logger.info(
            "Passo 2: a chamar Gemini API | model=%s | content_type=%s | chars=%s",
            self.model_id,
            content_type,
            len(text),
        )

        response = self.client.models.generate_content(
            model=self.model_id,
            contents=[prompt],
            config={"temperature": 0.2}
        )

        elapsed = time.time() - start
        response_text = response.text or ""

        logger.info("Passo 2: resposta Gemini recebida em %.2fs", elapsed)
        logger.debug("Resposta simplificação (início): %s", response_text[:800])

        parsed = self._safe_json_parse(response_text, stage="simplify")
        return parsed

    def _safe_json_parse(self, text: str, stage: str = "unknown") -> dict:
        cleaned = (text or "").strip()

        if cleaned.startswith("```json"):
            cleaned = cleaned[len("```json"):].strip()
        elif cleaned.startswith("```"):
            cleaned = cleaned[len("```"):].strip()

        if cleaned.endswith("```"):
            cleaned = cleaned[:-3].strip()

        try:
            parsed = json.loads(cleaned)

            if isinstance(parsed, list) and parsed and isinstance(parsed[0], dict):
                logger.info("JSON parsed com sucesso (%s) a partir de lista", stage)
                return parsed[0]

            if isinstance(parsed, dict):
                logger.info("JSON parsed com sucesso (%s)", stage)
                return parsed

        except Exception as e:
            logger.warning("Falha ao fazer parse JSON (%s): %s", stage, e)
            logger.debug("Texto bruto (%s): %s", stage, cleaned[:1500])

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

    def _split_lines_fallback(self, text: str, max_chars: int = 28) -> list[str]:
        """
        Fallback simples quando o modelo não devolve linhas.
        Mantém unidades de sentido curtas sem partir brutalmente o texto.
        """
        words = text.split()
        if not words:
            return []

        lines = []
        current = []

        for word in words:
            candidate = " ".join(current + [word]).strip()
            if len(candidate) <= max_chars or not current:
                current.append(word)
            else:
                lines.append(" ".join(current).strip())
                current = [word]

        if current:
            lines.append(" ".join(current).strip())

        return [line for line in lines if line]