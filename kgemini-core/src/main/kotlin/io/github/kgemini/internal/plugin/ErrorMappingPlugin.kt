package io.github.kgemini.internal.plugin

import io.github.kgemini.exception.*
import io.github.kgemini.internal.serialization.geminiJson
import io.github.kgemini.model.ErrorResponse
import io.ktor.client.plugins.api.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.time.Duration.Companion.seconds

internal val ErrorMappingPlugin = createClientPlugin("GeminiErrorMapping") {
    onResponse { response ->
        if (response.status.isSuccess()) return@onResponse

        val body = response.bodyAsText()
        val errorMessage = try {
            val errorResponse = geminiJson.decodeFromString<ErrorResponse>(body)
            errorResponse.error?.message ?: body
        } catch (_: Exception) {
            body
        }

        val retryAfter = response.headers["Retry-After"]?.toLongOrNull()?.seconds

        throw when (response.status.value) {
            400 -> InvalidRequestException(errorMessage)
            401, 403 -> AuthenticationException(response.status.value, errorMessage)
            404 -> ModelNotFoundException(errorMessage)
            429 -> RateLimitException(errorMessage, retryAfter)
            in 500..599 -> ServerException(response.status.value, errorMessage)
            else -> ServerException(response.status.value, "Unexpected status: ${response.status}: $errorMessage")
        }
    }
}
