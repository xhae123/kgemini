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

package io.github.kgemini.internal.model

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ModelResolverTest : FunSpec({

    test("free → gemini-2.0-flash") {
        ModelResolver.resolve("free") shouldBe "gemini-2.0-flash"
    }

    test("fast → gemini-2.0-flash") {
        ModelResolver.resolve("fast") shouldBe "gemini-2.0-flash"
    }

    test("cheap → gemini-2.0-flash-lite") {
        ModelResolver.resolve("cheap") shouldBe "gemini-2.0-flash-lite"
    }

    test("smart → gemini-2.5-pro") {
        ModelResolver.resolve("smart") shouldBe "gemini-2.5-pro"
    }

    test("대소문자 무관") {
        ModelResolver.resolve("FREE") shouldBe "gemini-2.0-flash"
        ModelResolver.resolve("Smart") shouldBe "gemini-2.5-pro"
    }

    test("정확한 모델 ID는 그대로 통과") {
        ModelResolver.resolve("gemini-2.5-flash") shouldBe "gemini-2.5-flash"
        ModelResolver.resolve("gemini-3.0-ultra") shouldBe "gemini-3.0-ultra"
    }
})
