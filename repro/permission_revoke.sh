#!/usr/bin/env bash
# Planted bug: after the first camera grant the app stores camera_granted=true in
# SharedPreferences and trusts that flag from then on. If the user revokes the camera
# permission in Settings (which kills the app), the next "Scan coupon" tap calls
# CameraManager.openCamera without permission and crashes with SecurityException.
set -u
cd "$(dirname "$0")" && . ./lib.sh

adb shell pm clear $PKG >/dev/null      # first-time user: no grant, no saved flag
fresh_start
goto_checkout || exit 1
tap_id scan_coupon 5 || exit 1
tap_id com.android.permissioncontroller:id/permission_allow_foreground_only_button 5 || exit 1
sleep 2
echo "-- after grant: $(field_text coupon_status)"

adb shell pm revoke $PKG android.permission.CAMERA   # user revokes in Settings; Android kills the app
sleep 1
adb logcat -b all -c
launch
set_rotation 0
wait_for scan_coupon 3 2>/dev/null || goto_checkout || exit 1   # checkout restored, or navigate back
tap_id scan_coupon 5 || exit 1
sleep 2
report
