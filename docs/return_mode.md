# Return Mode

There's 4 mode available:

1. `ReturnMode.NONE` -> When image is picked, ImagePickerActivity will not dismissed even in Single Mode

2. `ReturnMode.ALL` -> When image is picked dismiss then deliver result

3. `ReturnMode.CAMERA_ONLY` -> When image is picked with Camera, dismiss then deliver the result

4. `ReturnMode.GALLERY_ONLY` -> Same as CAMERA_ONLY but with Gallery

You can define your selected mode with `setReturnMode()` method.
