package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class CountTokensRequest(
    val contents: List<Content> = emptyList(),
    val generateContentRequest: GenerateContentRequest? = null,
)
