# v0.2.36 Router Syntax Error After Active Handoff Removal

## Exact Error

The v0.2.36 Android build failed during Java compilation:

```text
> Task :app:compileDebugJavaWithJavac FAILED
/home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/audio/AndroidAudioOutputRouter.java:525: error: class, interface, enum, or record expected
}
^
1 error
```

## Reproduction Steps

1. Add the streaming route capability gate.
2. Remove active A2DP handoff startup from `AndroidAudioOutputRouter`.
3. Run the offline Android debug build:

```bash
./gradlew --no-daemon --offline clean assembleDebug
```

## Environment

- Repo: `/home/astra/codex/DualBT`
- App version under test: `0.2.36` / versionCode `37`
- Gradle: wrapper `8.5`
- Android SDK: `/home/astra/.android/sdk`
- Build mode: offline debug build with local Maven cache and debug keystore

## First Hypothesis

The manual removal of the `ActivatedOutput` helper left a dangling `blocked()` method and extra brace at the end of `AndroidAudioOutputRouter.java`.
