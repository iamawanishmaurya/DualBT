# App-Level Dual Audio Needs System Route Group Support

## Exact error

The app-level dual audio feature still cannot make two classic Bluetooth A2DP speakers play simultaneously on the tested Xiaomi device. The verified runtime symptom is that Android exposes one active A2DP media route at a time; selecting the second speaker switches the active output instead of adding it as a simultaneous output.

## Reproduction steps

1. Connect `Mini boost 1` and `Mini boost 2`.
2. Launch DualBT.
3. Select both speakers.
4. Start YouTube playback and request DualBT streaming.
5. Observe Android routes the captured media stream to only one active Bluetooth media output.

## Environment

- Date: 2026-05-25
- Device: Android phone `d1bc5c4a`
- App: `com.xpwnit.dualbt`
- Current version before this fix: `0.2.36`
- Relevant Android APIs checked: `AudioTrack#setPreferredDevice`, `MediaRouter2.RoutingController#selectRoute`, Bluetooth A2DP public APIs, Android combined audio routing notes.

## First hypothesis

A normal app cannot force two classic Bluetooth A2DP devices to become active media outputs when the OS only exposes one active route. The strongest app-level path is to use `MediaRouter2` system route grouping when the system controller exposes the second speaker as a selectable route. On Samsung-style or LE Audio sharing devices this can create true simultaneous system-level duplication; on devices that do not expose selectable group routes, the app must still block rather than fake success.
