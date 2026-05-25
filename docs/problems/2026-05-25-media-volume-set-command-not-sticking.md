# Media Volume Set Command Did Not Stick Before v0.2.33 Physical Test

## Exact error
The test setup attempted to set Android media volume to `2/15`, but a later volume read returned `8/15`.

```text
adb -s d1bc5c4a shell cmd media_session volume --set 2
[V] will set volume to index=2
[V] Connecting to AudioService

adb -s d1bc5c4a shell cmd media_session volume --get
[V] volume is 8 in range [0..15]
```

## Reproduction steps
1. User plays local YouTube audio to warm the Test 2 route.
2. Install and launch DualBT v0.2.33.
3. Run `adb -s d1bc5c4a shell cmd media_session volume --set 2`.
4. Dump UI and read media volume.
5. Media volume reports `8/15`.

## Environment
- Device: `d1bc5c4a`
- App version under test: DualBT v0.2.33 / versionCode `34`
- Active external player before install: YouTube
- Connected A2DP route: Mini boost address `41:42:2E:9E:5E:AE`

## First hypothesis
An active YouTube media session or Bluetooth AVRCP absolute-volume state is restoring the A2DP media volume after the command-line set attempt. The isolated app test should pause/stop external media, then set volume again and verify before playing the calibration tone.
