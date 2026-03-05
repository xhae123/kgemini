package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class CountTokensResponse(
    val totalTokens: Int? = null,
)
