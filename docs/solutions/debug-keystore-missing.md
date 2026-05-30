# Debug Keystore Regeneration

Problem: [docs/problems/2026-05-30-debug-keystore-missing.md](../problems/2026-05-30-debug-keystore-missing.md)

## What failed

The Android debug signing config pointed at `/tmp/dualbt-debug.keystore`, but that temporary keystore no longer existed.

## What worked

Regenerated `/tmp/dualbt-debug.keystore` with `keytool`, then reran `assembleDebug`.

## Why it worked

The signing config expects alias `androiddebugkey` and password `android`. Regenerating the debug keystore at the configured path allowed `:app:validateSigningDebug` to pass and the APK to package successfully.

## Commands run

```bash
./gradlew --no-daemon clean assembleDebug
keytool -genkeypair -v -keystore /tmp/dualbt-debug.keystore -storepass android -keypass android -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Android Debug,O=Android,C=US'
./gradlew --no-daemon clean assembleDebug
```
