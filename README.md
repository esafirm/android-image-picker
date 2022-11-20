<p align="center">
	<img  src="https://github.com/esafirm/android-image-picker/blob/master/art/logo.png?raw=true" width="140" height="140"/> 
</p>

<h2 align="center">Android Image Picker</h2>
<h4 align="center">No config yet highly configurable image picker for Android</h3>

<p align="center">
  <a href="https://android-arsenal.com/details/1/4618">
    <img src="https://img.shields.io/badge/Android%20Arsenal-ImagePicker-brightgreen.svg?style=flat" alt="Android Arsenal - ImagePicker" />
  </a>

  <a href="https://jitpack.io/#esafirm/android-image-picker">
    <img src="https://jitpack.io/v/esafirm/android-image-picker.svg" alt="jitpack - android image picker" />
  </a>
</p>

# Screenshot

<details>
	<summary>Click to see how image picker looks…</summary>
<img 
src="https://raw.githubusercontent.com/esafirm/android-image-picker/master/art/ss.gif" height="460" width="284"/>
</details>

## Download

Add this to your project's `build.gradle`

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

And add this to your module's `build.gradle`

```groovy
dependencies {
    implementation 'com.github.esafirm:android-image-picker:x.y.z'
}
```

change `x.y.z` to version in the [release page](https://github.com/esafirm/android-image-picker/releases)

# Usage

For full example, please refer to the `sample` app.

Also you can browse the issue labeled as
question [here](https://github.com/esafirm/android-image-picker/issues?utf8=%E2%9C%93&q=label%3Aquestion+)

## Start image picker activity

The simplest way to start

```kotlin
val launcher = registerImagePicker {
    // handle result here
}

launcher.launch()
```

Complete features of what you can do with ImagePicker

```kotlin
val config = ImagePickerConfig {
    mode = ImagePickerMode.SINGLE // default is multi image mode
    language = "in" // Set image picker language
    theme = R.style.ImagePickerTheme

    // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
    returnMode = if (returnAfterCapture) ReturnMode.ALL else ReturnMode.NONE

    isFolderMode = folderMode // set folder mode (false by default)
    isIncludeVideo = includeVideo // include video (false by default)
    isOnlyVideo = onlyVideo // include video (false by default)
    arrowColor = Color.RED // set toolbar arrow up color
    folderTitle = "Folder" // folder selection title
    imageTitle = "Tap to select" // image selection title
    doneButtonText = "DONE" // done button text
    limit = 10 // max images can be selected (99 by default)
    isShowCamera = true // show camera or not (true by default)
    savePath = ImagePickerSavePath("Camera") // captured image directory name ("Camera" folder by default)
    savePath =
        ImagePickerSavePath(Environment.getExternalStorageDirectory().path, isRelative = false) // can be a full path

    excludedImages = images.toFiles() // don't show anything on this selected images
    selectedImages = images  // original selected images, used in multi mode
}
```

If you want to call it outside `Activity` or `Fragment`, you can get the `Intent` with `createImagePickerIntent`

> Please note: handling in `onActivityResult` is not recommended since it's already deprecated in favor of the new
> result API

```kotlin
val intent = createImagePickerIntent(context, ImagePickerConfig())
startActivityForResult(intent, RC_IMAGE_PICKER)
```

## Receive result

when you're done picking images, result will be returned on launcher callback with type `List<Image>`. This list cannot
be null but can be empty

```kotlin
val launcher = registerImagePicker { result: List<Image> ->
    result.forEach { image ->
        println(image)
    }
}    
```

## Camera Only

Use `CameraOnlyConfig` instead of `ImagePickerConfig`

```kotlin
val launcher = registerImagePicker { }
launcher.launch(CameraOnlyConfig())
```

You also still can use the `DefaultCameraModule` but discouraged to do it.

# Wiki

- [Custom components](https://github.com/esafirm/android-image-picker/blob/main/docs/custom_components.md)
- [Using another image library](https://github.com/esafirm/android-image-picker/blob/main/docs/another_image_library.md)
- [Return mode](https://github.com/esafirm/android-image-picker/blob/main/docs/return_mode.md)
- [Save location](https://github.com/esafirm/android-image-picker/blob/main/docs/save_location.md)

# Version 2.x.x

If you still use the previous version, you can check `2.x` branch
in [here](https://github.com/esafirm/android-image-picker/tree/2.x)

# Support Me!

I would make myself more commited to this repo and OSS works in general.

Would you help me achieving this goals?

<a href='https://ko-fi.com/M4M41RRE0' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://cdn.ko-fi.com/cdn/kofi4.png?v=2' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

# Credits

- AIP Logo by [anaufalm](https://github.com/anaufalm)

# License

MIT @ Esa Firman
