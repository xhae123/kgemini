# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.2.1] - 2026-03-08

### Changed

- Default models updated from deprecated gemini-2.0 to gemini-2.5 series
  - `free` → `gemini-2.5-flash` (was `gemini-2.0-flash`)
  - `fast`/`cheap` → `gemini-2.5-flash-lite` (was `gemini-2.0-flash`/`gemini-2.0-flash-lite`)
- API key authentication via `x-goog-api-key` header (was query parameter)
- Config files read from classpath (`src/main/resources/`) instead of CWD
- Config priority simplified: `gemini.yml` > `gemini.properties` > defaults
- API key configured via `gemini.api-key` in config files

### Added

- Java interop documentation (`KGeminiKt.ask()`, `KGeminiKt.generate()`)
- `CODEOWNERS` for automatic PR reviewer assignment
- GitHub Discussions guide in CONTRIBUTING.md
- `gemini.yml` / `gemini.properties` / `.env` added to `.gitignore`

### Removed

- `FLASH_2_0`, `FLASH_2_0_LITE` from `GeminiModel.Known` (deprecated, retiring June 2026)
- Environment variable configuration (`GEMINI_API_KEY`, `GEMINI_MODEL`, etc.)

## [0.2.0] - 2026-03-08

### Changed

- **BREAKING**: Public API rewritten to `ask()`/`generate()` top-level functions
- **BREAKING**: Replaced Ktor/coroutines with blocking I/O (`java.net.http.HttpClient`)
- **BREAKING**: Minimum JDK raised from 17 to 21 (Virtual Thread friendly)
- External dependency reduced to `kotlinx-serialization-json` only

### Added

- File-based configuration: `gemini.properties`, `gemini.yml`
- Config priority: `gemini.yml` > `gemini.properties` > defaults
- Model aliases: `free`, `fast`, `cheap`, `smart`
- Duration parsing for config values: `60s`, `500ms`, raw milliseconds
- `response.totalTokens` convenience property

### Removed

- `KGemini` class, `KGeminiConfig` DSL, `KGeminiConfig.Builder`
- `generateStream()`, `generateAll()`, `countTokens()`, `listModels()`, `getModel()`
- `CountTokensRequest`, `CountTokensResponse`, `ModelInfo`, `ListModelsResponse`
- `StreamTimeoutException`, `StreamInterruptedException`
- Ktor plugins (`AuthPlugin`, `ErrorMappingPlugin`, `TimeoutPlugin`)
- Multi-engine support (`GeminiEngine`, `EngineProvider`)

## [0.1.0] - 2026-03-05

### Added

- `KGemini` client with API key authentication
- `generate()` for single text generation
- `generateStream()` for SSE-based streaming with partial failure recovery
- `generateAll()` for parallel multi-prompt generation
- `countTokens()` for token counting
- `listModels()` and `getModel()` for model discovery
- `GeminiModel` sealed interface with `Known`, `Pinned`, `Latest`, and `Custom` variants
- `GenerationConfig` support (temperature, topP, topK, maxOutputTokens, etc.)
- `SafetySetting` with type-safe `HarmCategory` and `HarmBlockThreshold` enums
- Sealed `GeminiException` hierarchy with specific exception types
- Automatic retry with exponential backoff + jitter for retryable exceptions
- `RateLimitException` respects `Retry-After` header from API responses
- Configurable retry via `retry {}` DSL (maxRetries, baseDelay, maxDelay)
- Configurable per-operation timeouts (connect, generate, stream first-byte, stream idle)
- Multiple HTTP engine support (CIO, OkHttp, Java)
- `kgemini-models` module with model metadata catalog (pricing, token limits)
