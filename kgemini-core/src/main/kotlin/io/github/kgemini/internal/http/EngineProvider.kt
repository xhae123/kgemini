package io.github.kgemini.internal.http

import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

public enum class GeminiEngine {
    CIO, OKHTTP, JAVA, AUTO
}

internal fun resolveEngine(engine: GeminiEngine): HttpClientEngineFactory<*> {
    return when (engine) {
        GeminiEngine.CIO -> CIO
        GeminiEngine.OKHTTP -> loadEngineOrFallback("io.ktor.client.engine.okhttp.OkHttp")
        GeminiEngine.JAVA -> loadEngineOrFallback("io.ktor.client.engine.java.Java")
        GeminiEngine.AUTO -> tryLoadOkHttp() ?: CIO
    }
}

@Suppress("UNCHECKED_CAST")
private fun loadEngineOrFallback(className: String): HttpClientEngineFactory<*> {
    return try {
        val clazz = Class.forName(className)
        // Ktor engines are Kotlin objects — access via INSTANCE field
        clazz.getField("INSTANCE").get(null) as HttpClientEngineFactory<*>
    } catch (_: ReflectiveOperationException) {
        CIO
    }
}

@Suppress("UNCHECKED_CAST")
private fun tryLoadOkHttp(): HttpClientEngineFactory<*>? {
    return try {
        val clazz = Class.forName("io.ktor.client.engine.okhttp.OkHttp")
        clazz.getField("INSTANCE").get(null) as HttpClientEngineFactory<*>
    } catch (_: ReflectiveOperationException) {
        null
    }
}
