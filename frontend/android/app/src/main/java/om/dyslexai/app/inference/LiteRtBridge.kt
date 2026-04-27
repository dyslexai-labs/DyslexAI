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
    fun runAudio(engine: Engine, audioPath: String, prompt: String): String {
        throw UnsupportedOperationException("Áudio ainda não ligado ao LiteRtBridge nesta etapa.")
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
        val text = message.contents.contents
            .filterIsInstance<Content.Text>()
            .joinToString("\n") { it.text }
            .trim()

        if (text.isNotBlank()) return text
        return message.toString().trim()
    }
}