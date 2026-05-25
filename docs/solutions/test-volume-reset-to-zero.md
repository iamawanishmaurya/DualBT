# Test Volume Reset To Zero

- Problem: `docs/problems/2026-05-24-test-volume-reset-to-zero.md`
- Solved: 2026-05-24 23:54 IST

## What Failed

The physical Mini Boost verification loop was about to continue while Android media volume was `0/15`, which would make any no-sound report meaningless.

## What Worked

Resetting the device media volume immediately before the next test and verifying it with `cmd media_session volume --get` restored the test level to `2/15`.

## Why It Worked

The problem was not app code; it was a stale device media-volume state. The test loop now has to check volume before every speaker tone so user feedback maps to route behavior instead of a muted device.

## Commands Run

```bash
adb -s d1bc5c4a shell cmd media_session volume --get
adb -s d1bc5c4a shell cmd media_session volume --set 2
adb -s d1bc5c4a shell cmd media_session volume --get
```
