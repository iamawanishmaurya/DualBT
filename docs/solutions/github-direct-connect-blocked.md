# GitHub Direct HTTPS Connect Blocked

- Problem: [2026-05-24-github-direct-connect-blocked.md](../problems/2026-05-24-github-direct-connect-blocked.md)
- Timestamp: 2026-05-24 17:10:22 IST
- Status: Environment-blocked, not fully solved inside this sandbox.

## What Failed

Bypassing local DNS with curl `--resolve` and Git `http.curloptResolve` still failed to open an HTTPS connection to GitHub on port 443.

## Researched Options

1. Restore DNS/network access to `github.com` and retry plain `git push`.
   - Trade-off: Correct durable fix, but requires environment/network policy changes outside the writable workspace.
2. Configure the required corporate or host proxy with `HTTPS_PROXY`/Git `http.proxy`.
   - Trade-off: Works if a proxy exists, but no proxy variables are configured in this shell.
3. Change system resolver settings such as `/etc/resolv.conf`.
   - Trade-off: Can fix DNS-only failures, but this test proves direct HTTPS connection is also blocked and `/etc` is outside the writable workspace.
4. Use SSH transport instead of HTTPS.
   - Trade-off: Can avoid HTTPS credential issues, but still requires outbound network access and DNS or host-key/IP configuration.
5. Use Git `http.curloptResolve` to bypass DNS for GitHub.
   - Trade-off: Workspace-safe and does not require root, but it failed because outbound HTTPS to GitHub is blocked.

## What Worked

Local branch, tag, and commit operations continue to work through `GIT_DIR=/tmp/DualBT.git` and `GIT_WORK_TREE=/home/astra/codex/DualBT`.

## Why It Worked

Local Git operations do not require outbound sockets. Remote GitHub publishing requires an allowed TCP connection to GitHub plus valid credentials.

## Decision

Do not keep retrying `git push` in this sandbox. The most efficient fix is to restore outbound network access or run the documented plain Git push commands from a host where GitHub HTTPS works. If a proxy is required, configure `HTTPS_PROXY` and Git `http.proxy` first, then push the branch and tags.

The repo now includes `scripts/push-github.sh` as the plain-Git publish path. It uses `/tmp/DualBT.git` and `/home/astra/codex/DualBT` by default, checks DNS and HTTPS reachability, then runs `git push -u origin implementation/dualbt-v0.1.0` and `git push origin --tags` without invoking `gh`.

Runtime verification in this sandbox exits at the script's DNS preflight, so no remote push is attempted until the environment can resolve and connect to GitHub.

## Commands Run

```bash
git --version
curl --version
timeout 20 curl --connect-timeout 10 --max-time 20 -I --resolve github.com:443:140.82.112.4 https://github.com/
timeout 20 env GIT_DIR=/tmp/DualBT.git GIT_WORK_TREE=/home/astra/codex/DualBT git -c http.curloptResolve=github.com:443:140.82.112.4 ls-remote origin HEAD
bash -n scripts/push-github.sh
scripts/push-github.sh
```
