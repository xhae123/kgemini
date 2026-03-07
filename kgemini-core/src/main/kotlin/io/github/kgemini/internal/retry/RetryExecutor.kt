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

package io.github.kgemini.internal.retry

import io.github.kgemini.exception.GeminiException
import io.github.kgemini.exception.RateLimitException
import kotlin.math.min
import kotlin.random.Random

internal object RetryExecutor {

    fun <T> withRetry(
        maxRetries: Int,
        baseDelayMs: Long,
        maxDelayMs: Long,
        block: () -> T,
    ): T {
        var lastException: GeminiException? = null
        repeat(maxRetries + 1) { attempt ->
            try {
                return block()
            } catch (e: GeminiException) {
                if (!e.retryable || attempt >= maxRetries) throw e
                lastException = e
                val sleepMs = computeBackoff(attempt, baseDelayMs, maxDelayMs, e)
                Thread.sleep(sleepMs)
            }
        }
        throw lastException!!
    }

    private fun computeBackoff(
        attempt: Int,
        baseDelayMs: Long,
        maxDelayMs: Long,
        exception: GeminiException,
    ): Long {
        if (exception is RateLimitException && exception.retryAfter != null) {
            return exception.retryAfter.inWholeMilliseconds
        }
        val exponentialMs = baseDelayMs * (1L shl attempt)
        val cappedMs = min(exponentialMs, maxDelayMs)
        val jitter = (cappedMs * 0.25 * (Random.nextDouble() * 2 - 1)).toLong()
        return (cappedMs + jitter).coerceAtLeast(0L)
    }
}
