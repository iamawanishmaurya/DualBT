# Plain Java Verification Included Android Framework Classes

- Timestamp: 2026-05-24 23:22 IST
- Step: Fresh verification before committing v0.2.20.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Toolchain: local `javac`
  - Branch: `implementation/dualbt-v0.1.0`

## Exact Error

The broad verification compile included Android framework-dependent production files:

```text
app/src/main/java/com/xpwnit/dualbt/bt/AndroidBluetoothScanner.java:3: error: package android does not exist
import android.Manifest;
              ^
app/src/main/java/com/xpwnit/dualbt/audio/AndroidAudioOutputRouter.java:3: error: package android.content does not exist
import android.content.Context;
                      ^
app/src/main/java/com/xpwnit/dualbt/audio/SpeakerCalibrationPlayer.java:3: error: package android.content does not exist
import android.content.Context;
                      ^
100 errors
only showing the first 100 errors, of 274 total; use -Xmaxerrs if you would like to see more
```

## Reproduction Steps

1. From `/home/astra/codex/DualBT`, create `/tmp/dualbt-test-classes`.
2. Run `javac` over both `app/src/main/java` and `app/src/test/java`.
3. Observe that Android SDK classes are missing from the plain Java compiler classpath.

## First Hypothesis

This is a verification-command error, not a v0.2.20 source regression. The repo's plain Java tests should be compiled from the test source list with `-sourcepath app/src/main/java:app/src/test/java`, allowing `javac` to compile only the non-Android production classes referenced by those tests. Android framework-dependent files should be verified by Gradle/AGP instead.
