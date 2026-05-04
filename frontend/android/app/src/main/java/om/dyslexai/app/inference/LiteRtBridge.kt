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

    @JvmStatic
    fun createEngine(modelPath: String, cacheDir: String, enableVision: Boolean): Engine {
        val engineConfig = EngineConfig(
            modelPath = modelPath,
            backend = Backend.CPU(),
            visionBackend = if (enableVision) Backend.GPU() else null,
            audioBackend = Backend.CPU(),
            maxNumTokens = 512,
            maxNumImages = if (enableVision) 1 else null,
            cacheDir = cacheDir,
        )

        return Engine(engineConfig).also { it.initialize() }
    }

    @JvmStatic
    fun runText(engine: Engine, prompt: String): String {
        val conversation = createConversation(engine)
        return try {
            val message = conversation.sendMessage(prompt)
            extractText(message)
        } finally {
            conversation.close()
        }
    }

    @JvmStatic
    fun runImage(engine: Engine, imagePath: String, prompt: String): String {
        val conversation = createConversation(engine)
        return try {
            val message = conversation.sendMessage(
                Contents.of(
                    Content.ImageFile(imagePath),
                    Content.Text(prompt),
                )
            )
            extractText(message)
        } finally {
            conversation.close()
        }
    }

    @JvmStatic
    fun runAudioFile(engine: Engine, audioPath: String, prompt: String): String {
        val conversation = createConversation(engine)

        return try {
            android.util.Log.i("LiteRtBridge", "runAudioFile() -> path=$audioPath")

            val message = conversation.sendMessage(
                Contents.of(
                    Content.AudioFile(audioPath),
                    Content.Text(prompt)
                )
            )

            extractText(message)
        } finally {
            conversation.close()
        }
    }

    private fun createConversation(engine: Engine): Conversation {
        val config = ConversationConfig(
            systemInstruction = Contents.of(
                "Return only the requested result."
            ),
            samplerConfig = SamplerConfig(
                topK = 20,
                topP = 0.9,
                temperature = 0.2,
                seed = 42,
            ),
        )

        return engine.createConversation(config)
    }

    private fun extractText(message: Message): String {
        val allParts = message.contents.contents
        val textParts = allParts.filterIsInstance<Content.Text>()

        android.util.Log.i(
            "LiteRtBridge",
            "extractText() -> total parts=${allParts.size}, text parts=${textParts.size}"
        )

        allParts.forEachIndexed { index, part ->
            android.util.Log.i(
                "LiteRtBridge",
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
