#!/usr/bin/env bash
# Planted bug: the detail screen's deal popup is shown from a GlobalScope job that holds
# the Activity it started with. Rotating while the detail loads (5 s) destroys that
# Activity, so showing the popup through its FragmentManager crashes with
# IllegalStateException: FragmentManager has been destroyed.
set -u
cd "$(dirname "$0")" && . ./lib.sh

fresh_start
tap_id product_3 10                     # open "Mango Box"; detail now loads for 5 s
sleep 1                                 # load in flight, holding this Activity
set_rotation 1                          # rotate to landscape mid-load
sleep 6                                 # let the old load finish
report
set_rotation 0
