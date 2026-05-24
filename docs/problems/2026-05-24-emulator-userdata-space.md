# Emulator Userdata Space

- Timestamp: 2026-05-24 09:07:24 IST
- Environment: `/home/astra/codex/DualBT`, writable AVD home `/tmp/dualbt-avd`, AVD `DualBT_Test_API30`.

## Exact Error

```text
FATAL        | Not enough space to create userdata partition. Available: 2387.26 MB at /tmp/dualbt-avd/DualBT_Test_API30.avd, need 7372.80 MB.
```

## Reproduction Steps

1. Create an AVD with `ANDROID_AVD_HOME=/tmp/dualbt-avd`.
2. Launch it with `/home/astra/.android/sdk/emulator/emulator -avd DualBT_Test_API30 -no-window -no-audio -no-snapshot -gpu swiftshader_indirect`.
3. Observe the emulator exits because `/tmp` does not have enough free space for the userdata partition.

## First Hypothesis

The default Play Store image profile allocates a large userdata partition. A smaller profile or explicit smaller partition size is required in a writable location.

