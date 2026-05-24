# GitHub Direct HTTPS Connect Blocked

- Timestamp: 2026-05-24 17:10:22 IST
- Environment: `/home/astra/codex/DualBT`, shell `zsh`, sandbox `workspace-write`, current date 2026-05-24.

## Exact Error

```text
curl: (7) Failed to connect to github.com port 443 after 0 ms: Could not connect to server
```

```text
fatal: unable to access 'https://github.com/iamawanishmaurya/DualBT.git/': Failed to connect to github.com port 443 after 0 ms: Could not connect to server
```

## Reproduction Steps

1. Run `timeout 20 curl --connect-timeout 10 --max-time 20 -I --resolve github.com:443:140.82.112.4 https://github.com/`.
2. Observe curl fail immediately with code 7.
3. Run `timeout 20 env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git -c http.curloptResolve=github.com:443:140.82.112.4 ls-remote origin HEAD`.
4. Observe Git fail immediately with code 128.

## First Hypothesis

The environment blocks outbound HTTPS sockets to GitHub from shell commands. Since DNS was bypassed for both curl and Git, fixing only `/etc/resolv.conf` or Git host resolution is insufficient.
