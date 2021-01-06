package com.rickb.imagepicker.model

/**
 * Created by boss1088 on 8/22/16.
 */
class Folder(var folderName: String) {
    var images: MutableList<Image> = mutableListOf()
}