package io.github.kgemini.internal.plugin

import io.github.kgemini.exception.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*

class ErrorMappingPluginTest : FunSpec({

    fun errorClient(status: HttpStatusCode, body: String = """{"error":{"code":${status.value},"message":"test error"}}"""): HttpClient {
        return HttpClient(MockEngine {
            respond(body, status, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }) {
            install(ErrorMappingPlugin)
        }
    }

    test("200 응답은 예외 없이 통과") {
        HttpClient(MockEngine {
            respond("OK", HttpStatusCode.OK)
        }) {
            install(ErrorMappingPlugin)
        }.use { it.get("https://example.com") }
    }

    test("400 → InvalidRequestException") {
        errorClient(HttpStatusCode.BadRequest).use { client ->
            val ex = shouldThrow<InvalidRequestException> {
                client.get("https://example.com")
            }
            ex.retryable shouldBe false
        }
    }

    test("401 → AuthenticationException") {
        errorClient(HttpStatusCode.Unauthorized).use { client ->
            val ex = shouldThrow<AuthenticationException> {
                client.get("https://example.com")
            }
            ex.retryable shouldBe false
            ex.statusCode shouldBe 401
        }
    }

    test("403 → AuthenticationException") {
        errorClient(HttpStatusCode.Forbidden).use { client ->
            val ex = shouldThrow<AuthenticationException> {
                client.get("https://example.com")
            }
            ex.statusCode shouldBe 403
        }
    }

    test("404 → ModelNotFoundException") {
        errorClient(HttpStatusCode.NotFound).use { client ->
            shouldThrow<ModelNotFoundException> {
                client.get("https://example.com")
            }
        }
    }

    test("429 → RateLimitException (retryable)") {
        HttpClient(MockEngine {
            respond(
                """{"error":{"code":429,"message":"quota exceeded"}}""",
                HttpStatusCode.TooManyRequests,
                headersOf(
                    HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
                    "Retry-After" to listOf("30"),
                ),
            )
        }) {
            install(ErrorMappingPlugin)
        }.use { client ->
            val ex = shouldThrow<RateLimitException> {
                client.get("https://example.com")
            }
            ex.retryable shouldBe true
            ex.retryAfter!!.inWholeSeconds shouldBe 30
        }
    }

    test("500 → ServerException (retryable)") {
        errorClient(HttpStatusCode.InternalServerError).use { client ->
            val ex = shouldThrow<ServerException> {
                client.get("https://example.com")
            }
            ex.retryable shouldBe true
        }
    }

    test("에러 응답 body에서 message 추출") {
        errorClient(
            HttpStatusCode.BadRequest,
            """{"error":{"code":400,"message":"Invalid model name","status":"INVALID_ARGUMENT"}}""",
        ).use { client ->
            val ex = shouldThrow<InvalidRequestException> {
                client.get("https://example.com")
            }
            ex.message shouldBe "Invalid model name"
        }
    }

    test("파싱 불가능한 에러 body — raw body를 message로 사용") {
        errorClient(HttpStatusCode.BadRequest, "not json").use { client ->
            val ex = shouldThrow<InvalidRequestException> {
                client.get("https://example.com")
            }
            ex.message shouldBe "not json"
        }
    }
})
