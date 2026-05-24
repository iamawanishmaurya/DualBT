# Plain Java Verification Android Classpath

- Problem: `docs/problems/2026-05-24-plain-java-verification-android-classpath.md`
- Solved: 2026-05-24 23:23 IST

## What Failed

The verification command asked local `javac` to compile all Java production files, including Android framework classes such as `AndroidAudioOutputRouter`, `SpeakerCalibrationPlayer`, and `AndroidBluetoothScanner`. Local `javac` does not have the Android framework classpath.

## What Worked

Generated a sorted list of test source files and compiled it with `-sourcepath app/src/main/java:app/src/test/java`. This lets `javac` resolve the plain-Java production classes referenced by the tests without compiling unrelated Android framework entry points.

## Why It Worked

The repo's fast unit checks are intentionally plain Java tests. Android framework integration is verified separately by the Gradle Android build, which supplies the Android SDK classpath.

## Commands Run

```bash
classes=/tmp/dualbt-plain-tests-v0220/classes
sources=/tmp/dualbt-plain-test-sources-v0220.txt
mkdir -p "$classes"
find "$classes" -mindepth 1 -delete
find app/src/test/java -name '*Test.java' | sort > "$sources"
javac -sourcepath app/src/main/java:app/src/test/java -d "$classes" @"$sources"
while IFS= read -r source; do
  class=${source#app/src/test/java/}
  class=${class%.java}
  class=${class//\//.}
  java -cp "$classes" "$class"
done < "$sources"
```

## Verification Result

The corrected plain Java verification command exited with code 0.
