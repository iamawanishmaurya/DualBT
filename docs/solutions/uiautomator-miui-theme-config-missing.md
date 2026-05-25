# UIAutomator MIUI Theme Config Missing

- Problems:
  - [docs/problems/2026-05-24-uiautomator-miui-theme-config-missing.md](../problems/2026-05-24-uiautomator-miui-theme-config-missing.md)
  - [docs/problems/2026-05-25-uiautomator-miui-theme-config-missing.md](../problems/2026-05-25-uiautomator-miui-theme-config-missing.md)
- Solved: 2026-05-25 00:16 IST

## What Failed

`uiautomator dump` printed a MIUI framework stack trace because `/data/system/theme_config/theme_compatibility.xml` was missing. The command still wrote a valid hierarchy XML, so the app was not failing, but the test tool output was noisy and repeated during physical speaker testing.

## Research

Possible fixes considered after the warning repeated:

1. Keep using `adb exec-out uiautomator dump /dev/tty` and manually ignore the stack trace. This is fast but keeps mixing noisy stderr with the XML stream.
2. Dump to an on-device file such as `/sdcard/window_dump.xml`, pull the XML, and validate it before use. This matches common UIAutomator dump workflows documented in Android UI dump references and avoids treating MIUI stderr as the app result.
3. Use screenshots and fixed coordinates for this known DualBT flow. This avoids UIAutomator entirely, but gives less structured state evidence.
4. Move to Appium or UiAutomator2 page-source collection. This is heavier than needed and adds a server/session dependency for a simple physical smoke test.
5. Repair or recreate the missing MIUI theme file on the phone. This is unsafe and out of scope because the missing file is owned by the ROM, not DualBT.

## What Worked

Use `scripts/dump-ui-safe.sh` for hierarchy evidence when needed, and fixed ADB coordinates for the known DualBT controls. The wrapper writes the hierarchy to `/sdcard`, pulls it to a local file, validates `<hierarchy>`, and emits a single warning if MIUI produced the known `theme_compatibility.xml` noise.

## Why It Worked

The missing file is inside MIUI's system theme compatibility loader, outside DualBT. The safe dump path preserves the valid hierarchy output while treating the ROM stack trace as tool noise instead of a DualBT failure. Fixed coordinates keep the physical test moving without repeatedly triggering the same noisy command.

## Commands Run

```bash
adb -s d1bc5c4a shell uiautomator dump /sdcard/dualbt-ui.xml
adb -s d1bc5c4a exec-out screencap -p > /tmp/dualbt-v0218-launch.png
adb -s d1bc5c4a exec-out screencap -p > /tmp/dualbt-v0218-scrolled.png
adb -s d1bc5c4a exec-out screencap -p > /tmp/dualbt-v0218-two-selected.png
adb -s d1bc5c4a exec-out screencap -p > /tmp/dualbt-v0218-projection.png
scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-v0224-two-selected.xml
rg -n "Mini boost 1|Mini boost 2|Selected|Start Stream" /tmp/dualbt-v0224-two-selected.xml
```
