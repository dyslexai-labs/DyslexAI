package om.dyslexai.app.inference;

public class RuntimeFactory {

    public static LocalModelRuntime createDefaultRuntime() {
        return new GemmaLocalRuntime();
    }
}
