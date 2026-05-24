# AAPT2 Maven Artifact Missing

- Timestamp: 2026-05-24 08:51:27 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Gradle 8.5 offline mode, Android Gradle Plugin 8.2.2.

## Exact Error

```text
Execution failed for task ':app:processDebugResources'.
> A failure occurred while executing com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask$TaskAction
   > Could not isolate value com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask$TaskWorkActionParameters_Decorated@6281dd71 of type LinkApplicationAndroidResourcesTask.TaskWorkActionParameters
      > Could not resolve all files for configuration ':app:detachedConfiguration2'.
         > Could not resolve com.android.tools.build:aapt2:8.2.2-10154469.
           Required by:
               project :app
            > No cached version of com.android.tools.build:aapt2:8.2.2-10154469 available for offline mode.
```

## Reproduction Steps

1. Seed `/tmp/dualbt-offline-maven` with missing AGP classpath artifacts.
2. Run `./gradlew --no-daemon --offline assembleDebug`.
3. Observe the build reaches `:app:processDebugResources`.
4. Observe resource processing fails because the Maven-packaged `aapt2` artifact is not cached.

## First Hypothesis

AGP 8.2.2 resolves `aapt2` through Maven even though the Android SDK also has build-tools binaries. The local offline Maven repository needs a compatible `com.android.tools.build:aapt2:8.2.2-10154469` artifact, or the project must use an AGP version whose `aapt2` artifact is already cached.

