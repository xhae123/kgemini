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

package io.github.kgemini.models

import io.github.kgemini.model.GeminiModel

/**
 * Metadata for a Gemini model including pricing and recommended use cases.
 */
public data class ModelMetadata(
    val model: GeminiModel.Known,
    val displayName: String,
    val description: String,
    val contextWindow: Int,
    val maxOutputTokens: Int,
    val inputPricePerMToken: Double,
    val outputPricePerMToken: Double,
    val bestFor: List<String>,
)

/**
 * Catalog of known Gemini models with their metadata.
 */
public object ModelCatalog {

    public val entries: List<ModelMetadata> = listOf(
        ModelMetadata(
            model = GeminiModel.Known.FLASH_2_0,
            displayName = "Gemini 2.0 Flash",
            description = "Fast, versatile multimodal model for diverse tasks.",
            contextWindow = 1_048_576,
            maxOutputTokens = 8_192,
            inputPricePerMToken = 0.10,
            outputPricePerMToken = 0.40,
            bestFor = listOf("general chat", "summarization", "classification"),
        ),
        ModelMetadata(
            model = GeminiModel.Known.FLASH_2_0_LITE,
            displayName = "Gemini 2.0 Flash Lite",
            description = "Lightweight, cost-efficient model for high-throughput tasks.",
            contextWindow = 1_048_576,
            maxOutputTokens = 8_192,
            inputPricePerMToken = 0.075,
            outputPricePerMToken = 0.30,
            bestFor = listOf("cost-sensitive tasks", "high-throughput processing"),
        ),
        ModelMetadata(
            model = GeminiModel.Known.FLASH_2_5,
            displayName = "Gemini 2.5 Flash",
            description = "Enhanced reasoning model with thinking capabilities.",
            contextWindow = 1_048_576,
            maxOutputTokens = 65_536,
            inputPricePerMToken = 0.15,
            outputPricePerMToken = 0.60,
            bestFor = listOf("complex reasoning", "code generation", "agentic tasks"),
        ),
        ModelMetadata(
            model = GeminiModel.Known.PRO_2_5,
            displayName = "Gemini 2.5 Pro",
            description = "Most capable model for advanced reasoning and planning.",
            contextWindow = 1_048_576,
            maxOutputTokens = 65_536,
            inputPricePerMToken = 1.25,
            outputPricePerMToken = 10.00,
            bestFor = listOf("advanced reasoning", "planning", "multi-step problem solving"),
        ),
    )

    private val byModel: Map<GeminiModel.Known, ModelMetadata> =
        entries.associateBy { it.model }

    /**
     * Returns the [ModelMetadata] for the given [model].
     *
     * @throws IllegalArgumentException if the model is not found in the catalog.
     */
    public fun of(model: GeminiModel.Known): ModelMetadata =
        byModel[model] ?: throw IllegalArgumentException("No metadata for model: ${model.id}")
}
