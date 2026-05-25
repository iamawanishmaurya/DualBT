# ADB Stopservice Failed For Running Stream

## Exact error
Attempting to stop the running DualBT foreground streaming service with `adb shell am stopservice` failed.

```text
adb -s d1bc5c4a shell am stopservice -n com.xpwnit.dualbt/.service.DualBTService
Stopping service: Intent { cmp=com.xpwnit.dualbt/.service.DualBTService }
Error stopping service
```

## Reproduction steps
1. Start DualBT streaming through the MediaProjection flow.
2. Run `adb -s d1bc5c4a shell am stopservice -n com.xpwnit.dualbt/.service.DualBTService`.
3. Observe `Error stopping service`.
4. Tail DualBT logs and see capture still running.

## Environment
- Device: `d1bc5c4a`
- App package: `com.xpwnit.dualbt`
- App version: `0.2.34` / versionCode `35`
- Service: `com.xpwnit.dualbt/.service.DualBTService`
- State: foreground MediaProjection capture active

## First hypothesis
The service is foreground/capture-bound and not accepting the generic stopservice command path in this state. Use package force-stop or the in-app Stop Streaming button to stop the test service before patching.
