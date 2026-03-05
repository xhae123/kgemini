package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class GenerateContentRequest(
    val contents: List<Content> = emptyList(),
    val safetySettings: List<SafetySetting>? = null,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null,
)
