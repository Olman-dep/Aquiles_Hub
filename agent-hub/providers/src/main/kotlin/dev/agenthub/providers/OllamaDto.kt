package dev.agenthub.providers

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OllamaMessage(val role: String, val content: String = "")

@Serializable
internal data class OllamaChatRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = true,
    @SerialName("keep_alive") val keepAlive: String? = null,
)

@Serializable
internal data class OllamaChatChunk(
    val message: OllamaMessage? = null,
    val done: Boolean = false,
    @SerialName("prompt_eval_count") val promptEvalCount: Int = 0,
    @SerialName("eval_count") val evalCount: Int = 0,
    @SerialName("total_duration") val totalDurationNs: Long = 0,
    @SerialName("eval_duration") val evalDurationNs: Long = 0,
    val error: String? = null,
)

@Serializable
internal data class OllamaTagsResponse(val models: List<OllamaTag> = emptyList())

@Serializable
internal data class OllamaTag(
    val name: String,
    val size: Long = 0,
    val details: OllamaTagDetails? = null,
)

@Serializable
internal data class OllamaTagDetails(
    @SerialName("parameter_size") val parameterSize: String? = null,
    @SerialName("quantization_level") val quantizationLevel: String? = null,
)
