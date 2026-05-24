# Emulator KVM Unavailable

- Problem: [2026-05-24-emulator-kvm-unavailable.md](../problems/2026-05-24-emulator-kvm-unavailable.md)
- Follow-up blocker: [2026-05-24-emulator-adb-socket-blocked.md](../problems/2026-05-24-emulator-adb-socket-blocked.md)
- Timestamp: 2026-05-24 09:13:56 IST

## What Failed

The emulator could not use hardware acceleration because `/dev/kvm` is not available in the sandbox.

## What Worked

Launching with `-accel off` bypassed the immediate KVM requirement and allowed the emulator to initialize graphics and boot properties.

## Why It Worked

`-accel off` requests software emulation for the x86 image. This got past the KVM check, but runtime validation still could not complete because emulator-to-ADB socket communication is blocked.

## Commands Run

```bash
env ANDROID_AVD_HOME=/home/astra/codex/DualBT/.avd-tmp ANDROID_SDK_HOME=/tmp/dualbt-android-home /home/astra/.android/sdk/emulator/emulator -avd DualBT_Copy_API30 -no-window -no-audio -no-snapshot -no-metrics -accel off -gpu swiftshader_indirect
```

