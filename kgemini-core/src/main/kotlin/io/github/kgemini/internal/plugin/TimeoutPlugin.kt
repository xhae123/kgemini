package io.github.kgemini.internal.plugin

import io.github.kgemini.exception.ConnectTimeoutException
import io.github.kgemini.exception.GenerateTimeoutException
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal val TimeoutPlugin = createClientPlugin("GeminiTimeout", ::TimeoutPluginConfig) {
    val connectTimeout = pluginConfig.connect
    val generateTimeout = pluginConfig.generate

    onRequest { request, _ ->
        request.timeout {
            connectTimeoutMillis = connectTimeout.inWholeMilliseconds
            requestTimeoutMillis = generateTimeout.inWholeMilliseconds
            socketTimeoutMillis = generateTimeout.inWholeMilliseconds
        }
    }

    on(ResponseException) { cause ->
        when (cause) {
            is HttpRequestTimeoutException -> throw GenerateTimeoutException(cause = cause)
            is ConnectTimeoutException -> throw ConnectTimeoutException(cause = cause)
            else -> throw cause
        }
    }
}

internal class TimeoutPluginConfig {
    var connect: Duration = 5.seconds
    var generate: Duration = 30.seconds
    var streamFirstByte: Duration = 10.seconds
    var streamIdle: Duration = 5.seconds
}

// Ktor의 ResponseException hook를 커스텀 Plugin에서 사용하기 위한 hook 정의
internal object ResponseException :
    ClientHook<suspend (cause: Throwable) -> Unit> {
    override fun install(
        client: io.ktor.client.HttpClient,
        handler: suspend (cause: Throwable) -> Unit
    ) {
        client.requestPipeline.intercept(io.ktor.client.request.HttpRequestPipeline.Before) {
            try {
                proceed()
            } catch (cause: Throwable) {
                handler(cause)
            }
        }
    }
}
