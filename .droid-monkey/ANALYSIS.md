# MonkeyMart analysis

## What the app does
MonkeyMart is a single-activity Jetpack Compose grocery/toy shop. `MainActivity` (the only activity in the app manifest, `AndroidManifest.xml:20`) hosts a hand-rolled route switch (`MainActivity.kt:61-82`) with three screens: a product list (Shop), a product detail with a "deal" popup (Detail), and a checkout form (Checkout). The catalogue is an in-memory fake with artificial delays (`ProductRepository.kt:17-42`) and orders go to a fake backend that fails with an `IOException` when offline (`OrderRepository.kt:26-30`). Checkout can also open the camera to "scan a coupon" (`CouponScanner.kt`). The profile's `ComponentActivity` and `PreviewActivity` are not in the app manifest; they come from the `debugImplementation` compose test-manifest and tooling dependencies (`app/build.gradle.kts:54-55`) and are not user flows.

Navigation state is `rememberSaveable` (`MainActivity.kt:62`), so the screen and product id survive rotation and process death. Everything else (detail product, form fields, order result, coupon status) is not saved in the same way.

## User flows worth testing

### 1. place_order (Shop -> Detail -> Checkout -> confirmation)
- Steps: open the app, tap a product card, wait for detail to load (5 s), tap "Buy now", fill the six fields (name, email, phone, address, city, pincode), tap "Place order", see "Order placed".
- Screens: ShopScreen (`shop_list`, `product_<id>`), DetailScreen (`detail_screen`, `buy_now`), CheckoutScreen (`checkout_screen`, `field_*`, `place_order`, `order_confirmed`).
- Data lost: the six typed form fields, the in-flight order (`placing`), and the confirmation (`orderId`) if the ViewModel is cleared. The route itself is kept.
- Code: `MainActivity.kt:62-81`, `ShopScreen.kt:36-37`, `DetailScreen.kt:95-98`, `CheckoutScreen.kt:34-82`, `CheckoutViewModel.kt:13-35`, `OrderRepository.kt:26-35`.

### 2. detail_deal_popup (Shop -> Detail with deal dialog)
- Steps: open a product, wait on the spinner while the detail loads (5 s), see the "Deal unlocked" dialog, tap "Nice".
- Screens: ShopScreen, DetailScreen (`detail_loading`, `detail_screen`, `detail_name`, "Deal unlocked" dialog).
- Data lost: the loaded product (`remember`, not saved) so the 5 s load is repeated; the dialog may appear again or not at all.
- Code: `DetailScreen.kt:40-68` (DealLoader, dialog), `DetailScreen.kt:72-76`, `ProductRepository.kt:36-39`.

### 3. scan_coupon (Checkout -> camera)
- Steps: on Checkout, tap "Scan coupon", grant the camera permission, see "Scanner ready: ..."; later tap "Scan coupon" again.
- Screens: CheckoutScreen with `ScanCouponButton` (`scan_coupon`, `coupon_status`).
- Data lost: the coupon status text (`remember`, `CouponScanner.kt:63`), and typed form fields if the permission change kills the process.
- Code: `CouponScanner.kt:33-57` (cached grant + openCamera), `CouponScanner.kt:60-86` (button), `CheckoutScreen.kt:74`.

### 4. browse_shop (Shop list)
- Steps: open the app, scroll the product list, open and go back from a product.
- Screens: ShopScreen. Data at risk: only scroll position (list is reloaded in 800 ms). Low value.
- Code: `ShopScreen.kt:34-70`, `MainActivity.kt:66-68`.

## Risks by flow

### place_order
1. **network_loss / airplane_mode (crash)**: tapping "Place order" with no network makes `submit` throw `IOException` (`OrderRepository.kt:27`). `placeOrder` calls it inside `viewModelScope.launch` with no try/catch (`CheckoutViewModel.kt:31-32`) and no handler, so the exception crashes the app. The network check runs only at the start of `submit`; the later `delay(1_500)` (`OrderRepository.kt:28`) is not affected by a drop. Trigger: be offline when tapping "Place order".
2. **process_death (data loss)**: the six form fields and `placing`/`orderId` are plain `mutableStateOf` in the ViewModel (`CheckoutViewModel.kt:14-24`), no `SavedStateHandle`. After process death the route `checkout:<id>` is restored (`MainActivity.kt:62`) but the form is blank and "Place order" is disabled (`CheckoutScreen.kt:78`). An order in flight is silently forgotten; the user cannot tell whether it was placed.
3. **dont_keep_activities / low_memory (data loss)**: with "Don't keep activities" the activity is destroyed without a configuration change, so the ViewModelStore is cleared and the form is lost the same way as in risk 2, even though the process survives. Low memory behaves like process death for a backgrounded app.
4. **rotation (probably OK)**: the ViewModel survives rotation (`CheckoutScreen.kt:34`), the scroll state is saveable (`CheckoutScreen.kt:52`) and `orderId` is kept, so form data and the confirmation should survive. Worth one check during `placing` and on the confirmation screen, since `vm.city` is read there (`CheckoutScreen.kt:44`).
5. **font_scale (layout)**: the Checkout column scrolls (`CheckoutScreen.kt:52`) so it is low risk; the Detail column does not scroll (see detail_deal_popup risk 4).

