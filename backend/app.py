import os
import uuid
from flask import Flask, jsonify, request
from flask_cors import CORS

from config import Config
from services.processor_factory import get_processor

app = Flask(__name__)
app.config.from_object(Config)
CORS(app)

os.makedirs(app.config["UPLOAD_FOLDER"], exist_ok=True)
os.makedirs(app.config["TEMP_FOLDER"], exist_ok=True)


@app.route("/api/health", methods=["GET"])
def health():
    return jsonify({
        "success": True,
        "status": "ok",
        "processor_backend": app.config["PROCESSOR_BACKEND"]
    })


@app.route("/api/process", methods=["POST"])
def process_image():
    if "image" not in request.files:
        return jsonify({
            "success": False,
            "error": "Nenhuma imagem recebida."
        }), 400

    image = request.files["image"]

    if not image.filename:
        return jsonify({
            "success": False,
            "error": "Ficheiro inválido."
        }), 400

    extension = os.path.splitext(image.filename)[1].lower() or ".jpg"
    filename = f"{uuid.uuid4().hex}{extension}"
    image_path = os.path.join(app.config["UPLOAD_FOLDER"], filename)

    try:
        image.save(image_path)

        processor = get_processor()
        result = processor.process_image(image_path)

        return jsonify(result)

    except Exception as e:
        return jsonify({
            "success": False,
            "error": str(e)
        }), 500

    finally:
        try:
            if os.path.exists(image_path):
                os.remove(image_path)
        except OSError:
            pass


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)