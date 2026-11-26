package dev.agenthub.core

import kotlinx.coroutines.flow.Flow

enum class Role(val wire: String) { SYSTEM("system"), USER("user"), ASSISTANT("assistant"), TOOL("tool") }

data class ChatMessage(val role: Role, val content: String)

data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val keepAlive: String? = null,
)

/** Consumo de una respuesta. Ollama lo reporta exacto en el último chunk. */
data class Usage(
    val promptTokens: Int = 0,
    val completionTokens: Int = 0,
    val totalDurationMs: Long = 0,
    val evalDurationMs: Long = 0,
) {
    val totalTokens: Int get() = promptTokens + completionTokens
    val tokensPerSecond: Double
        get() = if (evalDurationMs > 0) completionTokens * 1000.0 / evalDurationMs else 0.0
}

data class ModelInfo(val name: String, val sizeBytes: Long, val parameterSize: String?, val quantization: String?)

sealed interface ChatEvent {
    data class Token(val text: String) : ChatEvent
    data class Done(val usage: Usage) : ChatEvent
}

interface LlmProvider {
    suspend fun listModels(): List<ModelInfo>
    fun chat(request: ChatRequest): Flow<ChatEvent>
}
