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

import io.github.kgemini.internal.serialization.geminiJson
import kotlinx.serialization.encodeToString

internal class GeminiHttpClient(
    private val apiKey: String,
    private val generateTimeoutMs: Long,
    private val executor: HttpExecutor,
) {
    inline fun <reified T, reified R> post(url: String, body: T): R {
        val jsonBody = geminiJson.encodeToString<T>(body)
        val headers = mapOf(
            "Content-Type" to "application/json",
            "x-goog-api-key" to apiKey,
        )

        val result = executor.execute(url, jsonBody, headers, generateTimeoutMs)
        ErrorMapper.throwIfError(result)

        return geminiJson.decodeFromString(result.body)
    }
}
