package om.dyslexai.app.inference;

import android.content.Context;
import android.util.Log;

import com.getcapacitor.JSObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ModelManager {

    private static final String TAG = "ModelManager";

    public static final String MODEL_PATH = "/data/local/tmp/llm/gemma/model.litertlm";
    public static final String MODEL_URL = "https://huggingface.co/litert-community/gemma-4-E4B-it-litert-lm/resolve/main/gemma-4-E4B-it.litertlm";
    public static final long MIN_VALID_MODEL_BYTES = 512L * 1024L * 1024L;
    private static final String APP_MODEL_RELATIVE_PATH = "llm/gemma/model.litertlm";

    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static final int BUFFER_SIZE = 1024 * 1024;

    public interface ProgressCallback {
        void onProgress(JSObject progress);
    }

    public JSObject getState(Context context) {
        File modelFile = getInstalledModelFile(context);
        long size = modelFile.exists() ? modelFile.length() : 0L;
        boolean installed = isValidModelFile(modelFile);

        JSObject state = new JSObject();
        state.put("status", installed ? "installed" : "missing");
        state.put("installed", installed);
        state.put("path", modelFile.getAbsolutePath());
        state.put("preferredPath", MODEL_PATH);
        state.put("downloadUrl", MODEL_URL);
        state.put("bytesDownloaded", size);
        state.put("totalBytes", installed ? size : 0L);
        state.put("downloadedMb", bytesToMb(size));
        state.put("totalMb", installed ? bytesToMb(size) : 0.0);
        state.put("percent", installed ? 100 : 0);
        return state;
    }

    public boolean isModelInstalled(Context context) {
        return isValidModelFile(getInstalledModelFile(context));
    }

    public String resolveModelPath(Context context) {
        return getInstalledModelFile(context).getAbsolutePath();
    }

    public JSObject ensureInstalled(Context context, ProgressCallback callback) throws Exception {
        if (isModelInstalled(context)) {
            JSObject installed = getState(context);
            installed.put("message", "Model installed successfully");
            emit(callback, installed);
            return installed;
        }

        File modelFile = getDownloadTargetFile(context);
        if (modelFile.exists() && modelFile.length() < MIN_VALID_MODEL_BYTES) {
            Log.w(TAG, "Modelo existente inválido. A apagar ficheiro: " + modelFile.getAbsolutePath());
            deleteQuietly(modelFile);
        }

        downloadModel(context, callback);

        if (!isModelInstalled(context)) {
            throw new IOException("Download concluído, mas o modelo não passou a validação mínima.");
        }

        JSObject installed = getState(context);
        installed.put("status", "installed");
        installed.put("message", "Model installed successfully");
        emit(callback, installed);
        return installed;
    }

    private void downloadModel(Context context, ProgressCallback callback) throws Exception {
        File modelFile = getDownloadTargetFile(context);
        File parentDir = modelFile.getParentFile();
        if (parentDir == null) {
            throw new IOException("Caminho do modelo inválido.");
        }

        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Não foi possível criar a pasta do modelo: " + parentDir.getAbsolutePath());
        }

        File partialFile = new File(parentDir, modelFile.getName() + ".download");
        deleteQuietly(partialFile);

        HttpURLConnection connection = null;
        long downloaded = 0L;
        long total = 0L;
        long lastEmitMs = 0L;

        try {
            emit(callback, progress("downloading", 0L, 0L, modelFile.getAbsolutePath(), "Downloading model..."));

            URL url = new URL(MODEL_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(READ_TIMEOUT_MS);
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("Resposta HTTP inesperada ao descarregar modelo: " + responseCode);
            }

            total = connection.getContentLengthLong();

            try (
                    BufferedInputStream input = new BufferedInputStream(connection.getInputStream(), BUFFER_SIZE);
                    FileOutputStream output = new FileOutputStream(partialFile)
            ) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;

                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    downloaded += read;

                    long now = System.currentTimeMillis();
                    if (now - lastEmitMs >= 500 || (total > 0 && downloaded == total)) {
                        lastEmitMs = now;
                        emit(callback, progress("downloading", downloaded, total, modelFile.getAbsolutePath(), "Downloading model..."));
                    }
                }

                output.flush();
            }

            if (total > 0 && downloaded != total) {
                throw new IOException("Download incompleto: " + downloaded + " de " + total + " bytes.");
            }

            if (partialFile.length() < MIN_VALID_MODEL_BYTES) {
                throw new IOException("O ficheiro descarregado é demasiado pequeno para ser um modelo válido.");
            }

            deleteQuietly(modelFile);
            if (!partialFile.renameTo(modelFile)) {
                throw new IOException("Não foi possível finalizar a instalação do modelo.");
            }

            modelFile.setReadable(true, false);
            Log.i(TAG, "Modelo instalado em " + modelFile.getAbsolutePath() + " bytes=" + modelFile.length());
        } catch (Exception e) {
            deleteQuietly(partialFile);
            deleteQuietly(modelFile);
            JSObject error = progress("error", downloaded, total, modelFile.getAbsolutePath(), "Download failed. Please check your connection and try again.");
            error.put("error", e.getMessage());
            emit(callback, error);
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private File getInstalledModelFile(Context context) {
        File preferredModel = new File(MODEL_PATH);
        if (isValidModelFile(preferredModel)) {
            return preferredModel;
        }

        return getAppModelFile(context);
    }

    private File getDownloadTargetFile(Context context) {
        File preferredModel = new File(MODEL_PATH);
        File preferredParent = preferredModel.getParentFile();
        if (preferredParent != null && preferredParent.exists() && preferredParent.canWrite()) {
            return preferredModel;
        }

        return getAppModelFile(context);
    }

    private File getAppModelFile(Context context) {
        return new File(context.getFilesDir(), APP_MODEL_RELATIVE_PATH);
    }

    private boolean isValidModelFile(File modelFile) {
        return modelFile.exists() && modelFile.isFile() && modelFile.length() >= MIN_VALID_MODEL_BYTES;
    }

    private JSObject progress(String status, long downloaded, long total, String path, String message) {
        int percent = total > 0 ? (int) Math.min(100L, Math.max(0L, (downloaded * 100L) / total)) : 0;

        JSObject progress = new JSObject();
        progress.put("status", status);
        progress.put("path", path);
        progress.put("preferredPath", MODEL_PATH);
        progress.put("bytesDownloaded", downloaded);
        progress.put("totalBytes", total);
        progress.put("downloadedMb", bytesToMb(downloaded));
        progress.put("totalMb", total > 0 ? bytesToMb(total) : 0.0);
        progress.put("percent", percent);
        progress.put("message", message);
        return progress;
    }

    private void emit(ProgressCallback callback, JSObject payload) {
        if (callback != null) {
            callback.onProgress(payload);
        }
    }

    private double bytesToMb(long bytes) {
        return Double.parseDouble(String.format(Locale.US, "%.1f", bytes / 1024.0 / 1024.0));
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists() && !file.delete()) {
            Log.w(TAG, "Não foi possível apagar ficheiro: " + file.getAbsolutePath());
        }
    }
}
