# Changelog

All notable changes to DualBT will be documented in this file.

## [0.2.36] - 2026-05-25

### Fixed
- Blocked streaming startup when Android exposes only one active classic A2DP route, preventing DualBT from presenting a false two-speaker stream where both output tracks route to the same speaker.
- Added a system audio output picker action that opens Xiaomi's audio relay picker when available, or Android Bluetooth settings otherwise, so testers can verify the platform route without leaving the app blind.

## [0.2.35] - 2026-05-25

### Fixed
- Rejected generic phone SCO routes in the streaming hybrid split planner so a selected speaker leg cannot be silently routed to the handset.
- Let active A2DP streaming tracks use default media after a handoff when Android's public route metadata remains stale, matching the verified calibration behavior.

## [0.2.34] - 2026-05-25

### Fixed
- Allowed calibration tests to use default media playback after an attempted A2DP handoff when Android switches the real speaker but keeps public `AudioDeviceInfo` route metadata stale.
- Prevented the Test 1 calibration path from falling into generic SCO fallback after a Mini Boost A2DP handoff, avoiding blocked tests and Bluetooth media-volume mute side effects.

## [0.2.33] - 2026-05-25

### Fixed
- Requested transient audio focus for each speaker calibration tone so app tests behave like active media playback instead of relying on a silent or paused media pipeline.
- Raised calibration tone generation to the clipped-safe maximum gain while keeping system volume control external and logged.

## [0.2.32] - 2026-05-25

### Fixed
- Kept speaker calibration `AudioTrack` playback alive until the written tone has drained or a bounded Bluetooth latency tail expires, preventing route handoffs from being cut off before the physical speaker can play.
- Logged calibration target frames and drain timeout so physical speaker tests can distinguish silent routes from prematurely released playback.

## [0.2.31] - 2026-05-25

### Fixed
- Added a default media calibration route for accepted A2DP active-device switches when Android's public `AudioDeviceInfo` list still exposes the previous Mini Boost address.
- Kept route logs explicit by marking this path as `media-default-after-a2dp-activation`.

## [0.2.30] - 2026-05-25

### Fixed
- Added delayed Bluetooth output rescans after active-device attempts so calibration can pick up an A2DP route that appears shortly after Android's route change.
- Blocked generic phone/SCO communication fallback unless Android exposes a communication route matching the selected speaker, preventing false `Test 1` starts on the phone route.

## [0.2.29] - 2026-05-25

### Fixed
- Persisted selected speaker addresses and output gain across Activity/ViewModel recreation so UIAutomator dumps and MIUI activity restarts cannot reset physical-test setup to `0/2` and `100%`.
- Added plain Java state persistence coverage for restored selected-speaker order and saved speaker-address encoding.

## [0.2.28] - 2026-05-25

### Fixed
- Added in-app calibration volume restore so Bluetooth handoffs cannot leave Test 1 or Test 2 muted after the external test volume was set.
- Added the `MODIFY_AUDIO_SETTINGS` permission required for reliable stream volume and communication audio adjustments.

## [0.2.27] - 2026-05-25

### Fixed
- Removed the unsafe calibration fallback that reused the first visible A2DP output after active-device handoff when that output still reported the other Mini Boost address.
- Added a calibration route planner so `Test 2` uses targeted Headset/SCO when no direct media output matches `Mini boost 2`.

## [0.2.26] - 2026-05-25

### Fixed
- Converted the Headset/SCO output leg to mono 16 kHz `STREAM_VOICE_CALL` PCM so the `Mini boost 2` communication route is compatible with Android Bluetooth SCO restrictions.
- Routed the `Test 2` calibration path through targeted Headset/SCO when Android exposes only one direct A2DP media output, instead of silently blocking or reusing the same media speaker.

## [0.2.25] - 2026-05-25

### Added
- Added an experimental hybrid Bluetooth split planner and Headset/SCO route activation path so one selected Mini Boost can remain on A2DP media while the other is targeted through the headset communication route.
- Added route logging that exposes wrapped reflective active-device failures instead of hiding them behind `InvocationTargetException`.

## [0.2.24] - 2026-05-25

