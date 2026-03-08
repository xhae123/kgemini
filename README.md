<h1 align="center">kgemini</h1>

<p align="center">
  <strong>The simplest way to call Gemini from Kotlin.</strong>
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.1.20-7F52FF.svg?logo=kotlin&logoColor=white" alt="Kotlin"></a>
  <a href="https://github.com/xhae123/kgemini/actions"><img src="https://img.shields.io/github/actions/workflow/status/xhae123/kgemini/ci.yml?label=CI" alt="CI"></a>
  <img src="https://img.shields.io/badge/JDK-21%2B-orange.svg?logo=openjdk&logoColor=white" alt="JDK 21+">
</p>

<br>

```kotlin
val answer = ask("Explain coroutines in one sentence")
```

No builder. No config object. No boilerplate. **Just ask.**

---

## Quick Start

**1. Add dependency**

```kotlin
// build.gradle.kts
implementation("io.github.kgemini:kgemini-core:0.2.0")
```

```groovy
// build.gradle
implementation 'io.github.kgemini:kgemini-core:0.2.0'
```

**2. Add API key** to `src/main/resources/gemini.yml` ([Get one here](https://aistudio.google.com/apikey) — free, no credit card)

```yaml
gemini:
  api-key: your-api-key
```

```kotlin
// 3. Done.
import io.github.kgemini.ask

fun main() {
    println(ask("What is the meaning of life?"))
}
```

That's the whole tutorial.

## Why kgemini?

<table>
<tr>
<th>Other SDKs (8 lines)</th>
<th>kgemini (1 line)</th>
</tr>
<tr>
<td>

```kotlin
val model = GenerativeModel.Builder()
    .setModelName("gemini-2.5-flash")
    .setApiKey(apiKey)
    .build()
val request = GenerateContentRequest.newBuilder()
    .addContents(Content.newBuilder()
        .addParts(Part.newBuilder()
            .setText("hello").build())
        .build())
    .build()
val response = model.generateContent(request)
val text = response.candidates[0]
    .content.parts[0].text
```

</td>
<td>

```kotlin
val text = ask("hello")
```

</td>
</tr>
</table>

### Design Choices

- **Blocking I/O** — Virtual Thread friendly. No coroutines needed.
- **Zero dependencies** — Only `kotlinx-serialization-json`. That's it.
- **Zero config** — Works out of the box with sensible defaults.
- **Typed errors** — Sealed exception hierarchy. No string matching.
- **Auto retry** — Retryable errors (429, 5xx) are automatically retried with exponential backoff.

## Java

Kotlin top-level functions are accessed via `KGeminiKt` in Java:

```java
import static io.github.kgemini.KGeminiKt.ask;
import static io.github.kgemini.KGeminiKt.generate;

String answer = ask("Explain virtual threads in one sentence");
```

## Full Response

When you need more than just text:

```kotlin
val response = generate("Tell me about Mars")
response.text          // generated text
response.totalTokens   // token usage
```

## Models

Pick a model by alias or use any Gemini model ID directly.

| Alias | Model | Best For |
|-------|-------|----------|
| **`free`** (default) | `gemini-2.5-flash` | General use. Free tier available. |
| **`fast`** | `gemini-2.5-flash-lite` | Low latency, high throughput |
| **`cheap`** | `gemini-2.5-flash-lite` | Bulk processing on a budget |
| **`smart`** | `gemini-2.5-pro` | Complex reasoning, planning |

```yaml
# Switch model — no code change (gemini.yml)
gemini:
  model: smart
```

Any valid model ID works too: `gemini-2.5-flash-latest`, `gemini-2.5-pro-001`, etc.

## Configuration

Zero config required. Override only when needed.

Config files go in your **classpath** (`src/main/resources/`):

<table>
<tr>
<th>gemini.yml (recommended)</th>
<th>gemini.properties</th>
</tr>
<tr>
<td>

```yaml
gemini:
  api-key: your-api-key
  model: smart
  timeout: 60s
```

</td>
<td>

```properties
gemini.api-key=your-api-key
gemini.model=smart
gemini.timeout=60s
```

</td>
</tr>
</table>

```
your-project/
└── src/main/resources/
    ├── gemini.yml          ← recommended
    └── gemini.properties   ← also works
```

> **Tip:** Add `gemini.properties` and `gemini.yml` to your `.gitignore` if they contain API keys.

**Priority:** `gemini.yml` > `gemini.properties` > defaults

| Key | Default |
|-----|---------|
| `model` | `free` (`gemini-2.5-flash`) |
| `timeout` | `30s` |
| `max-retries` | `3` |

## Error Handling

Errors are typed. Retryable ones are automatically retried.

> **No API key?** kgemini throws `IllegalStateException` immediately with a clear message — no silent failures.

```kotlin
try {
    ask("question")
} catch (e: GeminiException) {
    when (e) {
        is RateLimitException      -> // auto-retried; thrown after exhausting retries
        is AuthenticationException -> // invalid API key (401/403)
        is ModelNotFoundException  -> // invalid model ID (404)
        is InvalidRequestException -> // fix your prompt (400)
        is ServerException         -> // Gemini is down (5xx, auto-retried)
        is ConnectionException     -> // check network (auto-retried)
        is TimeoutException        -> // took too long (auto-retried)
    }
}
```

## Virtual Threads

kgemini uses blocking I/O by design — pair with JDK 21+ Virtual Threads for scalable concurrency without coroutines.

```properties
# Spring Boot 3.2+ — one line, zero code change
spring.threads.virtual.enabled=true
```

## Requirements

| | Version |
|---|---------|
| JDK | 21+ |
| Kotlin | 2.1+ |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Questions and ideas welcome in [Discussions](../../discussions).

## License

Apache License 2.0 — see [LICENSE](LICENSE).
