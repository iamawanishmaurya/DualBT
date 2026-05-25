# Test 2 v0.2.33 Low-Volume No-Sound Resolution

Problem: `docs/problems/2026-05-25-test2-focus-route-no-sound-at-2-v0233.md`

## What failed
DualBT v0.2.33 generated and drained the Test 2 calibration tone at Android media volume `2/15`, but the user could not hear it on the physical speaker.

## What worked
Keeping DualBT in the foreground, keeping in-app gain at `10%`, raising Android media volume to `9/15`, and rerunning Test 2 produced an audible beep on physical `Mini boost 2`.

## Why it worked
The app route and audio-focus path were valid, but `2/15` was below the practical audible threshold for this speaker/test environment. At `9/15`, Android still routed the AudioTrack to A2DP address `41:42:2E:9E:5E:AE`, the app received transient audio focus, and the user confirmed the intended speaker played.

## Commands run
```bash
adb -s d1bc5c4a shell input keyevent KEYCODE_MEDIA_PAUSE
adb -s d1bc5c4a shell am force-stop com.google.android.youtube
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a shell cmd media_session volume --set 9
scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-ui-v0233-test2-9.xml
adb -s d1bc5c4a logcat -c
adb -s d1bc5c4a shell input tap 770 906
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 140 files/logs/dualbt.log
adb -s d1bc5c4a shell cmd media_session volume --get
adb -s d1bc5c4a shell dumpsys audio
```
