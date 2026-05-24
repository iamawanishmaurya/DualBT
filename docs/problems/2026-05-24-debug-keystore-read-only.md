# Debug Keystore Read-Only

- Timestamp: 2026-05-24 08:54:00 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Android Gradle Plugin 8.2.2.

## Exact Error

```text
Execution failed for task ':app:validateSigningDebug'.
> java.io.IOException: Unable to create debug keystore in /home/astra/.android because it is not writable.
```

## Reproduction Steps

1. Set `android.aapt2FromMavenOverride=/home/astra/.android/sdk/build-tools/34.0.0/aapt2`.
2. Run `./gradlew --no-daemon --offline assembleDebug`.
3. Observe the build reaches `:app:validateSigningDebug`.
4. Observe debug keystore creation fails because `/home/astra/.android` is read-only.

## First Hypothesis

AGP's default debug signing config writes to `$HOME/.android/debug.keystore`. Configure the debug signing config to use a writable keystore path under `/tmp`.

