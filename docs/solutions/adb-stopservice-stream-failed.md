# ADB Stopservice Stream Failure Resolution

Problem: `docs/problems/2026-05-25-adb-stopservice-stream-failed.md`

## What failed
`adb shell am stopservice` returned `Error stopping service` while DualBT's foreground MediaProjection stream was active.

## What worked
Force-stopping the package stopped the foreground service and removed the app process before router patching continued.

## Why it worked
`am force-stop` terminates the package process and all services, so it bypassed the foreground-service stop path that rejected the direct `stopservice` command.

## Commands run
```bash
adb -s d1bc5c4a shell am stopservice -n com.xpwnit.dualbt/.service.DualBTService
adb -s d1bc5c4a shell am force-stop com.xpwnit.dualbt
adb -s d1bc5c4a shell dumpsys activity services com.xpwnit.dualbt
adb -s d1bc5c4a shell pidof com.xpwnit.dualbt
```
