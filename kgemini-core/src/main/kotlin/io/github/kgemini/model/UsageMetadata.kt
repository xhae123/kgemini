package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class UsageMetadata(
    val promptTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null,
)
