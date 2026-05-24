# Gradle Wrapper Cache Read-Only

- Timestamp: 2026-05-24 08:10:02 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Java 21.0.11, Android SDK `/home/astra/.android/sdk`.

## Exact Error

```text
Exception in thread "main" java.io.FileNotFoundException: /home/astra/.gradle/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5-bin.zip.lck (Read-only file system)
	at java.base/java.io.RandomAccessFile.open0(Native Method)
	at java.base/java.io.RandomAccessFile.open(RandomAccessFile.java:356)
	at java.base/java.io.RandomAccessFile.<init>(RandomAccessFile.java:273)
	at java.base/java.io.RandomAccessFile.<init>(RandomAccessFile.java:223)
	at org.gradle.wrapper.GradleWrapperMain.main(SourceFile:67)
```

## Reproduction Steps

1. Run `./gradlew --version` from `/home/astra/codex/DualBT`.
2. Observe Gradle wrapper tries to create a lock file under `/home/astra/.gradle/wrapper/dists/...`.
3. Observe the command exits with code 1 because that location is read-only.

## First Hypothesis

The default Gradle user home is not writable in this environment. Set `GRADLE_USER_HOME` to a writable path such as `/tmp/dualbt-gradle-home` for all Gradle commands.

