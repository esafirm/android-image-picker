package com.esafirm.imagepicker.model

import java.util.ArrayList

/**
 * Created by boss1088 on 8/22/16.
 */
class Folder(var folderName: String) {
    var images: MutableList<Image> = mutableListOf()
}