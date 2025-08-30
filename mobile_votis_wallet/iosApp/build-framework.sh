#!/bin/bash

# Build script for ComposeApp framework
# This script builds the Kotlin/Native framework for the current iOS target

set -e

cd "$SRCROOT/.."

# Determine the target based on SDK
if [[ "$SDK_NAME" = *"iphoneos"* ]]; then
    TARGET="iosArm64"
elif [[ "$SDK_NAME" = *"iphonesimulator"* ]]; then
    if [[ "$(uname -m)" == "arm64" ]]; then
        TARGET="iosSimulatorArm64"
    else
        TARGET="iosX64"
    fi
else
    echo "Unknown SDK: $SDK_NAME"
    exit 1
fi

# Build the framework for the specific target and configuration
echo "Building framework for target: $TARGET, configuration: $CONFIGURATION"
./gradlew :composeApp:link${CONFIGURATION}Framework${TARGET}

# Copy the framework to the expected location
TARGET_LOWER=$(echo "$TARGET" | tr '[:upper:]' '[:lower:]')
CONFIG_LOWER=$(echo "$CONFIGURATION" | tr '[:upper:]' '[:lower:]')
FRAMEWORK_PATH="composeApp/build/bin/${TARGET_LOWER}/${CONFIG_LOWER}Framework/ComposeApp.framework"
if [[ -d "$FRAMEWORK_PATH" ]]; then
    echo "Framework built successfully at: $FRAMEWORK_PATH"
    # Create symlink or copy to standard location if needed
    mkdir -p "composeApp/build/xcode-frameworks/$CONFIGURATION/$SDK_NAME"
    rsync -av "$FRAMEWORK_PATH/" "composeApp/build/xcode-frameworks/$CONFIGURATION/$SDK_NAME/ComposeApp.framework/"
    echo "Framework copied to: composeApp/build/xcode-frameworks/$CONFIGURATION/$SDK_NAME/ComposeApp.framework"
else
    echo "Framework build failed - framework not found at: $FRAMEWORK_PATH"
    exit 1
fi
