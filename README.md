# kgemini

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-7F52FF.svg)](https://kotlinlang.org)

The simplest way to call Gemini from Kotlin.

```kotlin
val answer = ask("Explain coroutines in one sentence")
```

No builder. No config object. No boilerplate. Just ask.

## Setup

**1.** Add dependency

```kotlin
dependencies {
    implementation("io.github.kgemini:kgemini-core:0.1.0")
}
```

**2.** Set your API key

```bash
export GEMINI_API_KEY=your-api-key
```

**3.** Done

```kotlin
import io.github.kgemini.ask

fun main() {
    println(ask("What is the meaning of life?"))
}
```

That's the whole tutorial.

## Why kgemini?

Other SDKs:

```kotlin
val model = GenerativeModel.Builder()
    .setModelName("gemini-2.0-flash")
    .setApiKey(apiKey)
    .build()
val request = GenerateContentRequest.newBuilder()
    .addContents(Content.newBuilder()
        .addParts(Part.newBuilder().setText("hello").build())
        .build())
    .build()
val response = model.generateContent(request)
val text = response.candidates[0].content.parts[0].text
```

kgemini:

```kotlin
val text = ask("hello")
```

## Full Response

When you need more than just text:

```kotlin
val response = generate("Tell me about Mars")
response.text          // generated text
response.totalTokens   // token usage
```

## Models

Use aliases for quick selection, or specify any model ID directly.

| Alias | Model | Description |
|-------|-------|-------------|
| `free` | gemini-2.0-flash | Free tier. Good enough for most use cases |
| `fast` | gemini-2.0-flash | Fastest responses |
| `cheap` | gemini-2.0-flash-lite | Cheapest. For bulk processing |
| `smart` | gemini-2.5-pro | Smartest. For complex reasoning |

Default is `free` (no config needed).

```bash
export GEMINI_MODEL=smart
```

Any valid Gemini model ID also works directly: `gemini-2.5-flash`, `gemini-2.5-pro-latest`, etc.

## Configuration

Zero config required. Sensible defaults just work.

Override only when needed — no code changes, just drop a file:

```properties
# gemini.properties
gemini.model=gemini-2.5-flash
gemini.timeout=60s
```

```yaml
# gemini.yml
gemini:
  model: gemini-2.5-flash
  timeout: 60s
```

```bash
# or environment variables
export GEMINI_MODEL=gemini-2.5-flash
export GEMINI_TIMEOUT=60s
```

Priority: env var > gemini.yml > gemini.properties > defaults

| Key | Default |
|-----|---------|
| model | `gemini-2.0-flash` |
| timeout | `30s` |
| max-retries | `3` |

## Error Handling

Errors are typed. Retryable ones are automatically retried.

```kotlin
try {
    ask("question")
} catch (e: GeminiException) {
    when (e) {
        is RateLimitException -> // auto-retried, this is after exhausting retries
        is AuthenticationException -> // check your API key
        is ModelNotFoundException -> // check model ID
        is InvalidRequestException -> // fix your prompt
        is ServerException -> // Gemini is down
        is ConnectionException -> // check network
        is TimeoutException -> // took too long
    }
}
```

## Virtual Threads

kgemini uses blocking I/O by design. Pair with JDK 21+ Virtual Threads for scalable, thread-efficient execution.

```properties
# Spring Boot 3.2+ — one line, zero code change
server.tomcat.threads.virtual.enabled=true
```

## Requirements

- JDK 21+
- Kotlin 2.1+

## License

Apache License 2.0 — see [LICENSE](LICENSE).
