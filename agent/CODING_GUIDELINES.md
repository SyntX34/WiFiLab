# Coding Guidelines

## General

- Keep the project native Android. No WebView application shells or web prototypes.
- Use Kotlin and Jetpack Compose.
- Keep files focused. A parser, a screen and a view model should not be combined without reason.
- Prefer immutable data classes and read only collections.
- Use constructor injection.
- Do not add placeholder code that pretends to work.
- Keep behavior local and privacy preserving.

## Kotlin style

- Four space indentation.
- Trailing commas in multiline declarations.
- Explicit types for public mutable state and repository contracts.
- Use `val` unless mutation is required.
- Keep functions small and name them for behavior.
- Follow the official Kotlin coding conventions except where this document is more specific.

## Comment rule

All comments must use block style with a leading asterisk on each line. Do not use slash line comments. Do not use dashes inside comments.

Valid:

```kotlin
/*
 * Parses the next Ethernet frame.
 */
```

Also valid:

```kotlin
/** Returns the normalized network address. */
```

Invalid:

```kotlin
// parse the frame
```

Avoid comments that restate the code. Use comments to explain non obvious protocol or safety decisions.

## Compose

- Use Material 3 components and the shared theme in `ui/theme`.
- Prefer stateless composables with explicit callbacks.
- Use `collectAsStateWithLifecycle` for view model state.
- Put repeated visual elements in `ui/components`.
- Keep large screens split into private composable functions.
- Provide meaningful content descriptions for actionable icons.
- Do not hardcode user visible strings into long adapter helpers when a shared resource or constant is clearer.
- Handle empty, loading and error states explicitly.

## Architecture

- UI calls view models.
- View models call domain use cases or repository interfaces.
- Data implements domain repository interfaces.
- Pure domain logic must not need Android.
- New persistent data must go through Room or DataStore, not global mutable state.
- Do not let view models call Android Wi Fi or socket APIs directly.
- Hilt bindings belong in `core/di`.

## Security and privacy

- No external upload of scans, audit results, captures or password input.
- No background scanning.
- No root assumptions.
- No attack implementation.
- Validate network scopes in the domain layer as well as the UI.
- Use bounded parsers and timeouts for untrusted data.
- Do not log sensitive network metadata.
- Use synthetic test data only.

## Testing

Add JVM unit tests for:

- packet parsing, including malformed input
- address and baseline calculations
- password entropy behavior
- capability parsing and model mapping

Use Android instrumented tests only when an Android framework behavior is the subject.

Tests must not depend on network availability, a device, wall clock timing variance or real captures.

## Documentation

- Update README for user facing changes.
- Update agent and architecture notes for structural changes.
- Update privacy and security docs when data handling changes.
- Keep markdown links valid.

## Git

- Keep commits focused.
- Do not commit `local.properties`, build output, IDE state, SDK copies or signing material.
- Do not commit real packet captures, SSIDs, BSSIDs or credentials.
- Verify the Gradle wrapper and tests before opening a pull request.
