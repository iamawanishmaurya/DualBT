# Emulator Snapshot Pending

- Timestamp: 2026-05-24 09:03:22 IST
- Environment: `/home/astra/codex/DualBT`, Android emulator `/home/astra/.android/sdk/emulator/emulator`, AVD `Medium_Phone_API_36.1`.

## Exact Error

```text
FATAL        | A snapshot operation for 'Medium_Phone_API_36.1' is pending and timeout has expired. Exiting...
```

The launch output also included sandbox socket warnings:

```text
[2:2:20260524,090309.150334:ERROR socket.cc:45] setsockopt: Operation not permitted (1)
```

## Reproduction Steps

1. Run `/home/astra/.android/sdk/emulator/emulator -avd Medium_Phone_API_36.1 -no-window -no-audio -no-snapshot -gpu swiftshader_indirect`.
2. Observe emulator startup checks pass.
3. Observe launch exits with code 1 because a snapshot operation is pending.

## First Hypothesis

The AVD has stale snapshot state. A clean launch with `-wipe-data`, or a different AVD without pending snapshot state, should be tried next.

