# Test 2 Direct A2DP No-Sound Resolution

Problem: `docs/problems/2026-05-25-test2-direct-a2dp-no-sound-v0232.md`

## What failed
DualBT v0.2.32 routed Test 2 to the selected A2DP endpoint and drained the calibration tone, but the user reported no audible sound at Android media volume `2/15`.

## What worked
The later v0.2.33 run added transient audio focus, kept the same direct A2DP endpoint, raised Android media volume to `9/15`, and produced an audible Test 2 beep on physical `Mini boost 2`.

## Why it worked
The original v0.2.32 route evidence showed the correct A2DP address, but the test did not match YouTube's effective media conditions. v0.2.33's focus request plus a verified `9/15` Android media volume matched the working YouTube route closely enough for the calibration beep to be audible.

## Commands run
```bash
adb -s d1bc5c4a shell cmd media_session volume --set 9
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a logcat -c
adb -s d1bc5c4a shell input tap 770 906
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 140 files/logs/dualbt.log
adb -s d1bc5c4a shell cmd media_session volume --get
adb -s d1bc5c4a shell dumpsys audio
```
