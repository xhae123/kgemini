# Contributing to kgemini

Thank you for your interest in contributing to kgemini!

## Prerequisites

- **JDK 21+** (Gradle toolchain auto-downloads if not installed)
- **Kotlin 2.1.20** (managed by Gradle)

## Project Structure

```
kgemini/
├── kgemini-core/       # Core client library
├── kgemini-models/     # Model metadata catalog (pricing, limits)
├── kgemini-bom/        # Bill of Materials for version alignment
└── gradle/
    └── libs.versions.toml  # Dependency version catalog
```

## Build & Test

```bash
# Build core module
./gradlew :kgemini-core:build

# Run tests only
./gradlew :kgemini-core:test

# Build all modules
./gradlew build
```

## Code Conventions

### Visibility

This project uses Kotlin's `explicitApi()` mode. All public declarations must have explicit visibility modifiers:

```kotlin
// Required
public fun doSomething(): String { ... }
public class MyClass { ... }
internal fun helperFunction() { ... }
```

### Style

- **No `Any` type** — use proper types everywhere
- **Self-documenting code** — clear naming over comments
- **Comments explain "why"** — not "what"
- **KDoc on public API** — document all public functions and classes

### Naming

- Classes: `PascalCase`
- Functions/properties: `camelCase`
- Constants: `SCREAMING_SNAKE_CASE`
- Packages: `lowercase`

### Error Handling

- Use the sealed `GeminiException` hierarchy — do not throw generic exceptions
- Preserve context (HTTP status, raw response) in exceptions

### Testing

- Framework: [Kotest](https://kotest.io/) with JUnit5 runner
- Mock HTTP via `HttpExecutor` fun interface — pass a lambda, no mock library needed
- Test both success and failure paths

## Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]
```

**Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`

**Scopes:** `core`, `models`, `bom`

**Examples:**
```
feat(core): add retry with exponential backoff
fix(core): handle Retry-After header case sensitivity
test(core): add config priority tests
```

## Pull Request Process

1. Fork the repository
2. Create a feature branch from `main`
3. Make your changes following the conventions above
4. Ensure all tests pass: `./gradlew :kgemini-core:build`
5. Submit a pull request with a clear description of your changes

## Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.1.20 |
| kotlinx-serialization | 1.8.1 |
| Kotest | 6.0.0.M1 |
| JDK | 21+ |
