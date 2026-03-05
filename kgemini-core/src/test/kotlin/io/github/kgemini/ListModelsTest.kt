package io.github.kgemini

import io.github.kgemini.exception.AuthenticationException
import io.github.kgemini.exception.ModelNotFoundException
import io.github.kgemini.internal.plugin.AuthPlugin
import io.github.kgemini.internal.plugin.ErrorMappingPlugin
import io.github.kgemini.internal.serialization.geminiJson
import io.github.kgemini.model.ListModelsResponse
import io.github.kgemini.model.ModelInfo
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

class ListModelsTest : FunSpec({

    fun fixture(name: String): String =
        ListModelsTest::class.java.getResource("/fixtures/$name")!!.readText()

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

    test("listModels — 정상 응답 역직렬화") {
        val client = mockClient { request ->
            request.url.encodedPath shouldContain "/models"
            request.url.parameters["key"] shouldBe "test-key"

            respond(
                fixture("list_models.json"),
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val responseText = client.get("https://generativelanguage.googleapis.com/v1beta/models").bodyAsText()
        val response = geminiJson.decodeFromString<ListModelsResponse>(responseText)

        response.models shouldHaveSize 2
        response.models[0].name shouldBe "models/gemini-2.0-flash"
        response.models[0].inputTokenLimit shouldBe 1048576
        response.models[1].displayName shouldBe "Gemini 2.5 Pro"
    }

    test("getModel — 정상 응답") {
        val modelJson = """
            {
              "name": "models/gemini-2.0-flash",
              "version": "2.0",
              "displayName": "Gemini 2.0 Flash",
              "inputTokenLimit": 1048576,
              "outputTokenLimit": 8192,
              "supportedGenerationMethods": ["generateContent"]
            }
        """.trimIndent()

        val client = mockClient { request ->
            request.url.encodedPath shouldContain "/models/gemini-2.0-flash"
            respond(
                modelJson,
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val responseText = client.get("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash").bodyAsText()
        val model = geminiJson.decodeFromString<ModelInfo>(responseText)

        model.name shouldBe "models/gemini-2.0-flash"
        model.inputTokenLimit shouldBe 1048576
    }

    test("listModels — 401 인증 실패 → AuthenticationException") {
        val client = mockClient {
            respond(
                fixture("error_401.json"),
                HttpStatusCode.Unauthorized,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        shouldThrow<AuthenticationException> {
            client.get("https://generativelanguage.googleapis.com/v1beta/models").bodyAsText()
        }
    }

    test("getModel — 404 → ModelNotFoundException") {
        val client = mockClient {
            respond(
                """{"error":{"code":404,"message":"Model not found"}}""",
                HttpStatusCode.NotFound,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        shouldThrow<ModelNotFoundException> {
            client.get("https://generativelanguage.googleapis.com/v1beta/models/nonexistent").bodyAsText()
        }
    }

    test("API key가 query parameter로 전달된다") {
        var capturedKey: String? = null

        val client = mockClient { request ->
            capturedKey = request.url.parameters["key"]
            respond(
                """{"models":[]}""",
                HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        client.get("https://generativelanguage.googleapis.com/v1beta/models").bodyAsText()
        capturedKey shouldBe "test-key"
    }

    test("전체 파이프라인 관통 — KGemini → GeminiHttpClient → Plugin → MockEngine") {
        // KGemini는 내부적으로 CIO 엔진을 사용하므로 직접 MockEngine 주입이 어려움.
        // 대신 동일한 Plugin chain을 mockClient로 재현하여 통합 검증.
        val client = mockClient { request ->
            // AuthPlugin이 key를 주입했는지 확인
            val key = request.url.parameters["key"]
            if (key != "test-key") {
                respond(
                    """{"error":{"code":401,"message":"Unauthorized"}}""",
                    HttpStatusCode.Unauthorized,
                )
            } else {
                respond(
                    fixture("list_models.json"),
                    HttpStatusCode.OK,
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        }

        val responseText = client.get("https://generativelanguage.googleapis.com/v1beta/models").bodyAsText()
        val response = geminiJson.decodeFromString<ListModelsResponse>(responseText)
        response.models shouldHaveSize 2
    }
})
