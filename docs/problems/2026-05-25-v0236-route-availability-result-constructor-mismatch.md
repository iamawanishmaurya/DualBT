# v0.2.36 Route Availability Result Constructor Mismatch

## Exact Error

The v0.2.36 Android build failed after the router syntax fix:

```text
> Task :app:compileDebugJavaWithJavac
/home/astra/codex/DualBT/app/src/main/java/com/xpwnit/dualbt/audio/AndroidAudioRouteAvailability.java:19: error: constructor Result in class Result cannot be applied to given types;
            return new Result(false, 0, "Android audio context is unavailable");
                   ^
  required: boolean,int,String,String
  found:    boolean,int,String
  reason: actual and formal argument lists differ in length
```

The same constructor mismatch appeared at lines 22 and 26.

## Reproduction Steps

1. Add `statusMessage` to `AndroidAudioRouteAvailability.Result`.
2. Leave the early-return constructor calls unchanged.
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

The new `Result` constructor requires a fourth `statusMessage` argument, but the invalid-context early returns were not updated.
