# Device Runtime Permission Grant Mismatch

- Problem: [2026-05-24-device-runtime-permission-grant-mismatch.md](../problems/2026-05-24-device-runtime-permission-grant-mismatch.md)
- Timestamp: 2026-05-24 20:27:20 IST

## What Failed

Granting all permissions in a single command sequence produced one visible `POST_NOTIFICATIONS` error and left `RECORD_AUDIO`/`BLUETOOTH_SCAN` unverified. `POST_NOTIFICATIONS` does not exist on the API 31 test device.

## What Worked

Granting the API 31 runtime permissions individually worked for:

- `android.permission.RECORD_AUDIO`
- `android.permission.BLUETOOTH_SCAN`
- `android.permission.BLUETOOTH_CONNECT`

## Why It Worked

The API 31 device supports Bluetooth and audio runtime permissions, but not Android 13's notification runtime permission. Running individual `pm grant` commands made each permission result explicit.

## Commands Run

```bash
adb -s d1bc5c4a shell pm grant com.xpwnit.dualbt android.permission.RECORD_AUDIO
adb -s d1bc5c4a shell pm grant com.xpwnit.dualbt android.permission.BLUETOOTH_SCAN
adb -s d1bc5c4a shell pm grant com.xpwnit.dualbt android.permission.BLUETOOTH_CONNECT
adb -s d1bc5c4a shell dumpsys package com.xpwnit.dualbt
adb -s d1bc5c4a shell appops get com.xpwnit.dualbt
```
