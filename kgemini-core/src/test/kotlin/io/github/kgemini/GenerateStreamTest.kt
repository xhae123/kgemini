package io.github.kgemini

import io.github.kgemini.exception.InvalidRequestException
import io.github.kgemini.internal.plugin.AuthPlugin
import io.github.kgemini.internal.plugin.ErrorMappingPlugin
import io.github.kgemini.internal.serialization.geminiJson
import io.github.kgemini.model.GenerateContentResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class GenerateStreamTest : FunSpec({

    fun fixture(name: String): String =
        GenerateStreamTest::class.java.getResource("/fixtures/$name")!!.readText()

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

    fun parseSseChunks(sseBody: String): List<GenerateContentResponse> {
        return sseBody.lines()
            .filter { it.startsWith("data: ") }
            .map { it.removePrefix("data: ") }
            .map { geminiJson.decodeFromString(it) }
    }

    test("SSE 청크 2개 파싱 — data: 접두사 제거 후 역직렬화") {
        val sseBody = fixture("stream_generate_content.txt")

        val client = mockClient { request ->
            request.url.encodedPath shouldContain ":streamGenerateContent"
            request.url.parameters["alt"] shouldBe "sse"

            respond(
                sseBody,
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "text/event-stream"),
            )
        }

        client.use {
            val responseText = it.post(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:streamGenerateContent"
            ) {
                contentType(ContentType.Application.Json)
                setBody("{}")
                parameter("alt", "sse")
            }.bodyAsText()

            val chunks = parseSseChunks(responseText)

            chunks shouldHaveSize 2
            chunks[0].text shouldBe "Hello"
            chunks[1].text shouldBe " world!"
            chunks[1].candidates?.first()?.finishReason shouldBe "STOP"
            chunks[1].usageMetadata?.totalTokenCount shouldBe 5
        }
    }

    test("빈 줄과 data: 외 SSE 라인은 무시된다") {
        val sseBody = """
            |: comment line
            |
            |data: {"candidates":[{"content":{"parts":[{"text":"only this"}],"role":"model"},"finishReason":"STOP","index":0}]}
            |
            |event: done
            |
        """.trimMargin()

        val client = mockClient {
            respond(
                sseBody,
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "text/event-stream"),
            )
        }

        client.use {
            val responseText = it.post(
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:streamGenerateContent"
            ) {
                contentType(ContentType.Application.Json)
                setBody("{}")
                parameter("alt", "sse")
            }.bodyAsText()

            val chunks = parseSseChunks(responseText)

            chunks shouldHaveSize 1
            chunks[0].text shouldBe "only this"
        }
    }

    test("스트림 에러 응답 — 400 → InvalidRequestException") {
        val client = mockClient {
            respond(
                """{"error":{"code":400,"message":"Invalid request"}}""",
                HttpStatusCode.BadRequest,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        client.use {
            shouldThrow<InvalidRequestException> {
                it.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:streamGenerateContent") {
                    contentType(ContentType.Application.Json)
                    setBody("{}")
                    parameter("alt", "sse")
                }.bodyAsText()
            }
        }
    }
})
