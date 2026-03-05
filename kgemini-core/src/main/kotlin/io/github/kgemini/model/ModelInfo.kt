package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class ModelInfo(
    val name: String? = null,
    val version: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val inputTokenLimit: Int? = null,
    val outputTokenLimit: Int? = null,
    val supportedGenerationMethods: List<String> = emptyList(),
    val temperature: Double? = null,
    val topP: Double? = null,
    val topK: Int? = null,
    val maxTemperature: Double? = null,
)

@Serializable
internal data class ListModelsResponse(
    val models: List<ModelInfo> = emptyList(),
    val nextPageToken: String? = null,
)
