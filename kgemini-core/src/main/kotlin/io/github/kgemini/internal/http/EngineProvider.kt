/*
 * Copyright 2025 kgemini contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
