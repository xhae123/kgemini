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

package io.github.kgemini.internal.plugin

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.time.Duration.Companion.seconds

class TimeoutPluginTest : FunSpec({

    test("timeout plugin이 정상 설치되고 요청이 성공한다") {
        HttpClient(MockEngine {
            respond("OK", HttpStatusCode.OK)
        }) {
            install(HttpTimeout)
            install(TimeoutPlugin) {
                connect = 3.seconds
                generate = 15.seconds
            }
        }.use { it.get("https://example.com") }
    }

    test("timeout 설정값이 config에 반영된다") {
        val config = TimeoutPluginConfig().apply {
            connect = 10.seconds
            generate = 60.seconds
        }

        config.connect shouldBe 10.seconds
        config.generate shouldBe 60.seconds
    }
})
