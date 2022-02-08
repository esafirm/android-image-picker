package com.esafirm.imagepicker.model

enum class FolderType {
    Local,
    Shared
}

class Folder(var folderName: String) {
    var images: MutableList<Image> = mutableListOf()
    var type: FolderType = FolderType.Local
}