import json
import time

from gradio_client import Client, handle_file
from services.base_processor import BaseProcessor


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

        self.client = Client(**client_kwargs)

    def process_image(self, image_path: str):
        job = self.client.submit(
            handle_file(image_path),
            api_name=self.api_name
        )

        while not job.done():
            status = job.status()
            print(f"[Kaggle job] status={status.code} rank={status.rank} eta={status.eta}")
            time.sleep(2)

        result = job.result(timeout=1800)

        if isinstance(result, str):
            result = json.loads(result)

        if not isinstance(result, dict):
            raise ValueError(f"Resposta inesperada do Kaggle bridge: {type(result)}")

        return {
            "success": bool(result.get("success", True)),
            "original_text": result.get("original_text", ""),
            "simplified_text": result.get("simplified_text", ""),
            "original_lines": result.get("original_lines", []),
            "simplified_lines": result.get("simplified_lines", []),
            "meta": result.get("meta", {"provider": "kaggle"}),
        }