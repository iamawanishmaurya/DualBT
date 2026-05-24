# Gradle Wildcard IP Detection Failed

- Timestamp: 2026-05-24 08:12:38 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Java 21.0.11, Gradle 8.5, Android SDK `/home/astra/.android/sdk`, `GRADLE_USER_HOME=/tmp/dualbt-gradle-home`.

## Exact Error

```text
FAILURE: Build failed with an exception.

* What went wrong:
Could not determine a usable wildcard IP for this machine.

* Try:
> Run with --stacktrace option to get the stack trace.
> Run with --info or --debug option to get more log output.
> Run with --scan to get full insights.
> Get more help at https://help.gradle.org.
```

## Reproduction Steps

1. Set `ANDROID_HOME=/home/astra/.android/sdk`.
2. Set `ANDROID_SDK_ROOT=/home/astra/.android/sdk`.
3. Set `GRADLE_USER_HOME=/tmp/dualbt-gradle-home`.
4. Run `./gradlew assembleDebug`.
5. Observe the command exits with code 1 and the wildcard IP error above.

## First Hypothesis

Gradle is trying to start or communicate with daemon infrastructure in an environment where wildcard network binding cannot be determined. The next attempt should avoid the daemon and force IPv4 stack preference instead of rerunning the same command unchanged.

## Repeated Occurrence During Device Testing

- Timestamp: 2026-05-24 18:53:20 IST
- Command: `./gradlew --no-daemon --offline clean assembleDebug`
- Result: The same wildcard IP error occurred after recreating `/tmp/dualbt-gradle-home` from the durable Gradle cache.

```text
Could not determine a usable wildcard IP for this machine.
```
