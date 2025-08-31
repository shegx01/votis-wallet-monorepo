# iOS Build Setup Guide

This guide explains how to build the iOS app from both Xcode and Android Studio.

## Prerequisites

- Xcode 16.2+
- CocoaPods installed
- Java 11+ (configured in gradle.properties)

## Building from Xcode

1. Open `iosApp.xcworkspace` (NOT .xcodeproj)
2. Select your target device/simulator
3. Build and run

## Building from Android Studio

Due to how Android Studio handles iOS builds, CocoaPods frameworks need to be synced between build directories.

### First Time Setup

1. Build the project once from Xcode first to generate frameworks
2. Run the sync script: `./sync-frameworks.sh`
3. Now you can build from Android Studio

### Daily Workflow

If you encounter "Framework not found" errors when building from Android Studio:

```bash
cd iosApp
./sync-frameworks.sh
```

This script automatically:
- Finds CocoaPods frameworks built by Xcode
- Creates symbolic links in Android Studio's build directory
- Works with any simulator/device combination

### Automated Sync (Optional)

You can add the sync script to your build phases in Xcode:

1. Open `iosApp.xcodeproj` in Xcode
2. Select the `iosApp` target
3. Go to "Build Phases"
4. Add a new "Run Script Phase"
5. Add this script: `"$SRCROOT/sync-frameworks.sh"`
6. Move it after the "Embed Pods Frameworks" phase

## Troubleshooting

### "Multiple commands produce" error
✅ **Fixed** - Product name and references have been corrected

### "Framework not found" error
- Run `./sync-frameworks.sh` after building from Xcode
- Ensure both Xcode and Android Studio are using the same iOS simulator version

### "Java 8" error
✅ **Fixed** - gradle.properties now specifies Java 11+ path

### "Missing bundle ID" error
✅ **Fixed** - PRODUCT_BUNDLE_IDENTIFIER configured in Xcode project

### Clean build needed
If you need to clean everything:

```bash
# Clean CocoaPods
rm -rf Pods Podfile.lock
pod install

# Clean Xcode build cache
# In Xcode: Product → Clean Build Folder

# Run sync script
./sync-frameworks.sh
```

## Framework Details

The following CocoaPods frameworks are automatically synced:
- AppAuth (Google Sign-In dependency)
- GTMAppAuth (Google Sign-In dependency)  
- GTMSessionFetcher (Google Sign-In dependency)
- GoogleSignIn (Main Google Sign-In framework)
- Pods_iosApp.framework (CocoaPods umbrella framework)

## Notes

- Always use `.xcworkspace` file, not `.xcodeproj`
- The sync script is safe to run multiple times
- Symbolic links are used, so frameworks aren't duplicated on disk
- Script works automatically with any simulator or device
