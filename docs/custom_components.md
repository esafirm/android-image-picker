# Custom Components

You also can change how to process the image and how to get the image files through `ImageLoader` and `ImageFileLoader`
To change this, simply set it on `ImagePickerComponentHolder`

```java
ImagePickerComponentHolder.getInstance()
	.setImageLoader(new GrayScaleImageLoader())
	.setImageFileLoader(new WebpImageFileLoader())
```	

Happy coding!
