# UIAutomator MIUI Theme Config Missing

- Timestamp: 2026-05-24 22:36:40 IST
- Step: Dump DualBT UI hierarchy for volume-control coordinates.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: `d1bc5c4a`
  - Tool: `adb shell uiautomator dump /sdcard/dualbt-ui.xml`
  - App: `com.xpwnit.dualbt` versionName `0.2.17`, versionCode `18`

## Exact Error

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
    at com.android.commands.uiautomator.Launcher.main(Launcher.java:83)
    at com.android.internal.os.RuntimeInit.nativeFinishInit(Native Method)
    at com.android.internal.os.RuntimeInit.main(RuntimeInit.java:363)
Caused by: android.system.ErrnoException: open failed: ENOENT (No such file or directory)
    at libcore.io.Linux.open(Native Method)
    at libcore.io.ForwardingOs.open(ForwardingOs.java:574)
    at libcore.io.BlockGuardOs.open(BlockGuardOs.java:274)
    at libcore.io.IoBridge.open(IoBridge.java:560)
    ... 20 more
UI hierchary dumped to: /sdcard/dualbt-ui.xml
```

## Reproduction Steps

1. Start DualBT on the MIUI physical device.
2. Run `adb -s d1bc5c4a shell uiautomator dump /sdcard/dualbt-ui.xml`.

## First Hypothesis

The MIUI framework attempts to load a missing system theme compatibility file while calculating display size. The dump command still writes `/sdcard/dualbt-ui.xml`, so this is likely a noisy MIUI tooling issue rather than a DualBT app failure.
