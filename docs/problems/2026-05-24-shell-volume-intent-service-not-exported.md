# Shell Volume Intent Service Not Exported

## Exact Error

```text
Starting service: Intent { act=com.xpwnit.dualbt.SET_OUTPUT_VOLUME cmp=com.xpwnit.dualbt/.service.DualBTService (has extras) }
Error: Requires permission not exported from uid 10531
```

## Reproduction Steps

1. Start DualBT v0.2.15 streaming.
2. Launch YouTube playback.
3. Try to update DualBT output volume directly from ADB:

```bash
adb -s d1bc5c4a shell am startservice -n com.xpwnit.dualbt/.service.DualBTService -a com.xpwnit.dualbt.SET_OUTPUT_VOLUME --ei output_volume_percent 40
```

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- App: `com.xpwnit.dualbt` v0.2.15
- Caller: Android shell UID
- Target service: `DualBTService`

## First Hypothesis

The service is intentionally not exported, so shell callers cannot send internal control intents. Verify the volume path through the in-app `Volume -` and `Volume +` controls instead of weakening the service export boundary.
