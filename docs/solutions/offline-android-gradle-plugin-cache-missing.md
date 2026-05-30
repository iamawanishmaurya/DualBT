# Offline Android Gradle Plugin Cache Recovery

Problem: [docs/problems/2026-05-30-offline-android-gradle-plugin-cache-missing.md](../problems/2026-05-30-offline-android-gradle-plugin-cache-missing.md)

## What failed

The offline Android build could not resolve Android Gradle Plugin `8.2.2` because the `/tmp/dualbt-offline-maven` cache no longer contained the plugin artifacts.

## What worked

Built without `--offline` so Gradle could restore the missing Android Gradle Plugin dependencies through the configured repositories.

## Why it worked

The missing plugin was a temporary cache problem, not a source problem. Allowing network resolution restored the Gradle dependency cache and moved the build to the next validation task.

## Commands run

```bash
./gradlew --no-daemon --offline clean assembleDebug
./gradlew --no-daemon clean assembleDebug
```
