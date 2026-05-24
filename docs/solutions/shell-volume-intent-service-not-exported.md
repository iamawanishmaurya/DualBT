# Shell Volume Intent Service Not Exported

- Problem: [docs/problems/2026-05-24-shell-volume-intent-service-not-exported.md](../problems/2026-05-24-shell-volume-intent-service-not-exported.md)
- Solved: 2026-05-24 22:48 IST

## What Failed

ADB shell could not call `DualBTService` with a volume intent because the service is not exported. Android rejected the call with `Requires permission not exported from uid 10531`.

## What Worked

The service remained non-exported. Volume control was implemented through trusted in-app entry points:

- Foreground activity controls send internal service intents.
- Foreground notification actions send internal `PendingIntent`s.
- v0.2.18 observes Android media-volume changes so hardware volume keys control DualBT output without external callers invoking the service.

## Why It Worked

Internal app components and service-created `PendingIntent`s can reach the non-exported service without opening a public control surface. Hardware media volume changes are observed by the running service instead of being sent as shell intents.

## Commands Run

```bash
adb -s d1bc5c4a shell am startservice -n com.xpwnit.dualbt/.service.DualBTService -a com.xpwnit.dualbt.SET_OUTPUT_VOLUME --ei output_volume_percent 40
adb -s d1bc5c4a shell input keyevent KEYCODE_VOLUME_DOWN
adb -s d1bc5c4a shell input keyevent KEYCODE_VOLUME_UP
adb -s d1bc5c4a shell run-as com.xpwnit.dualbt tail -n 60 files/logs/dualbt.log
```
