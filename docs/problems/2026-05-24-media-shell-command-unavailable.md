# Media Shell Command Unavailable

## Exact Error

```text
/system/bin/sh: media: inaccessible or not found
```

## Reproduction Steps

1. Prepare the v0.2.13 calibration test on physical device `d1bc5c4a`.
2. Try to inspect or set playback volume with `adb -s d1bc5c4a shell media volume --help`.
3. Observe that the `media` shell command is unavailable on this MIUI device.

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- Shell command: `adb shell media`
- Date: 2026-05-24

## First Hypothesis

MIUI does not expose the shorthand `media` binary to the shell. Use the available framework command `cmd media_session volume` for stream volume control.
