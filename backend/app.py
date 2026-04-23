import os
import uuid
from flask import Flask, jsonify, request
from flask_cors import CORS

from config import Config
from services.processor_factory import get_processor
from services.logger_utils import get_logger

logger = get_logger("app")

app = Flask(__name__)
app.config.from_object(Config)
CORS(app, resources={r"/api/*": {"origins": [app.config["ALLOWED_ORIGIN"]]}})

os.makedirs(app.config["UPLOAD_FOLDER"], exist_ok=True)
os.makedirs(app.config["TEMP_FOLDER"], exist_ok=True)


def _save_temp_upload(file_storage, default_extension: str):
    extension = os.path.splitext(file_storage.filename)[1].lower() or default_extension
    filename = f"{uuid.uuid4().hex}{extension}"
    file_path = os.path.join(app.config["UPLOAD_FOLDER"], filename)
    file_storage.save(file_path)
    return file_path


def _validate_uploaded_file(field_name: str):
    if field_name not in request.files:
        return None, (jsonify({"success": False, "error": f"Nenhum ficheiro recebido no campo '{field_name}'."}), 400)

    uploaded = request.files[field_name]
    if not uploaded.filename:
        return None, (jsonify({"success": False, "error": "Ficheiro inválido."}), 400)

    return uploaded, None


@app.route("/api/health", methods=["GET"])
def health():
    logger.info("Health check chamado.")
    return jsonify({
        "success": True,
        "status": "ok",
        "processor_backend": app.config["PROCESSOR_BACKEND"]
    })


@app.route("/api/process", methods=["POST"])
def process_image():
    logger.info("Recebido pedido em /api/process")
    image, error_response = _validate_uploaded_file("image")
    if error_response:
        logger.warning("Pedido inválido em /api/process")
        return error_response

    image_path = None
    try:
        image_path = _save_temp_upload(image, ".jpg")
        file_size = os.path.getsize(image_path) if os.path.exists(image_path) else 0
        logger.info(
            "Imagem guardada temporariamente em %s | nome_original=%s | bytes=%s",
            image_path,
            image.filename,
            file_size,
        )

        processor = get_processor()
        logger.info("Processor selecionado: %s", processor.__class__.__name__)

        result = processor.process_image(image_path)

        logger.info(
            "Processamento concluído | success=%s | original_lines=%s | simplified_lines=%s",
            result.get("success"),
            len(result.get("original_lines", []) or []),
            len(result.get("simplified_lines", []) or []),
        )

        return jsonify(result)

    except Exception as e:
        logger.exception("Erro ao processar imagem.")
        return jsonify({"success": False, "error": str(e)}), 500

    finally:
        try:
            if image_path and os.path.exists(image_path):
                os.remove(image_path)
                logger.info("Ficheiro temporário removido: %s", image_path)
        except OSError:
            logger.warning("Falha ao remover ficheiro temporário: %s", image_path)


@app.route("/api/process-audio", methods=["POST"])
def process_audio():
    logger.info("Recebido pedido em /api/process-audio")
    audio, error_response = _validate_uploaded_file("audio")
    if error_response:
        logger.warning("Pedido inválido em /api/process-audio")
        return error_response

    audio_path = None
    try:
        audio_path = _save_temp_upload(audio, ".wav")
        file_size = os.path.getsize(audio_path) if os.path.exists(audio_path) else 0
        logger.info(
            "Áudio guardado temporariamente em %s | nome_original=%s | bytes=%s",
            audio_path,
            audio.filename,
            file_size,
        )

        processor = get_processor()
        logger.info("Processor selecionado para áudio: %s", processor.__class__.__name__)
        result = processor.process_audio(audio_path)

        logger.info(
            "Processamento de áudio concluído | success=%s | transcription_len=%s",
            result.get("success"),
            len(result.get("transcription", "") or ""),
        )

        return jsonify(result)

    except NotImplementedError as e:
        logger.warning("Backend atual não suporta áudio: %s", e)
        return jsonify({"success": False, "error": str(e)}), 501
    except Exception as e:
        logger.exception("Erro ao processar áudio.")
        return jsonify({"success": False, "error": str(e)}), 500
    finally:
        try:
            if audio_path and os.path.exists(audio_path):
                os.remove(audio_path)
                logger.info("Ficheiro temporário removido: %s", audio_path)
        except OSError:
            logger.warning("Falha ao remover ficheiro temporário: %s", audio_path)


if __name__ == "__main__":
    logger.info(
        "A iniciar Flask | PROCESSOR_BACKEND=%s | host=0.0.0.0 | port=5000",
        app.config["PROCESSOR_BACKEND"],
    )
    app.run(host="0.0.0.0", port=5000, debug=True)
