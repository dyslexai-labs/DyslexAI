from config import Config
from services.kaggle_processor import KaggleProcessor
from services.mock_processor import MockProcessor
from services.ollama_processor import OllamaProcessor


def get_processor():
    backend = Config.PROCESSOR_BACKEND.lower()

    if backend == "mock":
        return MockProcessor()

    if backend == "ollama":
        return OllamaProcessor(
            base_url=Config.OLLAMA_BASE_URL,
            model=Config.OLLAMA_MODEL
        )

    if backend == "kaggle":
        return KaggleProcessor(
            bridge_url=Config.KAGGLE_BRIDGE_URL,
            username=Config.KAGGLE_BRIDGE_USER,
            password=Config.KAGGLE_BRIDGE_PASSWORD,
            api_name=Config.KAGGLE_BRIDGE_API_NAME,
        )

    raise ValueError(f"Backend inválido: {backend}")