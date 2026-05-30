# Gradle Java Agent Path Missing After Tmp Cleanup

## Exact error

```text
Error opening zip file or JAR manifest missing : /tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar
Error occurred during initialization of VM
agent library failed Agent_OnLoad: instrument
```

## Reproduction steps

1. Run the offline Android debug build with `JAVA_OPTS` containing a hardcoded `-javaagent:/tmp/dualbt-gradle-home/.../gradle-instrumentation-agent-8.5.jar`.
2. Observe the JVM fails before Gradle starts because the jar is missing.

## Environment

- Date: 2026-05-30
- Worktree: `/home/astra/codex/DualBT`
- Gradle user home: `/tmp/dualbt-gradle-home`
- Command: `./gradlew --no-daemon --offline clean assembleDebug`

## First hypothesis

The Gradle distribution under `/tmp/dualbt-gradle-home` was cleaned or recreated, invalidating the previous hardcoded `-javaagent` path. The build should be rerun with the same module-open options but without the stale `-javaagent` entry.
