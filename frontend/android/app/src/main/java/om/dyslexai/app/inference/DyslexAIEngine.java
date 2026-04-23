package om.dyslexai.app.inference;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DyslexAIEngine {

    private static final String TAG = "DyslexAIEngine";
    private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\\r?\\n+|(?<=[\\.!\\?])\\s+");

    private final LocalModelRuntime runtime;

    public DyslexAIEngine(LocalModelRuntime runtime) {
        this.runtime = runtime;
    }

    public JSObject health() {
        JSObject ret = new JSObject();
        ret.put("success", true);
        ret.put("status", runtime.isReady() ? "ready" : "not_initialized");
        ret.put("source", "mobile");
        ret.put("engine", runtime.getName());
        return ret;
    }

    public JSObject getCapabilities() {
        JSObject ret = new JSObject();
        ret.put("text", true);
        ret.put("image", true);
        ret.put("audio", false);
        ret.put("syllables", false);
        ret.put("source", "mobile");
        ret.put("engine", runtime.getName());
        ret.put("ready", runtime.isReady());
        return ret;
    }

    public JSObject processText(Context context, String text) throws Exception {
        ensureRuntime(context);

        String prompt = buildSimplificationPrompt(text);
        Log.i(TAG, "Prompt de simplificação enviada ao runtime:\n" + prompt);

        String simplified = cleanModelText(runtime.inferText(prompt));
        if (simplified.isEmpty()) {
            simplified = text;
        }

        return buildResult(text, simplified);
    }

    public JSObject processImage(Context context, String imageBase64, String mimeType) throws Exception {
        ensureRuntime(context);

        byte[] imageBytes = decodeBase64Payload(imageBase64);

        String imagePrompt = buildImageExtractionPrompt();
        Log.i(TAG, "Prompt de extração de imagem enviada ao runtime:\n" + imagePrompt);

        String extracted = cleanModelText(runtime.inferImage(imageBytes, mimeType, imagePrompt));
        if (extracted.isEmpty()) {
            throw new Exception("O modelo não conseguiu extrair texto visível da imagem.");
        }

        String simplifyPrompt = buildSimplificationPrompt(extracted);
        Log.i(TAG, "Prompt de simplificação da imagem enviada ao runtime:\n" + simplifyPrompt);

        String simplified = cleanModelText(runtime.inferText(simplifyPrompt));
        if (simplified.isEmpty()) {
            simplified = extracted;
        }

        return buildResult(extracted, simplified);
    }

    private void ensureRuntime(Context context) throws Exception {
        if (!runtime.isReady()) {
            runtime.initialize(context);
        }
    }

    private JSObject buildResult(String originalText, String simplifiedText) {
        JSObject result = new JSObject();
        result.put("success", true);
        result.put("original_text", originalText);
        result.put("simplified_text", simplifiedText);
        result.put("original_lines", toJsArray(splitLines(originalText)));
        result.put("simplified_lines", toJsArray(splitLines(simplifiedText)));

        JSObject meta = new JSObject();
        meta.put("source", "mobile");
        meta.put("engine", runtime.getName());

        result.put("meta", meta);
        return result;
    }

    private byte[] decodeBase64Payload(String imageBase64) {
        String normalized = imageBase64 == null ? "" : imageBase64.trim();
        int commaIndex = normalized.indexOf(',');
        if (commaIndex >= 0) {
            normalized = normalized.substring(commaIndex + 1);
        }
        return Base64.decode(normalized, Base64.DEFAULT);
    }

    private String buildImageExtractionPrompt() {
        return "Extrai só o texto visível. Mantém a ordem. Devolve só o texto.";
    }

    private String buildSimplificationPrompt(String text) {
        return "Simplifica em português europeu. Usa frases curtas e palavras simples. Devolve só o texto final.\n\n"
                + text;
    }

    private List<String> splitLines(String text) {
        List<String> result = new ArrayList<>();
        String[] parts = LINE_SPLIT_PATTERN.split(text == null ? "" : text.trim());

        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }

        if (result.isEmpty() && text != null && !text.trim().isEmpty()) {
            result.add(text.trim());
        }

        return result;
    }

    private String cleanModelText(String text) {
        String cleaned = text == null ? "" : text.trim();
        cleaned = cleaned.replaceAll("^```(?:text)?", "").replaceAll("```$", "").trim();
        cleaned = cleaned.replaceAll("^([Oo]riginal|[Tt]exto(?: extra[ií]do)?|[Rr]esposta|[Ss]implificado)\\s*[:：]\\s*", "").trim();
        cleaned = cleaned.replaceAll("^\"|\"$", "").trim();
        cleaned = cleaned.replaceAll("\\s+\n", "\n");
        cleaned = cleaned.replaceAll("[ \t]+", " ").trim();
        return cleaned;
    }

    private JSArray toJsArray(List<String> items) {
        JSArray array = new JSArray();
        for (String item : items) {
            array.put(item);
        }
        return array;
    }
}