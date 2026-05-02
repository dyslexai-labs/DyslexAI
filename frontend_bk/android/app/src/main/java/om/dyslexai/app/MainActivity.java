package om.dyslexai.app;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;
import om.dyslexai.app.plugins.DyslexAIPlugin;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(DyslexAIPlugin.class);
        super.onCreate(savedInstanceState);
    }
}