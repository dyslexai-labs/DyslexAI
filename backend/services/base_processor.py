from abc import ABC, abstractmethod
from typing import Any, Dict


class BaseProcessor(ABC):
    @abstractmethod
    def process_image(self, image_path: str) -> Dict[str, Any]:
        pass

    def process_audio(self, audio_path: str) -> Dict[str, Any]:
        raise NotImplementedError("Este processor não suporta áudio.")
