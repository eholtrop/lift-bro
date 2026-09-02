#!/bin/bash
# run-ios-screenshots.sh
# Runs the iOS App Store screenshot pipeline on a remote Mac and pulls the
# framed screenshots back into fastlane/metadata/ios/.
#
# Flow:
#   1. rsync this project to $REMOTE_HOST:$REMOTE_PATH (same excludes as deploy-ios-device.sh)
#   2. Run `fastlane ios generate_app_store_screenshots` on the remote
#   3. Pull framed screenshots + raw maestro captures back locally
#
# Options:
#   --skip-build   Reuse the already-installed app (skip Xcode build + install)
#   --flow <name>  Run only a single Maestro screenshot flow (e.g. 03_workout_set_entry)
#   --no-frame     Skip frameit / commit-dir copy (raw captures only)

set -e

REMOTE_USER="eholtrop"
REMOTE_HOST="dante.local"
REMOTE_PATH="~/Projects/lift-bro"

SKIP_BUILD=0
FLOW=""
NO_FRAME=0

usage() {
  cat <<EOF
Usage: $0 [options]

Runs the iOS App Store screenshot pipeline on $REMOTE_HOST and pulls the
framed screenshots back into fastlane/metadata/ios/en-US/images/phoneScreenshots/.

Options:
  --skip-build   Reuse the already-installed app on the simulator (skip Xcode build + install)
  --flow <name>  Run only a single Maestro screenshot flow (e.g. 03_workout_set_entry)
  --no-frame     Skip frameit; raw captures stay in build/generated/ios-screenshot-tests/
  -h, --help     Show this help
EOF
}

while [ $# -gt 0 ]; do
  case "$1" in
    --skip-build) SKIP_BUILD=1 ;;
    --flow) FLOW="$2"; shift ;;
    --no-frame) NO_FRAME=1 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
  esac
  shift
done

FASTLANE_OPTS=""
[ "$SKIP_BUILD" -eq 1 ] && FASTLANE_OPTS="$FASTLANE_OPTS skip_build:true"
[ -n "$FLOW" ] && FASTLANE_OPTS="$FASTLANE_OPTS flow:$FLOW"
[ "$NO_FRAME" -eq 1 ] && FASTLANE_OPTS="$FASTLANE_OPTS no_frame:true"

echo "=== Generating iOS App Store screenshots ==="
echo "Remote: $REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH"
[ "$SKIP_BUILD" -eq 1 ] && echo "Mode: skip-build (reuse installed app)"
[ -n "$FLOW" ] && echo "Flow: $FLOW"
[ "$NO_FRAME" -eq 1 ] && echo "Framing: skipped"
echo ""

# Pre-warm Gradle daemon on remote while we sync files
echo "[1/3] Pre-warming Gradle daemon on $REMOTE_HOST..."
ssh "$REMOTE_USER@$REMOTE_HOST" \
  "cd $REMOTE_PATH && ./gradlew --stop > /dev/null 2>&1; ./gradlew --version > /dev/null 2>&1 &"

echo "[2/3] Syncing project to $REMOTE_HOST..."
rsync -a --info=progress2 \
  --delete \
  --exclude '.bundle/config' \
  --exclude '.git' \
  --exclude '.idea/' \
  --exclude '.gradle/' \
  --exclude 'build/' \
  --exclude 'fastlane/build/' \
  --exclude '*.iml' \
  --exclude 'DerivedData/' \
  --exclude 'iosApp/build/' \
  --exclude 'node_modules/' \
  --exclude 'marketing-website/' \
  --exclude 'maestro_tests/' \
  --exclude 'hooks/' \
  --exclude 'iosApp/iosApp/GoogleService-Info.plist' \
  --exclude 'iosApp/Configuration.storekit' \
  --exclude 'iosApp/StoreKitTestCertificate.cer' \
  --exclude 'local.properties' \
  --exclude 'xcuserdata/' \
  . "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH/"

echo ""
echo "[3/3] Running screenshot pipeline on remote..."

ssh "$REMOTE_USER@$REMOTE_HOST" "
   cd $REMOTE_PATH &&
   ~/.rbenv/shims/bundle install &&
   ~/.rbenv/shims/bundle exec fastlane ios generate_app_store_screenshots$FASTLANE_OPTS
"

echo ""
echo "=== Pulling screenshots back to dev machine ==="
mkdir -p "fastlane/metadata/ios/en-US/images/phoneScreenshots"
mkdir -p "build/generated/ios-screenshot-tests"

# Framed screenshots (also synced back if --no-frame, in which case nothing is copied)
rsync -a --info=progress2 \
  "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH/fastlane/metadata/ios/en-US/images/phoneScreenshots/" \
  "fastlane/metadata/ios/en-US/images/phoneScreenshots/"

# Raw maestro captures + command logs, for debugging flow failures
rsync -a --info=progress2 \
  "$REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH/build/generated/screenshot-tests/" \
  "build/generated/ios-screenshot-tests/"

echo ""
echo "=== Done ==="
echo "Framed screenshots: fastlane/metadata/ios/en-US/images/phoneScreenshots/"
echo "Raw maestro captures: build/generated/ios-screenshot-tests/"