package om.dyslexai.app.plugins;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WavAudioRecorder {

    private static final String TAG = "WavAudioRecorder";

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BITS_PER_SAMPLE = 16;
    private static final int CHANNELS = 1;

    private AudioRecord audioRecord;
    private Thread recordingThread;
    private volatile boolean recording = false;
    private ByteArrayOutputStream pcmBuffer;

    public synchronized void start() throws Exception {
        if (recording) {
            Log.w(TAG, "start() chamado mas já estava a gravar.");
            return;
        }

        int minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
        );

        if (minBufferSize <= 0) {
            throw new Exception("AudioRecord não suporta 16 kHz mono PCM neste dispositivo.");
        }

        int bufferSize = Math.max(minBufferSize, SAMPLE_RATE * 2);

        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
        );

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            throw new Exception("Não foi possível inicializar AudioRecord.");
        }

        pcmBuffer = new ByteArrayOutputStream();
        recording = true;

        audioRecord.startRecording();

        recordingThread = new Thread(() -> {
            byte[] buffer = new byte[bufferSize];

            while (recording) {
                int read = audioRecord.read(buffer, 0, buffer.length);

                if (read > 0) {
                    pcmBuffer.write(buffer, 0, read);
                }
            }
        }, "DyslexAI-WavRecorder");

        recordingThread.start();

        Log.i(TAG, "Gravação WAV iniciada: 16 kHz, mono, PCM 16-bit.");
    }

    public synchronized byte[] stopAndGetWavBytes() throws Exception {
        if (!recording && audioRecord == null) {
            throw new Exception("Não existe gravação ativa.");
        }

        recording = false;

        if (recordingThread != null) {
            try {
                recordingThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            recordingThread = null;
        }

        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception ignored) {
            }

            audioRecord.release();
            audioRecord = null;
        }

        byte[] pcmBytes = pcmBuffer == null ? new byte[0] : pcmBuffer.toByteArray();
        pcmBuffer = null;

        if (pcmBytes.length == 0) {
            throw new Exception("A gravação não captou áudio.");
        }

        byte[] wavBytes = buildWavFile(pcmBytes);

        Log.i(TAG, "Gravação WAV terminada. PCM bytes=" + pcmBytes.length +
                ", WAV bytes=" + wavBytes.length);

        return wavBytes;
    }

    public synchronized boolean isRecording() {
        return recording;
    }

    public synchronized void cancel() {
        recording = false;

        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception ignored) {
            }

            try {
                audioRecord.release();
            } catch (Exception ignored) {
            }

            audioRecord = null;
        }

        recordingThread = null;
        pcmBuffer = null;
    }

    public String toDataUrl(byte[] wavBytes) {
        String base64 = Base64.encodeToString(wavBytes, Base64.NO_WRAP);
        return "data:audio/wav;base64," + base64;
    }

    private byte[] buildWavFile(byte[] pcmBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int byteRate = SAMPLE_RATE * CHANNELS * BITS_PER_SAMPLE / 8;
        int blockAlign = CHANNELS * BITS_PER_SAMPLE / 8;
        int dataSize = pcmBytes.length;
        int chunkSize = 36 + dataSize;

        writeString(out, "RIFF");
        writeIntLE(out, chunkSize);
        writeString(out, "WAVE");

        writeString(out, "fmt ");
        writeIntLE(out, 16);
        writeShortLE(out, (short) 1);
        writeShortLE(out, (short) CHANNELS);
        writeIntLE(out, SAMPLE_RATE);
        writeIntLE(out, byteRate);
        writeShortLE(out, (short) blockAlign);
        writeShortLE(out, (short) BITS_PER_SAMPLE);

        writeString(out, "data");
        writeIntLE(out, dataSize);
        out.write(pcmBytes);

        return out.toByteArray();
    }

    private void writeString(ByteArrayOutputStream out, String value) throws IOException {
        out.write(value.getBytes("US-ASCII"));
    }

    private void writeIntLE(ByteArrayOutputStream out, int value) {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
        out.write((value >> 16) & 0xff);
        out.write((value >> 24) & 0xff);
    }

    private void writeShortLE(ByteArrayOutputStream out, short value) {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
    }
}