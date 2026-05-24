# Volume Controls Lost After Activity Recreate

## Exact Error

After starting DualBT streaming and switching to YouTube/notifications, reopening `MainActivity` shows:

```text
Volume -
100%
Volume +
Test 1 disabled
Test 2 disabled
```

The foreground service is still running, but the recreated activity has a fresh `StreamSessionController`, so `adjustOutputVolume()` does not send updates because `streamSession.isStreaming()` is false.

## Reproduction Steps

1. Install DualBT v0.2.16.
2. Select two Mini Boost speakers and start streaming.
3. Launch YouTube and open/collapse notifications.
4. Bring DualBT `MainActivity` back to the foreground.
5. Observe that the activity-local selected/streaming state is reset while the service is still active.

## Environment

- Device: Redmi Note 9 Pro, serial `d1bc5c4a`
- App: `com.xpwnit.dualbt` v0.2.16
- Active source: YouTube
- Active service: `DualBTService`

## First Hypothesis

The UI volume controls should not depend solely on activity-local streaming state. Always send volume updates from the foreground activity, and make the service stop itself if it receives a volume-only action while no stream is running.
