# Using Another Image Loader Library 

By default, Image picker is using [Glide](https://github.com/bumptech/glide) as its image loader library. 

You can change this by defining custom image loader. You can find the documentation in [here](https://github.com/esafirm/android-image-picker/blob/master/docs/custom_components.md)

Next thing you wanna do is excluding Glide library from ImagePicker. You can achieve this with Gradle. Ex: 

```groovy
implementation("com.github.esafirm.android-image-picker:imagepicker:x.y.z") {
    exclude group: 'com.github.bumptech.glide', module: 'glide'
})
``` 

You can find more about this in [here](https://github.com/esafirm/android-image-picker/issues/105). Thanks to [Galaxer](https://github.com/Galaxer) 🙏
