package io.github.kgemini.internal.http

internal object Endpoints {
    const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"

    fun listModels(): String = "$BASE_URL/models"

    fun getModel(modelId: String): String {
        val name = if (modelId.startsWith("models/")) modelId else "models/$modelId"
        return "$BASE_URL/$name"
    }

    fun generateContent(modelId: String): String =
        "$BASE_URL/models/$modelId:generateContent"

    fun streamGenerateContent(modelId: String): String =
        "$BASE_URL/models/$modelId:streamGenerateContent"

    fun countTokens(modelId: String): String =
        "$BASE_URL/models/$modelId:countTokens"

    fun embedContent(modelId: String): String =
        "$BASE_URL/models/$modelId:embedContent"
}
