# Safe Laptop Watcher Start

Problem: [docs/problems/2026-05-30-watch-start-pkill-pattern-aborted-shell.md](../problems/2026-05-30-watch-start-pkill-pattern-aborted-shell.md)

## What failed

Starting the watcher with a preceding broad `pkill -f` pattern could kill the launcher shell itself.

## What worked

Start the watcher directly with `nohup`, write its PID to `/tmp/dualbt-laptop-audio-watch.pid`, then verify it with `ps` and the watcher log.

## Why it worked

The safe start path avoids matching the active shell command line. Existing watcher cleanup can be done later by PID file instead of broad pattern matching.

## Commands run

```bash
nohup /home/astra/codex/DualBT/scripts/dualbt-laptop-audio-watch.sh --watch > /tmp/dualbt-laptop-audio-watch.log 2>&1 &
echo $! > /tmp/dualbt-laptop-audio-watch.pid
```
