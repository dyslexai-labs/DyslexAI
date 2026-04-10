from services.base_processor import BaseProcessor
from services.text_utils import ensure_result_shape


class MockProcessor(BaseProcessor):
    def process_image(self, image_path: str):
        original_text = """Salazar e o Estado Novo.

Se o problema da agitação e desordem foi travado pela ditadura militar, com a extinção das liberdades e a repressão, o problema das finanças precisava ainda de uma solução.

Como chegou Salazar ao poder?

Em 1928, Óscar Carmona convidou António de Oliveira Salazar para ser ministro das Finanças."""

        simplified_text = """Salazar ganhou importância no governo.

Primeiro, foi convidado para ministro das Finanças.
Depois, teve mais poder.

Ainda havia problemas para resolver no país."""

        return ensure_result_shape(
            original_text=original_text,
            simplified_text=simplified_text,
            provider="mock"
        )