### Fixed
- Moved playback capture and experimental A2DP handoff startup off the service main-thread command path so Bluetooth profile callbacks can be delivered on the main looper.

## [0.2.23] - 2026-05-25

### Added
- Added an experimental streaming A2DP active-device handoff path that creates each output track after switching to its selected Mini Boost target.

### Changed
- The route availability gate now allows the one-direct-A2DP-route case only as an explicitly logged experiment instead of blocking before physical verification.

## [0.2.22] - 2026-05-24

### Added
- Added an experimental calibration-only A2DP active-device handoff before speaker tests so `Test 2` can try to switch Android's active Bluetooth media endpoint before playing.

## [0.2.21] - 2026-05-24

### Fixed
- Blocked unsupported dual-speaker route sets in the activity before MediaProjection so the UI no longer stays in a false streaming state after the service rejects one exposed Bluetooth media route.
- Added a user-visible route availability status when Android exposes fewer than two direct speaker routes.

## [0.2.20] - 2026-05-24

### Fixed
- Added a calibration run gate so rapid `Test 1`/`Test 2` taps cannot release an active calibration `AudioTrack` while it is still writing PCM.

## [0.2.19] - 2026-05-24

### Fixed
- Disabled the generic Bluetooth SCO fallback after physical testing showed it could make `Test 1` and `Test 2` beep the same Mini Boost speaker.
- Added direct-route validation so streaming starts only when Android exposes two distinct non-SCO media output routes for the selected speakers.

### Added
- Added plain Java route-support tests for rejecting one A2DP route plus a generic SCO fallback.

## [0.2.18] - 2026-05-24

### Added
- Added system media-volume syncing in the foreground service so hardware volume keys can control DualBT output gain while YouTube remains the foreground source app.
- Added a plain Java `OutputVolumeMapperTest` for Android media-volume to DualBT gain mapping.

## [0.2.17] - 2026-05-24

### Fixed
- Fixed volume controls after activity recreation by always forwarding foreground UI volume changes to the running service and making volume-only service starts stop cleanly when no stream is active.

## [0.2.16] - 2026-05-24

### Added
- Added foreground-service notification `Vol -` and `Vol +` actions so DualBT output gain can be adjusted while YouTube remains the foreground source app.

## [0.2.15] - 2026-05-24

### Added
- Added visible DualBT output volume controls that scale captured PCM before writing to the selected speaker routes.
- Added a tested PCM gain scaler with 0-200% gain and signed 16-bit clipping.
- Moved `Test 1` and `Test 2` controls into a stable top-level calibration panel for safer physical-device testing.

## [0.2.14] - 2026-05-24

### Fixed
- Switched speaker calibration playback from static-buffer `AudioTrack` output to streamed min-buffer playback after physical testing showed the static calibration track failed to initialize on the Redmi Note 9 Pro.

## [0.2.13] - 2026-05-24

### Added
- Added per-speaker `Test 1` and `Test 2` calibration controls for selected devices so identical Mini Boost speakers can be identified before dual playback testing.
- Added a routed Android calibration tone player that uses the same A2DP and communication-SCO fallback strategy as the streaming output path.
- Added a plain Java calibration tone generator test.

## [0.2.12] - 2026-05-24

### Added
- Added duplicate Bluetooth speaker aliases so the two physical `Mini boost 4` speakers display as `Mini boost 1` and `Mini boost 2` while preserving their original addresses.
- Added an experimental A2DP plus communication-SCO fallback for devices that expose one Bluetooth media sink but a separate Bluetooth communication route.
- Added tests for duplicate Mini Boost aliases and output fallback planning.

### Changed
- Ignored the local `music.webm` test asset so user-provided media is not staged by `git add -A`.

## [0.2.11] - 2026-05-24

### Added
- Added a verified Java `AudioTrack` output router for the default APK path that creates two output tracks, prefers matching Bluetooth output devices, writes split PCM to both routes, and logs output routing/write evidence.
- Added route-matching tests for two same-name `Mini boost 4` speakers with different Bluetooth addresses.

### Changed
- Playback capture now excludes DualBT's own UID to avoid recapturing the app's output track audio.
- Renamed the stream button from `Start Mock Stream` to `Start Stream` in the verified Java UI.

