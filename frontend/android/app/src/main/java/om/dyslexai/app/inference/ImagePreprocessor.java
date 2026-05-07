package om.dyslexai.app.inference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.JSObject;

import java.io.ByteArrayOutputStream;

public class ImagePreprocessor {
    private static final String TAG = "ImagePreprocessor";

    public static JSObject prepare(String input, String mode) throws Exception {
        long start = System.currentTimeMillis();

        byte[] inputBytes = decodeBase64OrDataUrl(input);
        int inputSizeKb = Math.round(inputBytes.length / 1024f);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.length, bounds);

        int sourceWidthBeforeDecode = bounds.outWidth;
        int sourceHeightBeforeDecode = bounds.outHeight;

        int maxDecodeEdge = getMaxDecodeEdge(mode);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxDecodeEdge);
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap source = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.length, options);

        if (source == null) {
            throw new Exception("Não foi possível descodificar a imagem.");
        }

        int decodedWidth = source.getWidth();
        int decodedHeight = source.getHeight();

        Rect crop = detectUsefulCrop(source);
        Bitmap cropped = Bitmap.createBitmap(source, crop.x, crop.y, crop.width, crop.height);

        if (cropped != source) {
            source.recycle();
        }

        Bitmap enhanced = enhanceContrast(cropped, getContrast(mode), getBrightness(mode));

        if (enhanced != cropped) {
            cropped.recycle();
        }

        Bitmap resized = resizeToMaxEdge(enhanced, getFinalMaxEdge(mode));

        if (resized != enhanced) {
            enhanced.recycle();
        }

        int quality = getJpegQuality(mode);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, quality, output);

        byte[] finalBytes = output.toByteArray();
        int finalSizeKb = Math.round(finalBytes.length / 1024f);

        String finalBase64 = Base64.encodeToString(finalBytes, Base64.NO_WRAP);
        String dataUrl = "data:image/jpeg;base64," + finalBase64;

        JSObject result = new JSObject();
        result.put("imageBase64", dataUrl);
        result.put("mimeType", "image/jpeg");
        result.put("mode", mode);
        result.put("inputSizeKb", inputSizeKb);
        result.put("finalSizeKb", finalSizeKb);
        result.put("originalWidth", sourceWidthBeforeDecode);
        result.put("originalHeight", sourceHeightBeforeDecode);
        result.put("decodedWidth", decodedWidth);
        result.put("decodedHeight", decodedHeight);
        result.put("cropX", crop.x);
        result.put("cropY", crop.y);
        result.put("cropWidth", crop.width);
        result.put("cropHeight", crop.height);
        result.put("finalWidth", resized.getWidth());
        result.put("finalHeight", resized.getHeight());
        result.put("jpegQuality", quality);
        result.put("elapsedMs", System.currentTimeMillis() - start);

        Log.i(TAG, "prepare() INÍCIO");
        Log.i(TAG, "Imagem recebida: " + sourceWidthBeforeDecode + "x" + sourceHeightBeforeDecode + " | " + inputSizeKb + " KB");
        Log.i(TAG, "Imagem descodificada: " + decodedWidth + "x" + decodedHeight + " | inSampleSize=" + options.inSampleSize);
        Log.i(TAG, "Crop aplicado: x=" + crop.x + ", y=" + crop.y + ", w=" + crop.width + ", h=" + crop.height);
        Log.i(TAG, "Imagem final: " + resized.getWidth() + "x" + resized.getHeight() + " | " + finalSizeKb + " KB | quality=" + quality);
        Log.i(TAG, "Redução aproximada: " + inputSizeKb + " KB -> " + finalSizeKb + " KB");
        Log.i(TAG, "prepare() FIM em " + (System.currentTimeMillis() - start) + " ms");

        resized.recycle();

        return result;
    }

    private static byte[] decodeBase64OrDataUrl(String input) {
        String clean = input;
        int commaIndex = input.indexOf(",");

        if (input.startsWith("data:") && commaIndex >= 0) {
            clean = input.substring(commaIndex + 1);
        }

        return Base64.decode(clean, Base64.DEFAULT);
    }

    private static int calculateInSampleSize(int width, int height, int maxEdge) {
        int inSampleSize = 1;
        int largest = Math.max(width, height);

        while ((largest / inSampleSize) > maxEdge * 2) {
            inSampleSize *= 2;
        }

        return Math.max(1, inSampleSize);
    }

    private static Rect detectUsefulCrop(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int minX = width;
        int minY = height;
        int maxX = 0;
        int maxY = 0;

        int step = Math.max(2, Math.min(width, height) / 450);
        int threshold = 244;

        for (int y = 0; y < height; y += step) {
            for (int x = 0; x < width; x += step) {
                int pixel = bitmap.getPixel(x, y);

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                boolean useful = r < threshold || g < threshold || b < threshold;

                if (useful) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (minX >= maxX || minY >= maxY) {
            return new Rect(0, 0, width, height);
        }

        int padding = Math.round(Math.min(width, height) * 0.035f);

        int x = Math.max(0, minX - padding);
        int y = Math.max(0, minY - padding);
        int right = Math.min(width, maxX + padding);
        int bottom = Math.min(height, maxY + padding);

        return new Rect(x, y, Math.max(1, right - x), Math.max(1, bottom - y));
    }

    private static Bitmap resizeToMaxEdge(Bitmap source, int maxEdge) {
        int width = source.getWidth();
        int height = source.getHeight();

        int largest = Math.max(width, height);

        if (largest <= maxEdge) {
            return source;
        }

        float scale = maxEdge / (float) largest;
        int targetWidth = Math.max(1, Math.round(width * scale));
        int targetHeight = Math.max(1, Math.round(height * scale));

        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
    }

    private static Bitmap enhanceContrast(Bitmap source, float contrast, float brightness) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int[] pixels = new int[width * height];

        source.getPixels(pixels, 0, width, 0, 0, width, height);

        float brightOffset = (brightness - 1.0f) * 255.0f;

        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i];

            int a = Color.alpha(pixel);
            int r = adjustChannel(Color.red(pixel), contrast, brightOffset);
            int g = adjustChannel(Color.green(pixel), contrast, brightOffset);
            int b = adjustChannel(Color.blue(pixel), contrast, brightOffset);

            pixels[i] = Color.argb(a, r, g, b);
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height);

        return result;
    }

    private static int adjustChannel(int value, float contrast, float brightnessOffset) {
        int adjusted = Math.round(((value - 128) * contrast) + 128 + brightnessOffset);
        return Math.max(0, Math.min(255, adjusted));
    }

    private static int getMaxDecodeEdge(String mode) {
        if ("accurate".equals(mode)) return 2600;
        if ("fast".equals(mode)) return 1600;
        return 2200;
    }

    private static int getFinalMaxEdge(String mode) {
        if ("accurate".equals(mode)) return 1600;
        if ("fast".equals(mode)) return 1000;
        return 1300;
    }

    private static int getJpegQuality(String mode) {
        if ("accurate".equals(mode)) return 82;
        if ("fast".equals(mode)) return 68;
        return 74;
    }

    private static float getContrast(String mode) {
        if ("accurate".equals(mode)) return 1.08f;
        if ("fast".equals(mode)) return 1.12f;
        return 1.10f;
    }

    private static float getBrightness(String mode) {
        if ("accurate".equals(mode)) return 1.02f;
        if ("fast".equals(mode)) return 1.04f;
        return 1.03f;
    }

    private static class Rect {
        final int x;
        final int y;
        final int width;
        final int height;

        Rect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}