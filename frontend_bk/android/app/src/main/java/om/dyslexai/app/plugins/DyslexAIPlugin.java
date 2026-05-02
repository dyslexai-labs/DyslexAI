package om.dyslexai.app.plugins;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import om.dyslexai.app.inference.DyslexAIEngine;
import om.dyslexai.app.inference.LocalModelRuntime;
import om.dyslexai.app.inference.RuntimeFactory;

@CapacitorPlugin(name = "DyslexAI")
public class DyslexAIPlugin extends Plugin {

    private static final String TAG = "DyslexAIPlugin";

    private DyslexAIEngine engine;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;
    private final ConcurrentHashMap<String, PluginCall> pendingTtsCalls = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final WavAudioRecorder wavAudioRecorder = new WavAudioRecorder();

    @Override
    public void load() {
        Log.i(TAG, "load() -> a carregar plugin DyslexAI...");
        LocalModelRuntime runtime = RuntimeFactory.createDefaultRuntime();
        Log.i(TAG, "load() -> runtime criado: " + runtime.getName());
        engine = new DyslexAIEngine(runtime);
        initTextToSpeech();
        Log.i(TAG, "load() -> engine criado com sucesso.");
    }

    @PluginMethod
    public void health(PluginCall call) {
        Log.i(TAG, "health() chamado.");
        call.resolve(engine.health());
    }

    @PluginMethod
    public void getCapabilities(PluginCall call) {
        Log.i(TAG, "getCapabilities() chamado.");
        call.resolve(engine.getCapabilities());
    }

    @PluginMethod
    public void processText(PluginCall call) {
        String text = call.getString("text");

        Log.i(TAG, "processText() chamado. text length=" + (text == null ? 0 : text.length()));

        if (text == null || text.trim().isEmpty()) {
            Log.w(TAG, "processText() -> texto vazio.");
            call.reject("Texto vazio.");
            return;
        }

        executor.execute(() -> {
            try {
                Log.i(TAG, "processText() -> a enviar para o engine...");
                JSObject result = engine.processText(getContext(), text);
                Log.i(TAG, "processText() -> concluído com sucesso.");
                call.resolve(result);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao processar texto: " + e.getMessage(), e);
                call.reject("Erro ao processar texto: " + e.getMessage(), e);
            }
        });
    }

    @PluginMethod
    public void processImage(PluginCall call) {
        String imageBase64 = call.getString("imageBase64");
        String mimeType = call.getString("mimeType", "image/jpeg");

        Log.i(TAG, "processImage() chamado. mimeType=" + mimeType +
                ", base64 length=" + (imageBase64 == null ? 0 : imageBase64.length()));

        if (imageBase64 == null || imageBase64.trim().isEmpty()) {
            Log.w(TAG, "processImage() -> imagem vazia.");
            call.reject("Imagem vazia.");
            return;
        }

        executor.execute(() -> {
            try {
                Log.i(TAG, "processImage() -> a enviar para o engine...");
                JSObject result = engine.processImage(getContext(), imageBase64, mimeType);
                Log.i(TAG, "processImage() -> concluído com sucesso.");
                call.resolve(result);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao processar imagem: " + e.getMessage(), e);
                call.reject("Erro ao processar imagem: " + e.getMessage(), e);
            }
        });
    }

    @PluginMethod
    public void generateReadingPhrase(PluginCall call) {
        String ageGroup = call.getString("ageGroup", "8-10");
        String level = call.getString("level", "1");
        String type = call.getString("type", "simple_sentence");

        Log.i(TAG, "generateReadingPhrase() chamado. ageGroup=" + ageGroup +
                ", level=" + level + ", type=" + type);

        executor.execute(() -> {
            try {
                Log.i(TAG, "generateReadingPhrase() -> a enviar para o engine...");
                JSObject result = engine.generateReadingPhrase(getContext(), ageGroup, level, type);
                Log.i(TAG, "generateReadingPhrase() -> concluído com sucesso.");
                call.resolve(result);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao gerar frase: " + e.getMessage(), e);
                call.reject("Erro ao gerar frase: " + e.getMessage(), e);
            }
        });
    }

