package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class GenerateContentRequest(
    val contents: List<Content> = emptyList(),
    val safetySettings: List<SafetySetting>? = null,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null,
) {
    public class Builder internal constructor() {
        public var safetySettings: List<SafetySetting>? = null
        public var generationConfig: GenerationConfig? = null
        public var systemInstruction: String? = null

        internal fun build(prompt: String): GenerateContentRequest =
            GenerateContentRequest(
                contents = listOf(Content.user(prompt)),
                safetySettings = safetySettings,
                generationConfig = generationConfig,
                systemInstruction = systemInstruction?.let { Content.text(it) },
            )
    }
}
