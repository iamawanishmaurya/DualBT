# Gradle Source Exclude Unresolved

- Timestamp: 2026-05-24 09:24:20 IST
- Environment: `/home/astra/codex/DualBT`, Gradle 8.5, Android Gradle Plugin 8.2.2.

## Exact Error

```text
e: file:///home/astra/codex/DualBT/app/build.gradle.kts:62:22: Unresolved reference. None of the following candidates is applicable because of receiver type mismatch:
public fun Configuration.exclude(group: String? = ..., module: String? = ...): Configuration defined in org.gradle.kotlin.dsl
e: file:///home/astra/codex/DualBT/app/build.gradle.kts:70:22: Unresolved reference. None of the following candidates is applicable because of receiver type mismatch:
public fun Configuration.exclude(group: String? = ..., module: String? = ...): Configuration defined in org.gradle.kotlin.dsl
```

## Reproduction Steps

1. Add `java.exclude(...)` inside `android.sourceSets.getByName("main")`.
2. Run the default offline build.
3. Observe Kotlin DSL script compilation fails because that receiver does not expose the expected `exclude` method.

## First Hypothesis

The exclusion should be applied to `JavaCompile` tasks instead of the Android source set's `java` property.