## [0.2.10] - 2026-05-24

### Added
- Added a safe UI dump wrapper for physical-device validation that treats known MIUI `uiautomator` theme-config noise as non-blocking only after validating XML hierarchy output.
- Added physical-device validation documentation for app install, scrcpy mirroring/control, logging overlay, dark/light theme checks, MediaProjection consent, and playback-capture start/stop.

### Fixed
- Fixed a physical-device launch crash by deferring light system-bar appearance updates until after the activity content root exists.

## [0.2.9] - 2026-05-24

### Added
- Added a tested Codex watchdog polling utility that records one-minute heartbeat lines for long-running work.
- Added a watchdog controller for starting, checking, tailing, and stopping the detached heartbeat process.
- Added watchdog documentation and ignored the generated heartbeat log file.

## [0.2.8] - 2026-05-24

### Added
- Added a tested Java audio capture specification for the default service path.
- Added a Java AudioPlaybackCapture engine that starts `AudioRecord` from MediaProjection consent, reads PCM, and fans out captured bytes to two output buffers.

### Changed
- `DualBTService` now starts real playback capture after validating MediaProjection consent and exactly two selected route targets, and stops capture during service teardown.

## [0.2.7] - 2026-05-24

### Added
- Added a tested stream route plan serializer for selected speaker targets.

### Changed
- `MainActivity` now passes the selected two-speaker route plan into `DualBTService` after capture consent.
- `DualBTService` now rejects capture starts that do not include exactly two route targets.

## [0.2.6] - 2026-05-24

### Added
- Added bonded Bluetooth audio-device discovery for the default Java build path.
- Added a tested Bluetooth speaker catalog with audio filtering, de-duplication, and mock fallback.

### Changed
- Device refresh now scans real bonded Bluetooth audio devices when permissions and adapter state allow it, then falls back to emulator mock speakers.

## [0.2.5] - 2026-05-24

### Added
- Added a tested Java PCM splitter for the default offline build path.
- Added a repository-local plain Java PCM splitter test.

## [0.2.4] - 2026-05-24

### Added
- Added persistent internal file logging with append, newline escaping, rotation, and clear support.
- Added a repository-local plain Java file-log sink test.

### Changed
- `AppLogger` now initializes a file sink from `DualBTApp` and persists each logcat/in-app log entry to app internal storage.

## [0.2.3] - 2026-05-24

### Added
- Added a repository-local plain Java stream-session controller test.
- Added MediaProjection permission gating before mock streaming starts.

### Changed
- Streaming now enters a pending permission state, starts the foreground service only after capture consent, and stops the service when streaming stops.
- `DualBTService` now requires MediaProjection consent extras before entering foreground mode.

## [0.2.2] - 2026-05-24

### Added
- Added a tested stream session controller for the default Java build path.

### Changed
- Mock streaming now requires exactly two selected speakers, rejects a third selection, locks selection while streaming, and logs blocked start/selection attempts.

## [0.2.1] - 2026-05-24

### Added
- Added a conditional `DUALBT_PLANNED_STACK=true` Gradle path for the original Kotlin, Compose, Hilt, and NDK architecture.

### Changed
- Kept the verified Java framework APK path as the default offline build while excluding fallback Java classes when the planned stack is enabled.

## [0.2.0] - 2026-05-24

### Added
- Added a platform-only Android implementation that can build from the available offline cache.
- Added macOS-inspired dark and light glassmorphism UI, animated background orbs, mock speaker cards, streaming state animations, and responsive scroll layout.
- Added a full in-app and logcat logging system with filtering and clearing.

### Changed
- Replaced the dependency-heavy Compose/Hilt/Oboe build path with Android framework APIs so the app can be compiled and installed in the restricted environment.

## [0.1.0] - 2026-05-24

### Added
- Initialized local release tracking and repository hygiene.
- Added implementation step logging under `docs/steps.md`.
- Added problem and solution documentation for workspace setup issues.

### Notes
- The workspace contains a read-only `.git` placeholder, so Git commands for this session use `GIT_DIR=/tmp/DualBT.git` and `GIT_WORK_TREE=/home/astra/codex/DualBT`.
- No non-English project content was found during initial setup review.
