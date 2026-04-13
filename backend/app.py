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
CORS(app)

os.makedirs(app.config["UPLOAD_FOLDER"], exist_ok=True)
os.makedirs(app.config["TEMP_FOLDER"], exist_ok=True)


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

    if "image" not in request.files:
        logger.warning("Pedido sem campo 'image'.")
        return jsonify({
            "success": False,
            "error": "Nenhuma imagem recebida."
        }), 400

    image = request.files["image"]

    if not image.filename:
        logger.warning("Pedido com ficheiro sem nome.")
        return jsonify({
            "success": False,
            "error": "Ficheiro inválido."
        }), 400

    extension = os.path.splitext(image.filename)[1].lower() or ".jpg"
    filename = f"{uuid.uuid4().hex}{extension}"
    image_path = os.path.join(app.config["UPLOAD_FOLDER"], filename)

    try:
        image.save(image_path)
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
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500

    finally:
        try:
            if os.path.exists(image_path):
                os.remove(image_path)
                logger.info("Ficheiro temporário removido: %s", image_path)
        except OSError:
            logger.warning("Falha ao remover ficheiro temporário: %s", image_path)


if __name__ == "__main__":
    logger.info(
        "A iniciar Flask | PROCESSOR_BACKEND=%s | host=0.0.0.0 | port=5000",
        app.config["PROCESSOR_BACKEND"],
    )
    app.run(host="0.0.0.0", port=5000, debug=True)
