# Emulator ADB Socket Blocked

- Timestamp: 2026-05-24 09:13:56 IST
- Environment: `/home/astra/codex/DualBT`, copied writable AVD `.avd-tmp/DualBT_Copy_API30.avd`, Android emulator 36.5.11.0, sandbox with restricted networking.

## Exact Error

```text
ERROR        | Unable to connect to adb daemon on port: 5037
WARNING      | jdwp port creation fails, Icebox will not work.
ERROR        | It seems too many emulator instances are running on this machine. Aborting.
WARNING      | QEMU main loop exits abnormally with code 1
```

## Reproduction Steps

1. Launch the copied writable AVD with `-accel off`.
2. Observe emulator graphics and boot properties initialize.
3. Observe the emulator cannot connect to ADB on port 5037 and aborts.

## First Hypothesis

The sandbox's network/socket restrictions prevent emulator-to-ADB communication and JDWP port creation. Since ADB integration is required for install and launch validation, full emulator testing is blocked in this environment.

