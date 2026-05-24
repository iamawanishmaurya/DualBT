# Emulator KVM Unavailable

- Timestamp: 2026-05-24 09:13:00 IST
- Environment: `/home/astra/codex/DualBT`, copied writable AVD `.avd-tmp/DualBT_Copy_API30.avd`, Android emulator 36.5.11.0.

## Exact Error

```text
ERROR        | x86 emulation currently requires hardware acceleration!
CPU acceleration status: /dev/kvm is not found: VT disabled in BIOS or KVM kernel module not loaded
```

## Reproduction Steps

1. Copy an existing API 30 x86 AVD into `.avd-tmp`.
2. Remove the stale lock from the copied AVD.
3. Launch with `ANDROID_AVD_HOME=/home/astra/codex/DualBT/.avd-tmp emulator -avd DualBT_Copy_API30 -no-window -no-audio -no-snapshot -no-metrics -gpu swiftshader_indirect`.
4. Observe emulator exits because `/dev/kvm` is unavailable.

## First Hypothesis

The current sandbox does not expose KVM hardware acceleration. If the installed x86 emulator refuses software acceleration, Android runtime validation cannot complete in this environment.

