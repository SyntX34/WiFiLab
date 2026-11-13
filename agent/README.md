# Agent Documentation

This directory gives a new AI agent or maintainer the minimum reliable context needed to work on WiFiLab without guessing.

Read these files in order:

1. [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md): objective, non goals, safety model and current feature set.
2. [ARCHITECTURE.md](ARCHITECTURE.md): packages, data flow, persistence and key implementation details.
3. [CODING_GUIDELINES.md](CODING_GUIDELINES.md): conventions, comment rules, tests and review standards.
4. [VERIFICATION.md](VERIFICATION.md): build commands, checks and known validation gaps.

## Quick start

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

The debug APK path is `app/build/outputs/apk/debug/app-debug.apk`.

## Ground rules

- This is a real native Android project, not a web app or mockup.
- Do not add offensive networking capabilities.
- Do not upload captures, scans or research data.
- Do not introduce placeholder features that appear functional.
- Keep all user research data local by default.
- Run tests after parser or security calculation changes.
- Preserve the author attribution to SyntX34.
