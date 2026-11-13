# Contributing to WiFiLab

Thank you for considering a contribution. WiFiLab is a defensive wireless security research application. Contributions must preserve its safety boundaries, privacy posture and local first design.

## Code of conduct

All participation is covered by [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). Be respectful, specific and constructive.

## Scope and safety

Accepted contributions improve:

- Android permissions correctness and graceful capability handling
- local discovery and audit accuracy without invasive techniques
- offline parsers, analysis quality and tests
- educational content accuracy
- Compose usability, accessibility and visual polish
- privacy, local storage and data deletion behavior
- documentation and maintainability

Not accepted:

- deauthentication, beacon flooding or packet injection
- rogue access point creation
- evasion, exploitation or credential theft features
- telemetry, advertising or cloud upload of scan data and captures
- code that assumes root or bypasses Android security boundaries
- hidden network operations that are not clearly disclosed

If a feature is conceptually useful for education, implement it as a simulation with fixed data and clear labels.

## Development setup

1. Install JDK 21 and Android SDK Platform 34 with Build Tools 34.0.0.
2. Clone the repository.
3. Create a machine specific `local.properties` with `sdk.dir`.
4. Open the project in Android Studio or run Gradle from the command line.
5. Use a physical device for Wi Fi behavior when possible. Emulator Wi Fi results are limited.

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

## Branch and pull request workflow

- Fork the repository and create a focused branch.
- Keep unrelated changes out of the pull request.
- Include tests for new parsing, model or security calculation behavior.
- Update documentation when behavior, permissions or architecture changes.
- Confirm that CI passes before requesting review.
- Describe the user impact, safety impact and test evidence in the pull request.

Commit messages should be concise and action oriented. Example prefixes:

```text
feat: add DNS answer parsing
fix: handle scan permission denial
docs: explain local network scope
test: cover PCAPNG timestamp resolution
```

## Kotlin and Compose conventions

- Use Kotlin, Jetpack Compose and Material 3.
- Keep files focused. Do not place unrelated screens or parsers in one file.
- Follow the official Kotlin style with four space indentation.
- Prefer immutable state and StateFlow in view models.
- Use constructor injection with Hilt.
- Put pure logic in the domain layer and test it on the JVM.
- Keep Android framework calls in data or UI layers.
- Use block comments. Every comment line should use the leading asterisk form. Avoid line comments and do not use dashes inside comments.
- Avoid placeholder implementations. A screen must either be functional or clearly identify a platform capability as unavailable.
- Respect `minSdk` 26 and test API 33 and newer permission behavior.
- Do not introduce a new dependency without a clear need and license compatibility.

## Testing expectations

Unit tests are required for:

- capability and packet parsers
- subnet and baseline calculations
- entropy and security calculations
- model transformations

Manual verification is required for:

- permission request and denial flows
- scan throttling and cooldown behavior
- audit cancellation
- SAF import and local file deletion
- layout on a narrow phone and a larger screen

Do not include real captures, SSIDs, BSSIDs, addresses or credentials in tests or issues. Use synthetic values.

## Documentation

Update the relevant files when changing behavior:

- `README.md` for user facing features and setup
- `agent/ARCHITECTURE.md` for structural changes
- `SECURITY.md` for security or privacy changes
- `docs/PRIVACY.md` for data handling changes

## License

By contributing, you agree that your contribution is licensed under the GNU General Public License v3.0, consistent with the repository license.
