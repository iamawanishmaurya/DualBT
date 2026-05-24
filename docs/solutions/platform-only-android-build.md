# Platform-Only Android Build

- Problems:
  - [2026-05-24-ndk-source-properties-missing.md](../problems/2026-05-24-ndk-source-properties-missing.md)
  - [2026-05-24-offline-runtime-dependencies-missing.md](../problems/2026-05-24-offline-runtime-dependencies-missing.md)
- Timestamp: 2026-05-24 08:59:01 IST

## What Failed

The initial Compose/Hilt/Oboe/NDK build path depended on artifacts unavailable in the offline cache, and AGP selected an incomplete NDK stub.

## What Worked

Replaced the runtime dependency-heavy path with a platform-only Java Android implementation using framework views, animations, mock devices, and in-app logging.

## Why It Worked

The Android framework and Android Gradle Plugin were locally available, while Compose, Hilt, Oboe, AndroidX runtime libraries, and test libraries were not.

## Commands Run

```bash
javac -cp app/src/main/java -d /tmp/dualbt-logger-test/classes /tmp/dualbt-logger-test/LogStoreTest.java app/src/main/java/com/xpwnit/dualbt/logging/LogStore.java
java -cp /tmp/dualbt-logger-test/classes LogStoreTest
```

