# Plain Java Test Source Omission

## Exact Error

```text
app/src/test/java/com/xpwnit/dualbt/ui/SystemBarAppearancePolicyTest.java:6: error: cannot find symbol
                SystemBarAppearancePolicy.shouldApplyLightSystemBars(30, false, false),
                ^
  symbol:   variable SystemBarAppearancePolicy
  location: class SystemBarAppearancePolicyTest
...
Error: Could not find or load main class com.xpwnit.dualbt.ui.SystemBarAppearancePolicyTest
Caused by: java.lang.ClassNotFoundException: com.xpwnit.dualbt.ui.SystemBarAppearancePolicyTest
exit=1
```

## Reproduction Steps

1. Add the v0.2.13 calibration tone test.
2. Compile the plain Java tests with a hand-written `javac` source list.
3. Omit `app/src/main/java/com/xpwnit/dualbt/ui/SystemBarAppearancePolicy.java`.
4. Observe `SystemBarAppearancePolicyTest` fail to compile.

## Environment

- Repository: `/home/astra/codex/DualBT`
- Toolchain: local `javac` / `java`
- Date: 2026-05-24

## First Hypothesis

The failure is not caused by the v0.2.13 code; it is a local test harness source-list omission. Include the missing policy class or replace the hand-written source list with a generated source list for plain Java-compatible files.
