# MonkeyMart

A small Android shopping app (Kotlin, Jetpack Compose) with three screens: shop list → item detail → checkout.

It is the demo target for [droid-monkey](https://github.com/shubhasai/droid-monkey), an agent that attacks Android apps with real-world faults, fixes what breaks, and proves each fix by replaying the attack. MonkeyMart contains **four deliberately planted lifecycle bugs**, so every droid-monkey run has a known right answer. droid-monkey itself is not specific to this app.

## Planted bugs

The `planted-bugs` tag marks the baseline with all four bugs present. Each bug arrived with the feature commit that introduced it.

| Fault | Where | What happens | Correct fix |
|---|---|---|---|
| Rotation | `ui/DetailScreen.kt`, `DealLoader` | The deal popup is shown from a `GlobalScope` job holding the Activity that started it. Rotating during the 5 s load crashes: `IllegalStateException: FragmentManager has been destroyed` | Lifecycle-aware scope (e.g. `viewModelScope`/`LaunchedEffect`), state in a ViewModel, no captured Activity |
| Process death | `ui/CheckoutViewModel.kt` | The six checkout fields are plain `mutableStateOf` with no `SavedStateHandle`. After the OS kills the app in the background, the user returns to an empty form (lost state, no crash) | `SavedStateHandle` (or `rememberSaveable`) |
| Network loss | `data/OrderRepository.kt`, `CheckoutViewModel.placeOrder` | `submit()` throws `IOException` when offline and nothing catches it: `java.io.IOException` crash | Catch the error, show a retry state, keep the form |
| Permission revoke | `ui/CouponScanner.kt` | A cached `camera_granted` flag in SharedPreferences is trusted after the user revokes the camera permission: `SecurityException` from `openCamera` | Check the permission at use time, request it again |

## Reproducing by hand

Needs a running emulator and `adb` on PATH.

```bash
./gradlew :app:installDebug
repro/run_all.sh          # one line per bug, about 2 minutes
repro/rotation.sh         # or any single bug
```

Verified on the `Pixel_10` AVD (Android 16, API 36):

```
rotation           crashed: java.lang.IllegalStateException
process_death      lost state: checkout form empty after relaunch
network_loss       crashed: java.io.IOException
permission_revoke  crashed: java.lang.SecurityException
```

Notes from building the repros, which droid-monkey's attacks also follow:

- Launch like a launcher tap (`MAIN`/`LAUNCHER`, `NEW_TASK|RESET_TASK_IF_NEEDED`). Otherwise a relaunch starts a new task and saved state is never restored.
- `am kill` only works once the app is in the background. Confirm the PID is gone before you judge the result.
- Android 16 can restore an app's last orientation on launch even with rotation locked, so set the rotation and wait until the display reports it.
- `pm revoke` kills the app process.
