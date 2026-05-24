# Second Mini Boost 4 Pairing Timed Out

## Exact Error
`adb -s d1bc5c4a shell dumpsys bluetooth_manager` showed the second `Mini boost 4` leaving the pairing flow before it bonded:

```text
Connection Events:
  05-24 20:50:22.756 CLASSICAL-DISCONNECTED  41:42:2e Mini boos  status = LMP Response Timeout

Bond Events:
  20:49:45.915  41:42:2e:9e:5e:ae  btif_dm_create_bond  BOND_STATE_NONE
  20:49:45.916  41:42:2e:9e:5e:ae  bond_state_changed   BOND_STATE_BONDING
  20:49:52.367  41:42:2e:9e:5e:ae  bond_state_changed   BOND_STATE_BONDING
  20:50:22.659  41:42:2e:9e:5e:ae  bond_state_changed   BOND_STATE_NONE
  20:50:23.052  41:42:2e:9e:5e:ae  Invalid value        BOND_STATE_NONE
```

## Reproduction Steps
1. Connect the first `Mini boost 4` speaker from Android Bluetooth Settings.
2. Scroll to `AVAILABLE DEVICES`.
3. Tap the second `Mini boost 4`.
4. Capture UI and Bluetooth diagnostics before accepting the pairing dialog.
5. Observe the second speaker return to `BOND_STATE_NONE` with `LMP Response Timeout`.

## Environment
- Device: Redmi Note 9 Pro (`d1bc5c4a`)
- Android API: 31 / MIUI Bluetooth Settings
- First speaker connected: `41:42:26:B3:62:1C` (`Mini boost 4`)
- Second speaker attempt: `41:42:2e:9e:5e:ae` (`Mini boost 4`)
- App under test: DualBT `0.2.10` debug build

## First Hypothesis
The second speaker timed out because the pairing dialog stayed open while diagnostics were being collected, or because the speaker left pairable mode while the phone already had one same-name speaker connected. The next attempt should refresh the available-device list and accept the pair dialog immediately.

## Additional Evidence
- A later tap on the stale pairing sheet did not create a new bond event; `dumpsys bluetooth_manager` still listed only `41:42:26:B3:62:1C` as bonded.
- Android Settings returned to showing only the first `Mini boost 4` as `Connected | Battery 100% | Active`.

## Solution
- [Second Mini Boost 4 Pair Timeout Solution](../solutions/second-mini-boost-pair-timeout.md)
