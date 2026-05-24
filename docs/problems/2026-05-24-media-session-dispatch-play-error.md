# Media Session Dispatch Play Error

## Exact Error

```text
java.lang.IllegalArgumentException: packageName may not be empty
usage: media_session [subcommand] [options]
       media_session dispatch KEY
...
```

## Reproduction Steps

1. Launch a YouTube URL on physical device `d1bc5c4a`.
2. Run `adb -s d1bc5c4a shell cmd media_session dispatch play`.
3. Observe the `packageName may not be empty` error from the media session command.

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- Foreground app: `com.google.android.youtube`
- Command service: `cmd media_session`
- Date: 2026-05-24

## First Hypothesis

This MIUI command path requires package context for media-session dispatch or has a device-specific shell bug. Use direct app launch, UI/keyevent playback, or YouTube's visible session state instead of relying on `cmd media_session dispatch`.
