package om.dyslexai.app.inference;

import android.content.Context;

public interface LocalModelRuntime {
    void initialize(Context context) throws Exception;
    boolean isReady();
    String inferText(String prompt) throws Exception;
    String inferImage(byte[] imageBytes, String mimeType, String prompt) throws Exception;
    String inferAudio(byte[] audioBytes, String mimeType, String prompt) throws Exception;
    String getName();
}