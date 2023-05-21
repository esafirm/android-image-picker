package com.esafirm.imagepicker.model

class Folder(
    var folderName: String
) : BaseItem {

    var images: MutableList<Image> = mutableListOf()

    override fun getItemName() = folderName

}