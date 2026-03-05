package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class EmbedContentRequest(
    val model: String? = null,
    val content: Content,
    val taskType: String? = null,
    val title: String? = null,
    val outputDimensionality: Int? = null,
)
