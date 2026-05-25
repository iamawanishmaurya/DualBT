# Test 2 9/15 Rerun Foreground Fix

Problem: `docs/problems/2026-05-25-test2-9-volume-tap-missed-youtube-foreground.md`

## What failed
The coordinate tap intended for DualBT's Test 2 button did not start a calibration run because YouTube was the focused foreground app.

## What worked
Pausing media, force-stopping YouTube, starting `com.xpwnit.dualbt/.MainActivity`, setting Android media volume to `9/15`, and verifying the UI before tapping Test 2.

## Why it worked
The tap coordinates are only valid when DualBT is the foreground surface. Once DualBT was focused, UIAutomator confirmed `Ready to stream`, `2/2`, `10%`, and enabled `Test 1`/`Test 2` controls before the retry.

## Commands run
```bash
adb -s d1bc5c4a shell input keyevent KEYCODE_MEDIA_PAUSE
adb -s d1bc5c4a shell am force-stop com.google.android.youtube
adb -s d1bc5c4a shell am start -n com.xpwnit.dualbt/.MainActivity
adb -s d1bc5c4a shell cmd media_session volume --set 9
adb -s d1bc5c4a shell cmd media_session volume --get
adb -s d1bc5c4a shell dumpsys window | rg -n "mCurrentFocus|mFocusedApp"
scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-ui-v0233-test2-9.xml
rg -n "Ready to stream|2/2|Test 1|Test 2|10%|100%|Volume" /tmp/dualbt-ui-v0233-test2-9.xml
```
