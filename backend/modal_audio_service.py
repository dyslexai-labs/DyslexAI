import os
import re
import json
import tempfile

import modal

app = modal.App("dyslexai-audio")

image = (
    modal.Image.debian_slim(python_version="3.11")
    .apt_install("ffmpeg")
    .pip_install(
        "pillow",
        "torch",
        "torchvision",
        "transformers",
        "accelerate",
        "librosa",
        "soundfile",
        "python-multipart",
        "fastapi[standard]",
    )
)

model_volume = modal.Volume.from_name("gemma4-audio-cache", create_if_missing=True)


def extract_json_block(text: str):
    if not text:
        return None

    fenced = re.search(r"```json\s*(\{.*?\})\s*```", text, re.DOTALL)
    if fenced:
        candidate = fenced.group(1)
        try:
            return json.loads(candidate)
        except Exception:
            pass

    fenced_generic = re.search(r"```\s*(\{.*?\})\s*```", text, re.DOTALL)
    if fenced_generic:
        candidate = fenced_generic.group(1)
        try:
            return json.loads(candidate)
        except Exception:
            pass

    matches = re.findall(r"\{.*?\}", text, re.DOTALL)
    for candidate in reversed(matches):
        try:
            return json.loads(candidate)
        except Exception:
            continue

    return None

@app.function(
    image=image,
    gpu="T4",
    timeout=900,
    scaledown_window=300,
    volumes={"/cache": model_volume},
)
@modal.fastapi_endpoint(method="POST")
def process_audio():
    from fastapi import File, UploadFile
    from fastapi.responses import JSONResponse
    import numpy as np
    import librosa
    import torch
    from transformers import AutoProcessor, AutoModelForMultimodalLM

    MODEL_ID = "google/gemma-4-E4B-it"

    if not hasattr(process_audio, "_processor"):
        processor = AutoProcessor.from_pretrained(MODEL_ID, cache_dir="/cache")
        model = AutoModelForMultimodalLM.from_pretrained(
            MODEL_ID,
            torch_dtype="auto",
            device_map="auto",
            cache_dir="/cache",
        )
        process_audio._processor = processor
        process_audio._model = model

    processor = process_audio._processor
    model = process_audio._model

    async def handler(audio: UploadFile = File(...)):
        with tempfile.NamedTemporaryFile(suffix=".webm", delete=False) as tmp:
            tmp.write(await audio.read())
            temp_path = tmp.name

        try:
            audio_array, sr = librosa.load(temp_path, sr=16000, mono=True)
            audio_array = np.asarray(audio_array, dtype=np.float32)

            messages = [
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "text",
                            "text": (
                                "You are an educational assistant for dyslexic students.\n"
                                "Tasks:\n"
                                "1. Transcribe exactly what the student said.\n"
                                "2. Rewrite the sentence clearly in European Portuguese.\n"
                                "3. Split the sentence into words for guided reading.\n\n"
                                "Respond only in valid JSON with this schema:\n"
                                "{"
                                '"transcription":"...",'
                                '"clean_text":"...",'
                                '"words":["..."],'
                                '"language":"pt-PT"'
                                "}"
                            ),
                        },
                        {
                            "type": "audio",
                            "audio": audio_array,
                        },
                    ],
                }
            ]

            inputs = processor.apply_chat_template(
                messages,
                add_generation_prompt=True,
                tokenize=True,
                return_dict=True,
                return_tensors="pt",
            )
            inputs = {k: v.to(model.device) for k, v in inputs.items()}

            with torch.no_grad():
                output = model.generate(**inputs, max_new_tokens=256)

            raw = processor.batch_decode(
                output,
                skip_special_tokens=True,
                clean_up_tokenization_spaces=False,
            )[0]

            parsed = extract_json_block(raw)

            return JSONResponse({
                "success": True,
                "raw_response": raw,
                "parsed": parsed,
            })

        except Exception as e:
            return JSONResponse({
                "success": False,
                "error": str(e),
            }, status_code=500)

        finally:
            try:
                os.remove(temp_path)
            except Exception:
                pass

    return handler