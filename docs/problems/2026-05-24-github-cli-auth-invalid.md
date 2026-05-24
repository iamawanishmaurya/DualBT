# GitHub CLI Auth Invalid

- Timestamp: 2026-05-24 16:44:26 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, sandbox `workspace-write`, current date 2026-05-24.

## Exact Error

```text
github.com
  X Failed to log in to github.com account iamawanishmaurya (default)
  - Active account: true
  - The token in default is invalid.
  - To re-authenticate, run: gh auth login -h github.com
  - To forget about this account, run: gh auth logout -h github.com -u iamawanishmaurya
```

## Reproduction Steps

1. Run `gh auth status` in `/home/astra/codex/DualBT`.
2. Observe that the active GitHub CLI account is `iamawanishmaurya`.
3. Observe the invalid-token error above.

## First Hypothesis

The stored GitHub CLI credential for `iamawanishmaurya` is expired or revoked. A successful push over HTTPS requires either a valid Git credential/token or a refreshed GitHub CLI authentication session.
