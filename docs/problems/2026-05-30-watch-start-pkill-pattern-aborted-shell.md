# Laptop Watcher Start Aborted By Broad pkill Pattern

## Exact error

The detached watcher start command returned abnormally with no output:

```text
Process exited with code -1
```

The command used a broad `pkill -f '/home/astra/codex/DualBT/scripts/dualbt-laptop-audio-watch.sh --watch'` before starting the watcher.

## Reproduction steps

1. Run a shell command that first calls `pkill -f` for the watcher pattern.
2. Start `scripts/dualbt-laptop-audio-watch.sh --watch` in the same command.
3. Observe the shell exits abnormally before printing the expected PID or log output.

## Environment

- Date: 2026-05-30
- Shell: zsh launched from Codex exec
- Script: `scripts/dualbt-laptop-audio-watch.sh`

## First hypothesis

The broad `pkill -f` matched the current shell command line and killed the launcher before it could start or report the watcher. The fix is to avoid broad pattern killing and start the watcher directly, recording its PID.
