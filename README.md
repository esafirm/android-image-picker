<p align="center">
	<img  src="https://github.com/esafirm/android-image-picker/blob/master/art/logo.png?raw=true" width="140" height="140"/> 
</p>

<h2 align="center">Android Image Picker</h2>
<h3 align="center">No config yet highly configurable image picker for Android</h3>

<center>

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-ImagePicker-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/4618)

[![](https://jitpack.io/v/esafirm/android-image-picker.svg)](https://jitpack.io/#esafirm/android-image-picker)

</center>

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
	implementation 'com.github.esafirm.android-image-picker:imagepicker:x.y.z'
	// If you have a problem with Glide, please use the same Glide version or simply open an issue
	implementation 'com.github.bumptech.glide:glide:4.5.0'
}
```

change `x.y.z` to version in the [release page](https://github.com/esafirm/android-image-picker/releases)

# Usage

For full example, please refer to the `sample` app.

Also you can browse the issue labeled as question [here](https://github.com/esafirm/android-image-picker/issues?utf8=%E2%9C%93&q=label%3Aquestion+)

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
    savePath = ImagePickerSavePath(Environment.getExternalStorageDirectory().path, isRelative = false) // can be a full path

    excludedImages = images.toFiles() // don't show anything on this selected images
    selectedImages = images  // original selected images, used in multi mode
}
```

If you want to call it outside `Activity` or `Fragment`, you can get the `Intent` with `createImagePickerIntent`

> Please note: handling in `onActivityResult` is not recommended since it's already deprecated in favor of the new result API

```kotlin
val intent = createImagePickerIntent(context, ImagePickerConfig())
startActivityForResult(intent, RC_IMAGE_PICKER)
```

## Receive result

when you're done picking images, result will be returned on launcher callback with type `List<Image>`. This list cannot be null but can be empty

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

- [Custom components](https://github.com/esafirm/android-image-picker/blob/master/docs/custom_components.md)
- [Using another image library](https://github.com/esafirm/android-image-picker/blob/master/docs/another_image_library.md)
- [Return mode](https://github.com/esafirm/android-image-picker/blob/master/docs/return_mode.md)
- [Save location](https://github.com/esafirm/android-image-picker/blob/master/docs/save_location.md)

# AndroidX and version 2.0.0 above

As version 2.0.0 above, we already use AndroidX artifact in our library.
If you have any trouble adding this version to your current project like [this](https://github.com/esafirm/android-image-picker/issues/226)

Please add this to your `gradle.properties` :

```
android.useAndroidX=true
android.enableJetifier=true
```

# Support Me!

I would make myself more commited to this repo and OSS works in general.

Would you help me achieving this goals?

<a href='https://ko-fi.com/M4M41RRE0' target='_blank'><img height='36' style='border:0px;height:36px;' src='https://cdn.ko-fi.com/cdn/kofi4.png?v=2' border='0' alt='Buy Me a Coffee at ko-fi.com' /></a>

# Credits

- AIP Logo by [anaufalm](https://github.com/anaufalm)

# Modification License

```
Copyright (c) 2016 Esa Firman

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```

# Original License

[The Original Image Picker](https://github.com/nguyenhoanglam/ImagePicker)

[You can find the original lincense here ](https://raw.githubusercontent.com/esafirm/ImagePicker/master/ORIGINAL_LICENSE)
