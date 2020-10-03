# Set Save Location

By default, ImagePicker will try to save the image generated from camera to `Environment.DIRECTORY_PICTURES` with directory name `"Camera"`. 

You can change the directory name only by using:

```java
ImagePicker.create(activity).imageDirectory(String dirName)
``` 

Or you can change the full path of the save location by using:

```java
ImagePicker.create(activity).imageFullDirectory(String fullPath)
```
