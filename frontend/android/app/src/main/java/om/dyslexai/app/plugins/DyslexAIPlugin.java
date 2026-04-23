package om.dyslexai.app.plugins;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import om.dyslexai.app.inference.DyslexAIEngine;
import om.dyslexai.app.inference.LocalModelRuntime;
import om.dyslexai.app.inference.RuntimeFactory;

@CapacitorPlugin(name = "DyslexAI")
public class DyslexAIPlugin extends Plugin {

    private DyslexAIEngine engine;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void load() {
        LocalModelRuntime runtime = RuntimeFactory.createDefaultRuntime();
        engine = new DyslexAIEngine(runtime);
    }

    @PluginMethod
    public void health(PluginCall call) {
        call.resolve(engine.health());
    }

    @PluginMethod
    public void getCapabilities(PluginCall call) {
        call.resolve(engine.getCapabilities());
    }

    @PluginMethod
    public void processText(PluginCall call) {
        String text = call.getString("text");

        if (text == null || text.trim().isEmpty()) {
            call.reject("Texto vazio.");
            return;
        }

        executor.execute(() -> {
            try {
                JSObject result = engine.processText(getContext(), text);
                call.resolve(result);
            } catch (Exception e) {
                call.reject("Erro ao processar texto: " + e.getMessage(), e);
            }
        });
    }

    @PluginMethod
    public void processImage(PluginCall call) {
        String imageBase64 = call.getString("imageBase64");
        String mimeType = call.getString("mimeType", "image/jpeg");

        if (imageBase64 == null || imageBase64.trim().isEmpty()) {
            call.reject("Imagem vazia.");
            return;
        }

        executor.execute(() -> {
            try {
                JSObject result = engine.processImage(getContext(), imageBase64, mimeType);
                call.resolve(result);
            } catch (Exception e) {
                call.reject("Erro ao processar imagem: " + e.getMessage(), e);
            }
        });
    }
}