# Keytool Distinguished Name Quoting

- Timestamp: 2026-05-24 08:55:03 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, Java keytool.

## Exact Error

```text
Illegal option:  Debug,O=Android,C=US
```

## Reproduction Steps

1. Run `keytool -genkeypair ... -dname CN=Android Debug,O=Android,C=US`.
2. Observe the shell splits the distinguished name at the space in `Android Debug`.
3. Observe keytool treats the remaining token as an illegal option.

## First Hypothesis

The distinguished name must be shell-quoted so keytool receives it as one argument.

