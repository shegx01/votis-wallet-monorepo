# iOS Dark Mode Launch Screen Testing Guide

## Current Setup

The iOS launch screen is configured with:
- ✅ Dark mode assets in `LaunchImage.imageset/`
- ✅ Proper `Contents.json` with dark mode variants
- ✅ `UIUserInterfaceStyle: Automatic` in Info.plist
- ✅ Dark background color in `SplashBackgroundColor.colorset`

## Assets Available

**Light Mode:**
- `LaunchImage.png` (1x)
- `LaunchImage@2x.png` (2x) 
- `LaunchImage@3x.png` (3x)

**Dark Mode:**
- `LaunchImage-Dark.png` (1x)
- `LaunchImage-Dark@2x.png` (2x)
- `LaunchImage-Dark@3x.png` (3x)

## Testing Steps

### 1. Clean Build (Essential)
iOS aggressively caches launch screens. Always clean before testing:
```bash
# In Xcode
Product → Clean Build Folder (Cmd+Shift+K)
```

### 2. Delete App from Simulator/Device
Launch screen images are cached at the system level:
```bash
# Delete the app completely from simulator/device
# Reinstall fresh copy
```

### 3. Test Dark Mode
**On Simulator:**
```bash
# Device → Appearance → Dark
# Or use Simulator menu: Features → Toggle Appearance
```

**On Device:**
```bash
# Settings → Display & Brightness → Dark
```

### 4. Force App Termination
```bash
# Double-tap home button and swipe up on app
# Or Settings → General → iPhone Storage → [App] → Offload App
```

## Troubleshooting

### Issue: Only Light Assets Show
**Solution:** Delete app completely and reinstall

### Issue: Background Wrong Color  
**Solution:** Check `SplashBackgroundColor.colorset` has both light/dark variants

### Issue: Image Wrong but Background Right
**Solution:** Verify `LaunchImage.imageset/Contents.json` has proper dark mode entries

### Issue: Works on Simulator but not Device
**Solution:** Test on physical device in different lighting conditions

## Verification Checklist

- [ ] Clean build folder in Xcode
- [ ] Delete app from simulator/device  
- [ ] Install fresh copy
- [ ] Test in light mode
- [ ] Switch to dark mode
- [ ] Force quit and relaunch app
- [ ] Verify dark assets appear
- [ ] Test on physical device

## Expected Behavior

**Light Mode:**
- Background: `#F5FAFB` (light blue-gray)
- Logo: Standard light mode asset

**Dark Mode:** 
- Background: `#000000` (pure black)
- Logo: Dark mode optimized asset (typically lighter/white elements)

## Notes

- iOS launch screens use trait collections for dark mode detection
- Changes require app deletion and reinstallation to take effect
- Launch screen assets are system-level cached, not app-level cached
- Physical devices may behave differently than simulators
