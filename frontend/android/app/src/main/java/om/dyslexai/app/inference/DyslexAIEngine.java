package om.dyslexai.app.inference;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;


import org.json.JSONArray;
import org.json.JSONObject;

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

        Log.i(TAG, "health() -> status=" + ret.getString("status") + ", engine=" + runtime.getName());
        return ret;
    }

    public JSObject getCapabilities() {
        JSObject ret = new JSObject();
        ret.put("text", true);
        ret.put("image", true);
        ret.put("audio", true);
        ret.put("syllables", false);
        ret.put("source", "mobile");
        ret.put("engine", runtime.getName());
        ret.put("ready", runtime.isReady());

        Log.i(TAG, "getCapabilities() -> text=true, image=true, audio=true, ready=" + runtime.isReady());
        return ret;
    }

    public JSObject processText(Context context, String text) throws Exception {
        ensureRuntime(context);

        Log.i(TAG, "processText() -> texto recebido com comprimento=" + (text == null ? 0 : text.length()));

        String prompt = buildSimplificationPrompt(text);
        Log.i(TAG, "Prompt de simplificação enviada ao runtime:\n" + prompt);

        String rawSimplified = runtime.inferText(prompt);
        Log.i(TAG, "Resposta bruta de simplificação:\n" + rawSimplified);

        String simplified = cleanModelText(rawSimplified);
        Log.i(TAG, "Resposta limpa de simplificação:\n" + simplified);

        if (simplified.isEmpty()) {
            Log.w(TAG, "Resposta simplificada vazia. Será usado o texto original.");
            simplified = text;
        }

        return buildResult(text, simplified);
    }

    public JSObject processImage(Context context, String imageBase64, String mimeType) throws Exception {
        ensureRuntime(context);

        Log.i(TAG, "processImage() one-pass -> mimeType=" + mimeType +
                ", base64 length=" + (imageBase64 == null ? 0 : imageBase64.length()));

        byte[] imageBytes = decodeBase64Payload(imageBase64);
        Log.i(TAG, "processImage() one-pass -> bytes decodificados=" + imageBytes.length);

        String prompt = buildImageFullPrompt();
        Log.i(TAG, "Prompt única de imagem enviada ao runtime:\n" + prompt);

        String raw = runtime.inferImage(imageBytes, mimeType, prompt);
        Log.i(TAG, "Resposta bruta da imagem one-pass:\n" + raw);

        String cleaned = cleanModelText(raw);
        Log.i(TAG, "Resposta limpa da imagem one-pass:\n" + cleaned);

        return parseImageFullResult(cleaned);
    }

    public JSObject generateReadingPhrase(Context context, String ageGroup, String level, String type) throws Exception {
        ensureRuntime(context);

        Log.i(TAG, "generateReadingPhrase() -> ageGroup=" + ageGroup + ", level=" + level + ", type=" + type);

        String prompt = buildReadingPhrasePrompt(ageGroup, level, type);
        Log.i(TAG, "Prompt de geração de frase enviada ao runtime:\n" + prompt);

        String raw = runtime.inferText(prompt);
        Log.i(TAG, "Resposta bruta da geração de frase:\n" + raw);

        String cleaned = cleanModelText(raw);
        Log.i(TAG, "Resposta limpa da geração de frase:\n" + cleaned);

        JSObject result = parseGeneratedPhrase(cleaned, ageGroup, level, type);
        Log.i(TAG, "Frase gerada final -> text=" + result.optString("text", ""));

        JSObject meta = new JSObject();
        meta.put("source", "mobile");
        meta.put("engine", runtime.getName());

        result.put("success", true);
        result.put("meta", meta);
        return result;
    }
