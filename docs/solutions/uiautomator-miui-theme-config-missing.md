# UIAutomator MIUI Theme Config Missing

- Problem: [docs/problems/2026-05-24-uiautomator-miui-theme-config-missing.md](../problems/2026-05-24-uiautomator-miui-theme-config-missing.md)
- Solved: 2026-05-24 22:43 IST

## What Failed

`uiautomator dump` printed a MIUI framework stack trace because `/data/system/theme_config/theme_compatibility.xml` was missing. The command still wrote `/sdcard/dualbt-ui.xml`, so the app was not failing, but the test tool output was noisy.

## What Worked

For the remaining physical-device UI driving, screenshots from `adb exec-out screencap -p` were used instead of another UIAutomator dump. This avoided repeating the MIUI tooling stack trace and gave visible coordinate evidence for selecting `Mini boost 1`, `Mini boost 2`, and starting the stream.

## Why It Worked

The missing file is inside MIUI's system theme compatibility loader, outside DualBT. Screenshots exercise the device display path without loading the same UIAutomator display-size code path that printed the warning.

## Commands Run

```bash
adb -s d1bc5c4a shell uiautomator dump /sdcard/dualbt-ui.xml
adb -s d1bc5c4a exec-out screencap -p > /tmp/dualbt-v0218-launch.png
adb -s d1bc5c4a exec-out screencap -p > /tmp/dualbt-v0218-scrolled.png
adb -s d1bc5c4a exec-out screencap -p > /tmp/dualbt-v0218-two-selected.png
adb -s d1bc5c4a exec-out screencap -p > /tmp/dualbt-v0218-projection.png
```
