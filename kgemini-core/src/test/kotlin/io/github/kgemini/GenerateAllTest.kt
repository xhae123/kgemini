package io.github.kgemini

import io.github.kgemini.exception.InvalidRequestException
import io.github.kgemini.model.GenerateContentRequest
import io.github.kgemini.model.GenerationConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import java.util.concurrent.atomic.AtomicInteger

class GenerateAllTest : FunSpec({

    fun fixture(name: String): String =
        GenerateAllTest::class.java.getResource("/fixtures/$name")!!.readText()

    fun mockKGemini(handler: MockRequestHandler): KGemini {
        return KGemini("test-key") {
            testEngine = MockEngine(handler)
        }
    }

    test("generateAll — 여러 프롬프트, 입력 순서 일치") {
        val capturedPrompts = mutableListOf<String>()

        val kgemini = mockKGemini { request ->
            val body = String(request.body.toByteArray())
            synchronized(capturedPrompts) { capturedPrompts.add(body) }
            respond(
                fixture("generate_content.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val results = kgemini.use {
            it.generateAll("a", "b", "c")
        }

        results shouldHaveSize 3
        results.forEach { it.text shouldBe "Hello! How can I help you today?" }
        capturedPrompts shouldHaveSize 3
    }

    test("generateAll — 단일 프롬프트") {
        val kgemini = mockKGemini {
            respond(
                fixture("generate_content.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val results = kgemini.use {
            it.generateAll("single")
        }

        results shouldHaveSize 1
        results[0].text shouldBe "Hello! How can I help you today?"
    }

    test("generateAll — 공유 설정 적용") {
        val capturedBodies = mutableListOf<String>()

        val kgemini = mockKGemini { request ->
            val body = String(request.body.toByteArray())
            synchronized(capturedBodies) { capturedBodies.add(body) }
            respond(
                fixture("generate_content.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        kgemini.use {
            it.generateAll("a", "b") {
                generationConfig = GenerationConfig(temperature = 0.5, maxOutputTokens = 100)
            }
        }

        capturedBodies shouldHaveSize 2
        capturedBodies.forEach { body ->
            body shouldContain "\"temperature\""
            body shouldContain "\"maxOutputTokens\""
        }
    }

    test("generateAll — 하나가 에러 시 전체 예외 전파") {
        val callCount = AtomicInteger(0)

        val kgemini = mockKGemini { request ->
            val idx = callCount.getAndIncrement()
            if (idx == 1) {
                respond(
                    """{"error":{"code":400,"message":"Invalid request"}}""",
                    HttpStatusCode.BadRequest,
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

        shouldThrow<InvalidRequestException> {
            kgemini.use {
                it.generateAll("ok", "bad", "ok2")
            }
        }
    }
})
