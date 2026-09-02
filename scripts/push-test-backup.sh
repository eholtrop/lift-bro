#!/bin/bash
# push-test-backup.sh
# Push test backup file to device/emulator for screenshot generation

set -e

PLATFORM=$1

# Resolve backup file relative to project root (parent of scripts/)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_FILE="$PROJECT_ROOT/build/generated/test-data/screenshot_test_backup.json"

if [ -z "$PLATFORM" ]; then
  echo "Usage: $0 <android|ios> [simulator_udid]"
  echo ""
  echo "Pushes test backup file to device/emulator for screenshot generation."
  echo "iOS: simulator_udid is optional; defaults to the first booted simulator."
  exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
  echo "Error: Backup file not found at $BACKUP_FILE"
  exit 1
fi

case $PLATFORM in
  android)
    echo "📱 Pushing backup to Android device/emulator..."
    
    # Check if adb is available
    if ! command -v adb &> /dev/null; then
      echo "Error: adb not found. Please install Android SDK platform-tools."
      exit 1
    fi
    
    # Wait for device
    echo "Waiting for device..."
    adb wait-for-device
    
    # Ensure Download directory exists
    adb shell mkdir -p /sdcard/Download
    
    # Push backup file to Downloads folder
    adb push "$BACKUP_FILE" /sdcard/Download/
    
    echo "✅ Backup pushed to /sdcard/Download/"
    echo "   File: screenshot_test_backup.json"
    ;;
    
  ios)
    echo "📱 Pushing backup to iOS simulator..."
    
    # Check if xcrun is available
    if ! command -v xcrun &> /dev/null; then
      echo "Error: xcrun not found. Please install Xcode."
      exit 1
    fi
    
    # Optional explicit simulator UDID; otherwise use first booted
    SIMULATOR_ID="${2:-}"
    if [ -z "$SIMULATOR_ID" ]; then
      SIMULATOR_ID=$(xcrun simctl list devices booted | grep -o '[A-F0-9-]\{36\}' | head -1)
    fi
    
    if [ -z "$SIMULATOR_ID" ]; then
      echo "Error: No iOS simulator is booted."
      echo "Start a simulator first: open -a Simulator"
      exit 1
    fi
    
    echo "Found simulator: $SIMULATOR_ID"
    
    # The iOS document picker's "On My iPhone" location is backed by the Files
    # app's local File Provider storage, NOT the app's Documents container.
    # Push the backup into every group with a "File Provider Storage" dir so
    # the restore flow's file picker can see it.
    DEVICE_DATA="$HOME/Library/Developer/CoreSimulator/Devices/$SIMULATOR_ID/data"
    COPIED=0
    for target in "$DEVICE_DATA/Containers/Shared/AppGroup"/*/"File Provider Storage"; do
      [ -d "$target" ] || continue
      cp "$BACKUP_FILE" "$target/screenshot_test_backup.json"
      echo "  📄 -> $target/screenshot_test_backup.json"
      COPIED=1
    done
    
    if [ "$COPIED" -eq 0 ]; then
      echo "Error: No 'File Provider Storage' app-group directories found for simulator $SIMULATOR_ID."
      echo "Boot the simulator once so the Files app creates its local File Provider, then retry."
      exit 1
    fi
    
    echo "✅ Backup pushed to simulator File Provider storage"
    ;;
    
  *)
    echo "Error: Unknown platform '$PLATFORM'"
    echo "Usage: $0 <android|ios>"
    exit 1
    ;;
esac

echo ""
echo "🎯 Next steps:"
echo "   1. Run Maestro screenshot flows:"
echo "      maestro test .maestro/screenshots/"
echo "   2. Or use Fastlane:"
echo "      fastlane android generate_play_store_screenshots"
