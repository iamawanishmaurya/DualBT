# UIAutomator MIUI Theme Config Missing During Physical Testing

- Date: 2026-05-25 00:15:20 IST
- Environment: Physical Android device `d1bc5c4a`, MIUI/Android shell, DualBT v0.2.24, ADB `uiautomator dump /dev/tty`

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
```

## Reproduction Steps

1. Connect physical device `d1bc5c4a`.
2. Launch DualBT v0.2.24.
3. Run `adb -s d1bc5c4a exec-out uiautomator dump /dev/tty`.

## First Hypothesis

The MIUI framework attempts to read an optional theme compatibility XML file while measuring the display for UIAutomator. The dump still returns usable XML, so this is likely a device/ROM instrumentation warning rather than a DualBT application defect.
