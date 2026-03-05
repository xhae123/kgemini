package io.github.kgemini.internal.plugin

import io.ktor.client.plugins.api.*
import io.ktor.client.request.*

internal val ApiKeyAttributeKey = io.ktor.util.AttributeKey<String>("GeminiApiKey")

internal val AuthPlugin = createClientPlugin("GeminiAuth", ::AuthPluginConfig) {
    val apiKey = pluginConfig.apiKey

    onRequest { request, _ ->
        request.parameter("key", apiKey)
    }
}

internal class AuthPluginConfig {
    var apiKey: String = ""
}
