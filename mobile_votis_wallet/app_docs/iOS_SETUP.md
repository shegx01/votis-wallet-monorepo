# iOS App Setup Instructions

## Overview
The iOS app uses a Kotlin Multiplatform framework (`ComposeApp`) that needs to be built before the iOS app can be compiled. This guide explains how to configure Xcode to automatically build the framework.

## Framework Build Script
A build script (`build-framework.sh`) has been created that:
- Detects the target platform (iOS device vs simulator, ARM64 vs x64)
- Builds the appropriate Kotlin framework
- Copies it to the expected location for Xcode

## Xcode Configuration

### Method 1: Add Build Phase (Recommended)

1. Open `iosApp.xcodeproj` in Xcode
2. Select the `iosApp` project in the navigator
3. Select the `iosApp` target
4. Go to the "Build Phases" tab
5. Click the `+` button and select "New Run Script Phase"
6. Move the new "Run Script" phase to be **before** the "Compile Sources" phase
7. In the script box, enter:
   ```bash
   cd "$SRCROOT"
   ./build-framework.sh
   ```
8. Check "Show environment variables in build log" if you want detailed output

### Method 2: Framework Search Paths (Alternative)

If you prefer to build the framework manually before opening Xcode:

1. Run the build script manually:
   ```bash
   cd iosApp
   ./build-framework.sh
   ```
2. In Xcode, go to Build Settings for the iosApp target
3. Add the framework path to "Framework Search Paths":
   - `$(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`

### Build Configuration

The framework will be built for the correct target based on:
- **Debug/Release**: Based on Xcode's build configuration
- **Device/Simulator**: Based on the selected destination
- **Architecture**: Automatically detected (ARM64 for M1 Macs and devices, x64 for Intel Macs)

## Framework Locations

After building, the framework will be available at:
- `composeApp/build/bin/iosX64/debugFramework/ComposeApp.framework` (Intel simulator)
- `composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework` (ARM64 simulator)
- `composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework` (Device debug)
- Similar paths for release builds

## Troubleshooting

### "No such module 'ComposeApp'"
- Ensure the build script ran successfully
- Check that the framework exists in the expected location
- Verify framework search paths in Build Settings

### Build Script Fails
- Ensure you're in the `iosApp` directory when running the script
- Check that Gradle is accessible (`./gradlew` works from the parent directory)
- Verify all Kotlin modules compile successfully

### Performance
- First build will take longer as it downloads dependencies
- Subsequent builds are incremental and much faster
- Consider using Method 2 for development if builds become slow

## Manual Framework Build

To build the framework manually for testing:

```bash
# From the root mobile_votis_wallet directory
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64     # M1 Mac simulator
./gradlew :composeApp:linkDebugFrameworkIosX64                # Intel Mac simulator  
./gradlew :composeApp:linkDebugFrameworkIosArm64              # Device
./gradlew :composeApp:linkReleaseFrameworkIosArm64            # Device release
```
