# Custom Components

You also can change how to process the image and how to get the image files through `ImageLoader` and `ImageFileLoader`
To change this, first, create custom `ImagePickerComponents`.
You could inherit from `DefaultImagePickerComponents` or directly implements from `ImagePickerComponents` interface.

```kotlin
class CustomImagePickerComponents(
    context: Context
) : DefaultImagePickerComponents(context.applicationContext) {
    override val imageLoader: ImageLoader
        get() = GrayscaleImageLoader()
}
```

Then, use it by calling `setInternalComponent` on `ImagePickerComponentsHolder`.
```kotlin
ImagePickerComponentsHolder.setInternalComponent(
    CustomImagePickerComponents(context)
)
```

Happy coding!
