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

package io.github.kgemini.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GeminiModelTest : FunSpec({

    test("Known 모델의 id") {
        GeminiModel.Known.FLASH_2_5.id shouldBe "gemini-2.5-flash"
        GeminiModel.Known.FLASH_2_5_LITE.id shouldBe "gemini-2.5-flash-lite"
        GeminiModel.Known.PRO_2_5.id shouldBe "gemini-2.5-pro"
    }

    test("Pinned 모델 — 버전 고정") {
        val pinned = GeminiModel.Known.FLASH_2_5.pinned("001")
        pinned.id shouldBe "gemini-2.5-flash-001"
    }

    test("Latest 모델") {
        val latest = GeminiModel.Known.FLASH_2_5.latest
        latest.id shouldBe "gemini-2.5-flash-latest"
    }

    test("Stable — Known 자체 반환") {
        val stable = GeminiModel.Known.FLASH_2_5.stable
        stable shouldBe GeminiModel.Known.FLASH_2_5
    }

    test("Custom 모델 — 라이브러리 업데이트 없이 새 모델 사용") {
        val custom = GeminiModel.Custom("gemini-3.0-ultra")
        custom.id shouldBe "gemini-3.0-ultra"
    }
})
