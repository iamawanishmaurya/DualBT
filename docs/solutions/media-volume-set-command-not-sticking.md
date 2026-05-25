# Solution: Media Volume Set Command Did Not Stick

## Linked problem
- `docs/problems/2026-05-25-media-volume-set-command-not-sticking.md`

## What failed
Setting Android media volume to `2/15` while YouTube was still active did not hold; a later read returned `8/15`.

## What worked
Pausing/stopping the active YouTube media session, then setting media volume again, made the volume read back as `2/15`.

## Why it worked
The active YouTube/AVRCP media path was able to restore or retain a higher A2DP media volume after the initial command. Removing that active session allowed the command-line media volume set to apply to the current test environment.

## Commands run

```bash
adb -s d1bc5c4a shell input keyevent KEYCODE_MEDIA_PAUSE
adb -s d1bc5c4a shell am force-stop com.google.android.youtube
adb -s d1bc5c4a shell cmd media_session volume --set 2
sleep 1
adb -s d1bc5c4a shell cmd media_session volume --get
```

Verified output:

```text
[V] volume is 2 in range [0..15]
```
