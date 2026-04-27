package om.dyslexai.app.inference;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class GemmaLocalRuntime implements LocalModelRuntime {

    private static final String TAG = "GemmaLocalRuntime";
    private static final String MODEL_PATH = "/data/local/tmp/llm/gemma/model.litertlm";

    private boolean initialized = false;
    private String cacheDirPath;

    private com.google.ai.edge.litertlm.Engine engine;

    @Override
    public synchronized void initialize(Context context) throws Exception {
        if (initialized) return;

        File modelFile = new File(MODEL_PATH);
        if (!modelFile.exists()) {
            throw new Exception("Modelo não encontrado em " + MODEL_PATH);
        }

        cacheDirPath = context.getCacheDir().getAbsolutePath();

        Log.i(TAG, "Modelo encontrado: " + MODEL_PATH);
        Log.i(TAG, "A inicializar engine LiteRT-LM com suporte de visão...");

        engine = LiteRtBridge.createEngine(MODEL_PATH, cacheDirPath, true);

        initialized = true;
        Log.i(TAG, "Engine inicializada com sucesso.");
    }

    @Override
    public boolean isReady() {
        return initialized;
    }

    @Override
    public String inferText(String prompt) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Runtime ainda não inicializado.");
        }

        Log.i(TAG, "A inferir texto localmente...");
        Log.i(TAG, "Prompt de texto enviada:\n" + prompt);

        String response = LiteRtBridge.runText(engine, prompt);

        Log.i(TAG, "Resposta de texto recebida:\n" + response);
        return response;
    }

    @Override
    public String inferImage(byte[] imageBytes, String mimeType, String prompt) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Runtime ainda não inicializado.");
        }

        File imageFile = persistTempImage(imageBytes, mimeType);
        Log.i(TAG, "A inferir imagem localmente: " + imageFile.getAbsolutePath());
        Log.i(TAG, "MimeType da imagem: " + mimeType);
        Log.i(TAG, "Tamanho da imagem em bytes: " + imageBytes.length);
        Log.i(TAG, "Prompt de imagem enviada:\n" + prompt);

        String response = LiteRtBridge.runImage(engine, imageFile.getAbsolutePath(), prompt);

        Log.i(TAG, "Resposta da imagem recebida:\n" + response);
        return response;
    }

    @Override
    public String inferAudio(byte[] audioBytes, String mimeType, String prompt) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Runtime ainda não inicializado.");
        }

        Log.i(TAG, "inferAudio() chamado.");
        Log.i(TAG, "MimeType do áudio: " + mimeType);
        Log.i(TAG, "Tamanho do áudio em bytes: " + (audioBytes == null ? 0 : audioBytes.length));
        Log.i(TAG, "Prompt de áudio enviada:\n" + prompt);

        throw new UnsupportedOperationException("Áudio ainda não ligado ao LiteRtBridge nesta etapa.");
    }

    @Override
    public String getName() {
        return "gemma3-270m-local";
    }

private File persistTempImage(byte[] imageBytes, String mimeType) throws IOException {
    if (cacheDirPath == null) {
        throw new IOException("Diretoria de cache indisponível.");
    }

    File output = new File(
            cacheDirPath,
            "dyslexai-image-" + UUID.randomUUID() + guessExtension(mimeType)
    );

    try (FileOutputStream fos = new FileOutputStream(output)) {
        fos.write(imageBytes);
        fos.flush();
    }

    output.setReadable(true, false);

    return output;
}

    private String guessExtension(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) return ".jpg";

        String normalized = mimeType.toLowerCase(Locale.ROOT);
        if (normalized.contains("png")) return ".png";
        if (normalized.contains("webp")) return ".webp";
        return ".jpg";
    }
}