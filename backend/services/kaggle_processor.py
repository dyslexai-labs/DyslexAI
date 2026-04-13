import json
import time

from gradio_client import Client, handle_file
from services.base_processor import BaseProcessor
from services.logger_utils import get_logger

logger = get_logger("kaggle_processor")


class KaggleProcessor(BaseProcessor):
    def __init__(
        self,
        bridge_url: str,
        username: str = "",
        password: str = "",
        api_name: str = "/process_image",
    ):
        self.bridge_url = bridge_url
        self.username = username or ""
        self.password = password or ""
        self.api_name = api_name

        if not self.bridge_url:
            raise ValueError("KAGGLE_BRIDGE_URL não está configurado.")

        client_kwargs = {
            "src": self.bridge_url,
            "httpx_kwargs": {"timeout": 1800.0},
        }

        if self.username:
            client_kwargs["auth"] = (self.username, self.password)

        logger.info(
            "Inicializar KaggleProcessor | bridge_url=%s | api_name=%s | auth=%s",
            self.bridge_url,
            self.api_name,
            "SIM" if self.username else "NAO",
        )
        self.client = Client(**client_kwargs)

    def process_image(self, image_path: str):
        logger.info("A enviar pedido para bridge Kaggle | image_path=%s", image_path)
        start = time.time()

        job = self.client.submit(
            handle_file(image_path),
            api_name=self.api_name
        )

        while not job.done():
            status = job.status()
            logger.info(
                "Kaggle job | status=%s | rank=%s | eta=%s",
                getattr(status, "code", None),
                getattr(status, "rank", None),
                getattr(status, "eta", None),
            )
            time.sleep(2)

        logger.info("Job Kaggle concluído. A obter resultado final.")
        result = job.result(timeout=1800)
        logger.info("Resultado bruto recebido da bridge | type=%s | elapsed=%.2fs", type(result).__name__, time.time() - start)

        if isinstance(result, str):
            logger.info("Resultado veio como string. A converter JSON.")
            result = json.loads(result)

        if not isinstance(result, dict):
            logger.error("Resposta inesperada do Kaggle bridge: %s", type(result))
            raise ValueError(f"Resposta inesperada do Kaggle bridge: {type(result)}")

        normalized = {
            "success": bool(result.get("success", True)),
            "original_text": result.get("original_text", ""),
            "simplified_text": result.get("simplified_text", ""),
            "original_lines": result.get("original_lines", []),
            "simplified_lines": result.get("simplified_lines", []),
            "meta": result.get("meta", {"provider": "kaggle"}),
        }
        normalized.setdefault("meta", {})
        normalized["meta"].setdefault("debug", {})
        normalized["meta"]["debug"].update({
            "processor": self.__class__.__name__,
            "bridge_url": self.bridge_url,
            "api_name": self.api_name,
        })

        logger.info(
            "Resultado Kaggle normalizado | success=%s | original_lines=%s | simplified_lines=%s",
            normalized.get("success"),
            len(normalized.get("original_lines", []) or []),
            len(normalized.get("simplified_lines", []) or []),
        )
        return normalized
