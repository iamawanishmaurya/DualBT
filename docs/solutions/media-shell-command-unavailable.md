# Media Shell Command Unavailable Solution

## Linked Problem

- `docs/problems/2026-05-24-media-shell-command-unavailable.md`

## What Failed

The MIUI device returned `/system/bin/sh: media: inaccessible or not found` for the shorthand `adb shell media` command.

## What Worked

The framework-backed media session command is available:

```bash
adb -s d1bc5c4a shell cmd media_session volume --stream 3 --set 12
adb -s d1bc5c4a shell cmd media_session volume --stream 0 --set 10
```

## Why It Worked

`cmd media_session` is exposed through Android's command service even when the convenience `media` binary is missing or blocked.

## Commands Run

```bash
adb -s d1bc5c4a shell media volume --help
adb -s d1bc5c4a shell cmd media_session help
```
