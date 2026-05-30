# Laptop Watcher Process Did Not Stay Alive

## Exact error

The watcher PID file contained `1331487`, but no matching process was running:

```text
/tmp/dualbt-laptop-audio-watch.pid:
1331487

ps -ef | rg 'dualbt-laptop-audio-watch\.sh --watch':
no process

/tmp/dualbt-laptop-audio-watch.log:
[2026-05-30 20:28:07] Moved Zen sink input 990 to dualbt_bluetooth_pair
```

## Reproduction steps

1. Start `scripts/dualbt-laptop-audio-watch.sh --watch` manually with `nohup`.
2. Check the PID file later.
3. Observe the PID is stale and the watcher is no longer running.

## Environment

- Date: 2026-05-30
- Script: `scripts/dualbt-laptop-audio-watch.sh`
- PID file: `/tmp/dualbt-laptop-audio-watch.pid`
- Log file: `/tmp/dualbt-laptop-audio-watch.log`

## First hypothesis

Manual background launch is fragile and does not verify that the watcher survives startup. The script should own daemon start/stop behavior, write a PID file, verify the process after launch, and avoid broad `pkill` patterns.
