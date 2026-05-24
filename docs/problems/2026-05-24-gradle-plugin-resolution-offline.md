# Gradle Plugin Resolution Offline

- Timestamp: 2026-05-24 08:24:47 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Gradle 8.5 using `/tmp/dualbt-gradle-home`, Android SDK `/home/astra/.android/sdk`.

## Exact Error

```text
FAILURE: Build failed with an exception.

* Where:
Build file '/home/astra/codex/DualBT/build.gradle.kts' line: 1

* What went wrong:
Plugin [id: 'com.android.application', version: '8.2.2', apply: false] was not found in any of the following sources:

- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
- Plugin Repositories (could not resolve plugin artifact 'com.android.application:com.android.application.gradle.plugin:8.2.2')
  Searched in the following repositories:
    Google
    MavenRepo
    Gradle Central Plugin Repository
```

## Reproduction Steps

1. Use the patched writable Gradle home at `/tmp/dualbt-gradle-home`.
2. Run `./gradlew --no-daemon assembleDebug`.
3. Observe Gradle reaches plugin resolution and cannot resolve Android Gradle Plugin `8.2.2`.

## First Hypothesis

Network/DNS restrictions prevent downloading Gradle plugin artifacts into the writable cache. The read-only global Gradle cache may already contain Android Gradle Plugin artifacts that can be copied into `/tmp/dualbt-gradle-home`.

