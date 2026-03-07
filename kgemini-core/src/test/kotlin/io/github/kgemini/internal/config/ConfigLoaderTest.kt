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

package io.github.kgemini.internal.config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ConfigLoaderTest : FunSpec({

    test("YamlParser — flat key-value 파싱") {
        val yaml = """
            gemini:
              model: free
              timeout: 30000
              api-key: test-key
        """.trimIndent()

        val result = YamlParser.parse(yaml)
        result["gemini.model"] shouldBe "free"
        result["gemini.timeout"] shouldBe "30000"
        result["gemini.api-key"] shouldBe "test-key"
    }

    test("YamlParser — 주석과 빈 줄 무시") {
        val yaml = """
            # This is a comment

            gemini:
              model: smart
        """.trimIndent()

        val result = YamlParser.parse(yaml)
        result["gemini.model"] shouldBe "smart"
    }

    test("YamlParser — top-level key") {
        val yaml = "api-key: my-key"
        val result = YamlParser.parse(yaml)
        result["api-key"] shouldBe "my-key"
    }
})
