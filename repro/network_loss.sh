#!/usr/bin/env bash
# Planted bug: "Place order" calls OrderRepository.submit(), which throws IOException when
# the device is offline. Nothing catches it, so the app crashes instead of showing a retry.
set -u
cd "$(dirname "$0")" && . ./lib.sh
trap network_on EXIT

fresh_start
goto_checkout || exit 1
fill_checkout
network_off                     # connectivity drops before the user submits
tap_id place_order 5 || exit 1
sleep 3
report
