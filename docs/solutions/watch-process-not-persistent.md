# Persistent Laptop Watcher Daemon

Problem: [docs/problems/2026-05-30-watch-process-not-persistent.md](../problems/2026-05-30-watch-process-not-persistent.md)

## What failed

Manual `nohup ... --watch &` produced a stale PID file and did not verify that the watcher stayed alive.

## What worked

The second manual daemon attempt also exited after the launching shell ended, so I stopped retrying the same fix and evaluated alternatives:

1. `nohup` plus PID file: simplest, but already produced a stale PID in this Codex exec environment.
2. `setsid`: detaches from the launching session better than plain `nohup`, but still has no restart supervision.
3. `disown`: shell-specific and not useful after a non-interactive launcher exits.
4. `tmux` or `screen`: persistent, but manual and not ideal for automatic Bluetooth repair.
5. systemd user service: supervised, restartable, tied to the user audio session, and compatible with PipeWire user services.

The selected fix is a systemd user service with `Restart=always`, plus script-owned daemon helpers for manual use:

1. Resolve the script path.
2. Start `--watch` with `nohup`.
3. Write `/tmp/dualbt-laptop-audio-watch.pid`.
4. Verify the PID is alive after startup.
5. Stop by PID file instead of broad process-name matching.
6. Install `scripts/dualbt-laptop-audio-watch.service` under the user systemd manager for durable supervision.

## Why it worked

The user systemd manager is already running with PipeWire active. systemd owns the watcher process instead of the transient shell and restarts it if it exits.

## Commands run

```bash
cat /tmp/dualbt-laptop-audio-watch.pid
ps -ef | rg 'dualbt-laptop-audio-watch\\.sh --watch'
tail -n 80 /tmp/dualbt-laptop-audio-watch.log
systemctl --user is-system-running
systemctl --user status pipewire.service --no-pager
install -m 0644 scripts/dualbt-laptop-audio-watch.service /home/astra/.config/systemd/user/dualbt-laptop-audio-watch.service
systemctl --user daemon-reload
systemctl --user enable --now dualbt-laptop-audio-watch.service
systemctl --user status dualbt-laptop-audio-watch.service --no-pager
journalctl --user -u dualbt-laptop-audio-watch.service --no-pager -n 60
```
