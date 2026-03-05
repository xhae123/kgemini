package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class EmbedContentResponse(
    val embedding: ContentEmbedding? = null,
)

@Serializable
public data class ContentEmbedding(
    val values: List<Float> = emptyList(),
)
