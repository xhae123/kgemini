package io.github.kgemini.model

import kotlinx.serialization.Serializable

@Serializable
public sealed interface GeminiModel {
    public val id: String

    public enum class Known(override val id: String) : GeminiModel {
        FLASH_2_0("gemini-2.0-flash"),
        FLASH_2_0_LITE("gemini-2.0-flash-lite"),
        FLASH_2_5("gemini-2.5-flash"),
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
