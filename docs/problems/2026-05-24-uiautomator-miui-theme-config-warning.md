# UIAutomator MIUI Theme Config Warning

- Timestamp: 2026-05-24 20:32:43 IST
- Environment: `/home/astra/codex/DualBT`, device `d1bc5c4a` (`Redmi Note 9 Pro`, API 31 / MIUI), command `adb -s d1bc5c4a shell uiautomator dump`.

## Exact Error

The same stderr stack appeared on repeated UI dumps:

```text
java.io.FileNotFoundException: /data/system/theme_config/theme_compatibility.xml: open failed: ENOENT (No such file or directory)
	at libcore.io.IoBridge.open(IoBridge.java:574)
	at java.io.FileInputStream.<init>(FileInputStream.java:179)
	at java.io.FileInputStream.<init>(FileInputStream.java:133)
	at java.io.FileReader.<init>(FileReader.java:60)
	at miui.content.res.ThemeCompatibilityLoader.getVersion(ThemeCompatibilityLoader.java:108)
	at miui.content.res.ThemeCompatibilityLoader.getConfigDocumentTree(ThemeCompatibilityLoader.java:126)
	at miui.content.res.ThemeCompatibilityLoader.loadConfig(ThemeCompatibilityLoader.java:59)
	at miui.content.res.ThemeCompatibility.<clinit>(ThemeCompatibility.java:31)
	at android.content.res.MiuiResourcesImpl.<clinit>(MiuiResourcesImpl.java:41)
	at android.content.res.MiuiResources.<init>(MiuiResources.java:58)
	at android.content.res.IMiuiResourceImpl.createResources(IMiuiResourceImpl.java:13)
	at android.content.res.ThemeManagerStub.createMiuiResources(ThemeManagerStub.java:56)
	at android.content.res.Resources.getSystem(Resources.java:235)
	at android.util.MiuiMultiWindowAdapter.<clinit>(MiuiMultiWindowAdapter.java:84)
	at com.xiaomi.freeform.MiuiFreeformImpl.getSize(MiuiFreeformImpl.java:53)
	at android.util.MiuiFreeformUtils.getSize(MiuiFreeformUtils.java:49)
	at android.view.Display.getSize(Display.java:802)
	at com.android.commands.uiautomator.DumpCommand.run(DumpCommand.java:110)
```

## Reproduction Steps

1. Launch `com.xpwnit.dualbt/.MainActivity` on the Redmi Note 9 Pro.
2. Run `adb -s d1bc5c4a shell uiautomator dump /sdcard/dualbt-after-fix.xml`.
3. Run `adb -s d1bc5c4a shell uiautomator dump /sdcard/dualbt-selected.xml`.
4. Observe the same MIUI theme-config `FileNotFoundException` stack each time.

## First Hypothesis

This is MIUI framework noise from the device-side `uiautomator` process, not a DualBT app crash. The XML file is still created and can be pulled, but repeated stderr makes automated UI validation noisy. Per the repeated-error rule, research 3-5 fixes before changing the validation path.

## Solution

Resolved by `docs/solutions/uiautomator-miui-theme-config-warning.md`.
