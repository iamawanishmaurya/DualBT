# AAPT Not On Default PATH

## Exact Error

```text
zsh:1: command not found: aapt
```

## Reproduction Steps

1. Build the v0.2.13 debug APK successfully.
2. Run `aapt dump badging app/build/outputs/apk/debug/app-debug.apk` from the default shell environment.
3. Observe that `aapt` is not found.

## Environment

- Repository: `/home/astra/codex/DualBT`
- Shell: `zsh`
- Android SDK: `/home/astra/.android/sdk`
- Date: 2026-05-24

## First Hypothesis

The Android build-tools directory is not on the default shell `PATH`. Use the full SDK build-tools path, matching the path used by the Gradle build environment.
