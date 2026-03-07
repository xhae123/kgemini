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

package io.github.kgemini

import io.github.kgemini.internal.config.ConfigLoader
import io.github.kgemini.internal.config.GeminiConfig
import io.github.kgemini.internal.http.Endpoints
import io.github.kgemini.internal.http.GeminiHttpClient
import io.github.kgemini.internal.http.HttpExecutor
import io.github.kgemini.internal.http.JdkHttpExecutor
import io.github.kgemini.internal.model.ModelResolver
import io.github.kgemini.internal.retry.RetryExecutor
import io.github.kgemini.model.Content
import io.github.kgemini.model.GenerateContentRequest
import io.github.kgemini.model.GenerateContentResponse

/**
 * One-line Gemini API call.
 *
 * ```kotlin
 * val answer = ask("Explain coroutines")
 * ```
 *
 * @throws io.github.kgemini.exception.GeminiException on API or network errors
 * @throws IllegalStateException if response contains no text
 */
public fun ask(prompt: String): String {
    val response = KGeminiInstance.client.generate(prompt)
    return response.text ?: error("Gemini returned no text for prompt: ${prompt.take(50)}")
}

/**
 * Generate with full response details.
 *
 * ```kotlin
 * val response = generate("Tell me about Mars")
 * response.text
 * response.totalTokens
 * ```
 */
public fun generate(prompt: String): GenerateContentResponse {
    return KGeminiInstance.client.generate(prompt)
}

internal object KGeminiInstance {
    val client: GeminiClient by lazy { GeminiClient(ConfigLoader.load()) }
}

internal class GeminiClient(
    private val config: GeminiConfig,
    executor: HttpExecutor = JdkHttpExecutor(config.connectTimeoutMs),
) {
    private val modelId: String = ModelResolver.resolve(config.model)

    private val httpClient: GeminiHttpClient = GeminiHttpClient(
        apiKey = config.apiKey,
        generateTimeoutMs = config.generateTimeoutMs,
        executor = executor,
    )

    fun generate(prompt: String): GenerateContentResponse {
        val request = GenerateContentRequest(
            contents = listOf(Content.user(prompt)),
        )
        return RetryExecutor.withRetry(
            maxRetries = config.maxRetries,
            baseDelayMs = config.retryBaseDelayMs,
            maxDelayMs = config.retryMaxDelayMs,
        ) {
            httpClient.post(Endpoints.generateContent(modelId), request)
        }
    }
}
