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

package io.github.kgemini.internal.http

import io.github.kgemini.exception.ConnectTimeoutException
import io.github.kgemini.exception.ConnectionException
import io.github.kgemini.exception.GenerateTimeoutException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration

internal fun interface HttpExecutor {
    fun execute(url: String, body: String, headers: Map<String, String>, timeoutMs: Long): HttpResult
}

internal data class HttpResult(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, List<String>>,
)

internal class JdkHttpExecutor(connectTimeoutMs: Long) : HttpExecutor {

    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofMillis(connectTimeoutMs))
        .build()

    override fun execute(url: String, body: String, headers: Map<String, String>, timeoutMs: Long): HttpResult {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(Duration.ofMillis(timeoutMs))

        headers.forEach { (k, v) -> builder.header(k, v) }

        val response = try {
            client.send(builder.build(), HttpResponse.BodyHandlers.ofString())
        } catch (e: HttpTimeoutException) {
            throw GenerateTimeoutException(cause = e)
        } catch (e: java.net.ConnectException) {
            throw ConnectTimeoutException(cause = e)
        } catch (e: java.io.IOException) {
            throw ConnectionException(e.message ?: "Connection failed", cause = e)
        }

        return HttpResult(
            statusCode = response.statusCode(),
            body = response.body(),
            // HTTP/1.1 헤더 키가 대소문자 혼용될 수 있으므로 소문자로 정규화
            headers = response.headers().map().mapKeys { (k, _) -> k.lowercase() },
        )
    }
}
