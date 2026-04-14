import os
from dotenv import load_dotenv

load_dotenv()


class Config:
    ALLOWED_ORIGIN = os.getenv("ALLOWED_ORIGIN", "*")

    SECRET_KEY = os.getenv("SECRET_KEY", "dev-secret-key")
    PROCESSOR_BACKEND = os.getenv("PROCESSOR_BACKEND", "kaggle")  # mock | kaggle | ollama | api | api_advanced
    LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")
    MAX_CONTENT_LENGTH = 10 * 1024 * 1024  # 10 MB

    UPLOAD_FOLDER = os.getenv("UPLOAD_FOLDER", "uploads")
    TEMP_FOLDER = os.getenv("TEMP_FOLDER", "temp")

    # Ollama
    OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
    OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "gemma3:4b")

    # Kaggle bridge
    KAGGLE_BRIDGE_URL = os.getenv("KAGGLE_BRIDGE_URL", "https://cb2b9d4b8abf2d1eb6.gradio.live")
    KAGGLE_BRIDGE_USER = os.getenv("KAGGLE_BRIDGE_USER", "admin")
    KAGGLE_BRIDGE_PASSWORD = os.getenv("KAGGLE_BRIDGE_PASSWORD", "pass1234")
    KAGGLE_BRIDGE_API_NAME = os.getenv("KAGGLE_BRIDGE_API_NAME", "/process_image")

    # Google / Gemma API
    GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
    GEMMA_MODEL_ID = os.getenv("GEMMA_MODEL_ID", "gemma-4-26b-a4b-it")
