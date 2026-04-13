from google import genai
import os

# Usa a tua API Key do AI Studio
client = genai.Client(api_key="AIzaSyCXoWFU8X6o5XIluVLb35mCFcMDt8gydgs")

def test_gemma4():
    print("--- Testando Gemma 4 E4B-it ---")
    
    # O identificador oficial para a API muitas vezes requer o prefixo 'models/'
    # ou o nome completo da família.
    MODEL_NAME = "gemma-4-26b-a4b-it" 

    try:
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents="Explica brevemente como ajudas pessoas com dislexia."
        )
        print("✅ Conectado ao Gemma 4 com sucesso!")
        print(f"Resposta: {response.text}")
        
    except Exception as e:
        print(f"❌ Erro ao aceder ao Gemma 4: {e}")
        print("\nTentando listar modelos disponíveis para encontrar o ID exato...")
        for m in client.models.list():
            if "gemma-4" in m.name:
                print(f"👉 Encontrado ID alternativo: {m.name}")

if __name__ == "__main__":
    test_gemma4()