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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class HarmCategory {
    @SerialName("HARM_CATEGORY_HARASSMENT")
    HARASSMENT,

    @SerialName("HARM_CATEGORY_HATE_SPEECH")
    HATE_SPEECH,

    @SerialName("HARM_CATEGORY_SEXUALLY_EXPLICIT")
    SEXUALLY_EXPLICIT,

    @SerialName("HARM_CATEGORY_DANGEROUS_CONTENT")
    DANGEROUS_CONTENT,

    @SerialName("HARM_CATEGORY_CIVIC_INTEGRITY")
    CIVIC_INTEGRITY;

    public companion object {
        public fun fromApiValue(value: String): HarmCategory? =
            entries.find { entry ->
                entry.name == value || "HARM_CATEGORY_${entry.name}" == value
            }
    }
}

@Serializable
public enum class HarmBlockThreshold {
    @SerialName("BLOCK_NONE")
    NONE,

    @SerialName("BLOCK_LOW_AND_ABOVE")
    LOW_AND_ABOVE,

    @SerialName("BLOCK_MEDIUM_AND_ABOVE")
    MEDIUM_AND_ABOVE,

    @SerialName("BLOCK_ONLY_HIGH")
    HIGH_ONLY,
}

@Serializable
public enum class HarmProbability {
    @SerialName("HARM_PROBABILITY_UNSPECIFIED")
    UNSPECIFIED,

    @SerialName("NEGLIGIBLE")
    NEGLIGIBLE,

    @SerialName("LOW")
    LOW,

    @SerialName("MEDIUM")
    MEDIUM,

    @SerialName("HIGH")
    HIGH,
}

@Serializable
public data class SafetySetting(
    val category: HarmCategory,
    val threshold: HarmBlockThreshold,
)

@Serializable
public data class SafetyRating(
    val category: HarmCategory,
    val probability: HarmProbability,
    val blocked: Boolean = false,
)
