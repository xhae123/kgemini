package io.github.kgemini.model

import io.github.kgemini.internal.serialization.geminiJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SerializationTest : FunSpec({

    fun fixture(name: String): String =
        SerializationTest::class.java.getResource("/fixtures/$name")!!.readText()

    test("ListModelsResponse 역직렬화") {
        val response = geminiJson.decodeFromString<ListModelsResponse>(fixture("list_models.json"))

        response.models shouldHaveSize 2
        response.models[0].name shouldBe "models/gemini-2.0-flash"
        response.models[0].inputTokenLimit shouldBe 1048576
        response.models[0].outputTokenLimit shouldBe 8192
        response.models[0].supportedGenerationMethods shouldHaveSize 2
        response.models[1].name shouldBe "models/gemini-2.5-pro"
    }

    test("GenerateContentResponse 역직렬화") {
        val response = geminiJson.decodeFromString<GenerateContentResponse>(fixture("generate_content.json"))

        response.text shouldBe "Hello! How can I help you today?"
        response.usageMetadata.shouldNotBeNull()
        response.usageMetadata!!.totalTokenCount shouldBe 10
        response.candidates.shouldNotBeNull()
        response.candidates!![0].finishReason shouldBe "STOP"
        response.candidates!![0].safetyRatings!! shouldHaveSize 2
    }

    test("CountTokensResponse 역직렬화") {
        val response = geminiJson.decodeFromString<CountTokensResponse>(fixture("count_tokens.json"))
        response.totalTokens shouldBe 7
    }

    test("ErrorResponse 역직렬화") {
        val response = geminiJson.decodeFromString<ErrorResponse>(fixture("error_401.json"))
        response.error.shouldNotBeNull()
        response.error!!.code shouldBe 401
        response.error!!.status shouldBe "UNAUTHENTICATED"
    }

    test("safety 차단 응답 역직렬화") {
        val response = geminiJson.decodeFromString<GenerateContentResponse>(fixture("safety_blocked.json"))

        response.text shouldBe null
        response.candidates.shouldNotBeNull()
        response.candidates!![0].finishReason shouldBe "SAFETY"
        response.candidates!![0].safetyRatings!![0].blocked shouldBe true
    }

    test("GenerateContentRequest 직렬화 왕복") {
        val request = GenerateContentRequest(
            contents = listOf(Content.user("Hello")),
            generationConfig = GenerationConfig(temperature = 0.7, maxOutputTokens = 100),
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, HarmBlockThreshold.MEDIUM_AND_ABOVE)
            ),
        )

        val json = geminiJson.encodeToString(GenerateContentRequest.serializer(), request)
        val decoded = geminiJson.decodeFromString<GenerateContentRequest>(json)

        decoded.contents shouldHaveSize 1
        decoded.contents[0].role shouldBe "user"
        decoded.generationConfig!!.temperature shouldBe 0.7
        decoded.safetySettings!! shouldHaveSize 1
    }

    test("unknown 필드가 있는 JSON 역직렬화 — 전방 호환성") {
        val jsonWithUnknown = """
            {
              "totalTokens": 42,
              "someNewField": "unknown_value",
              "anotherFutureField": 999
            }
        """.trimIndent()

        val response = geminiJson.decodeFromString<CountTokensResponse>(jsonWithUnknown)
        response.totalTokens shouldBe 42
    }

    test("Content 팩토리 메서드") {
        val user = Content.user("test prompt")
        user.role shouldBe "user"
        user.parts shouldHaveSize 1
        user.parts[0].text shouldBe "test prompt"

        val model = Content.model("response")
        model.role shouldBe "model"
    }
})
