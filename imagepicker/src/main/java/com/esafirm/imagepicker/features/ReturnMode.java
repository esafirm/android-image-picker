package com.esafirm.imagepicker.features;

/**
 * Define the ImagePicker return behaviour
 * NONE -> When image is picked, ImagePickerActivity will not dismissed even in Single Mode
 * ALL -> When image is picked dismiss then deliver result
 * CAMERA_ONLY -> When image is picked with Camera, dismiss then deliver the result
 * GALLERY_ONLY -> Same as CAMERA_ONLY but with Gallery
 */
public enum ReturnMode {
    NONE,
    ALL,
    CAMERA_ONLY,
    GALLERY_ONLY
}
