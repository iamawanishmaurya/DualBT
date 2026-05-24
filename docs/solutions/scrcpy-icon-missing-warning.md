# Scrcpy Icon Missing Warning Solution

- Timestamp: 2026-05-24 20:37:55 IST
- Problem: `docs/problems/2026-05-24-scrcpy-icon-missing-warning.md`

## What Failed

The host scrcpy installation could not load its window icon from `/home/astra/.local/share/icons/hicolor/256x256/apps/scrcpy.png`.

## What Worked

No DualBT or device fix was required. The same scrcpy command successfully:

- found `d1bc5c4a`;
- pushed `scrcpy-server`;
- started the controller and receiver threads;
- opened the OpenGL renderer;
- mirrored a `328x720` texture;
- exited cleanly when the 8 second time limit was reached.

## Why It Worked

The missing icon is a host desktop packaging warning. It does not block scrcpy's ADB server, video stream, or input controller.

## Commands Run

```bash
/home/astra/.local/bin/scrcpy --help
/home/astra/.local/bin/scrcpy -s d1bc5c4a --no-audio --stay-awake --window-title DualBT-test --max-size=720 --time-limit=8
```
