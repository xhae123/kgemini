package io.github.kgemini

import io.github.kgemini.internal.http.GeminiEngine
import io.github.kgemini.model.GeminiModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public class KGeminiConfig internal constructor() {
    public var model: GeminiModel = GeminiModel.Known.FLASH_2_0
    public var engine: GeminiEngine = GeminiEngine.CIO
    internal var testEngine: io.ktor.client.engine.HttpClientEngine? = null

    internal var connectTimeout: Duration = 5.seconds
    internal var generateTimeout: Duration = 30.seconds
    internal var streamFirstByte: Duration = 10.seconds
    internal var streamIdle: Duration = 5.seconds

    public fun timeouts(block: TimeoutConfig.() -> Unit) {
        TimeoutConfig().apply(block).also {
            connectTimeout = it.connect
            generateTimeout = it.generate
            streamFirstByte = it.streamFirstByte
            streamIdle = it.streamIdle
        }
    }
}

public class TimeoutConfig internal constructor() {
    public var connect: Duration = 5.seconds
    public var generate: Duration = 30.seconds
    public var streamFirstByte: Duration = 10.seconds
    public var streamIdle: Duration = 5.seconds
}
