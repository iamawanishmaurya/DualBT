# Gradle Source Exclude Unresolved

- Problem: [2026-05-24-gradle-source-exclude-unresolved.md](../problems/2026-05-24-gradle-source-exclude-unresolved.md)
- Timestamp: 2026-05-24 09:27:29 IST

## What Failed

Using `java.exclude(...)` inside `android.sourceSets.getByName("main")` failed Kotlin DSL script compilation.

## What Worked

Removed the Android source set exclusion and applied the fallback Java class exclusions to `JavaCompile` tasks only when `DUALBT_PLANNED_STACK=true`.

## Why It Worked

`JavaCompile` exposes Gradle's standard exclude API for source file patterns, and the exclusion is only needed for the planned-stack build where Kotlin classes duplicate the Java fallback entry points.

## Commands Run

```bash
javac -cp app/src/main/java -d /tmp/dualbt-logger-test/classes /tmp/dualbt-logger-test/LogStoreTest.java app/src/main/java/com/xpwnit/dualbt/logging/LogStore.java
java -cp /tmp/dualbt-logger-test/classes LogStoreTest
./gradlew --no-daemon --offline clean assembleDebug
DUALBT_PLANNED_STACK=true ./gradlew --no-daemon --offline :app:tasks
```

