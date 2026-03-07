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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class ConfigLoaderTest : FunSpec({

    fun noEnv(@Suppress("UNUSED_PARAMETER") key: String): String? = null

    test("defaults — API key만 제공하면 나머지는 기본값") {
        val config = ConfigLoader.resolve(
            env = { if (it == "GEMINI_API_KEY") "test-key" else null },
            properties = emptyMap(),
            yaml = emptyMap(),
        )

        config.apiKey shouldBe "test-key"
        config.model shouldBe "free"
        config.connectTimeoutMs shouldBe 5_000L
        config.generateTimeoutMs shouldBe 30_000L
        config.maxRetries shouldBe 3
    }

    test("우선순위: env > yaml > properties") {
        val config = ConfigLoader.resolve(
            env = { when (it) {
                "GEMINI_API_KEY" -> "env-key"
                "GEMINI_MODEL" -> "smart"
                else -> null
            }},
            properties = mapOf("gemini.model" to "cheap", "gemini.timeout" to "10000"),
            yaml = mapOf("gemini.model" to "fast", "gemini.timeout" to "20000"),
        )

        config.apiKey shouldBe "env-key"
        config.model shouldBe "smart"       // env wins
        config.generateTimeoutMs shouldBe 20_000L  // yaml wins over properties
    }

    test("yaml이 properties보다 우선") {
        val config = ConfigLoader.resolve(
            env = { if (it == "GEMINI_API_KEY") "key" else null },
            properties = mapOf("gemini.model" to "cheap"),
            yaml = mapOf("gemini.model" to "fast"),
        )

        config.model shouldBe "fast"
    }

    test("API key 없으면 에러") {
        val ex = shouldThrow<IllegalStateException> {
            ConfigLoader.resolve(
                env = ::noEnv,
                properties = emptyMap(),
                yaml = emptyMap(),
            )
        }
        ex.message shouldContain "API key"
    }

    test("duration 파싱 — '60s' 형식 지원") {
        val config = ConfigLoader.resolve(
            env = { if (it == "GEMINI_API_KEY") "key" else null },
            properties = emptyMap(),
            yaml = mapOf("gemini.timeout" to "60s", "gemini.connect-timeout" to "10s"),
        )

        config.generateTimeoutMs shouldBe 60_000L
        config.connectTimeoutMs shouldBe 10_000L
    }

    test("duration 파싱 — 밀리초 숫자도 지원") {
        val config = ConfigLoader.resolve(
            env = { if (it == "GEMINI_API_KEY") "key" else null },
            properties = mapOf("gemini.timeout" to "45000"),
            yaml = emptyMap(),
        )

        config.generateTimeoutMs shouldBe 45_000L
    }

    test("duration 파싱 — '500ms' 형식 지원") {
        val config = ConfigLoader.resolve(
            env = { if (it == "GEMINI_API_KEY") "key" else null },
            properties = mapOf("gemini.retry-base-delay" to "500ms"),
            yaml = emptyMap(),
        )

        config.retryBaseDelayMs shouldBe 500L
    }
})

class YamlParserTest : FunSpec({

    test("nested key-value 파싱") {
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

    test("주석과 빈 줄 무시") {
        val yaml = """
            # This is a comment

            gemini:
              model: smart
        """.trimIndent()

        val result = YamlParser.parse(yaml)
        result["gemini.model"] shouldBe "smart"
    }

    test("top-level key") {
        val yaml = "api-key: my-key"
        val result = YamlParser.parse(yaml)
        result["api-key"] shouldBe "my-key"
    }

    test("sibling nesting") {
        val yaml = """
            gemini:
              model: free
            other:
              key: value
        """.trimIndent()

        val result = YamlParser.parse(yaml)
        result["gemini.model"] shouldBe "free"
        result["other.key"] shouldBe "value"
    }
})

class ParseMillisTest : FunSpec({

    test("숫자만 — 밀리초로 해석") {
        "5000".parseMillis() shouldBe 5000L
    }

    test("s 접미사 — 초 → 밀리초") {
        "60s".parseMillis() shouldBe 60_000L
        "1s".parseMillis() shouldBe 1_000L
    }

    test("ms 접미사 — 밀리초 그대로") {
        "500ms".parseMillis() shouldBe 500L
    }

    test("잘못된 형식 — null") {
        "abc".parseMillis() shouldBe null
        "".parseMillis() shouldBe null
    }
})