### detail_deal_popup
1. **rotation / dont_keep_activities (crash)**: `DealLoader.load` captures the `FragmentActivity` and starts work in `GlobalScope` (`DetailScreen.kt:57-58`), which is not cancelled when the screen leaves or the activity is destroyed. After the 5 s delay (`ProductRepository.kt:37`) it calls `activity.supportFragmentManager` and `DialogFragment.show(...)` (`DetailScreen.kt:64`). `show` uses `commit()`, which throws `IllegalStateException` if the old activity's state was already saved or the FragmentManager is destroyed. The exception propagates out of a `GlobalScope` coroutine on Main and crashes the app. Trigger: rotate (or enable don't keep activities and leave/return) within 5 s of opening a product.
2. **background (crash)**: pressing Home during the 5 s load runs `onSaveInstanceState` before the dialog is shown, so `commit()` at `DetailScreen.kt:64` throws the same exception (covers a `process_death`-style background/foreground cycle without the process dying). Same trigger window.
3. **Duplicate work and stale dialog**: each recreation starts another load (`DetailScreen.kt:74-75`) because the product is `remember`ed (`DetailScreen.kt:73`) and the old `GlobalScope` job is never cancelled. After the dialog is already open, rotation restores it and a second load can show another dialog (same tag, `DetailScreen.kt:64`). Pressing Back to Shop during the load (`MainActivity.kt:66-68`) does not cancel the job, so the dialog pops up over the Shop screen.
4. **font_scale / rotation (layout)**: the Detail `Column` has no `verticalScroll` (`DetailScreen.kt:86-99`) and a 96 sp emoji (`DetailScreen.kt:91`); at large font scale or in landscape the "Buy now" button can be pushed off screen with no way to scroll. Plausible, not confirmed without running.
5. **process_death**: restores `detail:<id>`, the 5 s spinner reappears and the load restarts (`DetailScreen.kt:73-76`); no crash, since the old coroutine died with the process.

### scan_coupon
1. **permission_revoke (crash)**: after the first grant, a flag is saved to SharedPreferences (`CouponScanner.kt:36-38`, set at `:66`). Later taps check only that flag (`CouponScanner.kt:33-34`, `:76`) instead of `checkSelfPermission`. If the user revokes camera in Settings, the flag stays true, the button goes straight to `open`, and `cameras.openCamera` (`CouponScanner.kt:44`) throws `SecurityException` (lint is silenced at `:40`). Trigger: tap "Scan coupon" after revoking. Revoking in Settings also kills the process, so the form fields are lost too (risk 2 above).
2. **rotation (minor data loss)**: the coupon status is `remember`ed (`CouponScanner.kt:63`), so it resets on rotation. `onOpened` can also fire after the screen is gone, but it only writes state, so it is harmless.

### browse_shop
1. **process_death / rotation**: the list is reloaded in 800 ms (`ShopScreen.kt:37`); only scroll position can be lost. No crash path found; the `LaunchedEffect` is cancelled correctly.
2. **locale_rtl / dark_mode**: dark mode is handled by the theme (`Theme.kt:38-51`); `priceLabel` formats with the default locale (`ProductRepository.kt:14`), which only changes digits. Low risk.

### Static signals: confirmed or rejected
- `throws_io_exception` OrderRepository.kt:27: **confirmed**; it is the root cause of crash risk 1 (place_order).
- `viewmodel_state_not_saved` CheckoutViewModel.kt:13: **confirmed** (fields at `:14-19`). Rotation is safe; process death and don't-keep-activities lose data.
- `coroutine_without_error_handling` CheckoutViewModel.kt:31: **confirmed**; no try/catch and no `CoroutineExceptionHandler`.
- `permission_not_checked_at_use` CouponScanner.kt:44: **confirmed**; no `checkSelfPermission` anywhere in the file.
- `cached_permission_state` CouponScanner.kt:37: **confirmed**; the flag is written at `:37`, read at `:34`, and never cleared.
- `unscoped_coroutine` DetailScreen.kt:58: **confirmed**; `GlobalScope`.
- `activity_passed_to_singleton` DetailScreen.kt:57: **confirmed**; `DealLoader` is an `object` and receives the Activity from `DetailScreen.kt:75`.
- `fragment_transaction_from_callback` DetailScreen.kt:64: **confirmed in effect**, with a nuance: it is not a stored `FragmentManager` but `activity.supportFragmentManager` read late from a captured Activity. The failure is the same.
- No false positives found. The only wrong signal-adjacent entries in the profile are the `ComponentActivity` and `PreviewActivity`, which are debug-only dependency activities.

## Recommended attacks
1. detail_deal_popup: rotate the device within 5 s of opening a product (rotation) to crash `DealLoader` at `DetailScreen.kt:64`.
2. place_order: turn on airplane mode / drop the network, then tap "Place order" (network_loss, airplane_mode) to crash on the uncaught `IOException`.
3. scan_coupon: grant camera and tap "Scan coupon", revoke camera in Settings, return and tap "Scan coupon" again (permission_revoke).
4. place_order: fill the checkout form, then kill the process in the background and return (process_death) to see the lost form.
5. detail_deal_popup: enable "Don't keep activities" and open a product, leave and return within 5 s (dont_keep_activities).
6. place_order: fill the form with "Don't keep activities" on and switch apps (dont_keep_activities) to see the form lost without a process kill.
7. detail_deal_popup: press Home within 5 s of opening a product to hit `commit()` after `onSaveInstanceState`.
8. detail_deal_popup: set a large font scale in portrait and landscape to check that "Buy now" is reachable (font_scale).
9. place_order: rotate while "Place order" is spinning and on the confirmation screen (rotation).
10. place_order: apply low memory pressure while on Checkout with a filled form (low_memory).
