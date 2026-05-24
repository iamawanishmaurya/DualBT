# NDK Source Properties Missing

- Timestamp: 2026-05-24 08:30:20 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Gradle 8.5, Android Gradle Plugin 8.2.2, Android SDK `/home/astra/.android/sdk`.

## Exact Error

```text
FAILURE: Build completed with 2 failures.

1: Task failed with an exception.
-----------
* What went wrong:
A problem occurred configuring project ':app'.
> [CXX1101] NDK at /home/astra/.android/sdk/ndk/25.1.8937393 did not have a source.properties file
```

## Reproduction Steps

1. Run `./gradlew --no-daemon --offline assembleDebug` using the writable Gradle home and copied dependency cache.
2. Observe Gradle configures `:app`.
3. Observe Android Gradle Plugin selects `/home/astra/.android/sdk/ndk/25.1.8937393`.
4. Observe configuration fails because that NDK directory does not have `source.properties`.

## First Hypothesis

The Android SDK contains multiple NDK versions, and AGP selected an incomplete one. Pin `android.ndkVersion` to the complete installed NDK `27.1.12297006` if its `source.properties` file exists.

