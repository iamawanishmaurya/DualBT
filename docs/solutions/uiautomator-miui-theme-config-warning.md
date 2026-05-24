# UIAutomator MIUI Theme Config Warning Solution

- Timestamp: 2026-05-24 20:35:12 IST
- Problem: `docs/problems/2026-05-24-uiautomator-miui-theme-config-warning.md`

## What Failed

Repeated `adb shell uiautomator dump` calls on the Redmi Note 9 Pro printed a MIUI framework stack for missing `/data/system/theme_config/theme_compatibility.xml`. The command still created the XML hierarchy, but raw stderr made the validation signal noisy.

## Research

Possible fixes considered:

1. Keep the normal `uiautomator dump` workflow, pull the XML, and validate the hierarchy content. This matches the common `/sdcard/window_dump.xml` dump-and-pull approach described by Stack Overflow and UI dump tooling references: https://stackoverflow.com/questions/39223464/where-to-see-the-xml-file-after-adb-shell-uiautomator-dump-view-xml/44427917 and https://help.duoplus.net/docs/uiautomator-dump.
2. Stream XML with `adb exec-out uiautomator dump /dev/tty`. This avoids a separate pull, but still invokes the same device-side MIUI `uiautomator` process, so it is unlikely to remove the MIUI theme warning.
3. Move UI hierarchy collection to Appium UiAutomator2. Appium documents UiAutomator2 as its Android automation driver, but it requires a server, dependencies, and session setup: https://appium.github.io/appium.io/docs/en/drivers/android-uiautomator2/.
4. Use a UiAutomator2/Appium page-source path for dynamic screens. This can solve some native `uiautomator dump` limitations, but it is heavier than needed for this simple validation: https://stackoverflow.com/questions/51799277/how-to-take-ui-automator-dump-for-dynamic-screens.
5. Attempt to repair or replace the MIUI theme subsystem/file on the phone. This is risky, device-specific, and outside the app test scope because the DualBT app is not the source of the missing MIUI file.

## What Worked

Added `scripts/dump-ui-safe.sh`, a small validation wrapper that:

- runs `adb -s <serial> shell uiautomator dump`;
- pulls the generated XML file;
- fails only if ADB fails or the pulled XML is missing/invalid;
- emits a single warning when MIUI prints the known `theme_compatibility.xml` noise.

Added `scripts/test-dump-ui-safe.sh` with a fake ADB command that reproduces the warning while returning valid XML. The test now passes.

## Why It Worked

The failure mode is not missing hierarchy data; it is noisy stderr from Xiaomi's framework during hierarchy generation. Validating the XML file keeps the useful signal and avoids treating OEM stderr noise as an app failure.

## Commands Run

```bash
scripts/test-dump-ui-safe.sh
bash -n scripts/dump-ui-safe.sh scripts/test-dump-ui-safe.sh
```
