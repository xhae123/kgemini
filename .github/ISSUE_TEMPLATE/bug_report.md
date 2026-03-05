---
name: Bug Report
about: Report a bug in kgemini
title: "[Bug] "
labels: bug
assignees: ''
---

## Description

A clear and concise description of the bug.

## Steps to Reproduce

```kotlin
// Minimal code to reproduce the issue
val gemini = KGemini(apiKey = "...") {
    model = GeminiModel.Known.FLASH_2_0
}
// ...
```

## Expected Behavior

What you expected to happen.

## Actual Behavior

What actually happened. Include the full stack trace if applicable.

## Environment

- **kgemini version:** 0.1.0
- **Kotlin version:** 2.1.x
- **JDK version:** 17 / 21
- **OS:** macOS / Linux / Windows
- **HTTP engine:** CIO / OkHttp / Java
