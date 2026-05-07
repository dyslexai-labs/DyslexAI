package om.dyslexai.app.inference

import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig

object LiteRtBridge {
    private const val TAG = "LiteRtBridge"
    private const val ENGINE_MAX_TOKENS = 2048

    @JvmStatic
    fun createEngine(modelPath: String, cacheDir: String, enableVision: Boolean, useMainGpu: Boolean): Engine {
        if (!useMainGpu) {
            android.util.Log.w(
                TAG,
                "createEngine() -> textBackend=GPU desativado pela politica do dispositivo. A usar CPU no executor principal e GPU apenas na visão."
            )

            return createEngineWithBackend(
                modelPath = modelPath,
                cacheDir = cacheDir,
                enableVision = enableVision,
                mainBackend = Backend.CPU(),
                mainBackendLabel = "CPU(device-policy)",
            )
        }

        return try {
            createEngineWithBackend(
                modelPath = modelPath,
                cacheDir = cacheDir,
                enableVision = enableVision,
                mainBackend = Backend.GPU(),
                mainBackendLabel = "GPU",
            )
        } catch (gpuError: Throwable) {
            android.util.Log.w(
                TAG,
                "createEngine() -> falhou com textBackend=GPU. A tentar fallback para CPU.",
                gpuError
            )

            createEngineWithBackend(
                modelPath = modelPath,
                cacheDir = cacheDir,
                enableVision = enableVision,
                mainBackend = Backend.CPU(),
                mainBackendLabel = "CPU",
            )
        }
    }

    private fun createEngineWithBackend(
        modelPath: String,
        cacheDir: String,
        enableVision: Boolean,
        mainBackend: Backend,
        mainBackendLabel: String,
    ): Engine {
        val engineConfig = EngineConfig(
            modelPath = modelPath,
            backend = mainBackend,
            visionBackend = if (enableVision) Backend.GPU() else null,
            audioBackend = Backend.CPU(),
            maxNumTokens = ENGINE_MAX_TOKENS,
            maxNumImages = if (enableVision) 1 else null,
            cacheDir = cacheDir,
        )

        android.util.Log.i(
            TAG,
            "createEngine() -> maxNumTokens=$ENGINE_MAX_TOKENS, textBackend=$mainBackendLabel, visionBackend=${if (enableVision) "GPU" else "none"}, audioBackend=CPU"
        )

        return Engine(engineConfig).also { it.initialize() }
    }

    @JvmStatic
    fun runText(engine: Engine, prompt: String): String {
        val conversation = createConversation(
            engine = engine,
            systemInstruction = "Return only the requested result.",
            topK = 20,
            topP = 0.9,
            temperature = 0.2,
        )
        return try {
            val start = System.nanoTime()
            val message = conversation.sendMessage(prompt)
            android.util.Log.i(TAG, "runText() -> inferenciaMs=${elapsedMs(start)}, promptChars=${prompt.length}")
            extractText(message)
        } finally {
            conversation.close()
        }
    }

    @JvmStatic
    fun runImage(engine: Engine, imagePath: String, prompt: String): String {
        val conversation = createConversation(
            engine = engine,
            systemInstruction = "Responde apenas ao pedido.",
            topK = 1,
            topP = 1.0,
            temperature = 0.0,
        )
        return try {
            val start = System.nanoTime()
            val message = conversation.sendMessage(
                Contents.of(
                    Content.ImageFile(imagePath),
                    Content.Text(prompt),
                )
            )
            android.util.Log.i(TAG, "runImage() -> inferenciaMs=${elapsedMs(start)}, promptChars=${prompt.length}, imagePath=$imagePath")
            extractText(message)
        } finally {
            conversation.close()
        }
    }

    @JvmStatic
    fun runAudioFile(engine: Engine, audioPath: String, prompt: String): String {
        val conversation = createConversation(
            engine = engine,
            systemInstruction = "Return only the requested result.",
            topK = 20,
            topP = 0.9,
            temperature = 0.2,
        )

        return try {
            android.util.Log.i(TAG, "runAudioFile() -> path=$audioPath")
            val start = System.nanoTime()

            val message = conversation.sendMessage(
                Contents.of(
                    Content.AudioFile(audioPath),
                    Content.Text(prompt)
                )
            )

            android.util.Log.i(TAG, "runAudioFile() -> inferenciaMs=${elapsedMs(start)}, promptChars=${prompt.length}")
            extractText(message)
        } finally {
            conversation.close()
        }
    }

    private fun createConversation(
        engine: Engine,
        systemInstruction: String,
        topK: Int,
        topP: Double,
        temperature: Double,
    ): Conversation {
        val config = ConversationConfig(
            systemInstruction = Contents.of(
                systemInstruction
            ),
            samplerConfig = SamplerConfig(
                topK = topK,
                topP = topP,
                temperature = temperature,
                seed = 42,
            ),
        )

        return engine.createConversation(config)
    }

    private fun elapsedMs(startNanos: Long): Long {
        return (System.nanoTime() - startNanos) / 1_000_000L
    }

    private fun extractText(message: Message): String {
        val allParts = message.contents.contents
        val textParts = allParts.filterIsInstance<Content.Text>()

        android.util.Log.i(
            TAG,
            "extractText() -> total parts=${allParts.size}, text parts=${textParts.size}"
        )

        allParts.forEachIndexed { index, part ->
            android.util.Log.i(
                TAG,
                "extractText() -> part[$index]=${part::class.java.simpleName} | $part"
            )
        }

        val text = textParts
            .joinToString("\n") { it.text }
            .trim()

        if (text.isNotBlank()) return text

        val fallback = message.toString().trim()
        return if (fallback.isNotBlank()) fallback else "[VAZIO]"
    }
}
