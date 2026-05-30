# Gradle Java Agent Path Missing Fix

Problem: [docs/problems/2026-05-30-gradle-javaagent-path-missing.md](../problems/2026-05-30-gradle-javaagent-path-missing.md)

## What failed

The offline build used an old `JAVA_OPTS` value with a hardcoded Gradle instrumentation agent jar under `/tmp/dualbt-gradle-home`. That jar was no longer present, so the JVM exited before Gradle could start.

## What worked

Removed the stale `-javaagent` option while keeping the required Java module-open flags and memory options.

## Why it worked

The JVM no longer tried to load a missing jar from `/tmp`, so Gradle could start. The next failures were unrelated missing `/tmp` build caches and signing state, which were handled in separate problem/solution notes.

## Commands run

```bash
./gradlew --no-daemon --offline clean assembleDebug
./gradlew --no-daemon clean assembleDebug
```
