#!/bin/bash

set -e

REMOTE_HOST="dante.local"
REMOTE_PATH="~/Projects/lift-bro"
DEVICE_UDID="00008110-000E4C913EFB801E"

echo "=== Deploying Lift Bro to iOS device ==="
echo "Remote: $REMOTE_HOST"
echo "Device UDID: $DEVICE_UDID"
echo ""

echo "[1/2] Syncing project to $REMOTE_HOST..."
rsync -avz \
  --exclude '.git' \
  --exclude '.idea/' \
  --exclude '.gradle/' \
  --exclude 'build/' \
  --exclude '*.iml' \
  --exclude 'DerivedData/' \
  --exclude 'iosApp/build/' \
  --exclude 'iosApp/iosApp/GoogleService-Info.plist' \
  --exclude 'iosApp/Configuration.storekit' \
  --exclude 'iosApp/StoreKitTestCertificate.cer' \
  . "eholtrop@$REMOTE_HOST:$REMOTE_PATH/"

echo ""
echo "[2/2] Building iOS app on remote..."

# Fastlane
# ssh "eholtrop@$REMOTE_HOST" "source ~/.zshrc && export PATH=\"\$HOME/.rbenv/shims:\$HOME/.rbenv/bin:/usr/local/opt/rbenv/shims:/usr/local/opt/rbenv/bin:\$PATH\" && export RBENV_VERSION=3.3.0 && cd $REMOTE_PATH && bundle install && DEVICE_UDID=$DEVICE_UDID bundle exec fastlane ios build_debug_device device_udid:$DEVICE_UDID"

# xcode
# ssh "eholtrop@$REMOTE_HOST" "cd $REMOTE_PATH && xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS,id=00008110-000E4C913EFB801E' build DEVELOPMENT_TEAM=VZPX29WT9H && xcrun devicectl device install app --device 00008110-000E4C913EFB801E ~/Library/Developer/Xcode/DerivedData/iosApp-hcvusgxqxjpktmfuzvzusyfzcuhz/Build/Products/Debug-iphoneos/Lift\ Bro.app && xcrun devicectl device process launch --device 00008110-000E4C913EFB801E \"com.lift.bro\""

echo ""
echo "=== Build complete ==="
