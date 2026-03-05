package io.github.kgemini

import io.github.kgemini.exception.InvalidRequestException
import io.github.kgemini.exception.RateLimitException
import io.github.kgemini.internal.plugin.AuthPlugin
import io.github.kgemini.internal.plugin.ErrorMappingPlugin
import io.github.kgemini.internal.serialization.geminiJson
import io.github.kgemini.model.Content
import io.github.kgemini.model.GenerateContentRequest
import io.github.kgemini.model.GenerateContentResponse
import io.github.kgemini.model.GenerationConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class GenerateContentTest : FunSpec({

    fun fixture(name: String): String =
        GenerateContentTest::class.java.getResource("/fixtures/$name")!!.readText()

    fun mockClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine(handler)) {
            install(ContentNegotiation) {
                json(geminiJson)
            }
            install(AuthPlugin) {
                apiKey = "test-key"
            }
            install(ErrorMappingPlugin)
        }
    }

    test("generate(prompt) — 정상 응답, .text 검증") {
        val client = mockClient { request ->
            request.url.encodedPath shouldContain ":generateContent"
            request.method shouldBe HttpMethod.Post

            respond(
                fixture("generate_content.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        client.use {
            val responseText = it.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent") {
                contentType(ContentType.Application.Json)
                setBody(GenerateContentRequest(contents = listOf(Content.user("Hello"))))
            }.bodyAsText()
            val response = geminiJson.decodeFromString<GenerateContentResponse>(responseText)

            response.text shouldBe "Hello! How can I help you today?"
            response.usageMetadata?.totalTokenCount shouldBe 10
        }
    }

    test("generate(prompt) { generationConfig = ... } — DSL 설정이 Request에 반영") {
        var capturedBody: String? = null

        val client = mockClient { request ->
            capturedBody = String(request.body.toByteArray())
            respond(
                fixture("generate_content.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        client.use {
            val builder = GenerateContentRequest.Builder().apply {
                generationConfig = GenerationConfig(temperature = 0.5, maxOutputTokens = 100)
            }
            val builtRequest = builder.build("Test prompt")

            it.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent") {
                contentType(ContentType.Application.Json)
                setBody(builtRequest)
            }.bodyAsText()
        }

        capturedBody!! shouldContain "\"temperature\""
        capturedBody!! shouldContain "\"maxOutputTokens\""
        capturedBody!! shouldContain "Test prompt"
    }

    test("generate(request) — Request 직접 전달") {
        val client = mockClient { request ->
            val body = String(request.body.toByteArray())
            body shouldContain "Direct request"

            respond(
                fixture("generate_content.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        client.use {
            val request = GenerateContentRequest(
                contents = listOf(Content.user("Direct request")),
            )

            val responseText = it.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.bodyAsText()
            val response = geminiJson.decodeFromString<GenerateContentResponse>(responseText)

            response.text shouldBe "Hello! How can I help you today?"
        }
    }

    test("generate — 400 → InvalidRequestException") {
        val client = mockClient {
            respond(
                """{"error":{"code":400,"message":"Invalid request"}}""",
                HttpStatusCode.BadRequest,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        client.use {
            shouldThrow<InvalidRequestException> {
                it.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent") {
                    contentType(ContentType.Application.Json)
                    setBody(GenerateContentRequest(contents = listOf(Content.user("bad"))))
                }.bodyAsText()
            }
        }
    }

    test("generate — 429 → RateLimitException") {
        val client = mockClient {
            respond(
                fixture("error_429.json"),
                HttpStatusCode.TooManyRequests,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        client.use {
            shouldThrow<RateLimitException> {
                it.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent") {
                    contentType(ContentType.Application.Json)
                    setBody(GenerateContentRequest(contents = listOf(Content.user("rate limited"))))
                }.bodyAsText()
            }
        }
    }

    test("Builder — systemInstruction이 Content.text()로 변환된다") {
        var capturedBody: String? = null

        val client = mockClient { request ->
            capturedBody = String(request.body.toByteArray())
            respond(
                fixture("generate_content.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        client.use {
            val builtRequest = GenerateContentRequest.Builder().apply {
                systemInstruction = "You are a helpful assistant."
            }.build("Hello")

            it.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent") {
                contentType(ContentType.Application.Json)
                setBody(builtRequest)
            }.bodyAsText()
        }

        capturedBody!! shouldContain "You are a helpful assistant."
        capturedBody!! shouldContain "systemInstruction"
    }
})
