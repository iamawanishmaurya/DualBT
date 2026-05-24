# Second Mini Boost 4 Pair Timeout Solution

## Problem Link
- [Second Mini Boost 4 Pairing Timed Out](../problems/2026-05-24-second-mini-boost-pair-timeout.md)

## What Failed
The first second-speaker attempt left the Android pairing dialog open while diagnostics were collected. The second `Mini boost 4` then returned to `BOND_STATE_NONE` with `LMP Response Timeout`.

## What Worked
Refreshing the available-device list, tapping the second `Mini boost 4`, waiting for the pairing sheet to fully render, and then accepting the `Pair` button produced a successful bond and A2DP/Headset connection.

## Why It Worked
The successful path kept the second speaker in an active pairing window until the Android pairing sheet was ready, then accepted the bond request before the speaker timed out. Android then completed `BOND_STATE_BONDED` and connected both Bluetooth profiles.

## Evidence
`dumpsys bluetooth_manager` after the successful retry reported:

```text
Bonded devices:
  41:42:26:B3:62:1C [BR/EDR] Mini boost 4
  41:42:2E:9E:5E:AE [BR/EDR] Mini boost 4

mDevice: 41:42:26:B3:62:1C(Mini boost 4) name=AvrcpControllerStateMachine state=Connected
mDevice: 41:42:2E:9E:5E:AE(Mini boost 4) name=AvrcpControllerStateMachine state=Connected

20:53:30.102  41:42:2e:9e:5e:ae  bond_state_changed   BOND_STATE_BONDED
```

The UI dump `/tmp/dualbt-mini-boost-second-accepted-final.xml` showed:

```text
Mini boost 4
SBC
Connected | Battery 100%
Mini boost 4
SBC
Connected | Battery 80%  | Active
```

## Commands Run
```bash
adb -s d1bc5c4a shell input swipe 540 2100 540 700 800
scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-mini-boost-find-second-retry.xml
adb -s d1bc5c4a shell input tap 540 1625
adb -s d1bc5c4a shell input tap 540 2200
scripts/dump-ui-safe.sh d1bc5c4a /tmp/dualbt-mini-boost-second-accepted-final.xml
adb -s d1bc5c4a shell dumpsys bluetooth_manager
```
