package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class GenerateContentResponse(
    val candidates: List<Candidate>? = null,
    val usageMetadata: UsageMetadata? = null,
    val promptFeedback: PromptFeedback? = null,
) {
    public val text: String?
        get() = candidates?.firstOrNull()?.content?.parts
            ?.mapNotNull { it.text }
            ?.joinToString("")
}

@Serializable
public data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null,
    val index: Int? = null,
)

@Serializable
public data class PromptFeedback(
    val blockReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null,
)
