# Solution: v0.2.35 Dual Tracks Routed To One A2DP Device

## Problem

Linked problem: `docs/problems/2026-05-25-v0235-dual-tracks-one-a2dp-route.md`

## What Failed

DualBT v0.2.35 started two media `AudioTrack`s after playback capture began, but Android routed both tracks to one public A2DP output route. The user heard YouTube audio on the speaker mapped to Test 2 instead of both speakers.

## Research Options Evaluated

1. **Android combined audio routing system APIs**
   - Android supports multi-device routing through audio policy system APIs such as `setPreferredDevicesForStrategy`.
   - Trade-off: These are privileged/system APIs and require vendor/HAL support. A normal Play-installed app cannot rely on them.

2. **Bluetooth LE Audio / Auracast**
   - Android's supported modern multi-listener path is LE Audio audio sharing/broadcasting with compatible phones and accessories.
   - Trade-off: The current Mini Boost speakers are BR/EDR classic Bluetooth devices, not LE Audio broadcast receivers, so this does not fix the current hardware.

3. **OEM dual-audio features**
   - Samsung exposes Dual Audio through its Media panel on supported Galaxy devices.
   - Trade-off: This is OEM-specific. The tested Redmi/MIUI device exposes a Xiaomi audio relay picker, but live probing showed it selects one active device, not both.

4. **Active A2DP handoff / hidden Bluetooth APIs**
   - The app can try to switch the active A2DP target and then create a track.
   - Trade-off: On this device the hidden active-device API is blocked by `BLUETOOTH_PRIVILEGED`, and the verified result was two app tracks sharing one routed A2DP device. This creates false success rather than simultaneous playback.

5. **Companion receiver or external dual-link hardware**
   - A second Android receiver, Wi-Fi synchronized audio endpoint, or external dual-link Bluetooth transmitter can make two physical speakers play at once.
   - Trade-off: This is a product architecture change or hardware requirement, not a safe in-app fix for one phone streaming to two classic A2DP sinks.

## Sources

- Android AOSP combined audio routing: `https://source.android.google.cn/docs/core/audio/combined-audio-routing?hl=en`
- Android Help for multiple Bluetooth audio accessories: `https://support.google.com/android/answer/16550669?hl=en-GB`
- Android `AudioRouting` API reference: `https://developer.android.com/reference/android/media/AudioRouting`
- Samsung Dual Audio support page: `https://www.samsung.com/us/support/answer/ANS10003436/`

## What Worked

DualBT v0.2.36 now blocks the known false-dual streaming path when Android exposes only one active classic A2DP route. It also adds an `Output` button that opens Xiaomi's audio output picker when available, or Android Bluetooth settings otherwise, so the tester can switch or inspect the platform route explicitly.

## Why It Worked

The fix stops treating active A2DP handoff as evidence of simultaneous streaming support. That handoff can verify each speaker one at a time, but it cannot keep two classic Bluetooth speakers playing together on this device. Blocking before MediaProjection prevents the app from capturing audio and silently sending both split buffers to the same routed speaker.

## Commands Run

```bash
adb -s d1bc5c4a shell dumpsys audio
adb -s d1bc5c4a shell dumpsys bluetooth_manager
adb -s d1bc5c4a shell settings list secure
adb -s d1bc5c4a shell settings list global
adb -s d1bc5c4a shell dumpsys package com.xiaomi.bluetooth
adb -s d1bc5c4a shell am start -a miui.bluetooth.mible.MiuiAudioRelayActivity
adb -s d1bc5c4a shell input tap 540 1394
adb -s d1bc5c4a shell input tap 540 2218
./gradlew --no-daemon --offline clean assembleDebug
adb -s d1bc5c4a install -r app/build/outputs/apk/debug/app-debug.apk
adb -s d1bc5c4a shell input tap 540 2218
adb -s d1bc5c4a shell dumpsys activity services com.xpwnit.dualbt
```

## Verification

- v0.2.36 installed with `versionName=0.2.36` and `versionCode=37`.
- Tapping `Start Stream` logs:

```text
Start blocked: Android exposes one active A2DP route at a time; active-device handoff cannot keep two classic Bluetooth speakers playing simultaneously.
```

- No Android `Start recording or casting with DualBT?` prompt appears.
- `dumpsys activity services com.xpwnit.dualbt` reports `(nothing)`.
- The app status shows `Dual route unavailable: 1/2 live routes`.
- The `Output` button opens Xiaomi's `Select device` picker.
