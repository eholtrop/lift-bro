#!/bin/sh

echo "--- Xcode Cloud BuildKonfig Environment Variable Bridge ---"

# Navigate to the root of your Kotlin Multiplatform project.
# IMPORTANT: Adjust this path!
# If your iosApp is at the root and shared is a subfolder: cd "$SRCROOT"
# If your iosApp and shared are siblings under a monorepo root: cd "$SRCROOT/.."
# If your iosApp is a subfolder and your shared module and gradlew are at the root:
cd "$SRCROOT/../.." # This is a common setup for KMM projects

echo "Current directory for Gradle: $(pwd)"

# Export the Xcode Cloud environment variables so Gradle can pick them up.
# Gradle's System.getenv() will read these.
# Check if the variables are set by Xcode Cloud before exporting to avoid empty strings
if [ -n "${LIFT_BRO_ADMOB_APP_ID}" ]; then
  export LIFT_BRO_ADMOB_APP_ID="${LIFT_BRO_ADMOB_APP_ID}"
  echo "Exported API_BASE_URL"
else
  echo "API_BASE_URL not set in Xcode Cloud, using default from build.gradle.kts"
fi

if [ -n "${LIFT_BRO_AD_UNIT_ID}" ]; then
  export LIFT_BRO_AD_UNIT_ID="${LIFT_BRO_AD_UNIT_ID}"
  echo "Exported API_KEY (redacted in logs)"
else
  echo "API_KEY not set in Xcode Cloud, using default from build.gradle.kts"
fi

# Explicitly run the Gradle task to generate BuildKonfig.
# This ensures the .kt file is generated *before* Xcode tries to compile the KMP framework.
echo "Running ./gradlew generateBuildKonfig"
./gradlew generateBuildKonfig

if [ $? -ne 0 ]; then
    echo "Error: Gradle generateBuildKonfig task failed!"
    exit 1 # Fail the build if Gradle fails
else
    echo "Gradle generateBuildKonfig task completed successfully."
fi

echo "--- Bridge complete ---"
exit 0
