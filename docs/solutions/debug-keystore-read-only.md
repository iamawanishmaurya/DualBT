# Debug Keystore Read-Only

- Problem: [2026-05-24-debug-keystore-read-only.md](../problems/2026-05-24-debug-keystore-read-only.md)
- Timestamp: 2026-05-24 08:59:01 IST

## What Failed

AGP could not create the default debug keystore under `/home/astra/.android`.

## What Worked

Configured debug signing to use `/tmp/dualbt-debug.keystore` and generated that keystore manually.

## Why It Worked

`/tmp` is writable in this sandbox, and the debug signing config accepts an explicit keystore path.

## Commands Run

```bash
keytool -genkeypair -v -keystore /tmp/dualbt-debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Android Debug,O=Android,C=US'
```

