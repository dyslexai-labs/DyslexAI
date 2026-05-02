package om.dyslexai.app.inference;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class Gemma4Runtime implements LocalModelRuntime {

    private static final String TAG = "Gemma4Runtime";
    private boolean initialized = false;

    @Override
    public void initialize(Context context) throws Exception {
        File modelFile = new File("/data/local/tmp/llm/gemma4/model.litertlm");

        if (!modelFile.exists()) {
            Log.e(TAG, "Modelo NÃO encontrado!");
            throw new Exception("Modelo não encontrado");
        }

        Log.i(TAG, "Modelo encontrado: " + modelFile.getAbsolutePath());
        initialized = true;
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

        Log.i(TAG, "Inferência chamada com prompt: " + prompt);
        return "Resposta simulada (Gemma futuro): " + prompt;
    }

    @Override
    public String inferImage(byte[] imageBytes, String mimeType, String prompt) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Runtime ainda não inicializado.");
        }

        Log.i(TAG, "Inferência de imagem chamada.");
        return "Extração de imagem simulada (Gemma futuro)";
    }

    @Override
    public String inferAudio(byte[] audioBytes, String mimeType, String prompt) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Runtime ainda não inicializado.");
        }

        Log.i(TAG, "Inferência de áudio chamada.");
        return "{\"transcription\":\"\",\"clean_text\":\"\",\"words\":[],\"issues\":[],\"language\":\"pt-PT\"}";
    }

    @Override
    public String getName() {
        return "gemma4-check-model";
    }
}
