# Physical Mini Boost 2 Silent During Test

- Timestamp: 2026-05-24 23:40 IST
- Step: User-required physical verification loop after v0.2.21.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.21`, versionCode `22`
  - Requested test volume: about `15%`

## Exact Error

User feedback:

```text
only seaker mini boost 1 when seleted in test and click on test producce sound and the mini boost 2 does not produce any sound
```

Process failure:

```text
why did you not ask me for verifiation that both are working
```

## Reproduction Steps

1. Select `Mini boost 1` and `Mini boost 2`.
2. Keep Android media volume near 15%.
3. Press the app's speaker test buttons.
4. Ask the user which physical speaker produces sound.

## First Hypothesis

The app is only able to play through the single Bluetooth media route Android currently exposes. Mini boost 2 is selected in the app, but Android does not expose it as a direct media output route, so app-side playback cannot reach it through `AudioTrack.setPreferredDevice`. The next validation step must be a human-in-the-loop physical speaker test, not only log inspection.
