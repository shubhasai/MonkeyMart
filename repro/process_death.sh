#!/usr/bin/env bash
# Planted bug: the checkout form lives in a ViewModel with plain mutableStateOf fields
# and no SavedStateHandle. It survives rotation, but when Android kills the app in the
# background the user comes back to an empty form (lost state, no crash).
set -u
cd "$(dirname "$0")" && . ./lib.sh

fresh_start
goto_checkout || exit 1
fill_checkout
echo "-- before"; checkout_values
kill_in_background || exit 1       # HOME, then kill the process as the OS does under memory pressure
relaunch_from_launcher              # user returns from recents/launcher
wait_for field_name 10 && { echo "-- after"; checkout_values; }
report
