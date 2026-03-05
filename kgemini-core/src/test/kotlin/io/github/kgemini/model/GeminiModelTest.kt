package io.github.kgemini.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GeminiModelTest : FunSpec({

    test("Known 모델의 id") {
        GeminiModel.Known.FLASH_2_0.id shouldBe "gemini-2.0-flash"
        GeminiModel.Known.PRO_2_5.id shouldBe "gemini-2.5-pro"
    }

    test("Pinned 모델 — 버전 고정") {
        val pinned = GeminiModel.Known.FLASH_2_0.pinned("001")
        pinned.id shouldBe "gemini-2.0-flash-001"
    }

    test("Latest 모델") {
        val latest = GeminiModel.Known.FLASH_2_0.latest
        latest.id shouldBe "gemini-2.0-flash-latest"
    }

    test("Stable — Known 자체 반환") {
        val stable = GeminiModel.Known.FLASH_2_0.stable
        stable shouldBe GeminiModel.Known.FLASH_2_0
    }

    test("Custom 모델 — 라이브러리 업데이트 없이 새 모델 사용") {
        val custom = GeminiModel.Custom("gemini-3.0-ultra")
        custom.id shouldBe "gemini-3.0-ultra"
    }
})
