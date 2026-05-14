package om.dyslexai.app.inference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DyslexAIEngine {

    private static final String TAG = "DyslexAIEngine";
    private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\\r?\\n+|(?<=[\\.!\\?])\\s+");
    private static final int IMAGE_MAX_EDGE_PX = 640;
    private static final int IMAGE_JPEG_QUALITY = 62;
    private static final double MIN_AUDIO_SPEECH_DURATION_SECONDS = 0.7;
    private static final double MAX_SILENCE_RMS = 250.0;
    private static final int MAX_SILENCE_PEAK = 1000;

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

    public void initialize(Context context) throws Exception {
        ensureRuntime(context);
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
        long totalStart = System.nanoTime();
        ensureRuntime(context);

        Log.i(TAG, "processImage() one-pass -> mimeType=" + mimeType +
                ", base64 length=" + (imageBase64 == null ? 0 : imageBase64.length()));

        long decodeStart = System.nanoTime();
        byte[] imageBytes = decodeBase64Payload(imageBase64);
        Log.i(TAG, "processImage() one-pass -> bytes decodificados=" + imageBytes.length +
                ", decodeMs=" + elapsedMs(decodeStart));

        String prompt = buildImageFullPrompt();
        Log.i(TAG, "Prompt única de imagem enviada ao runtime (" + prompt.length() + " chars):\n" + prompt);

        long inferStart = System.nanoTime();
        String raw = runtime.inferImage(imageBytes, safe(mimeType, "image/jpeg"), prompt);
        Log.i(TAG, "Resposta bruta da imagem one-pass inferMs=" + elapsedMs(inferStart) + ":\n" + raw);

        long parseStart = System.nanoTime();
        String cleaned = cleanModelText(raw);
        Log.i(TAG, "Resposta limpa da imagem one-pass:\n" + cleaned);

        JSObject result = parseImageFullResult(cleaned);
        JSONObject meta = result.optJSONObject("meta");
        if (meta == null) {
            meta = new JSObject();
            result.put("meta", meta);
        }
        meta.put("image_original_bytes", imageBytes.length);
        meta.put("image_mode", "frontend-image");
        meta.put("parse_ms", elapsedMs(parseStart));
        meta.put("total_ms", elapsedMs(totalStart));

        Log.i(TAG, "processImage() one-pass -> totalMs=" + elapsedMs(totalStart));
        return result;
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

        AudioSignalInfo signalInfo = inspectWavSignal(audioBytes);
        if (signalInfo != null) {
            Log.i(TAG, "processAudio() -> WAV analisado: durationSec=" + signalInfo.durationSeconds
                    + ", rms=" + signalInfo.rms
                    + ", peak=" + signalInfo.peak);

            if (!signalInfo.hasLikelySpeech()) {
                Log.w(TAG, "processAudio() -> áudio sem fala provável. Vou evitar inferência local.");
                return buildAudioNoSpeechResult(expectedText, mimeType, audioBytes.length, signalInfo);
            }
        }

        String transcriptionPrompt = buildAudioTranscriptionOnlyPrompt();
        Log.i(TAG, "Prompt de TRANSCRIÇÃO enviada ao runtime:\n" + transcriptionPrompt);

        String rawTranscription;
        try {
            rawTranscription = runtime.inferAudio(audioBytes, mimeType, transcriptionPrompt);
        } catch (Exception e) {
            Log.e(TAG, "inferAudio() falhou. Vou usar fallback controlado.", e);
            return buildAudioDecodeFallbackResult(expectedText, mimeType, audioBytes.length, e);
        }

        Log.i(TAG, "Resposta bruta da TRANSCRIÇÃO:\n" + rawTranscription);

        JSObject transcriptionParsed = parseModelJson(rawTranscription);
        String transcription = safe(transcriptionParsed.optString("transcription", ""), "");

        if (transcription.trim().isEmpty()) {
            transcription = extractLikelyPlainTranscription(rawTranscription);
        }

        transcription = cleanModelText(transcription);

        Log.i(TAG, "processAudio() -> transcrição isolada=" + transcription);

        String rawFeedback = "";
        JSObject feedbackParsed = new JSObject();

        if (!transcription.trim().isEmpty()) {
            String feedbackPrompt = buildAudioReadingFeedbackPrompt(expectedText, transcription);
            Log.i(TAG, "Prompt de FEEDBACK enviada ao runtime:\n" + feedbackPrompt);

            try {
                rawFeedback = runtime.inferText(feedbackPrompt);
                Log.i(TAG, "Resposta bruta do FEEDBACK:\n" + rawFeedback);
                feedbackParsed = parseModelJson(rawFeedback);
            } catch (Exception e) {
                Log.e(TAG, "inferText() para feedback falhou. Vou devolver transcrição sem análise completa.", e);
            }
        }

        String expectedTextFromFeedback = safe(feedbackParsed.optString("expected_text", ""), expectedText);
        String spokenText = safe(feedbackParsed.optString("spoken_text", ""), transcription);

        if (spokenText.trim().isEmpty()) {
            spokenText = transcription;
        }

        String cleanText = spokenText;

        String syllabifiedExpectedText = safe(feedbackParsed.optString("syllabified_expected_text", ""), "");
        String syllabifiedSpokenText = safe(feedbackParsed.optString("syllabified_spoken_text", ""), "");
        String feedbackComment = safe(feedbackParsed.optString("feedback_comment", ""), "");

        if (feedbackComment.trim().isEmpty()) {
            feedbackComment = "Tenta ler novamente com calma.";
        }

        JSArray issues = extractIssues(feedbackParsed);

        // Compatibilidade com o frontend atual: o botão “Ouvir comentário” pode continuar a ler estes campos.
        String comparisonSummary = feedbackComment;
        String positiveFeedback = "";
        String improvementTip = "";

        Log.i(TAG, "processAudio() -> transcription=" + transcription);
        Log.i(TAG, "processAudio() -> expected_text=" + expectedTextFromFeedback);
        Log.i(TAG, "processAudio() -> spoken_text=" + spokenText);
        Log.i(TAG, "processAudio() -> syllabified_expected_text=" + syllabifiedExpectedText);
        Log.i(TAG, "processAudio() -> syllabified_spoken_text=" + syllabifiedSpokenText);
        Log.i(TAG, "processAudio() -> feedback_comment=" + feedbackComment);
        Log.i(TAG, "processAudio() -> issues count=" + issues.length());

        JSObject result = new JSObject();
        result.put("success", true);
        result.put("transcription", transcription);
        result.put("expected_text", expectedTextFromFeedback);
        result.put("clean_text", cleanText);
        result.put("spoken_text", spokenText);
        result.put("spoken_lines", toJsArray(splitLines(spokenText)));
        result.put("syllabified_expected_text", syllabifiedExpectedText);
        result.put("syllabified_spoken_text", syllabifiedSpokenText);
        result.put("feedback_comment", feedbackComment);
        result.put("comparison_summary", comparisonSummary);
        result.put("positive_feedback", positiveFeedback);
        result.put("improvement_tip", improvementTip);
        result.put("issues", issues);
        result.put("raw", rawFeedback.trim().isEmpty() ? rawTranscription : rawFeedback);
        result.put("raw_transcription", rawTranscription);
        result.put("raw_feedback", rawFeedback);

        JSObject meta = new JSObject();
        meta.put("source", "mobile");
        meta.put("engine", runtime.getName());
        meta.put("mode", "audio-gemma-local-transcribe-then-feedback-v2-single-comment");

        result.put("meta", meta);

        return result;
    }

    private String buildAudioTranscriptionOnlyPrompt() {
        return "Transcreve o áudio em português europeu.\n" +
                "Regras obrigatórias:\n" +
                "- Escreve apenas o que foi dito no áudio.\n" +
                "- Não corrijas a frase.\n" +
                "- Não inventes palavras.\n" +
                "- Não uses nenhuma frase esperada, porque nesta etapa não existe frase esperada.\n" +
                "- Se não perceberes alguma palavra, escreve a aproximação que ouviste.\n" +
                "- Responde apenas em JSON válido, sem markdown.\n" +
                "Formato obrigatório: {\"transcription\":\"texto dito no áudio\"}";
    }

    private String buildAudioReadingFeedbackPrompt(String expectedText, String spokenTranscription) {
        String expected = safe(expectedText, "");
        String spoken = safe(spokenTranscription, "");

        return "És um assistente de leitura para crianças com dislexia.\n" +
                "Vais comparar duas frases já escritas.\n" +
                "Responde sempre em português de Portugal. Usa sempre 'tu' e nunca 'você'.\n\n" +

                "Frase original:\n" + expected + "\n\n" +
                "Frase transcrita da voz da criança:\n" + spoken + "\n\n" +

                "Tarefa:\n" +
                "1. Mantém exatamente a frase original em expected_text.\n" +
                "2. Mantém exatamente a frase transcrita em spoken_text.\n" +
                "3. Divide a frase original em sílabas.\n" +
                "4. Divide a frase transcrita em sílabas, se for legível.\n" +
                "5. Escreve UM comentário curto, simples e correto para a criança ouvir.\n\n" +

                "Regras para o comentário:\n" +
                "- Diz claramente se a frase está correta ou não.\n" +
                "- Se estiver correta, elogia de forma simples.\n" +
                "- Se estiver incorreta, identifica todos as diferenças entre as frases.\n" +
                "- Não digas que está correta se houver palavras diferentes, palavras a mais ou palavras em falta.\n" +
                "- Não contradigas a comparação.\n" +
                "- Usa frases curtas.\n" +
                "- Não uses termos técnicos.\n" +
                "- O comentário deve poder ser ouvido em voz alta por uma criança.\n\n" +

                "Regras para a divisão silábica:\n" +
                "- Mantém os espaços entre palavras.\n" +
                "- Usa hífen apenas dentro das palavras.\n" +
                "- Nunca juntes palavras diferentes com hífen.\n" +
                "- Não separes ditongos finais.\n" +
                "- As terminações 'eu', 'éu', 'iu', 'ou', 'ai', 'ei', 'oi', 'ui', 'ão', 'ãe' devem ficar juntas quando pertencem à mesma sílaba.\n" +
                "- Se a frase transcrita estiver ilegível ou incompleta, deixa syllabified_spoken_text vazio.\n\n" +

                "Exemplos corretos:\n" +
                "gato -> ga-to\n" +
                "rato -> ra-to\n" +
                "comeu -> co-meu\n" +
                "meu -> meu\n" +
                "deu -> deu\n" +
                "céu -> céu\n" +
                "não -> não\n" +
                "menina -> me-ni-na\n" +
                "computador -> com-pu-ta-dor\n" +
                "O gato comeu o rato. -> O ga-to co-meu o ra-to.\n\n" +

                "Exemplos errados:\n" +
                "comeu -> co-me-u\n" +
                "meu -> me-u\n" +
                "deu -> de-u\n" +
                "não -> nã-o\n" +
                "O gato comeu o rato. -> O-ga-to-co-me-u-o-ra-to\n\n" +

                "Antes de responder, verifica:\n" +
                "- se não escreveste 'co-me-u'; se escreveste, corrige para 'co-meu'.\n" +
                "- se não juntaste palavras diferentes com hífen.\n" +
                "- se o comentário diz claramente se a leitura está correta ou não.\n\n" +

                "Responde APENAS em JSON válido, sem markdown, sem ```json.\n\n" +

                "{\n" +
                "  \"expected_text\": \"frase original\",\n" +
                "  \"syllabified_expected_text\": \"frase original dividida em sílabas\",\n" +
                "  \"spoken_text\": \"frase transcrita\",\n" +
                "  \"syllabified_spoken_text\": \"frase transcrita dividida em sílabas ou vazio se não for legível\",\n" +
                "  \"feedback_comment\": \"comentário único, simples, correto e dirigido à criança\",\n" +
                "  \"issues\": [\n" +
                "    { \"type\": \"correta|omissão|troca|palavra_a_mais|pronúncia|ordem|outro\", \"expected\": \"...\", \"spoken\": \"...\", \"comment\": \"explicação simples\" }\n" +
                "  ]\n" +
                "}";
    }

    private String extractLikelyPlainTranscription(String raw) {
        String text = cleanModelText(raw);
        JSObject parsed = parseModelJson(text);
        String fromJson = safe(parsed.optString("transcription", ""), "");
        if (!fromJson.trim().isEmpty()) return cleanModelText(fromJson);

        text = text
                .replaceFirst("(?is)^```json\\s*", "")
                .replaceFirst("(?is)^```\\s*", "")
                .replaceFirst("(?is)```\\s*$", "")
                .replaceFirst("(?is)^json\\s*", "")
                .trim();

        int colon = text.indexOf(':');
        if (text.toLowerCase().contains("transcription") && colon >= 0 && colon < text.length() - 1) {
            text = text.substring(colon + 1).trim();
        }

        return cleanModelText(text);
    }

    private String cleanModelText(String value) {
        String text = safe(value, "").trim();
        text = text
                .replaceFirst("(?is)^```json\\s*", "")
                .replaceFirst("(?is)^```\\s*", "")
                .replaceFirst("(?is)```\\s*$", "")
                .replace("\\n", " ")
                .replace("\\r", " ")
                .trim();

        if ((text.startsWith("\"") && text.endsWith("\"")) ||
                (text.startsWith("'") && text.endsWith("'"))) {
            text = text.substring(1, text.length() - 1).trim();
        }

        return text.replaceAll("\\s+", " ").trim();
    }

    private JSObject buildAudioDecodeFallbackResult(String expectedText, String mimeType, int audioBytesLength, Exception error) {
        String fallbackText = safe(expectedText, "");

        if (fallbackText.trim().isEmpty()) {
            fallbackText = "Não consegui transcrever o áudio, mas a gravação foi recebida.";
        }

        JSObject result = new JSObject();
        result.put("success", true);
        result.put("transcription", fallbackText);
        result.put("expected_text", fallbackText);
        result.put("clean_text", fallbackText);
        result.put("spoken_text", fallbackText);
        result.put("spoken_lines", toJsArray(splitLines(fallbackText)));
        result.put("syllabified_expected_text", fallbackText);
        result.put("syllabified_spoken_text", fallbackText);
        result.put("feedback_comment", "Não foi possível obter uma análise completa da leitura. Tenta gravar novamente num local silencioso.");
        result.put("comparison_summary", "Não foi possível obter uma análise completa da leitura. Tenta gravar novamente num local silencioso.");
        result.put("positive_feedback", "");
        result.put("improvement_tip", "");
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

    private JSObject buildAudioNoSpeechResult(String expectedText, String mimeType, int audioBytesLength, AudioSignalInfo signalInfo) {
        String expected = safe(expectedText, "");
        String feedback = "Não ouvi fala suficiente na gravação. Tenta gravar novamente, falando perto do microfone.";

        JSObject result = new JSObject();
        result.put("success", true);
        result.put("no_speech_detected", true);
        result.put("transcription", "");
        result.put("expected_text", expected);
        result.put("clean_text", expected);
        result.put("spoken_text", "");
        result.put("spoken_lines", new JSArray());
        result.put("syllabified_expected_text", expected);
        result.put("syllabified_spoken_text", "");
        result.put("feedback_comment", feedback);
        result.put("comparison_summary", feedback);
        result.put("positive_feedback", "");
        result.put("improvement_tip", "Grava outra vez e lê a frase em voz alta.");
        result.put("issues", new JSArray());

        JSObject meta = new JSObject();
        meta.put("source", "mobile");
        meta.put("engine", runtime.getName());
        meta.put("mode", "audio-no-speech-detected");
        meta.put("mimeType", safe(mimeType, ""));
        meta.put("audioBytes", audioBytesLength);
        meta.put("durationSeconds", signalInfo == null ? 0.0 : signalInfo.durationSeconds);
        meta.put("rms", signalInfo == null ? 0.0 : signalInfo.rms);
        meta.put("peak", signalInfo == null ? 0 : signalInfo.peak);

        result.put("meta", meta);
        return result;
    }

    private AudioSignalInfo inspectWavSignal(byte[] wavBytes) {
        if (wavBytes == null || wavBytes.length < 44) {
            return null;
        }

        if (wavBytes[0] != 'R' || wavBytes[1] != 'I' || wavBytes[2] != 'F' || wavBytes[3] != 'F'
                || wavBytes[8] != 'W' || wavBytes[9] != 'A' || wavBytes[10] != 'V' || wavBytes[11] != 'E') {
            return null;
        }

        int sampleRate = readLeInt(wavBytes, 24);
        int bitsPerSample = readLeShort(wavBytes, 34);
        int dataOffset = -1;
        int dataSize = 0;

        int offset = 12;
        while (offset + 8 <= wavBytes.length) {
            String chunkId = new String(new byte[]{
                    wavBytes[offset],
                    wavBytes[offset + 1],
                    wavBytes[offset + 2],
                    wavBytes[offset + 3]
            });
            int chunkSize = readLeInt(wavBytes, offset + 4);
            int chunkDataOffset = offset + 8;

            if ("data".equals(chunkId)) {
                dataOffset = chunkDataOffset;
                dataSize = Math.max(0, Math.min(chunkSize, wavBytes.length - chunkDataOffset));
                break;
            }

            offset = chunkDataOffset + chunkSize + (chunkSize % 2);
        }

        if (sampleRate <= 0 || bitsPerSample != 16 || dataOffset < 0 || dataSize < 2) {
            return null;
        }

        int sampleCount = dataSize / 2;
        long sumSquares = 0L;
        int peak = 0;

        for (int i = 0; i < sampleCount; i++) {
            int sampleOffset = dataOffset + (i * 2);
            int sample = (short) (((wavBytes[sampleOffset + 1] & 0xff) << 8) | (wavBytes[sampleOffset] & 0xff));
            int abs = Math.abs(sample);
            if (abs > peak) {
                peak = abs;
            }
            sumSquares += (long) sample * (long) sample;
        }

        double rms = Math.sqrt(sumSquares / Math.max(1.0, sampleCount));
        double durationSeconds = sampleCount / (double) sampleRate;
        return new AudioSignalInfo(durationSeconds, rms, peak);
    }

    private int readLeInt(byte[] bytes, int offset) {
        if (offset < 0 || offset + 4 > bytes.length) return 0;
        return (bytes[offset] & 0xff)
                | ((bytes[offset + 1] & 0xff) << 8)
                | ((bytes[offset + 2] & 0xff) << 16)
                | ((bytes[offset + 3] & 0xff) << 24);
    }

    private int readLeShort(byte[] bytes, int offset) {
        if (offset < 0 || offset + 2 > bytes.length) return 0;
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }

    private static class AudioSignalInfo {
        final double durationSeconds;
        final double rms;
        final int peak;

        AudioSignalInfo(double durationSeconds, double rms, int peak) {
            this.durationSeconds = durationSeconds;
            this.rms = rms;
            this.peak = peak;
        }

        boolean hasLikelySpeech() {
            return durationSeconds >= MIN_AUDIO_SPEECH_DURATION_SECONDS
                    && (rms > MAX_SILENCE_RMS || peak > MAX_SILENCE_PEAK);
        }
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

    private byte[] optimizeImageBytesForInference(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return imageBytes == null ? new byte[0] : imageBytes;
        }

        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, bounds);

            int sourceWidth = bounds.outWidth;
            int sourceHeight = bounds.outHeight;

            if (sourceWidth <= 0 || sourceHeight <= 0) {
                Log.w(TAG, "optimizeImageBytesForInference() -> dimensões inválidas; usar imagem original.");
                return imageBytes;
            }

            int sampleSize = 1;
            int longest = Math.max(sourceWidth, sourceHeight);
            while ((longest / sampleSize) > (IMAGE_MAX_EDGE_PX * 2)) {
                sampleSize *= 2;
            }

            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = sampleSize;
            decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap decoded = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, decodeOptions);
            if (decoded == null) {
                Log.w(TAG, "optimizeImageBytesForInference() -> decode falhou; usar imagem original.");
                return imageBytes;
            }

            int width = decoded.getWidth();
            int height = decoded.getHeight();
            float scale = Math.min(1f, IMAGE_MAX_EDGE_PX / (float) Math.max(width, height));
            int targetWidth = Math.max(1, Math.round(width * scale));
            int targetHeight = Math.max(1, Math.round(height * scale));

            Bitmap scaled = decoded;
            if (targetWidth != width || targetHeight != height) {
                scaled = Bitmap.createScaledBitmap(decoded, targetWidth, targetHeight, true);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, IMAGE_JPEG_QUALITY, output);
            byte[] optimized = output.toByteArray();

            if (scaled != decoded) {
                scaled.recycle();
            }
            decoded.recycle();

            Log.i(TAG, "optimizeImageBytesForInference() -> original=" + sourceWidth + "x" + sourceHeight +
                    ", decoded=" + width + "x" + height +
                    ", final=" + targetWidth + "x" + targetHeight +
                    ", sampleSize=" + sampleSize +
                    ", quality=" + IMAGE_JPEG_QUALITY +
                    ", bytes=" + imageBytes.length + "->" + optimized.length);

            return optimized.length > 0 ? optimized : imageBytes;
        } catch (Exception e) {
            Log.w(TAG, "optimizeImageBytesForInference() -> falhou; usar imagem original.", e);
            return imageBytes;
        }
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
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

            JSObject labeled = parseImageLabeledResult(raw);
            if (labeled != null) {
                Log.w(TAG, "parseImageFullResult() -> resposta recuperada a partir de formato ORIGINAL/SIMPLIFICADO.");
                return labeled;
            }

            throw new Exception("O modelo devolveu uma resposta de imagem inválida.");
        }
    }

    private JSObject parseImageLabeledResult(String raw) {
        String text = cleanModelText(raw);
        if (text.trim().isEmpty()) return null;

        String lower = text.toLowerCase();
        int originalIndex = lower.indexOf("original:");
        int simplifiedIndex = lower.indexOf("simplificado:");

        if (originalIndex < 0 || simplifiedIndex < 0 || simplifiedIndex <= originalIndex) {
            return null;
        }

        String originalText = text
                .substring(originalIndex + "original:".length(), simplifiedIndex)
                .trim();
        String simplifiedText = text
                .substring(simplifiedIndex + "simplificado:".length())
                .trim();

        originalText = cleanModelText(originalText);
        simplifiedText = cleanModelText(simplifiedText);

        if (originalText.isEmpty()) return null;
        if (simplifiedText.isEmpty()) simplifiedText = originalText;

        JSObject result = new JSObject();
        result.put("success", true);
        result.put("original_text", originalText);
        result.put("simplified_text", simplifiedText);
        result.put("original_lines", toJsArray(splitLines(originalText)));
        result.put("simplified_lines", toJsArray(splitLines(simplifiedText)));

        JSObject meta = new JSObject();
        meta.put("source", "mobile");
        meta.put("engine", runtime.getName());
        meta.put("mode", "image-one-pass-labeled");

        result.put("meta", meta);
        return result;
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
        return "Transcreve o texto visível na imagem e cria uma versão simplificada para dislexia em português europeu.\n" +
                "Responde só neste formato curto:\n" +
                "ORIGINAL: texto transcrito\n" +
                "SIMPLIFICADO: texto simplificado";
    }
}
