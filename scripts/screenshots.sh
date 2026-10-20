#!/usr/bin/env bash
# Captures README screenshots of the sample app on a running emulator.
# The sample opens each screen from the `scene` extra and loads it live from the Spaceflight News API, with each
# story's own image from its news site. Each scene logs when its data and every image on screen have loaded; a scene
# that shows an error or does not finish loading fails the run. Every capture is also checked for the expected text
# and for a blank image.
#
#   bash scripts/screenshots.sh tablet   # pixel_tablet in landscape: article list and the newest article with a summary
#   bash scripts/screenshots.sh phone    # pixel_7: the feed, a search for "Artemis" and the reports
set -euo pipefail
source "$(dirname "$0")/screenshot-lib.sh"

device="${1:-phone}"

# The text each scene must show; the capture fails without it. None of these appear on the error screen.
expected_text() {
  case "$1" in
    feed) echo "Top story" ;;
    search) echo "results for" ;;
    reports) echo "Latest reports" ;;
    tablet) echo "Open on" ;;
  esac
}

install_sample
set_night_mode light
if [ "$device" = tablet ]; then
  ensure_landscape
  fresh_launch --es scene tablet
  wait_for_scene tablet
  capture "tablet-detail" "$(expected_text tablet)"
else
  for scene in feed search reports; do
    fresh_launch --es scene "$scene"
    wait_for_scene "$scene"
    capture "phone-$scene" "$(expected_text "$scene")"
  done
fi
