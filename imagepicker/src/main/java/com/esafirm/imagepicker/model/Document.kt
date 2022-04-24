package com.esafirm.imagepicker.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Document(val uri: Uri) : Parcelable
