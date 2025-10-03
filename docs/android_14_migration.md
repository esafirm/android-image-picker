# Android 14 (API 34) Migration Guide

This guide explains how to migrate your Android Image Picker library to support Android 14's new
granular media permissions.

## Overview

Android 14 introduces **granular media permissions** that give users more control over their media
access:

- **Full Access**: Traditional behavior - app can access all photos and videos
- **Partial Access**: New option - app can only access user-selected photos and videos

## What Changed

### Before Android 14

```
User grants permission → App gets access to ALL media
```

### Android 14+

```
User sees dialog with two options:
1. "Select photos and videos" → Partial access (READ_MEDIA_VISUAL_USER_SELECTED)
2. "Allow access to all media" → Full access (READ_MEDIA_IMAGES + READ_MEDIA_VIDEO)
```

## Migration Steps

### 1. Update Your Manifest

The library now includes the new permission automatically:

```xml
<!-- Android 14+ granular media permissions -->
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED" />
```

### 2. Update Your Code

#### Basic Usage (Recommended)

```kotlin
val imagePickerLauncher = registerImagePicker { images ->
    // Handle selected images
}

// Use default granular permission behavior
val config = ImagePickerConfig()
    .enableGranularPermissions() // Best user experience

imagePickerLauncher.launch(config)
```

#### Advanced Configuration

```kotlin
val config = ImagePickerConfig()
    .granularPermissions {
        showUpgradePrompt = true // Show "access all photos" option
        requestFullAccessFirst = false // Start with partial access
        allowPartialAccess = true // Allow app to work with limited access
        partialAccessMessage = "Custom message for partial access"
        showPartialAccessIndicator = true // Show UI indicator
    }
```

#### Legacy Behavior (Always Require Full Access)

```kotlin
val config = ImagePickerConfig()
    .requireFullMediaAccess() // Forces full access like before Android 14
```

#### Minimal UI (No Upgrade Prompts)

```kotlin
val config = ImagePickerConfig()
    .minimalPermissionUI() // Works with partial access, no upgrade prompts
```

## Permission States

The library automatically handles different permission states:

| State              | Description               | User Experience                                        |
|--------------------|---------------------------|--------------------------------------------------------|
| **Full Access**    | Traditional behavior      | All photos/videos available                            |
| **Partial Access** | Limited to user selection | Only selected photos/videos available + upgrade option |
| **No Access**      | No permissions granted    | Permission request dialog                              |

## User Experience Flow

### Default Behavior (Recommended)

1. **First Launch**: User sees Android's granular permission dialog
2. **User Chooses "Select photos"**: App works with limited access + shows upgrade option
3. **User Chooses "Allow all"**: App works with full access
4. **Upgrade Flow**: User can upgrade from partial to full access anytime

### Example Upgrade Flow

```
┌─────────────────────────────────┐
│ Image Picker                    │
│ ┌─────┐ ┌─────┐ ┌─────┐        │
│ │ IMG │ │ IMG │ │ IMG │        │
│ └─────┘ └─────┘ └─────┘        │
│                                 │
│ ⚠️ Showing limited photos       │
│ [Tap to see all photos] ←──────┼── Upgrade prompt
│                                 │
└─────────────────────────────────┘
```

## Configuration Options

### GranularPermissionConfig Properties

```kotlin
data class GranularPermissionConfig(
    // Show upgrade prompt when user has partial access
    val showUpgradePrompt: Boolean = true,

    // Request full access on first launch (vs. letting user choose)
    val requestFullAccessFirst: Boolean = false,

    // Custom message for partial access prompt
    val partialAccessMessage: String? = null,

    // Show indicator when in partial access mode
    val showPartialAccessIndicator: Boolean = true,

    // Allow app to work with partial access
    val allowPartialAccess: Boolean = true
)
```

### Preset Configurations

```kotlin
// Best user experience (default)
GranularPermissionConfig.default()

// Legacy behavior - always require full access
GranularPermissionConfig.requireFullAccess()

// Minimal UI - no upgrade prompts
GranularPermissionConfig.minimal()
```

## Backward Compatibility

✅ **Fully backward compatible** - no breaking changes

- Android 13 and below: Works exactly as before
- Android 14+: Adds granular permission support
- Existing code: Continues to work without changes

## Testing

### Test Scenarios

1. **Fresh Install on Android 14**:
    - Test both "Select photos" and "Allow all" choices
    - Verify upgrade flow works

2. **Permission Revocation**:
    - Test app behavior when permissions are revoked
    - Verify graceful degradation

3. **Backward Compatibility**:
    - Test on Android 13 and below
    - Ensure no regressions

### Testing Commands

```bash
# Test on Android 14 emulator
adb shell pm grant com.yourapp.package android.permission.READ_MEDIA_VISUAL_USER_SELECTED

# Revoke permissions for testing
adb shell pm revoke com.yourapp.package android.permission.READ_MEDIA_IMAGES
```

## Troubleshooting

### Common Issues

**Q: Users can't see all their photos**
A: This is expected with partial access. The upgrade prompt should guide users to grant full access.

**Q: App crashes on Android 14**
A: Ensure you're using the latest version of the library with Android 14 support.

**Q: Permission dialog doesn't show**
A: Check that your `targetSdkVersion` is set to 34 and you've included the new permission.

### Debug Logging

```kotlin
ImagePickerConfig()
    .enableLog(true) // Enable debug logging
```

## Migration Checklist

- [ ] Update library to latest version
- [ ] Test on Android 14 device/emulator
- [ ] Verify both permission flows work
- [ ] Test upgrade prompts
- [ ] Ensure backward compatibility
- [ ] Update app documentation

## Best Practices

1. **Use Default Configuration**: Provides the best user experience
2. **Test Both Flows**: Test partial and full access scenarios
3. **Educate Users**: Consider showing onboarding about permission choices
4. **Graceful Degradation**: Ensure app works well with limited access
5. **Respect User Choice**: Don't force full access if partial works for your use case

## Example Implementation

```kotlin
class MainActivity : AppCompatActivity() {

    private val imagePickerLauncher = registerImagePicker { images ->
        handleSelectedImages(images)
    }

    private fun openImagePicker() {
        val config = ImagePickerConfig {
            mode = ImagePickerMode.MULTIPLE
            limit = 10
        }.enableGranularPermissions() // Android 14 support

        imagePickerLauncher.launch(config)
    }

    private fun handleSelectedImages(images: List<Image>) {
        // Handle both full and partial access scenarios
        if (images.isEmpty()) {
            // Show appropriate message based on permission state
            showNoImagesMessage()
        } else {
            // Process selected images
            processImages(images)
        }
    }
}
```

This migration ensures your app provides the best user experience on Android 14 while maintaining
full backward compatibility.