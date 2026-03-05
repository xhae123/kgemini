package io.github.kgemini.internal.http

import io.github.kgemini.internal.plugin.AuthPlugin
import io.github.kgemini.internal.plugin.ErrorMappingPlugin
import io.github.kgemini.internal.plugin.TimeoutPlugin
import io.github.kgemini.internal.serialization.geminiJson
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.HttpTimeoutConfig.Companion.INFINITE_TIMEOUT_MS
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import java.io.Closeable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class GeminiHttpClient(
    apiKey: String,
    engine: GeminiEngine = GeminiEngine.CIO,
    testEngine: io.ktor.client.engine.HttpClientEngine? = null,
    connectTimeout: Duration = 5.seconds,
    generateTimeout: Duration = 30.seconds,
    streamFirstByte: Duration = 10.seconds,
    streamIdle: Duration = 5.seconds,
) : Closeable {

    val streamFirstByteTimeout: Duration = streamFirstByte
    val streamIdleTimeout: Duration = streamIdle

    val client: HttpClient = if (testEngine != null) HttpClient(testEngine) {
        install(ContentNegotiation) {
            json(geminiJson)
        }
        install(AuthPlugin) {
            this.apiKey = apiKey
        }
        install(ErrorMappingPlugin)
    } else HttpClient(resolveEngine(engine)) {
        install(ContentNegotiation) {
            json(geminiJson)
        }

        install(HttpTimeout)

        install(AuthPlugin) {
            this.apiKey = apiKey
        }

        install(TimeoutPlugin) {
            this.connect = connectTimeout
            this.generate = generateTimeout
        }

        install(ErrorMappingPlugin)
    }

    suspend inline fun <reified T> get(url: String): T {
        val response = client.get(url)
        return geminiJson.decodeFromString(response.bodyAsText())
    }

    suspend inline fun <reified R> post(url: String, body: Any): R {
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return geminiJson.decodeFromString(response.bodyAsText())
    }

    suspend fun streamPost(url: String, body: Any): HttpStatement {
        return client.preparePost(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
            parameter("alt", "sse")
            timeout {
                requestTimeoutMillis = INFINITE_TIMEOUT_MS
                socketTimeoutMillis = INFINITE_TIMEOUT_MS
            }
        }
    }

    override fun close() {
        client.close()
    }
}
