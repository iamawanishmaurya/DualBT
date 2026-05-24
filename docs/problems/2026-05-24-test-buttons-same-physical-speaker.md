# Test Buttons Same Physical Speaker

- Timestamp: 2026-05-24 22:57 IST
- Step: Physical Mini Boost routing test after v0.2.18 volume fix.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.18`, versionCode `19`
  - Selected speakers: `Mini boost 1` and `Mini boost 2`

## Exact Error

User feedback:

```text
Still we are faccing issue that only one device is being play also volume work now keep for testing keep volme =20 % and fix the issue of one devie being played one time also when user press on test 1 same speaker beep as when user press test 2
```

App/device logs around the physical test:

```text
SpeakerTest Test 1 started for Mini boost 1, mode=media, preferred=Mini boost 4@41:42:26:B3:62:1C#type=8, preferredAccepted=true, routed=Mini boost 4@41:42:26:B3:62:1C#type=8
SpeakerTest Test 2 started for Mini boost 2, mode=communication-sco, preferred=Redmi Note 9 Pro@00:00:00:00:00:00#type=7, preferredAccepted=true, routed=Redmi Note 9 Pro@00:00:00:00:00:00#type=7
```

System audio evidence:

```text
APM Connected device (A2DP sink only):
  type:0x80 (bt_a2dp) addr:0x80:41:42:2E:9E:5E:AE
Active communication device: AudioDeviceAttributes: role:output type:bt_a2dp addr:41:42:26:B3:62:1C
```

## Reproduction Steps

1. Install and open DualBT v0.2.18 on `d1bc5c4a`.
2. Select `Mini boost 1` and `Mini boost 2`.
3. Press `Test 1`.
4. Press `Test 2`.
5. Observe from the user that the same physical speaker beeps for both tests.

## First Hypothesis

The current fallback is not routing to two physical Bluetooth speakers. Android exposes one usable A2DP media sink and a SCO/communication route whose address is generic or maps back to the same active Bluetooth audio path. The app needs to stop presenting this fallback as verified dual-speaker output and either use a device-supported multi-output path or surface a clear unsupported/diagnostic state.
