## Changelog

**[Unreleased] - Android 14 (API 34) Support**

✨ **Major Update: Full Android 14 Granular Media Permissions Support**

- **New Features:**
  - Full support for `READ_MEDIA_VISUAL_USER_SELECTED` permission
  - Automatic detection of partial vs full media access
  - Permission upgrade flow for better user experience
  - New `GranularPermissionConfig` for customizing permission behavior
  - Extension functions: `enableGranularPermissions()`, `requireFullMediaAccess()`,
    `minimalPermissionUI()`

- **Enhanced Permission Handling:**
  - `MediaPermissionHelper` class for centralized permission logic
  - Smart permission state detection across Android versions
  - Graceful degradation when only partial access is granted
  - Enhanced file loader to handle limited media access scenarios

- **Improved User Experience:**
  - Non-intrusive upgrade prompts for partial access users
  - Customizable messages for permission states
  - Backward compatible - no breaking changes
  - Works seamlessly on Android 13 and below

- **Documentation:**
  - Complete Android 14 migration guide
  - Updated sample app with granular permission examples
  - Enhanced README with Android 14 information

- **Technical Details:**
  - Added `READ_MEDIA_VISUAL_USER_SELECTED` permission to manifest
  - Enhanced `ImagePickerFragment` with granular permission logic
  - Updated `DefaultImageFileLoader` for partial access scenarios
  - New configuration options in `ImagePickerConfig`
  - Comprehensive permission state management

✅ **Fully backward compatible - no breaking changes**

**2.4.0**
- Add `getUri()` for convenient [#285](https://github.com/esafirm/android-image-picker/pull/285)
- Fixes for Android Q [#290](https://github.com/esafirm/android-image-picker/pull/290) [#293](https://github.com/esafirm/android-image-picker/pull/293)
- Fixed some typo [#291](https://github.com/esafirm/android-image-picker/pull/291)
- Show video duration on the image picker [#271](https://github.com/esafirm/android-image-picker/pull/271)
- Use github workflow instead of CircleCI
- Migrate sample to Kotlin [#295](https://github.com/esafirm/android-image-picker/pull/295)

Thanks to @hjchin @sr1dh4r @xpdlf1004 @LinX64 for the PR 🔥

**2.3.1**
- Fix un-clickable Snackbar button

**2.3.0**
- Fix failed to create image file [#262](https://github.com/esafirm/android-image-picker/pull/262)
- Handle external scoped [Glide issue](https://github.com/bumptech/glide/issues/3896)
- Bump target SDK to Q
- Bump Glide to 4.11.0

**2.2.0**
- Configurable ImageFileLoader
- New approach on how to set custom ImageLoader
- Fix and improvements

**2.1.0**
- Support video only picker 
- Fix 0 bytes image | `369ba31`
- AGP 3.4.2

**2.0.0**
- ImagePicker is now in `Fragment` and you can use it in custom UI (please see sample)
- Remove Retrolambda
- Compatibility with Glide 4.9.0
- Add Option to exclude GIF from image picker
- Bug fixes and improvements

Also, we integrate our repo with CircleCi and add issue templating, because we want to improve our development experience in general

**1.12.0 - New Return Mode API** 

> BREAKING CHANGES!!! 

- [New] Return Mode API `setReturnMode` 

```
Define the ImagePicker return behaviour
1. ReturnMode.NONE -> When image is picked, ImagePickerActivity will not dismissed even in Single Mode
2. ReturnMode.ALL -> When image is picked dismiss then deliver result
3. ReturnMode.CAMERA_ONLY -> When image is picked with Camera, dismiss then deliver the result
4. ReturnMode.GALLERY_ONLY -> Same as CAMERA_ONLY but with Gallery
 ```

So if you want to mimic the `setReturnAfterFirst` behavior, all you have to do is 

```
ImagePicker.create(activity).setReturnMode(ReturnMode.ALL).start()
``` 

 - `setReturnAfterFirst` is now obsolete 
 - [New] set toolbar arrow color with `toolbarArrowColor(int color)`
 - Rename `ImagePicker` methods
    - `folderTitle` -> `toolbarFolderTitle`
    - `imageTitle` -> `toolbarImageTitle` 
 - Add capability to start without a request code 


 So instead of this

 ```
 ImagePicker.cameraOnly().start(RC_CAMERA /* int */);
 ```
 
 Now you can do this

 ```
 ImagePicker.cameraOnly().start()
 ```

 BUT, you have to handle the result with the helper method from `ImagePicker`  

 ```java
  @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // do your things
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
```

You can still use the usual result check if you define the request code by yourself. 
 
- Add convenience method `ImagePicker.getFirstImageOrNull(Intent data)` to fetch only first image from the result or return `null` 

---

**1.11.0 - Add `exclude()` Function**

Now you can exclude image from being shown in the picker

```java
ImagePicker.create(this)
    .exclude(image)           // exclude everything in `List<Image>`
    .excludeFiles(files)      // or you can exclude everything in `List<File>` 
    .start(RQ)
```

**1.10.1 - Fixes**

- Glide and AppCompat now marked as implementation
- Glide fixes
- Internal fixes

**1.10.0 - Add new way to do camera only**

- Add new way to do camera only

```java
ImagePicker.cameraOnly().start(activity)
```

- Remove ImmediateCameraModule
- Bugfixes 🐛

**1.7.4 - More fixes and improvement**

- Fix `returnAfterFirst` not working in gallery selection
- Add config checker

**1.7.3 - Bug fixes**

- Fix back button issue when `setFolderMode` set to true
- Expose `ImagePickerConfig` in `ImagePicker`. Now you can override `getConfig()` before called by `getIntent()`

**1.7.2 - Internal changes & bugfix**

- Fix `Snackbar` issue when permission not granted
- Add toggle log feature

```
ImagePicker.enableLog(false)
```

**1.6.0 - Custom ImageLoader**

- Adding custom image loader
- Removing traditional `Intent` starter
