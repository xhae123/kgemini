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

import io.github.kgemini.exception.*
import io.github.kgemini.internal.serialization.geminiJson
import io.github.kgemini.model.ErrorResponse
import kotlin.time.Duration.Companion.seconds

internal object ErrorMapper {

    fun throwIfError(result: HttpResult) {
        if (result.statusCode in 200..299) return

        val errorMessage = try {
            val errorResponse = geminiJson.decodeFromString<ErrorResponse>(result.body)
            errorResponse.error?.message ?: result.body
        } catch (_: Exception) {
            result.body
        }

        val retryAfter = result.headers["retry-after"]
            ?.firstOrNull()
            ?.toLongOrNull()
            ?.seconds

        throw when (result.statusCode) {
            400 -> InvalidRequestException(errorMessage)
            401, 403 -> AuthenticationException(result.statusCode, errorMessage)
            404 -> ModelNotFoundException(errorMessage)
            429 -> RateLimitException(errorMessage, retryAfter)
            in 500..599 -> ServerException(result.statusCode, errorMessage)
            else -> ServerException(result.statusCode, "Unexpected status ${result.statusCode}: $errorMessage")
        }
    }
}
