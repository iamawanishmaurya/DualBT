# Build Modes

DualBT currently has two build modes because the execution environment cannot download the runtime dependencies required by the original plan.

## Default Offline Build

The default build compiles the framework-only Java fallback:

```bash
./gradlew --no-daemon --offline clean assembleDebug
```

This mode produces an installable debug APK using only artifacts available in the local Android SDK and Gradle cache.

## Planned Stack Build

Set `DUALBT_PLANNED_STACK=true` to enable the original plan's Kotlin, Compose, Hilt, and NDK configuration:

```bash
DUALBT_PLANNED_STACK=true ./gradlew assembleDebug
```

This mode applies:

- Kotlin Android plugin `1.9.22`
- Hilt plugin `2.51`
- Compose compiler extension `1.5.8`
- NDK r27b with CMake and Prefab enabled
- Oboe dependency `1.8.0`

The planned stack also excludes the Java fallback entry points to avoid duplicate classes with the Kotlin sources.

## Current Constraint

The planned stack still requires dependency resolution for Compose, AndroidX, Hilt runtime, Oboe, and test libraries. In the current sandbox those artifacts are not cached and remote repository access is unavailable, so the default offline build remains the verified path.

