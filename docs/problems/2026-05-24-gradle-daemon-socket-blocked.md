# Gradle Daemon Socket Blocked

- Timestamp: 2026-05-24 08:14:26 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Java 21.0.11, Gradle 8.5, `GRADLE_USER_HOME=/tmp/dualbt-gradle-home`.

## Exact Error

```text
FAILURE: Build failed with an exception.

* What went wrong:
Unable to start the daemon process.
This problem might be caused by incorrect configuration of the daemon.
For example, an unrecognized jvm option is used.For more details on the daemon, please refer to https://docs.gradle.org/8.5/userguide/gradle_daemon.html in the Gradle documentation.
Process command line: /usr/lib/jvm/java-21-openjdk/bin/java --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.prefs/java.util.prefs=ALL-UNNAMED --add-opens=java.base/java.nio.charset=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED -Xmx4096m -Dfile.encoding=UTF-8 -Duser.country -Duser.language=en -Duser.variant -cp /tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/gradle-launcher-8.5.jar -javaagent:/tmp/dualbt-gradle-home/wrapper/dists/gradle-8.5-bin/5t9huq95ubn472n8rpzujfbqh/gradle-8.5/lib/agents/gradle-instrumentation-agent-8.5.jar org.gradle.launcher.daemon.bootstrap.GradleDaemon 8.5
Please read the following process output to find out more:
-----------------------

FAILURE: Build failed with an exception.

* What went wrong:
java.net.SocketException: Operation not permitted
```

## Reproduction Steps

1. Set `GRADLE_USER_HOME=/tmp/dualbt-gradle-home`.
2. Set Android SDK environment variables.
3. Run `./gradlew --no-daemon assembleDebug` while `org.gradle.jvmargs` is configured in `gradle.properties`.
4. Observe Gradle forks a single-use daemon and fails because socket creation is not permitted.

## First Hypothesis

Even with `--no-daemon`, Gradle forks a single-use daemon when project JVM args require a different process. The sandbox blocks daemon TCP server sockets, so the next strategy should remove project-level JVM args and keep `org.gradle.daemon=false`.

