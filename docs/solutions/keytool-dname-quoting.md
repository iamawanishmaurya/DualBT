# Keytool Distinguished Name Quoting

- Problem: [2026-05-24-keytool-dname-quoting.md](../problems/2026-05-24-keytool-dname-quoting.md)
- Timestamp: 2026-05-24 08:59:01 IST

## What Failed

The first `keytool` command passed an unquoted distinguished name containing a space.

## What Worked

Quoted the full distinguished name argument.

## Why It Worked

Shell quoting preserved the distinguished name as one argument for keytool.

## Commands Run

```bash
keytool -genkeypair -v -keystore /tmp/dualbt-debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=Android Debug,O=Android,C=US'
```

