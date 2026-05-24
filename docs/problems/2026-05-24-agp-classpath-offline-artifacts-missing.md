# Android Gradle Plugin Classpath Artifacts Missing

- Timestamp: 2026-05-24 08:44:22 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Gradle 8.5 offline mode, Android Gradle Plugin 8.2.2.

## Exact Error

```text
FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring root project 'DualBT'.
> Could not resolve all files for configuration ':classpath'.
   > Could not download asm-9.2.jar (org.ow2.asm:asm:9.2): No cached version available for offline mode
   > Could not download javapoet-1.10.0.jar (com.squareup:javapoet:1.10.0): No cached version available for offline mode
```

## Reproduction Steps

1. Remove unavailable Kotlin/Hilt/Compose dependencies.
2. Run `./gradlew --no-daemon --offline assembleDebug`.
3. Observe Android Gradle Plugin classpath resolution fails because `asm-9.2.jar` and `javapoet-1.10.0.jar` are not in the writable offline cache.

## First Hypothesis

The global Gradle cache may contain these artifacts in another cache layout or version. If not, offline Gradle builds are blocked until the missing classpath artifacts are available.

