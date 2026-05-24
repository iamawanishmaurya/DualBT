# Solution: Android Toybox Find Does Not Support GNU Printf

## Problem

Linked problem: [docs/problems/2026-05-24-android-find-printf-unsupported.md](../problems/2026-05-24-android-find-printf-unsupported.md)

## What Failed

The app-log inspection command used GNU `find -printf`, which is not portable to the Android shell implementation on the test device.

## What Worked

Use direct portable commands under `run-as`, for example:

```bash
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt ls -l files/logs
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt cat files/logs/dualbt.log
```

## Why It Worked

The replacement commands avoid GNU-only `find` formatting and rely on basic Android shell commands that are present on the physical device.

## Commands Run

```bash
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt find files -maxdepth 3 -type f -printf '%p %s\n'
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt ls -l files
```

