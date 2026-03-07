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

import io.github.kgemini.exception.InvalidRequestException
import io.github.kgemini.exception.ServerException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.atomic.AtomicInteger

class RetryTest : FunSpec({

    test("retryable 예외(500)는 재시도 후 성공") {
        val callCount = AtomicInteger(0)

        val result = RetryExecutor.withRetry(
            maxRetries = 3,
            baseDelayMs = 10,
            maxDelayMs = 50,
        ) {
            val attempt = callCount.getAndIncrement()
            if (attempt < 2) throw ServerException(500, "Internal error")
            "success"
        }

        result shouldBe "success"
        callCount.get() shouldBe 3
    }

    test("non-retryable 예외(400)는 즉시 실패") {
        val callCount = AtomicInteger(0)

        shouldThrow<InvalidRequestException> {
            RetryExecutor.withRetry(
                maxRetries = 3,
                baseDelayMs = 10,
                maxDelayMs = 50,
            ) {
                callCount.getAndIncrement()
                throw InvalidRequestException("Bad request")
            }
        }

        callCount.get() shouldBe 1
    }

    test("maxRetries 초과 시 마지막 예외 전파") {
        val callCount = AtomicInteger(0)

        shouldThrow<ServerException> {
            RetryExecutor.withRetry(
                maxRetries = 2,
                baseDelayMs = 10,
                maxDelayMs = 50,
            ) {
                callCount.getAndIncrement()
                throw ServerException(500, "Server error")
            }
        }

        // 1 initial + 2 retries = 3
        callCount.get() shouldBe 3
    }

    test("retry 비활성화 (maxRetries=0) — 재시도 없이 즉시 실패") {
        val callCount = AtomicInteger(0)

        shouldThrow<ServerException> {
            RetryExecutor.withRetry(
                maxRetries = 0,
                baseDelayMs = 10,
                maxDelayMs = 50,
            ) {
                callCount.getAndIncrement()
                throw ServerException(500, "Server error")
            }
        }

        callCount.get() shouldBe 1
    }
})