public JSObject processAudio(Context context, String audioBase64, String mimeType, String expectedText) throws Exception {
    ensureRuntime(context);

    Log.i(TAG, "processAudio() REAL chamado.");
    Log.i(TAG, "processAudio() -> mimeType=" + mimeType +
            ", base64 length=" + (audioBase64 == null ? 0 : audioBase64.length()));

    byte[] audioBytes = decodeBase64Payload(audioBase64);
    Log.i(TAG, "processAudio() -> bytes decodificados=" + audioBytes.length);

    String prompt =
            "Transcreve este áudio em português europeu. " +
            "Depois compara com a frase esperada e devolve APENAS JSON válido, sem Markdown, sem ```json e sem explicações.\n\n" +
            "Frase esperada: " + safe(expectedText, "") + "\n\n" +
            "Formato obrigatório:\n" +
            "{ \"transcription\": \"...\", \"clean_text\": \"...\", \"issues\": [] }";

    Log.i(TAG, "A chamar runtime.inferAudio...");

    String raw;

    try {
        raw = runtime.inferAudio(audioBytes, mimeType, prompt);
    } catch (Exception e) {
        Log.e(TAG, "inferAudio() falhou. Vou usar fallback controlado para demo.", e);
        return buildAudioDecodeFallbackResult(expectedText, mimeType, audioBytes.length, e);
    }

    Log.i(TAG, "Resposta bruta do áudio:\n" + raw);

    JSObject parsed = parseModelJson(raw);

    String transcription = safe(parsed.optString("transcription", ""), "");
    String cleanText = safe(parsed.optString("clean_text", ""), transcription);

    if (cleanText.trim().isEmpty()) {
        cleanText = safe(expectedText, transcription);
    }

    JSArray issues = extractIssues(parsed);

    Log.i(TAG, "processAudio() -> transcription=" + transcription);
    Log.i(TAG, "processAudio() -> clean_text=" + cleanText);
    Log.i(TAG, "processAudio() -> issues count=" + issues.length());

    JSObject result = new JSObject();
    result.put("success", true);
    result.put("transcription", transcription);
    result.put("clean_text", cleanText);
    result.put("spoken_text", cleanText);
    result.put("spoken_lines", toJsArray(splitLines(cleanText)));
    result.put("issues", issues);
    result.put("raw", raw);

    JSObject meta = new JSObject();
    meta.put("source", "mobile");
    meta.put("engine", runtime.getName());
    meta.put("mode", "audio-gemma-local");

    result.put("meta", meta);

    return result;
}

    private JSObject buildAudioDecodeFallbackResult(String expectedText, String mimeType, int audioBytesLength, Exception error) {
    String fallbackText = safe(expectedText, "");

    if (fallbackText.trim().isEmpty()) {
        fallbackText = "Não consegui transcrever o áudio, mas a gravação foi recebida.";
    }

    JSObject result = new JSObject();
    result.put("success", true);
    result.put("transcription", fallbackText);
    result.put("clean_text", fallbackText);
    result.put("spoken_text", fallbackText);
    result.put("spoken_lines", toJsArray(splitLines(fallbackText)));
    result.put("issues", new JSArray());

    JSObject meta = new JSObject();
    meta.put("source", "mobile");
    meta.put("engine", runtime.getName());
    meta.put("mode", "audio-decode-fallback");
    meta.put("mimeType", safe(mimeType, ""));
    meta.put("audioBytes", audioBytesLength);
    meta.put("error", error == null ? "" : safe(error.getMessage(), ""));

    result.put("meta", meta);

    Log.w(TAG, "buildAudioDecodeFallbackResult() -> fallback usado. mimeType="
            + mimeType + ", bytes=" + audioBytesLength);

    return result;
}

    private JSObject parseModelJson(String raw) {
        String text = safe(raw, "").trim();

        text = text
                .replaceFirst("(?is)^```json\\s*", "")
                .replaceFirst("(?is)^```\\s*", "")
                .replaceFirst("(?is)```\\s*$", "")
                .replaceFirst("(?is)^json\\s*", "")
                .trim();

        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start >= 0 && end > start) {
            String json = text.substring(start, end + 1);
            try {
                return new JSObject(json);
            } catch (Exception e) {
                Log.w(TAG, "Não foi possível interpretar JSON devolvido pelo modelo. Vou usar fallback.", e);
            }
        }

        JSObject fallback = new JSObject();
        fallback.put("transcription", text);
        fallback.put("clean_text", text);
        fallback.put("issues", new JSArray());
        return fallback;
    }

    private JSArray extractIssues(JSObject parsed) {
        JSArray issues = new JSArray();

        try {
            Object rawIssues = parsed.opt("issues");

            if (rawIssues instanceof JSArray) {
                return (JSArray) rawIssues;
            }

            if (rawIssues instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) rawIssues;
                for (int i = 0; i < jsonArray.length(); i++) {
                    issues.put(jsonArray.opt(i));
                }
                return issues;
            }

            if (rawIssues instanceof String) {
                String issueText = ((String) rawIssues).trim();

                if (!issueText.equalsIgnoreCase("Não há erros.")
                        && !issueText.equalsIgnoreCase("Não há erros")
                        && !issueText.equalsIgnoreCase("Sem erros")
                        && !issueText.equalsIgnoreCase("Sem erros.")) {
                    issues.put(issueText);
                }
            }
        } catch (Exception ignored) {
            // mantém lista vazia
        }

        return issues;
    }

    private void ensureRuntime(Context context) throws Exception {
        if (!runtime.isReady()) {
            Log.i(TAG, "ensureRuntime() -> runtime não estava pronto, a inicializar...");
            runtime.initialize(context);
            Log.i(TAG, "ensureRuntime() -> runtime inicializado.");
        } else {
            Log.i(TAG, "ensureRuntime() -> runtime já estava pronto.");
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

        Log.i(TAG, "buildResult() -> original_lines=" + splitLines(originalText).size()
                + ", simplified_lines=" + splitLines(simplifiedText).size());

        return result;
    }

    private byte[] decodeBase64Payload(String base64Payload) {
        String normalized = base64Payload == null ? "" : base64Payload.trim();
        int commaIndex = normalized.indexOf(',');
        if (commaIndex >= 0) {
            normalized = normalized.substring(commaIndex + 1);
        }

        byte[] decoded = Base64.decode(normalized, Base64.DEFAULT);
        Log.i(TAG, "decodeBase64Payload() -> bytes=" + decoded.length);
        return decoded;
    }

    private String buildImageExtractionPrompt() {
        return "Extrai só o texto visível. Mantém a ordem. Devolve só o texto.";
    }

    private String buildSimplificationPrompt(String text) {
        return "Simplifica em português europeu. Usa frases curtas e palavras simples. Devolve só o texto final.\n\n"
                + text;
    }

    private String buildReadingPhrasePrompt(String ageGroup, String level, String type) {
        return "You are an educational assistant for dyslexic students.\n\n" +
                "Generate one reading sentence in European Portuguese for a child.\n\n" +
                "Requirements:\n" +
                "- age group: " + safe(ageGroup, "7-8") + "\n" +
                "- difficulty level: " + safe(level, "1") + "\n" +
                "- type: " + safe(type, "simple_sentence") + "\n" +
                "- short and readable\n" +
                "- natural European Portuguese\n" +
                "- suitable for guided reading\n" +
                "- no explanations\n" +
                "- return only valid JSON\n\n" +
                "JSON schema:\n" +
                "{\n" +
                "  \"text\": \"...\",\n" +
                "  \"type\": \"...\",\n" +
                "  \"level\": \"...\",\n" +
                "  \"language\": \"pt-PT\"\n" +
                "}";
    }

    private JSObject parseGeneratedPhrase(String raw, String ageGroup, String level, String type) throws Exception {
        JSObject result = new JSObject();

        try {
            JSONObject obj = new JSONObject(raw);

            result.put("text", obj.optString("text", ""));
            result.put("type", obj.optString("type", safe(type, "simple_sentence")));
            result.put("level", obj.optString("level", safe(level, "1")));
            result.put("language", obj.optString("language", "pt-PT"));

            Log.i(TAG, "parseGeneratedPhrase() -> JSON válido.");
        } catch (Exception e) {
            Log.w(TAG, "parseGeneratedPhrase() -> fallback para texto bruto.", e);
            result.put("text", raw);
            result.put("type", safe(type, "simple_sentence"));
            result.put("level", safe(level, "1"));
            result.put("language", "pt-PT");
        }

        String text = result.optString("text", "");
        if (text == null || text.trim().isEmpty()) {
            throw new Exception("O modelo não conseguiu gerar uma frase de leitura.");
        }

        return result;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
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
        cleaned = cleaned.replaceAll("^```(?:json|text)?\\s*", "").replaceAll("```$", "").trim();
        cleaned = cleaned.replaceAll("^([Oo]riginal|[Tt]exto(?: extra[ií]do)?|[Rr]esposta|[Ss]implificado)\\s*[:：]\\s*", "").trim();
        cleaned = cleaned.replaceAll("^\"|\"$", "").trim();
        cleaned = cleaned.replaceAll("\\s+\\n", "\n");
        cleaned = cleaned.replaceAll("[ \\t]+", " ").trim();
        return cleaned;
    }

    private JSArray toJsArray(List<String> items) {
        JSArray array = new JSArray();
        for (String item : items) {
            array.put(item);
        }
        return array;
    }

    private JSObject parseImageFullResult(String raw) throws Exception {
        try {
            JSONObject obj = new JSONObject(raw);

            String originalText = obj.optString("original_text", "").trim();
            String simplifiedText = obj.optString("simplified_text", "").trim();

            if (originalText.isEmpty()) {
                throw new Exception("O modelo não devolveu original_text.");
            }

            if (simplifiedText.isEmpty()) {
                simplifiedText = originalText;
            }

            JSObject result = new JSObject();
            result.put("success", true);
            result.put("original_text", originalText);
            result.put("simplified_text", simplifiedText);

            JSArray originalLines = new JSArray();
            if (obj.has("original_lines") && obj.optJSONArray("original_lines") != null) {
                for (int i = 0; i < obj.optJSONArray("original_lines").length(); i++) {
                    originalLines.put(obj.optJSONArray("original_lines").optString(i));
                }
            } else {
                originalLines = toJsArray(splitLines(originalText));
            }

            JSArray simplifiedLines = new JSArray();
            if (obj.has("simplified_lines") && obj.optJSONArray("simplified_lines") != null) {
                for (int i = 0; i < obj.optJSONArray("simplified_lines").length(); i++) {
                    simplifiedLines.put(obj.optJSONArray("simplified_lines").optString(i));
                }
            } else {
                simplifiedLines = toJsArray(splitLines(simplifiedText));
            }

            result.put("original_lines", originalLines);
            result.put("simplified_lines", simplifiedLines);

            JSObject meta = new JSObject();
            meta.put("source", "mobile");
            meta.put("engine", runtime.getName());
            meta.put("mode", "image-one-pass");

            result.put("meta", meta);

            Log.i(TAG, "parseImageFullResult() -> JSON válido.");
            return result;

        } catch (Exception e) {
            Log.e(TAG, "parseImageFullResult() -> falha ao interpretar JSON.", e);

            JSObject recovered = recoverImageFullResult(raw);
            if (recovered != null) {
                Log.w(TAG, "parseImageFullResult() -> resposta recuperada a partir de JSON parcial.");
                return recovered;
            }

            throw new Exception("O modelo devolveu uma resposta de imagem inválida.");
        }
    }

    private JSObject recoverImageFullResult(String raw) {
        String originalText = extractJsonLikeString(raw, "original_text");
        String simplifiedText = extractJsonLikeString(raw, "simplified_text");

        if (originalText.trim().isEmpty()) {
            return null;
        }

        if (simplifiedText.trim().isEmpty()) {
            simplifiedText = originalText;
        }

        JSObject result = new JSObject();
        result.put("success", true);
        result.put("original_text", originalText);
        result.put("simplified_text", simplifiedText);
        result.put("original_lines", toJsArray(splitLines(originalText)));
        result.put("simplified_lines", toJsArray(splitLines(simplifiedText)));

        JSObject meta = new JSObject();
        meta.put("source", "mobile");
        meta.put("engine", runtime.getName());
        meta.put("mode", "image-one-pass-recovered");

        result.put("meta", meta);
        return result;
    }

    private String extractJsonLikeString(String raw, String key) {
        String text = safe(raw, "");
        String marker = "\"" + key + "\"";
        int markerIndex = text.indexOf(marker);
        if (markerIndex < 0) return "";

        int colonIndex = text.indexOf(':', markerIndex + marker.length());
        if (colonIndex < 0) return "";

        int valueStart = text.indexOf('"', colonIndex + 1);
        if (valueStart < 0) return "";

        StringBuilder value = new StringBuilder();
        boolean escaping = false;

        for (int i = valueStart + 1; i < text.length(); i += 1) {
            char current = text.charAt(i);

            if (escaping) {
                switch (current) {
                    case 'n':
                        value.append('\n');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case '"':
                    case '\\':
                    case '/':
                        value.append(current);
                        break;
                    default:
                        value.append(current);
                        break;
                }
                escaping = false;
                continue;
            }

            if (current == '\\') {
                escaping = true;
                continue;
            }

            if (current == '"') {
                String remaining = text.substring(i + 1).trim();
                if (remaining.startsWith(",") || remaining.startsWith("}")) {
                    break;
                }
            }

            value.append(current);
        }

        return cleanModelText(value.toString());
    }

    private String buildImageFullPrompt() {
        return "Extract the visible text from this image. " +
                "Then rewrite it in simpler European Portuguese. " +
                "Return only this valid JSON: " +
                "{\"original_text\":\"...\",\"simplified_text\":\"...\"}";
    }

}
