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

package io.github.kgemini.internal.config

import java.util.Properties

internal object ConfigLoader {

    private const val DEFAULT_MODEL = "free"
    private const val DEFAULT_CONNECT_TIMEOUT_MS = 5_000L
    private const val DEFAULT_GENERATE_TIMEOUT_MS = 30_000L
    private const val DEFAULT_MAX_RETRIES = 3
    private const val DEFAULT_RETRY_BASE_DELAY_MS = 1_000L
    private const val DEFAULT_RETRY_MAX_DELAY_MS = 30_000L

    fun load(): GeminiConfig {
        return resolve(
            properties = loadProperties(),
            yaml = loadYaml(),
        )
    }

    internal fun resolve(
        properties: Map<String, String>,
        yaml: Map<String, String>,
    ): GeminiConfig {
        fun lookup(key: String): String? =
            yaml[key] ?: properties[key]

        val apiKey = lookup("gemini.api-key")
            ?: error("API key not provided. Configure gemini.api-key in gemini.properties or gemini.yml (src/main/resources/).")

        return GeminiConfig(
            apiKey = apiKey,
            model = lookup("gemini.model") ?: DEFAULT_MODEL,
            connectTimeoutMs = lookup("gemini.connect-timeout")
                ?.parseMillis() ?: DEFAULT_CONNECT_TIMEOUT_MS,
            generateTimeoutMs = lookup("gemini.timeout")
                ?.parseMillis() ?: DEFAULT_GENERATE_TIMEOUT_MS,
            maxRetries = lookup("gemini.max-retries")
                ?.toIntOrNull() ?: DEFAULT_MAX_RETRIES,
            retryBaseDelayMs = lookup("gemini.retry-base-delay")
                ?.parseMillis() ?: DEFAULT_RETRY_BASE_DELAY_MS,
            retryMaxDelayMs = lookup("gemini.retry-max-delay")
                ?.parseMillis() ?: DEFAULT_RETRY_MAX_DELAY_MS,
        )
    }

    private fun loadProperties(): Map<String, String> {
        val stream = Thread.currentThread().contextClassLoader
            .getResourceAsStream("gemini.properties") ?: return emptyMap()

        val props = Properties()
        stream.use { props.load(it) }

        return props.entries.associate { (k, v) -> k.toString() to v.toString() }
    }

    private fun loadYaml(): Map<String, String> {
        val text = Thread.currentThread().contextClassLoader
            .getResourceAsStream("gemini.yml")?.use { it.reader().readText() }
            ?: return emptyMap()

        return YamlParser.parse(text)
    }
}

/**
 * "30s" → 30000, "5000" → 5000, "60s" → 60000, "500ms" → 500
 */
internal fun String.parseMillis(): Long? {
    val trimmed = trim()
    return when {
        trimmed.endsWith("ms") -> trimmed.removeSuffix("ms").trim().toLongOrNull()
        trimmed.endsWith("s") -> trimmed.removeSuffix("s").trim().toLongOrNull()?.times(1000)
        else -> trimmed.toLongOrNull()
    }
}
