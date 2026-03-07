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
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ErrorMapperTest : FunSpec({

    test("200 — 예외 없음") {
        shouldNotThrowAny {
            ErrorMapper.throwIfError(HttpResult(200, "{}", emptyMap()))
        }
    }

    test("400 → InvalidRequestException") {
        val ex = shouldThrow<InvalidRequestException> {
            ErrorMapper.throwIfError(
                HttpResult(400, """{"error":{"code":400,"message":"Invalid"}}""", emptyMap())
            )
        }
        ex.message shouldBe "Invalid"
    }

    test("401 → AuthenticationException") {
        shouldThrow<AuthenticationException> {
            ErrorMapper.throwIfError(
                HttpResult(401, """{"error":{"code":401,"message":"Unauthorized"}}""", emptyMap())
            )
        }
    }

    test("403 → AuthenticationException") {
        shouldThrow<AuthenticationException> {
            ErrorMapper.throwIfError(
                HttpResult(403, """{"error":{"code":403,"message":"Forbidden"}}""", emptyMap())
            )
        }
    }

    test("404 → ModelNotFoundException") {
        shouldThrow<ModelNotFoundException> {
            ErrorMapper.throwIfError(
                HttpResult(404, """{"error":{"code":404,"message":"Not found"}}""", emptyMap())
            )
        }
    }

    test("429 → RateLimitException") {
        shouldThrow<RateLimitException> {
            ErrorMapper.throwIfError(
                HttpResult(429, """{"error":{"code":429,"message":"Rate limited"}}""", emptyMap())
            )
        }
    }

    test("429 with Retry-After header") {
        val ex = shouldThrow<RateLimitException> {
            ErrorMapper.throwIfError(
                HttpResult(
                    429,
                    """{"error":{"code":429,"message":"Rate limited"}}""",
                    mapOf("retry-after" to listOf("5")),
                )
            )
        }
        ex.retryAfter?.inWholeSeconds shouldBe 5
    }

    test("500 → ServerException") {
        val ex = shouldThrow<ServerException> {
            ErrorMapper.throwIfError(
                HttpResult(500, """{"error":{"code":500,"message":"Internal"}}""", emptyMap())
            )
        }
        ex.statusCode shouldBe 500
        ex.retryable shouldBe true
    }

    test("JSON 파싱 실패 시 raw body를 메시지로 사용") {
        val ex = shouldThrow<ServerException> {
            ErrorMapper.throwIfError(
                HttpResult(500, "not json", emptyMap())
            )
        }
        ex.message shouldBe "not json"
    }
})
