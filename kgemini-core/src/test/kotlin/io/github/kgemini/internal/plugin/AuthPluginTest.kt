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
import io.ktor.client.request.*
import io.ktor.http.*

class AuthPluginTest : FunSpec({

    test("API key가 query parameter로 주입된다") {
        var capturedUrl: Url? = null

        val client = HttpClient(MockEngine { request ->
            capturedUrl = request.url
            respond("OK", HttpStatusCode.OK)
        }) {
            install(AuthPlugin) {
                apiKey = "test-api-key-123"
            }
        }

        client.use {
            it.get("https://generativelanguage.googleapis.com/v1beta/models")
        }
        capturedUrl!!.parameters["key"] shouldBe "test-api-key-123"
    }

    test("기존 query parameter를 유지하면서 key 추가") {
        var capturedUrl: Url? = null

        val client = HttpClient(MockEngine { request ->
            capturedUrl = request.url
            respond("OK", HttpStatusCode.OK)
        }) {
            install(AuthPlugin) {
                apiKey = "my-key"
            }
        }

        client.use {
            it.get("https://example.com/api?alt=sse")
        }
        capturedUrl!!.parameters["key"] shouldBe "my-key"
        capturedUrl!!.parameters["alt"] shouldBe "sse"
    }
})
