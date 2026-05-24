# Device Runtime Permission Grant Mismatch

- Timestamp: 2026-05-24 20:26:10 IST
- Environment: `/home/astra/codex/DualBT`, device `d1bc5c4a` (`Redmi Note 9 Pro`, API 31).

## Exact Error

```text
Exception occurred while executing 'grant':
java.lang.IllegalArgumentException: Unknown permission: android.permission.POST_NOTIFICATIONS
```

`dumpsys package com.xpwnit.dualbt` also reported:

```text
android.permission.BLUETOOTH_CONNECT: granted=true
android.permission.RECORD_AUDIO: granted=false
android.permission.BLUETOOTH_SCAN: granted=false
```

## Reproduction Steps

1. Install `app/build/outputs/apk/debug/app-debug.apk` on device `d1bc5c4a`.
2. Run `adb shell pm grant com.xpwnit.dualbt android.permission.POST_NOTIFICATIONS`.
3. Observe that API 31 does not know `POST_NOTIFICATIONS`.
4. Inspect runtime permissions with `adb shell dumpsys package com.xpwnit.dualbt`.
5. Observe that only `BLUETOOTH_CONNECT` is granted.

## First Hypothesis

`POST_NOTIFICATIONS` only exists on Android 13/API 33+, so it cannot be granted on this API 31 device. `RECORD_AUDIO` and `BLUETOOTH_SCAN` may require separate grant/app-op handling or user-facing permission flow verification on this MIUI device.
