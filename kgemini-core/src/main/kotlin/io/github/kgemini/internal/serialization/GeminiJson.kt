package io.github.kgemini.internal.serialization

import kotlinx.serialization.json.Json

internal val geminiJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    explicitNulls = false
    encodeDefaults = false
}
