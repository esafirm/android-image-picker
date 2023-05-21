# Set Save Location

By default, ImagePicker will try to save the image generated from camera to `Environment.DIRECTORY_PICTURES` with directory name `"Camera"`. 

You can change the directory name by using:

```kotlin
val config = ImagePickerConfig {
    savePath = ImagePickerSavePath(dirName)
}
```

Or you can change the full path of the save location by using:

```kotlin
val config = ImagePickerConfig {
    savePath = ImagePickerSavePath(
        path = fullPath, 
        isRelative = false,
    )
}
```
