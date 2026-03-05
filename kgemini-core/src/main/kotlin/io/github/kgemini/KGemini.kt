package io.github.kgemini

import io.github.kgemini.exception.GeminiException
import io.github.kgemini.exception.RateLimitException
import io.github.kgemini.exception.StreamInterruptedException
import io.github.kgemini.exception.StreamTimeoutException
import io.github.kgemini.internal.http.Endpoints
import io.github.kgemini.internal.http.GeminiHttpClient
import io.github.kgemini.internal.serialization.geminiJson
import io.github.kgemini.model.Content
import io.github.kgemini.model.CountTokensRequest
import io.github.kgemini.model.CountTokensResponse
import io.github.kgemini.model.GenerateContentRequest
import io.github.kgemini.model.GenerateContentResponse
import io.github.kgemini.model.ListModelsResponse
import io.github.kgemini.model.ModelInfo
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import java.io.Closeable
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public class KGemini(
    apiKey: String = System.getenv("GEMINI_API_KEY")
        ?: error("API key not provided. Set GEMINI_API_KEY environment variable or pass apiKey parameter."),
    configure: KGeminiConfig.() -> Unit = {},
) : Closeable {

    private val config: KGeminiConfig = KGeminiConfig().apply(configure)

    internal val httpClient: GeminiHttpClient = GeminiHttpClient(
        apiKey = apiKey,
        engine = config.engine,
        testEngine = config.testEngine,
        connectTimeout = config.connectTimeout,
        generateTimeout = config.generateTimeout,
        streamFirstByte = config.streamFirstByte,
        streamIdle = config.streamIdle,
    )

    private val maxRetries: Int = config.maxRetries
    private val retryBaseDelay: Duration = config.retryBaseDelay
    private val retryMaxDelay: Duration = config.retryMaxDelay

    public val model: io.github.kgemini.model.GeminiModel
        get() = config.model

    /**
     * GET /models — 사용 가능한 모델 목록 조회.
     */
    public suspend fun listModels(): List<ModelInfo> = withRetry {
        val response = httpClient.get<ListModelsResponse>(Endpoints.listModels())
        response.models
    }

    /**
     * GET /models/{model} — 특정 모델 정보 조회.
     */
    public suspend fun getModel(modelId: String): ModelInfo = withRetry {
        httpClient.get(Endpoints.getModel(modelId))
    }

    /**
     * POST /models/{model}:generateContent — 텍스트 생성 (편의 API).
     */
    public suspend fun generate(
        prompt: String,
        configure: GenerateContentRequest.Builder.() -> Unit = {},
    ): GenerateContentResponse {
        val request = GenerateContentRequest.Builder().apply(configure).build(prompt)
        return generate(request)
    }

    /**
     * POST /models/{model}:generateContent — 텍스트 생성 (Request 직접 전달).
     */
    public suspend fun generate(
        request: GenerateContentRequest,
    ): GenerateContentResponse = withRetry {
        httpClient.post(Endpoints.generateContent(config.model.id), request)
    }

    /**
     * 여러 프롬프트를 병렬로 생성. 결과는 입력 순서 보장.
     */
    public suspend fun generateAll(
        vararg prompts: String,
        configure: GenerateContentRequest.Builder.() -> Unit = {},
    ): List<GenerateContentResponse> = coroutineScope {
        prompts.map { prompt ->
            async { generate(prompt, configure) }
        }.awaitAll()
    }

    /**
     * POST /models/{model}:streamGenerateContent — 스트리밍 텍스트 생성 (편의 API).
     */
    public fun generateStream(
        prompt: String,
        configure: GenerateContentRequest.Builder.() -> Unit = {},
    ): Flow<GenerateContentResponse> {
        val request = GenerateContentRequest.Builder().apply(configure).build(prompt)
        return generateStream(request)
    }

    /**
     * POST /models/{model}:streamGenerateContent — 스트리밍 텍스트 생성 (Request 직접 전달).
     */
    public fun generateStream(
        request: GenerateContentRequest,
    ): Flow<GenerateContentResponse> = flow {
        val receivedChunks = mutableListOf<GenerateContentResponse>()
        val firstByteTimeout = httpClient.streamFirstByteTimeout
        val idleTimeout = httpClient.streamIdleTimeout

        try {
            val statement = httpClient.streamPost(
                Endpoints.streamGenerateContent(config.model.id),
                request,
            )
            withTimeout(firstByteTimeout) {
                statement.execute { response ->
                    val channel = response.bodyAsChannel()
                    var firstChunkReceived = false

                    while (!channel.isClosedForRead) {
                        val line = if (!firstChunkReceived) {
                            channel.readUTF8Line()
                        } else {
                            withTimeout(idleTimeout) { channel.readUTF8Line() }
                        } ?: break

                        if (!line.startsWith("data: ")) continue
                        if (!firstChunkReceived) firstChunkReceived = true

                        val json = line.removePrefix("data: ")
                        val chunk = geminiJson.decodeFromString<GenerateContentResponse>(json)
                        receivedChunks.add(chunk)
                        emit(chunk)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            if (receivedChunks.isEmpty()) {
                throw StreamTimeoutException("Stream first byte timed out", cause = e)
            } else {
                throw StreamTimeoutException(
                    "Stream idle timed out after ${receivedChunks.size} chunks", cause = e
                )
            }
        } catch (e: IOException) {
            throw StreamInterruptedException(
                receivedChunks = receivedChunks.toList(),
                partialUsage = receivedChunks.lastOrNull()?.usageMetadata,
                cause = e,
            )
        }
    }.flowOn(Dispatchers.Default)

    /**
     * POST /models/{model}:countTokens — 토큰 수 카운트 (편의 API).
     */
    public suspend fun countTokens(prompt: String): CountTokensResponse {
        val request = CountTokensRequest(contents = listOf(Content.user(prompt)))
        return countTokens(request)
    }

    /**
     * POST /models/{model}:countTokens — 토큰 수 카운트 (Request 직접 전달).
     */
    public suspend fun countTokens(request: CountTokensRequest): CountTokensResponse = withRetry {
        httpClient.post(Endpoints.countTokens(config.model.id), request)
    }

    /**
     * Retries the given block on retryable GeminiExceptions with exponential backoff + jitter.
     * For RateLimitException, uses Retry-After header value when available.
     */
    private suspend fun <T> withRetry(block: suspend () -> T): T {
        var lastException: GeminiException? = null
        repeat(maxRetries + 1) { attempt ->
            try {
                return block()
            } catch (e: GeminiException) {
                if (!e.retryable || attempt >= maxRetries) throw e
                lastException = e
                val backoff = computeBackoff(attempt, e)
                delay(backoff)
            }
        }
        throw lastException!!
    }

    private fun computeBackoff(attempt: Int, exception: GeminiException): Duration {
        // RateLimitException with Retry-After takes priority
        if (exception is RateLimitException && exception.retryAfter != null) {
            return exception.retryAfter
        }
        // Exponential backoff: baseDelay * 2^attempt, capped at maxDelay
        val exponentialMs = retryBaseDelay.inWholeMilliseconds * (1L shl attempt)
        val cappedMs = min(exponentialMs, retryMaxDelay.inWholeMilliseconds)
        // Add jitter: ±25%
        val jitter = (cappedMs * 0.25 * (Random.nextDouble() * 2 - 1)).toLong()
        val delayMs = (cappedMs + jitter).coerceAtLeast(0L)
        return delayMs.milliseconds
    }

    override fun close() {
        httpClient.close()
    }
}
