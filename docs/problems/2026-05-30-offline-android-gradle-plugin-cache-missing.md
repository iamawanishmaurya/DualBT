# Offline Android Gradle Plugin Cache Missing

## Exact error

```text
FAILURE: Build failed with an exception.

* Where:
Build file '/home/astra/codex/DualBT/build.gradle.kts' line: 19

* What went wrong:
Plugin [id: 'com.android.application', version: '8.2.2', apply: false] was not found in any of the following sources:

- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
- Plugin Repositories (could not resolve plugin artifact 'com.android.application:com.android.application.gradle.plugin:8.2.2')
  Searched in the following repositories:
    maven(file:/tmp/dualbt-offline-maven)
    Google
    MavenRepo
    Gradle Central Plugin Repository

BUILD FAILED in 4m 36s
```

## Reproduction steps

1. Restore Gradle wrapper distribution after `/tmp/dualbt-gradle-home` cleanup.
2. Run `./gradlew --no-daemon --offline clean assembleDebug`.
3. Observe the build fails resolving `com.android.application` because the offline Maven cache is missing the Android Gradle Plugin.

## Environment

- Date: 2026-05-30
- Worktree: `/home/astra/codex/DualBT`
- Gradle: 8.5 wrapper
- Android Gradle Plugin: 8.2.2
- Offline Maven cache: `/tmp/dualbt-offline-maven`

## First hypothesis

The previous offline Maven cache lived under `/tmp` and was cleaned. Since network access is available, rerun without `--offline` so Gradle can resolve the missing Android Gradle Plugin from configured repositories.
