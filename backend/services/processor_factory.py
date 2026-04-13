from config import Config
from services.gemma_api_processor import GemmaAPIProcessor
from services.kaggle_processor import KaggleProcessor
from services.mock_processor import MockProcessor
from services.ollama_processor import OllamaProcessor
from services.logger_utils import get_logger

logger = get_logger("processor_factory")


def get_processor():
    backend = Config.PROCESSOR_BACKEND.lower()
    logger.info("PROCESSOR_BACKEND=%s", backend)

    if backend == "mock":
        logger.info("A devolver MockProcessor")
        return MockProcessor()

    if backend == "ollama":
        logger.info(
            "A devolver OllamaProcessor | base_url=%s | model=%s",
            Config.OLLAMA_BASE_URL,
            Config.OLLAMA_MODEL,
        )
        return OllamaProcessor(
            base_url=Config.OLLAMA_BASE_URL,
            model=Config.OLLAMA_MODEL
        )

    if backend == "kaggle":
        logger.info(
            "A devolver KaggleProcessor | bridge_url=%s | api_name=%s | auth=%s",
            Config.KAGGLE_BRIDGE_URL,
            Config.KAGGLE_BRIDGE_API_NAME,
            "SIM" if Config.KAGGLE_BRIDGE_USER else "NAO",
        )
        return KaggleProcessor(
            bridge_url=Config.KAGGLE_BRIDGE_URL,
            username=Config.KAGGLE_BRIDGE_USER,
            password=Config.KAGGLE_BRIDGE_PASSWORD,
            api_name=Config.KAGGLE_BRIDGE_API_NAME,
        )

    if backend in {"api", "gemma_api", "google_api"}:
        logger.info(
            "A devolver GemmaAPIProcessor | model_id=%s | api_key_definida=%s",
            Config.GEMMA_MODEL_ID,
            "SIM" if Config.GEMINI_API_KEY else "NAO",
        )
        return GemmaAPIProcessor(
            api_key=Config.GEMINI_API_KEY,
            model_id=Config.GEMMA_MODEL_ID,
        )

    logger.error("Backend inválido: %s", backend)
    raise ValueError(f"Backend inválido: {backend}")
