package com.esafirm.rximagepicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.esafirm.rximagepicker.ShadowActivity.Companion.getStartIntent
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject

class RxImagePicker private constructor() {
    /* --------------------------------------------------- */
    /* > RxImagePicker */
    /* --------------------------------------------------- */
    private val subject = SerializedSubject<List<Image>, List<Image>>(PublishSubject.create())

    fun start(context: Context, imagePicker: ImagePicker): Observable<List<Image>> {
        startImagePicker(context, imagePicker)
        return subject
    }

    private fun startImagePicker(context: Context, imagePicker: ImagePicker) {
        val bundle = imagePicker.getIntent(context).extras
        val intent = getStartIntent(context, bundle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(intent)
    }

    fun onHandleResult(images: List<Image>) {
        subject.onNext(images)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: RxImagePicker? = null
        val instance: RxImagePicker
            get() {
                return INSTANCE ?: RxImagePicker().also {
                    INSTANCE = it
                }
            }
    }
}
