package om.dyslexai.app.inference;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class GemmaLocalRuntime implements LocalModelRuntime {

    private static final String TAG = "GemmaLocalRuntime";
    private static final String GPU_PREFS = "dyslexai_gpu_policy";
    private static final String KEY_MAIN_GPU_SUPPORTED = "main_gpu_supported";
    private static final String KEY_MAIN_GPU_CRASHED = "main_gpu_crashed";
    private static final String KEY_MAIN_GPU_PROBE_PENDING = "main_gpu_probe_pending";
    private static final String KEY_DEVICE_FINGERPRINT = "device_fingerprint";

    private boolean initialized = false;
    private String cacheDirPath;

    private com.google.ai.edge.litertlm.Engine engine;

    @Override
    public synchronized void initialize(Context context) throws Exception {
        if (initialized) return;
        long start = System.nanoTime();

        String modelPath = new ModelManager().resolveModelPath(context);
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new Exception("Modelo não encontrado em " + modelPath);
        }

        cacheDirPath = context.getCacheDir().getAbsolutePath();
        SharedPreferences gpuPrefs = context.getSharedPreferences(GPU_PREFS, Context.MODE_PRIVATE);
        reconcilePendingGpuProbe(gpuPrefs);
        boolean useMainGpu = shouldUseMainGpu(gpuPrefs);

        Log.i(TAG, "Modelo encontrado: " + modelPath);
        Log.i(TAG, "A inicializar engine LiteRT-LM com suporte de visão e áudio...");
        Log.i(TAG, "Política GPU -> useMainGpu=" + useMainGpu
                + ", manufacturer=" + Build.MANUFACTURER
                + ", brand=" + Build.BRAND
                + ", model=" + Build.MODEL);

        markGpuProbeStartedIfNeeded(gpuPrefs, useMainGpu);

        try {
            engine = LiteRtBridge.createEngine(modelPath, cacheDirPath, true, useMainGpu);
            markGpuProbeSucceededIfNeeded(gpuPrefs, useMainGpu);
        } catch (Exception e) {
            markGpuProbeFailedIfNeeded(gpuPrefs, useMainGpu);
            throw e;
        } catch (Error e) {
            markGpuProbeFailedIfNeeded(gpuPrefs, useMainGpu);
            throw e;
        }

        initialized = true;
        Log.i(TAG, "Engine inicializada com sucesso. initMs=" + elapsedMs(start));
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

        long start = System.nanoTime();
        String response = LiteRtBridge.runText(engine, prompt);

        Log.i(TAG, "Resposta de texto recebida. totalMs=" + elapsedMs(start) + "\n" + response);
        return response;
    }

    @Override
    public String inferImage(byte[] imageBytes, String mimeType, String prompt) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("Runtime ainda não inicializado.");
        }

        long persistStart = System.nanoTime();
        File imageFile = persistTempImage(imageBytes, mimeType);
        Log.i(TAG, "Imagem persistida em cache. persistMs=" + elapsedMs(persistStart));
        Log.i(TAG, "A inferir imagem localmente: " + imageFile.getAbsolutePath());
        Log.i(TAG, "MimeType da imagem: " + mimeType);
        Log.i(TAG, "Tamanho da imagem em bytes: " + imageBytes.length);
        Log.i(TAG, "Prompt de imagem enviada:\n" + prompt);

        long inferStart = System.nanoTime();
        String response = LiteRtBridge.runImage(engine, imageFile.getAbsolutePath(), prompt);

        Log.i(TAG, "Resposta da imagem recebida. totalInferMs=" + elapsedMs(inferStart) + "\n" + response);
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

        long persistStart = System.nanoTime();
        File audioFile = persistTempAudio(audioBytes, mimeType);
        Log.i(TAG, "Áudio persistido em cache. persistMs=" + elapsedMs(persistStart));
        Log.i(TAG, "A inferir áudio localmente: " + audioFile.getAbsolutePath());

        long inferStart = System.nanoTime();
        String response = LiteRtBridge.runAudioFile(engine, audioFile.getAbsolutePath(), prompt);

        Log.i(TAG, "Resposta de áudio recebida. totalInferMs=" + elapsedMs(inferStart) + "\n" + response);
        return response;
    }

    @Override
    public String getName() {
        return "gemma-3n-e2b-local";
    }

    private void reconcilePendingGpuProbe(SharedPreferences prefs) {
        String currentFingerprint = deviceFingerprint();
        String savedFingerprint = prefs.getString(KEY_DEVICE_FINGERPRINT, "");

        if (!currentFingerprint.equals(savedFingerprint)) {
            Log.i(TAG, "GPU policy -> dispositivo/modelo mudou. A reiniciar política GPU.");
            prefs.edit()
                    .clear()
                    .putString(KEY_DEVICE_FINGERPRINT, currentFingerprint)
                    .apply();
            return;
        }

        if (prefs.getBoolean(KEY_MAIN_GPU_PROBE_PENDING, false)) {
            Log.w(TAG, "GPU policy -> a tentativa anterior de GPU ficou pendente. Assumir crash nativo e desativar GPU principal neste dispositivo.");
            prefs.edit()
                    .putBoolean(KEY_MAIN_GPU_CRASHED, true)
                    .putBoolean(KEY_MAIN_GPU_SUPPORTED, false)
                    .putBoolean(KEY_MAIN_GPU_PROBE_PENDING, false)
                    .apply();
        }
    }

    private boolean shouldUseMainGpu(SharedPreferences prefs) {
        if (prefs.getBoolean(KEY_MAIN_GPU_CRASHED, false)) {
            return false;
        }

        if (prefs.contains(KEY_MAIN_GPU_SUPPORTED)) {
            return prefs.getBoolean(KEY_MAIN_GPU_SUPPORTED, true);
        }

        return true;
    }

    private void markGpuProbeStartedIfNeeded(SharedPreferences prefs, boolean useMainGpu) {
        if (!useMainGpu) return;

        prefs.edit()
                .putString(KEY_DEVICE_FINGERPRINT, deviceFingerprint())
                .putBoolean(KEY_MAIN_GPU_PROBE_PENDING, true)
                .apply();

        Log.i(TAG, "GPU policy -> probe GPU iniciado. Se a app morrer agora, o próximo arranque usará CPU principal.");
    }

    private void markGpuProbeSucceededIfNeeded(SharedPreferences prefs, boolean useMainGpu) {
        if (!useMainGpu) return;

        prefs.edit()
                .putString(KEY_DEVICE_FINGERPRINT, deviceFingerprint())
                .putBoolean(KEY_MAIN_GPU_SUPPORTED, true)
                .putBoolean(KEY_MAIN_GPU_CRASHED, false)
                .putBoolean(KEY_MAIN_GPU_PROBE_PENDING, false)
                .apply();

        Log.i(TAG, "GPU policy -> GPU principal validado para este dispositivo.");
    }

    private void markGpuProbeFailedIfNeeded(SharedPreferences prefs, boolean useMainGpu) {
        if (!useMainGpu) return;

        prefs.edit()
                .putString(KEY_DEVICE_FINGERPRINT, deviceFingerprint())
                .putBoolean(KEY_MAIN_GPU_SUPPORTED, false)
                .putBoolean(KEY_MAIN_GPU_CRASHED, true)
                .putBoolean(KEY_MAIN_GPU_PROBE_PENDING, false)
                .apply();

        Log.w(TAG, "GPU policy -> GPU principal falhou com exceção recuperável. Futuramente será usado CPU principal.");
    }

    private String deviceFingerprint() {
        return safeBuildValue(Build.MANUFACTURER) + "|"
                + safeBuildValue(Build.BRAND) + "|"
                + safeBuildValue(Build.MODEL) + "|"
                + safeBuildValue(Build.HARDWARE) + "|"
                + safeBuildValue(Build.VERSION.RELEASE);
    }

    private String safeBuildValue(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private File persistTempImage(byte[] imageBytes, String mimeType) throws IOException {
        if (cacheDirPath == null) {
            throw new IOException("Diretoria de cache indisponível.");
        }

        File output = new File(
                cacheDirPath,
                "dyslexai-image-" + UUID.randomUUID() + guessImageExtension(mimeType)
        );

        try (FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(imageBytes);
            fos.flush();
        }

        output.setReadable(true, false);
        return output;
    }

    private File persistTempAudio(byte[] audioBytes, String mimeType) throws IOException {
        if (cacheDirPath == null) {
            throw new IOException("Diretoria de cache indisponível.");
        }

        File output = new File(
                cacheDirPath,
                "dyslexai-audio-" + UUID.randomUUID() + guessAudioExtension(mimeType)
        );

        try (FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(audioBytes);
            fos.flush();
        }

        output.setReadable(true, false);
        return output;
    }

    private String guessImageExtension(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) return ".jpg";

        String normalized = mimeType.toLowerCase(Locale.ROOT);
        if (normalized.contains("png")) return ".png";
        if (normalized.contains("webp")) return ".webp";
        return ".jpg";
    }

    private String guessAudioExtension(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) return ".wav";

        String normalized = mimeType.toLowerCase(Locale.ROOT);
        if (normalized.contains("wav")) return ".wav";
        if (normalized.contains("mp4") || normalized.contains("m4a")) return ".m4a";
        if (normalized.contains("webm")) return ".webm";
        if (normalized.contains("ogg")) return ".ogg";
        return ".wav";
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
