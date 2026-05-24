# Steps Log Context Mismatch

## Exact Error

```text
apply_patch verification failed: Failed to find expected lines in /home/astra/codex/DualBT/docs/steps.md:
## 2026-05-24 21:57:12 IST - Start v0.2.12 Dual Route Stream

- Action: Started streaming in DualBT after selecting Mini boost 1 and Mini boost 2, accepted the MediaProjection prompt, and inspected the app log plus audio playback configuration.
- Result: Streaming is active; DualBT created two output tracks with distinct Android device IDs, using media A2DP for Mini boost 1 and communication-SCO fallback for Mini boost 2.
```

## Reproduction Steps

1. Resume the v0.2.12 physical playback test after context handoff.
2. Try to insert a new checkpoint in `docs/steps.md` using an expected block copied from the handoff summary.
3. Observe `apply_patch` fail because the actual file uses different wording for the same step.

## Environment

- Repository: `/home/astra/codex/DualBT`
- Date: 2026-05-24
- Tool: `apply_patch`
- Git work tree: `/home/astra/codex/DualBT`

## First Hypothesis

The handoff summary paraphrased the `docs/steps.md` entry, while the real file has the canonical text. Append the new checkpoint at the end of the file instead of matching the paraphrased block.
