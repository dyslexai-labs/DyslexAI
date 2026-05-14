package om.dyslexai.app.inference;

public class RuntimeFactory {

    // Centralizes the default runtime choice so the app keeps using GemmaLocalRuntime.
    public static LocalModelRuntime createDefaultRuntime() {
        return new GemmaLocalRuntime();
    }
}
