# AAPT2 Maven Artifact Missing

- Problem: [2026-05-24-aapt2-maven-artifact-missing.md](../problems/2026-05-24-aapt2-maven-artifact-missing.md)
- Timestamp: 2026-05-24 08:59:01 IST

## What Failed

AGP tried to resolve `com.android.tools.build:aapt2:8.2.2-10154469` from Maven while running offline.

## What Worked

Set `android.aapt2FromMavenOverride=/home/astra/.android/sdk/build-tools/34.0.0/aapt2`.

## Why It Worked

The override instructs AGP to use the SDK's local `aapt2` binary instead of resolving the Maven-packaged artifact.

## Commands Run

```bash
find /home/astra/.android/sdk -name 'aapt2*' -type f
```

