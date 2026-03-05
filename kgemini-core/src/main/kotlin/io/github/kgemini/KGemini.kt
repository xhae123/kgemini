package io.github.kgemini

import io.github.kgemini.internal.http.Endpoints
import io.github.kgemini.internal.http.GeminiHttpClient
import io.github.kgemini.model.ListModelsResponse
import io.github.kgemini.model.ModelInfo
import java.io.Closeable

public class KGemini(
    apiKey: String,
    configure: KGeminiConfig.() -> Unit = {},
) : Closeable {

    private val config: KGeminiConfig = KGeminiConfig().apply(configure)

    internal val httpClient: GeminiHttpClient = GeminiHttpClient(
        apiKey = apiKey,
        engine = config.engine,
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

    override fun close() {
        httpClient.close()
    }
}
