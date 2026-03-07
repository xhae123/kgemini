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

import java.io.File
import java.util.Properties

internal object ConfigLoader {

    private const val DEFAULT_MODEL = "free"
    private const val DEFAULT_CONNECT_TIMEOUT_MS = 5_000L
    private const val DEFAULT_GENERATE_TIMEOUT_MS = 30_000L
    private const val DEFAULT_MAX_RETRIES = 3
    private const val DEFAULT_RETRY_BASE_DELAY_MS = 1_000L
    private const val DEFAULT_RETRY_MAX_DELAY_MS = 30_000L

    fun load(): GeminiConfig {
        val props = loadProperties()
        val yaml = loadYaml()

        fun resolve(envKey: String, fileKey: String): String? =
            System.getenv(envKey) ?: yaml[fileKey] ?: props[fileKey]

        val apiKey = resolve("GEMINI_API_KEY", "gemini.api-key")
            ?: error("API key not provided. Set GEMINI_API_KEY environment variable or configure gemini.api-key in gemini.properties/gemini.yml.")

        return GeminiConfig(
            apiKey = apiKey,
            model = resolve("GEMINI_MODEL", "gemini.model") ?: DEFAULT_MODEL,
            connectTimeoutMs = resolve("GEMINI_CONNECT_TIMEOUT", "gemini.connect-timeout")
                ?.toLongOrNull() ?: DEFAULT_CONNECT_TIMEOUT_MS,
            generateTimeoutMs = resolve("GEMINI_TIMEOUT", "gemini.timeout")
                ?.toLongOrNull() ?: DEFAULT_GENERATE_TIMEOUT_MS,
            maxRetries = resolve("GEMINI_MAX_RETRIES", "gemini.max-retries")
                ?.toIntOrNull() ?: DEFAULT_MAX_RETRIES,
            retryBaseDelayMs = resolve("GEMINI_RETRY_BASE_DELAY", "gemini.retry-base-delay")
                ?.toLongOrNull() ?: DEFAULT_RETRY_BASE_DELAY_MS,
            retryMaxDelayMs = resolve("GEMINI_RETRY_MAX_DELAY", "gemini.retry-max-delay")
                ?.toLongOrNull() ?: DEFAULT_RETRY_MAX_DELAY_MS,
        )
    }

    private fun loadProperties(): Map<String, String> {
        val file = File("gemini.properties")
        if (!file.exists()) return emptyMap()

        val props = Properties()
        file.inputStream().use { props.load(it) }

        return props.entries.associate { (k, v) -> k.toString() to v.toString() }
    }

    private fun loadYaml(): Map<String, String> {
        val file = File("gemini.yml")
        if (!file.exists()) return emptyMap()

        return YamlParser.parse(file.readText())
    }
}
