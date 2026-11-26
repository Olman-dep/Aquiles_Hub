package dev.agenthub.playground

import dev.agenthub.core.*
import dev.agenthub.providers.OllamaProvider
import kotlinx.coroutines.runBlocking

/**
 * Uso:
 *   ./gradlew :playground:run                      -> lista tus modelos
 *   ./gradlew :playground:run --args="llama3.1 'Hola, quién eres?'"
 */
fun main(args: Array<String>) = runBlocking {
    val ollama = OllamaProvider()
    try {
        val models = ollama.listModels()
        println("Modelos en Ollama (${models.size}):")
        models.forEach {
            println("  • ${it.name}  ${it.parameterSize ?: ""} ${it.quantization ?: ""}  ${it.sizeBytes / 1_000_000} MB")
        }

        if (args.size < 2) {
            println("\nPara probar un chat: --args=\"<modelo> '<pregunta>'\"")
            return@runBlocking
        }

        val (model, prompt) = args
        println("\n[$model] > $prompt\n")
        ollama.chat(ChatRequest(model, listOf(ChatMessage(Role.USER, prompt)))).collect { event ->
            when (event) {
                is ChatEvent.Token -> print(event.text)
                is ChatEvent.Done -> {
                    val u = event.usage
                    println("\n\n— prompt: ${u.promptTokens} · respuesta: ${u.completionTokens} · " +
                        "total: ${u.totalTokens} tokens · ${"%.1f".format(u.tokensPerSecond)} tok/s · ${u.totalDurationMs} ms")
                }
            }
        }
    } finally {
        ollama.close()
    }
}
