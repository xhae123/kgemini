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