    @PluginMethod
    public void processAudio(PluginCall call) {
        String audioBase64 = call.getString("audioBase64");
        String mimeType = call.getString("mimeType", "audio/webm");
        String expectedText = call.getString("expectedText");

        Log.i(TAG, "processAudio() chamado. mimeType=" + mimeType +
                ", audio base64 length=" + (audioBase64 == null ? 0 : audioBase64.length()) +
                ", expectedText length=" + (expectedText == null ? 0 : expectedText.length()));

        if (audioBase64 == null || audioBase64.trim().isEmpty()) {
            Log.w(TAG, "processAudio() -> áudio vazio.");
            call.reject("Áudio vazio.");
            return;
        }

        executor.execute(() -> {
            try {
                Log.i(TAG, "processAudio() -> a enviar para o engine...");
                JSObject result = engine.processAudio(getContext(), audioBase64, mimeType, expectedText);
                Log.i(TAG, "processAudio() -> concluído com sucesso.");
                call.resolve(result);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao processar áudio: " + e.getMessage(), e);
                call.reject("Erro ao processar áudio: " + e.getMessage(), e);
            }
        });
    }
    @PluginMethod
    public void speak(PluginCall call) {
        String text = call.getString("text", "");
        Double rateValue = call.getDouble("rate", 1.0);

        if (text == null || text.trim().isEmpty()) {
            call.reject("Texto vazio para leitura.");
            return;
        }

        if (textToSpeech == null || !ttsReady) {
            Log.w(TAG, "speak() -> TTS ainda não está pronto.");
            call.reject("TTS Android ainda não está pronto.");
            return;
        }

        String utteranceId = "dyslexai-" + UUID.randomUUID();

        getActivity().runOnUiThread(() -> {
            try {
                resolvePendingTtsCalls("replaced");

                pendingTtsCalls.put(utteranceId, call);

                float rate = rateValue == null ? 1.0f : rateValue.floatValue();
                if (rate < 0.5f) rate = 0.5f;
                if (rate > 2.0f) rate = 2.0f;

                textToSpeech.setSpeechRate(rate);
                textToSpeech.setPitch(1.0f);

                Bundle params = new Bundle();
                int result = textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);

                if (result != TextToSpeech.SUCCESS) {
                    pendingTtsCalls.remove(utteranceId);
                    call.reject("Não foi possível iniciar o TTS Android.");
                    return;
                }

                Log.i(TAG, "speak() -> TTS iniciado. chars=" + text.length() + ", rate=" + rate);
            } catch (Exception e) {
                pendingTtsCalls.remove(utteranceId);
                Log.e(TAG, "Erro ao iniciar TTS: " + e.getMessage(), e);
                call.reject("Erro ao iniciar TTS: " + e.getMessage(), e);
            }
        });
    }

    @PluginMethod
    public void stopSpeaking(PluginCall call) {
        getActivity().runOnUiThread(() -> {
            if (textToSpeech != null) {
                textToSpeech.stop();
            }
            resolvePendingTtsCalls("stopped");

            JSObject ret = new JSObject();
            ret.put("success", true);
            ret.put("status", "stopped");
            call.resolve(ret);
        });
    }

    private void initTextToSpeech() {
        try {
            textToSpeech = new TextToSpeech(getContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int langResult = textToSpeech.setLanguage(new Locale("pt", "PT"));
                    ttsReady = langResult != TextToSpeech.LANG_MISSING_DATA
                            && langResult != TextToSpeech.LANG_NOT_SUPPORTED;

                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            Log.i(TAG, "TTS onStart: " + utteranceId);
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            resolvePendingTtsCall(utteranceId, "done");
                        }

                        @Override
                        public void onError(String utteranceId) {
                            PluginCall pending = pendingTtsCalls.remove(utteranceId);
                            if (pending != null) {
                                pending.reject("Erro durante a leitura TTS.");
                            }
                        }
                    });

                    Log.i(TAG, "TTS inicializado. ready=" + ttsReady);
                } else {
                    ttsReady = false;
                    Log.w(TAG, "Falha ao inicializar TextToSpeech. status=" + status);
                }
            });
        } catch (Exception e) {
            ttsReady = false;
            Log.e(TAG, "Erro ao inicializar TextToSpeech: " + e.getMessage(), e);
        }
    }

    private void resolvePendingTtsCall(String utteranceId, String status) {
        PluginCall pending = pendingTtsCalls.remove(utteranceId);
        if (pending == null) return;

        JSObject ret = new JSObject();
        ret.put("success", true);
        ret.put("status", status);
        pending.resolve(ret);
    }

    private void resolvePendingTtsCalls(String status) {
        for (String utteranceId : pendingTtsCalls.keySet()) {
            resolvePendingTtsCall(utteranceId, status);
        }
    }
@PluginMethod
public void startWavRecording(PluginCall call) {
    Log.i(TAG, "startWavRecording() chamado.");

    executor.execute(() -> {
        try {
            wavAudioRecorder.start();

            JSObject result = new JSObject();
            result.put("success", true);
            result.put("status", "recording");
            result.put("mimeType", "audio/wav");
            result.put("sampleRate", 16000);
            result.put("channels", 1);

            call.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar gravação WAV: " + e.getMessage(), e);
            call.reject("Erro ao iniciar gravação WAV: " + e.getMessage(), e);
        }
    });
}

@PluginMethod
public void stopWavRecording(PluginCall call) {
    Log.i(TAG, "stopWavRecording() chamado.");

    executor.execute(() -> {
        try {
            byte[] wavBytes = wavAudioRecorder.stopAndGetWavBytes();
            String audioBase64 = wavAudioRecorder.toDataUrl(wavBytes);

            JSObject result = new JSObject();
            result.put("success", true);
            result.put("status", "stopped");
            result.put("audioBase64", audioBase64);
            result.put("mimeType", "audio/wav");
            result.put("filename", "gravacao-aluno.wav");
            result.put("size", wavBytes.length);
            result.put("sampleRate", 16000);
            result.put("channels", 1);

            Log.i(TAG, "stopWavRecording() -> WAV pronto, bytes=" + wavBytes.length);

            call.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao parar gravação WAV: " + e.getMessage(), e);
            call.reject("Erro ao parar gravação WAV: " + e.getMessage(), e);
        }
    });
}


}
