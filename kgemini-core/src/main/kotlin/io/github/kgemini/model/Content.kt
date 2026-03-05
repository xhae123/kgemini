package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public data class Content(
    val role: String? = null,
    val parts: List<Part> = emptyList(),
) {
    public companion object {
        public fun user(text: String): Content = Content(
            role = "user",
            parts = listOf(Part(text = text)),
        )

        public fun model(text: String): Content = Content(
            role = "model",
            parts = listOf(Part(text = text)),
        )

        public fun text(text: String): Content = Content(
            parts = listOf(Part(text = text)),
        )
    }
}

/**
 * Gemini API Part — flat JSON 구조.
 * `{"text": "hello"}` 또는 `{"inlineData": {"mimeType": "...", "data": "..."}}`.
 * 필드 존재 여부로 타입 구분 (discriminated union이 아님).
 */
@Serializable
public data class Part(
    val text: String? = null,
    val inlineData: Blob? = null,
)

@Serializable
public data class Blob(
    val mimeType: String,
    val data: String,
)
