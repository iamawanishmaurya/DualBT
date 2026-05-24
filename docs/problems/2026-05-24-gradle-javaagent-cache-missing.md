# Gradle Javaagent Cache Missing

- Timestamp: 2026-05-24 18:51:00 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, device `d1bc5c4a` connected.

## Exact Error

```text
Error opening zip file or JAR manifest missing : /tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar
Error occurred during initialization of VM
agent library failed Agent_OnLoad: instrument
```

## Reproduction Steps

1. Run the known offline Gradle build command with `JAVA_OPTS` containing `-javaagent:/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar`.
2. Observe the JVM exits before Gradle starts because the Java agent JAR is missing.

## First Hypothesis

The previous `/tmp/dualbt-gradle-home` cache was refreshed or removed, so the hard-coded Gradle instrumentation agent path is stale. The build command should either recreate the Gradle cache or avoid pinning a missing Java agent.
