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
public sealed interface GeminiModel {
    public val id: String

    public enum class Known(override val id: String) : GeminiModel {
        /**
         * Gemini 2.5 Flash — best price-performance for most tasks.
         *
         * - Context: 1,048,576 tokens
         * - Pricing: $0.30 / 1M input, $2.50 / 1M output
         * - Free tier available
         * - Best for: general chat, code generation, agentic tasks
         */
        FLASH_2_5("gemini-2.5-flash"),

        /**
         * Gemini 2.5 Flash Lite — fastest and most budget-friendly.
         *
         * - Context: 1,048,576 tokens
         * - Pricing: $0.10 / 1M input, $0.40 / 1M output
         * - Free tier available
         * - Best for: high-throughput, low-latency, cost-sensitive tasks
         */
        FLASH_2_5_LITE("gemini-2.5-flash-lite"),

        /**
         * Gemini 2.5 Pro — most capable model for complex reasoning.
         *
         * - Context: 1,048,576 tokens
         * - Pricing: $1.25 / 1M input, $10.00 / 1M output
         * - Free tier available
         * - Best for: advanced reasoning, planning, multi-step problem solving
         */
        PRO_2_5("gemini-2.5-pro"),
    }

    public data class Pinned(val base: Known, val version: String) : GeminiModel {
        override val id: String = "${base.id}-$version"
    }

    public data class Latest(val base: Known) : GeminiModel {
        override val id: String = "${base.id}-latest"
    }

    public data class Custom(override val id: String) : GeminiModel
}

public fun GeminiModel.Known.pinned(version: String): GeminiModel.Pinned =
    GeminiModel.Pinned(this, version)

public val GeminiModel.Known.latest: GeminiModel.Latest
    get() = GeminiModel.Latest(this)

public val GeminiModel.Known.stable: GeminiModel.Known
    get() = this
