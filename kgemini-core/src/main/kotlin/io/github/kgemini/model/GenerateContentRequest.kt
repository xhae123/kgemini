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
