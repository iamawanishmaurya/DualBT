# Debug Keystore Missing After Tmp Cleanup

## Exact error

```text
Execution failed for task ':app:validateSigningDebug'.
> Keystore file '/tmp/dualbt-debug.keystore' not found for signing config 'debug'.

BUILD FAILED in 3m 34s
```

## Reproduction steps

1. Run `./gradlew --no-daemon clean assembleDebug` with `DUALBT_DEBUG_KEYSTORE=/tmp/dualbt-debug.keystore`.
2. Observe `:app:validateSigningDebug` fails because the debug keystore was removed from `/tmp`.

## Environment

- Date: 2026-05-30
- Worktree: `/home/astra/codex/DualBT`
- Debug keystore path: `/tmp/dualbt-debug.keystore`
- Task: `:app:validateSigningDebug`

## First hypothesis

The previous debug keystore was temporary state under `/tmp` and was cleaned. Regenerate the debug keystore with the expected alias/password values, then rerun the build.
