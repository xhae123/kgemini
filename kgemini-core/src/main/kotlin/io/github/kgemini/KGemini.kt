package io.github.kgemini

import io.github.kgemini.exception.StreamInterruptedException
import io.github.kgemini.exception.StreamTimeoutException
import io.github.kgemini.internal.http.Endpoints
import io.github.kgemini.internal.http.GeminiHttpClient
import io.github.kgemini.internal.serialization.geminiJson
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeout
import java.io.Closeable
import java.io.IOException

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

    public val model: io.github.kgemini.model.GeminiModel
        get() = config.model

    /**
     * GET /models — 사용 가능한 모델 목록 조회.
     */
    public suspend fun listModels(): List<ModelInfo> {
        val response = httpClient.get<ListModelsResponse>(Endpoints.listModels())
        return response.models
    }

    /**
     * GET /models/{model} — 특정 모델 정보 조회.
     */
    public suspend fun getModel(modelId: String): ModelInfo {
        return httpClient.get(Endpoints.getModel(modelId))
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
    ): GenerateContentResponse {
        return httpClient.post(Endpoints.generateContent(config.model.id), request)
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

    override fun close() {
        httpClient.close()
    }
}
