package io.github.kgemini.internal.plugin

import io.github.kgemini.exception.ConnectTimeoutException
import io.github.kgemini.exception.GenerateTimeoutException
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import kotlin.coroutines.cancellation.CancellationException
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

    on(RequestExceptionHook) { cause ->
        when {
            cause is CancellationException -> throw cause
            cause is HttpRequestTimeoutException -> throw GenerateTimeoutException(cause = cause)
            cause is io.ktor.client.network.sockets.ConnectTimeoutException ->
                throw ConnectTimeoutException(cause = cause)
            else -> throw cause
        }
    }
}

internal class TimeoutPluginConfig {
    var connect: Duration = 5.seconds
    var generate: Duration = 30.seconds
}

/**
 * Hook that intercepts exceptions thrown during request execution,
 * allowing mapping of Ktor exceptions to kgemini's exception hierarchy.
 */
internal object RequestExceptionHook :
    ClientHook<suspend (cause: Throwable) -> Unit> {
    override fun install(
        client: io.ktor.client.HttpClient,
        handler: suspend (cause: Throwable) -> Unit
    ) {
        client.requestPipeline.intercept(io.ktor.client.request.HttpRequestPipeline.Before) {
            try {
                proceed()
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: Throwable) {
                handler(cause)
            }
        }
    }
}
