package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
internal data class ErrorResponse(
    val error: ErrorDetail? = null,
)

@Serializable
internal data class ErrorDetail(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null,
)
