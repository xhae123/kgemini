/*
 * Copyright 2025 kgemini contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Gemini API Part — flat JSON structure.
 * e.g. `{"text": "hello"}` or `{"inlineData": {"mimeType": "...", "data": "..."}}`.
 * Type is determined by field presence (not a discriminated union).
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
