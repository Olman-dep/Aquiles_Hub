package dev.agenthub.providers

import dev.agenthub.core.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class OllamaProvider(
    private val baseUrl: String = "http://localhost:11434",
) : LlmProvider {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            // Un modelo grande puede tardar en cargarse y en generar: sin límite de petición.
            requestTimeoutMillis = Long.MAX_VALUE
            connectTimeoutMillis = 5_000
            socketTimeoutMillis = Long.MAX_VALUE
        }
    }

    override suspend fun listModels(): List<ModelInfo> =
        client.get("$baseUrl/api/tags").body<OllamaTagsResponse>().models.map {
            ModelInfo(it.name, it.size, it.details?.parameterSize, it.details?.quantizationLevel)
        }

    override fun chat(request: ChatRequest): Flow<ChatEvent> = flow {
        val body = OllamaChatRequest(
            model = request.model,
            messages = request.messages.map { OllamaMessage(it.role.wire, it.content) },
            keepAlive = request.keepAlive,
        )

        client.preparePost("$baseUrl/api/chat") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.execute { response ->
            if (!response.status.isSuccess()) {
                error("Ollama respondió ${response.status}: ${response.bodyAsText()}")
            }
            val channel = response.bodyAsChannel()
            // Ollama responde NDJSON: un objeto JSON por línea.
            while (true) {
                val line = channel.readUTF8Line() ?: break
                if (line.isBlank()) continue
                val chunk = json.decodeFromString<OllamaChatChunk>(line)
                chunk.error?.let { error("Ollama: $it") }

                chunk.message?.content?.takeIf { it.isNotEmpty() }?.let { emit(ChatEvent.Token(it)) }

                if (chunk.done) {
                    emit(
                        ChatEvent.Done(
                            Usage(
                                promptTokens = chunk.promptEvalCount,
                                completionTokens = chunk.evalCount,
                                totalDurationMs = chunk.totalDurationNs / 1_000_000,
                                evalDurationMs = chunk.evalDurationNs / 1_000_000,
                            )
                        )
                    )
                }
            }
        }
    }

    fun close() = client.close()
}
