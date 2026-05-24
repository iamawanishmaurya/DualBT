# Offline Runtime Dependencies Missing

- Timestamp: 2026-05-24 08:33:12 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Gradle 8.5 offline mode, Android SDK `/home/astra/.android/sdk`.

## Exact Error

```text
Execution failed for task ':app:checkDebugAarMetadata'.
> Could not resolve all files for configuration ':app:debugRuntimeClasspath'.
   > Could not resolve org.jetbrains.kotlin:kotlin-stdlib:1.9.22.
      > No cached version of org.jetbrains.kotlin:kotlin-stdlib:1.9.22 available for offline mode.
   > Could not resolve androidx.compose:compose-bom:2024.02.00.
      > No cached version of androidx.compose:compose-bom:2024.02.00 available for offline mode.
   > Could not resolve androidx.compose.ui:ui.
      > No cached version of androidx.compose.ui:ui: available for offline mode.
   > Could not resolve androidx.compose.material3:material3.
      > No cached version of androidx.compose.material3:material3: available for offline mode.
   > Could not resolve com.google.dagger:hilt-android:2.51.
      > No cached version of com.google.dagger:hilt-android:2.51 available for offline mode.
   > Could not resolve com.google.oboe:oboe:1.8.0.
      > No cached version of com.google.oboe:oboe:1.8.0 available for offline mode.
```

The full command output also listed missing cached versions for Compose tooling, animation, Activity Compose, Lifecycle Compose/runtime, Hilt Navigation Compose, and coroutines Android.

## Reproduction Steps

1. Copy global Gradle module cache into `/tmp/dualbt-gradle-home/caches`.
2. Run `./gradlew --no-daemon --offline assembleDebug`.
3. Observe `:app:checkDebugAarMetadata` fails because required runtime dependencies are not present in the copied cache.

## First Hypothesis

The global Gradle cache contains plugin artifacts but not this app's runtime dependency set. Either compatible cached versions must be found and the project adjusted, or network access is required to download the declared versions.

