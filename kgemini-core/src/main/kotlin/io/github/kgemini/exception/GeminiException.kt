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

package io.github.kgemini.exception

import io.github.kgemini.model.GenerateContentResponse
import io.github.kgemini.model.UsageMetadata
import kotlin.time.Duration

public sealed class GeminiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    public abstract val retryable: Boolean
}

// --- API Exceptions ---

public sealed class ApiException(
    public val statusCode: Int,
    message: String,
    cause: Throwable? = null,
) : GeminiException(message, cause)

public class InvalidRequestException(
    message: String,
    cause: Throwable? = null,
) : ApiException(400, message, cause) {
    override val retryable: Boolean = false
}

public class AuthenticationException(
    statusCode: Int = 401,
    message: String,
    cause: Throwable? = null,
) : ApiException(statusCode, message, cause) {
    override val retryable: Boolean = false
}

public class ModelNotFoundException(
    message: String,
    cause: Throwable? = null,
) : ApiException(404, message, cause) {
    override val retryable: Boolean = false
}

public class RateLimitException(
    message: String,
    public val retryAfter: Duration? = null,
    cause: Throwable? = null,
) : ApiException(429, message, cause) {
    override val retryable: Boolean = true
}

public class ServerException(
    statusCode: Int,
    message: String,
    cause: Throwable? = null,
) : ApiException(statusCode, message, cause) {
    override val retryable: Boolean = true
}

// --- Network Exceptions ---

public sealed class NetworkException(
    message: String,
    cause: Throwable? = null,
) : GeminiException(message, cause)

public class ConnectionException(
    message: String,
    cause: Throwable? = null,
) : NetworkException(message, cause) {
    override val retryable: Boolean = true
}

public sealed class TimeoutException(
    message: String,
    cause: Throwable? = null,
) : NetworkException(message, cause) {
    override val retryable: Boolean = true
}

public class ConnectTimeoutException(
    message: String = "Connection timed out",
    cause: Throwable? = null,
) : TimeoutException(message, cause)

public class GenerateTimeoutException(
    message: String = "Generate request timed out",
    cause: Throwable? = null,
) : TimeoutException(message, cause)

public class StreamTimeoutException(
    message: String = "Stream timed out",
    cause: Throwable? = null,
) : TimeoutException(message, cause)

public class StreamInterruptedException(
    message: String = "Stream interrupted",
    public val receivedChunks: List<GenerateContentResponse> = emptyList(),
    public val partialUsage: UsageMetadata? = null,
    cause: Throwable? = null,
) : NetworkException(message, cause) {
    override val retryable: Boolean = true
}

// --- Serialization Exception ---

public class GeminiSerializationException(
    message: String,
    public val rawResponse: String? = null,
    cause: Throwable? = null,
) : GeminiException(message, cause) {
    override val retryable: Boolean = false
}
