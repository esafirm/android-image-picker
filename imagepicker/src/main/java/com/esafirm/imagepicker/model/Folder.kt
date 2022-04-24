package com.esafirm.imagepicker.model

enum class FolderType {
    LOCAL,
    SHARED
}

class Folder(
    val folderName: String,
    val images: MutableList<Image> = mutableListOf(),
    val type: FolderType = FolderType.LOCAL
)