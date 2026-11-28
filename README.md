# 🛰️ Aquiles Hub

> Un **mission control** local y gratuito para orquestar y observar agentes de IA que corren sobre [Ollama](https://ollama.com). Ves en vivo qué hace cada modelo, a quién delega, qué herramientas usa y cuántos tokens consume.

Proyecto de aprendizaje y de uso real: Kotlin de punta a punta, sin APIs de pago.

---

## ✨ Qué es

Un "agente de agentes": un **supervisor** recibe una tarea, decide qué subagentes necesita (coder, tester, investigador…), les delega trabajo y combina resultados. Cada agente es un modelo de Ollama con su propio rol, herramientas y presupuesto de tokens. Una interfaz muestra todo lo que ocurre en tiempo real.

## 🧠 Principio de diseño: motor y pantalla separados

```
┌─────────────────────────────────────────────┐
│  UI (Compose Multiplatform Desktop)         │
│  solo dibuja eventos y envía comandos       │
└──────────────▲──────────────────────────────┘
               │  Flow<AgentEvent> / comandos
┌──────────────┴──────────────────────────────┐
│  ENGINE (Kotlin puro, sin UI)               │
│  orquestador · agentes · tools · metering   │
└───────┬─────────────────┬───────────────────┘
        │                 │
   Ollama API         SQLite + sandbox
```

El motor emite eventos (`AgentStarted`, `TokenStreamed`, `ToolCalled`, `Delegated`, `UsageUpdated`, `AgentFinished`, `Error`) y la UI solo se suscribe. La misma base sirve para escritorio hoy y web (WebSocket) mañana.

## 🧰 Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Kotlin/JVM 2.2, JDK 21 |
| Build | Gradle Kotlin DSL + version catalog |
| HTTP / streaming | Ktor Client (CIO), NDJSON |
| JSON | kotlinx.serialization |
| Concurrencia | Coroutines + Flow |
| UI | Compose Multiplatform Desktop *(Fase 2)* |
| Persistencia | SQLite + Exposed *(Fase 6)* |
| Sandbox de código | Docker sin red / `ProcessBuilder` con timeouts *(Fase 4)* |

## 📦 Módulos

```
agent-hub/
├── core/          dominio: mensajes, Usage, LlmProvider, eventos
├── providers/     OllamaProvider (después: otros proveedores)
├── playground/    ejecutable de pruebas por consola
├── tools/         (Fase 4) code-exec, archivos, shell, delegate
├── engine/        (Fase 3+) runtime de agentes, supervisor, planificador
├── metering/      (Fase 6) tokens, límites, presupuestos
├── storage/       (Fase 6) SQLite: sesiones, trazas, usage
├── app-desktop/   (Fase 2) Compose Desktop
└── server/        (Fase 7) Ktor + WebSocket
```

## 🖥️ Pantallas planificadas

1. **Mission Control** – lista de agentes con estado (💤 idle · 🧠 pensando · 🔧 tool · ⏳ esperando), grafo de delegaciones, stream en vivo por agente e inspector lateral.
2. **Trace** – línea de tiempo tipo Gantt por agente.
3. **Usage** – tokens por agente/modelo/sesión, tokens/s, límites.
4. **Agent Studio** – crear agentes: modelo (desde `/api/tags`), prompt, tools, presupuesto.
5. **Sandbox** – salida del código ejecutado y aprobación de comandos peligrosos.
6. **Barra inferior** – entrada tipo consola con comandos `/` y botón de parada global.

## ⚙️ Detalles específicos de Ollama

- Los tokens se leen exactos de `prompt_eval_count` y `eval_count` del último chunk.
- Tu GPU no carga todos los modelos a la vez: se usará `/api/ps` para ver qué hay en memoria y un **planificador** agrupará tareas por modelo para evitar recargas.
- No todos los modelos soportan *tool calling*; habrá un fallback por prompt estructurado.

## 🔒 Seguridad

Que un LLM ejecute código en tu máquina es lo más delicado. Por defecto: contenedor sin red, timeouts, límite de salida y **confirmación manual** para comandos destructivos.

## 🚀 Empezar

**Requisitos:** JDK 21, [Ollama](https://ollama.com) corriendo (`ollama serve`) y al menos un modelo (`ollama pull llama3.1`).

```bash
# 1. Genera el wrapper de Gradle (una sola vez, si no lo tienes)
gradle wrapper

# 2. Lista tus modelos
./gradlew :playground:run

# 3. Chatea con uno y mira el conteo de tokens
./gradlew :playground:run --args="llama3.1 'Explícame las corrutinas en 3 líneas'"
```

## 🗺️ Hoja de ruta

- [x] **Fase 1** – Cliente Ollama con streaming y conteo de tokens
- [ ] **Fase 2** – Ventana Compose con chat de un agente en streaming
- [ ] **Fase 3** – Varios agentes en paralelo + panel de estados
- [ ] **Fase 4** – Tools + sandbox con aprobación
- [ ] **Fase 5** – Delegación + grafo animado
- [ ] **Fase 6** – Usage, Trace, Agent Studio, persistencia
- [ ] **Fase 7** – Server/WebSocket + versión web

## 🎨 Estilo visual

Tema oscuro tipo IDE, un color por agente (nodo, tarjeta y línea de tiempo), fuente monoespaciada para el stream, pulso en el nodo activo y partículas en las conexiones al delegar.

## 📄 Licencia

MIT (a definir).
