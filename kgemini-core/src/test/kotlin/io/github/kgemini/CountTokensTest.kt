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
import io.github.kgemini.model.Content
import io.github.kgemini.model.CountTokensRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.engine.mock.*
import io.ktor.http.*

class CountTokensTest : FunSpec({

    fun fixture(name: String): String =
        CountTokensTest::class.java.getResource("/fixtures/$name")!!.readText()

    fun mockKGemini(handler: MockRequestHandler): KGemini {
        return KGemini("test-key") {
            testEngine = MockEngine(handler)
        }
    }

    test("countTokens(String) — totalTokens 값 검증") {
        val kgemini = mockKGemini { request ->
            request.url.encodedPath shouldContain ":countTokens"
            respond(
                fixture("count_tokens.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        kgemini.use {
            val response = it.countTokens("Hello")
            response.totalTokens shouldBe 7
        }
    }

    test("countTokens(CountTokensRequest) — Request 직접 전달") {
        val kgemini = mockKGemini { request ->
            request.url.encodedPath shouldContain ":countTokens"
            respond(
                fixture("count_tokens.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        kgemini.use {
            val request = CountTokensRequest(contents = listOf(Content.user("Hello world")))
            val response = it.countTokens(request)
            response.totalTokens shouldBe 7
        }
    }

    test("countTokens — 400 에러 시 InvalidRequestException 전파") {
        val kgemini = mockKGemini {
            respond(
                """{"error":{"code":400,"message":"Invalid request"}}""",
                HttpStatusCode.BadRequest,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        shouldThrow<InvalidRequestException> {
            kgemini.use {
                it.countTokens("bad input")
            }
        }
    }
})
