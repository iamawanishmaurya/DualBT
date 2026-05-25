# Test Volume Reset To Zero

- Timestamp: 2026-05-24 23:54 IST
- Step: Physical Mini Boost verification after v0.2.22 install.
- Environment:
  - Host: `/home/astra/codex/DualBT`
  - Device: Redmi Note 9 Pro, serial `d1bc5c4a`
  - App: `com.xpwnit.dualbt` versionName `0.2.22`, versionCode `23`

## Exact Error

The physical speaker test was supposed to keep Android media volume near 15%, but the live device reported muted media output:

```text
[V] volume is 0 in range [0..15]
```

## Reproduction Steps

1. Install and launch DualBT v0.2.22 on `d1bc5c4a`.
2. Set media volume to `2/15`.
3. Prepare the `Mini boost 1` and `Mini boost 2` physical test.
4. Re-check media volume with `adb -s d1bc5c4a shell cmd media_session volume --get`.

## First Hypothesis

The device media volume was changed outside DualBT during the physical test loop or by a previous route/volume command. Before every human-confirmed speaker test, the test harness must set and re-check media volume at `2/15` so physical feedback is meaningful.
