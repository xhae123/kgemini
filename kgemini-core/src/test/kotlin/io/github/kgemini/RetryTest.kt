package io.github.kgemini

import io.github.kgemini.exception.InvalidRequestException
import io.github.kgemini.exception.RateLimitException
import io.github.kgemini.exception.ServerException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import java.util.concurrent.atomic.AtomicInteger

class RetryTest : FunSpec({

    fun fixture(name: String): String =
        RetryTest::class.java.getResource("/fixtures/$name")!!.readText()

    fun mockKGemini(
        maxRetries: Int = 3,
        handler: MockRequestHandler,
    ): KGemini {
        return KGemini("test-key") {
            testEngine = MockEngine(handler)
            retry {
                this.maxRetries = maxRetries
                baseDelay = kotlin.time.Duration.parse("10ms")
                maxDelay = kotlin.time.Duration.parse("50ms")
            }
        }
    }

    test("retryable 예외(500)는 재시도 후 성공") {
        val callCount = AtomicInteger(0)

        val kgemini = mockKGemini { _ ->
            val attempt = callCount.getAndIncrement()
            if (attempt < 2) {
                respond(
                    """{"error":{"code":500,"message":"Internal error"}}""",
                    HttpStatusCode.InternalServerError,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            } else {
                respond(
                    fixture("generate_content.json"),
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        }

        kgemini.use {
            val response = it.generate("hello")
            response.text shouldBe "Hello! How can I help you today?"
        }
        callCount.get() shouldBe 3
    }

    test("non-retryable 예외(400)는 즉시 실패") {
        val callCount = AtomicInteger(0)

        val kgemini = mockKGemini { _ ->
            callCount.getAndIncrement()
            respond(
                """{"error":{"code":400,"message":"Bad request"}}""",
                HttpStatusCode.BadRequest,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        shouldThrow<InvalidRequestException> {
            kgemini.use { it.generate("bad") }
        }
        callCount.get() shouldBe 1
    }

    test("maxRetries 초과 시 마지막 예외 전파") {
        val callCount = AtomicInteger(0)

        val kgemini = mockKGemini(maxRetries = 2) { _ ->
            callCount.getAndIncrement()
            respond(
                """{"error":{"code":500,"message":"Server error"}}""",
                HttpStatusCode.InternalServerError,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        shouldThrow<ServerException> {
            kgemini.use { it.generate("fail") }
        }
        // 1 initial + 2 retries = 3
        callCount.get() shouldBe 3
    }

    test("429 RateLimitException도 재시도") {
        val callCount = AtomicInteger(0)

        val kgemini = mockKGemini { _ ->
            val attempt = callCount.getAndIncrement()
            if (attempt == 0) {
                respond(
                    """{"error":{"code":429,"message":"Rate limited"}}""",
                    HttpStatusCode.TooManyRequests,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            } else {
                respond(
                    fixture("generate_content.json"),
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        }

        kgemini.use {
            val response = it.generate("hello")
            response.text shouldBe "Hello! How can I help you today?"
        }
        callCount.get() shouldBe 2
    }

    test("retry 비활성화 (maxRetries=0) — 재시도 없이 즉시 실패") {
        val callCount = AtomicInteger(0)

        val kgemini = mockKGemini(maxRetries = 0) { _ ->
            callCount.getAndIncrement()
            respond(
                """{"error":{"code":500,"message":"Server error"}}""",
                HttpStatusCode.InternalServerError,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        shouldThrow<ServerException> {
            kgemini.use { it.generate("fail") }
        }
        callCount.get() shouldBe 1
    }

    test("countTokens도 재시도 동작") {
        val callCount = AtomicInteger(0)

        val kgemini = mockKGemini { _ ->
            val attempt = callCount.getAndIncrement()
            if (attempt == 0) {
                respond(
                    """{"error":{"code":500,"message":"Server error"}}""",
                    HttpStatusCode.InternalServerError,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            } else {
                respond(
                    fixture("count_tokens.json"),
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        }

        kgemini.use {
            val response = it.countTokens("hello")
            response.totalTokens shouldBe 7
        }
        callCount.get() shouldBe 2
    }
})
