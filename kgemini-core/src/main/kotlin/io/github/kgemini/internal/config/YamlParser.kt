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

package io.github.kgemini.internal.config

/**
 * Minimal YAML parser for flat key-value pairs.
 * Supports nested keys by flattening with dot notation.
 *
 * Example:
 *   gemini:
 *     model: free
 *     timeout: 30000
 *
 * Produces: {"gemini.model" -> "free", "gemini.timeout" -> "30000"}
 */
internal object YamlParser {

    fun parse(text: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val prefixStack = mutableListOf<Pair<Int, String>>() // (indent, key)

        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

            val indent = line.length - line.trimStart().length
            val colonIndex = trimmed.indexOf(':')
            if (colonIndex < 0) continue

            val key = trimmed.substring(0, colonIndex).trim()
            val value = trimmed.substring(colonIndex + 1).trim()

            // Pop prefixes that are at same or deeper indentation
            while (prefixStack.isNotEmpty() && prefixStack.last().first >= indent) {
                prefixStack.removeLast()
            }

            if (value.isEmpty()) {
                // This is a parent key (e.g. "gemini:")
                prefixStack.add(indent to key)
            } else {
                val fullKey = if (prefixStack.isEmpty()) {
                    key
                } else {
                    prefixStack.joinToString(".") { it.second } + "." + key
                }
                result[fullKey] = value
            }
        }

        return result
    }
}
