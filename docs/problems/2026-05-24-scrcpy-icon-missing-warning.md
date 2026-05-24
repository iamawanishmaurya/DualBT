# Scrcpy Icon Missing Warning

- Timestamp: 2026-05-24 20:37:55 IST
- Environment: `/home/astra/codex/DualBT`, host command `/home/astra/.local/bin/scrcpy`, device `d1bc5c4a`.

## Exact Error

```text
ERROR: Could not open icon image: /home/astra/.local/share/icons/hicolor/256x256/apps/scrcpy.png
WARN: Could not load icon
```

## Reproduction Steps

1. Run `/home/astra/.local/bin/scrcpy -s d1bc5c4a --no-audio --stay-awake --window-title DualBT-test --max-size=720 --time-limit=8`.
2. Observe the missing icon warning in host stderr.
3. The scrcpy session still connects, starts the controller thread, mirrors video, and exits cleanly at the time limit.

## First Hypothesis

The host scrcpy desktop icon asset is missing from the local installation. This does not affect Android control or mirroring because the session connects and the controller thread starts.

## Solution

Resolved by `docs/solutions/scrcpy-icon-missing-warning.md`.
