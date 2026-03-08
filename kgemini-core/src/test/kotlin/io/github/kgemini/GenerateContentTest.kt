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

import io.github.kgemini.exception.InvalidRequestException
import io.github.kgemini.exception.RateLimitException
import io.github.kgemini.internal.config.GeminiConfig
import io.github.kgemini.internal.http.HttpExecutor
import io.github.kgemini.internal.http.HttpResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class GenerateContentTest : FunSpec({

    fun fixture(name: String): String =
        GenerateContentTest::class.java.getResource("/fixtures/$name")!!.readText()

    fun testConfig() = GeminiConfig(
        apiKey = "test-key",
        model = "gemini-2.5-flash",
        connectTimeoutMs = 5_000,
        generateTimeoutMs = 30_000,
        maxRetries = 0,
        retryBaseDelayMs = 10,
        retryMaxDelayMs = 50,
    )

    fun testClient(executor: HttpExecutor) = GeminiClient(testConfig(), executor)

    test("generate(prompt) — 정상 응답, .text 검증") {
        val client = testClient { url, body, headers, _ ->
            url shouldContain ":generateContent"
            headers["x-goog-api-key"] shouldBe "test-key"
            body shouldContain "Hello"

            HttpResult(200, fixture("generate_content.json"), emptyMap())
        }

        val response = client.generate("Hello")
        response.text shouldBe "Hello! How can I help you today?"
        response.totalTokens shouldBe 10
    }

    test("generate — 400 → InvalidRequestException") {
        val client = testClient { _, _, _, _ ->
            HttpResult(
                400,
                """{"error":{"code":400,"message":"Invalid request"}}""",
                emptyMap(),
            )
        }

        shouldThrow<InvalidRequestException> {
            client.generate("bad")
        }
    }

    test("generate — 429 → RateLimitException") {
        val client = testClient { _, _, _, _ ->
            HttpResult(
                429,
                fixture("error_429.json"),
                emptyMap(),
            )
        }

        shouldThrow<RateLimitException> {
            client.generate("rate limited")
        }
    }

    test("요청 body에 user content가 포함된다") {
        var capturedBody: String? = null

        val client = testClient { _, body, _, _ ->
            capturedBody = body
            HttpResult(200, fixture("generate_content.json"), emptyMap())
        }

        client.generate("Test prompt")

        capturedBody!! shouldContain "Test prompt"
        capturedBody!! shouldContain "\"role\":\"user\""
    }
})
