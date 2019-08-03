## Changelog

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
