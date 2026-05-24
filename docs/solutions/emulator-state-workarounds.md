# Emulator State Workarounds

- Problems:
  - [2026-05-24-emulator-snapshot-pending.md](../problems/2026-05-24-emulator-snapshot-pending.md)
  - [2026-05-24-emulator-lock-read-only.md](../problems/2026-05-24-emulator-lock-read-only.md)
  - [2026-05-24-emulator-userdata-space.md](../problems/2026-05-24-emulator-userdata-space.md)
- Timestamp: 2026-05-24 09:13:56 IST

## What Failed

Existing AVDs had stale snapshot locks in read-only directories, and a newly-created `/tmp` AVD did not have enough free space for its default userdata partition.

## What Worked

Copied the smaller existing API 30 AVD into project-local ignored writable storage at `.avd-tmp/`, removed the copied lock file, and forced cold boot in the copied config.

## Why It Worked

The copied AVD already had userdata images, fit in the workspace filesystem, and was writable, so stale lock and insufficient `/tmp` space were bypassed.

## Commands Run

```bash
cp -a /home/astra/.android/avd/Instagram_Fresh_API_30_x86.avd .avd-tmp/DualBT_Copy_API30.avd
rm .avd-tmp/DualBT_Copy_API30.avd/multiinstance.lock
env ANDROID_AVD_HOME=/home/astra/codex/DualBT/.avd-tmp ANDROID_SDK_HOME=/tmp/dualbt-android-home /home/astra/.android/sdk/emulator/emulator -avd DualBT_Copy_API30 -no-window -no-audio -no-snapshot -no-metrics -gpu swiftshader_indirect
```

