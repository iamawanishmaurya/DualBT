# AAPT Not On Default PATH Solution

## Linked Problem

- `docs/problems/2026-05-24-aapt-not-on-default-path.md`

## What Failed

Running `aapt dump badging` from the default shell failed because `aapt` is not on the default `PATH`.

## What Worked

Using the explicit SDK build-tools binary worked:

```bash
/home/astra/.android/sdk/build-tools/34.0.0/aapt dump badging app/build/outputs/apk/debug/app-debug.apk
```

## Why It Worked

The Android SDK build-tools directory contains the `aapt` executable used by the Gradle build. Calling it by absolute path avoids relying on shell `PATH` setup.

## Commands Run

```bash
/home/astra/.android/sdk/build-tools/34.0.0/aapt dump badging app/build/outputs/apk/debug/app-debug.apk | rg "package:|sdkVersion|targetSdkVersion"
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
```
