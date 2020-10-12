package com.esafirm.rximagepicker

import android.content.Context
import android.content.Intent
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ImagePickerActivity
import com.esafirm.imagepicker.model.Image
import com.esafirm.rximagepicker.ShadowActivity.Companion.getStartIntent
import rx.Observable
import rx.Subscriber
import rx.subscriptions.Subscriptions

class ImagePickerObservable(
    context: Context,
    private val imagePicker: ImagePicker
) : Observable.OnSubscribe<List<Image?>?> {

    private val context: Context = context.applicationContext

    override fun call(subscriber: Subscriber<in List<Image?>?>) {
        startImagePicker()
        subscriber.apply {
            add(Subscriptions.create { finishImagePicker() })
            onCompleted()
        }
    }

    private fun finishImagePicker() {
        val intent = Intent(context, ImagePickerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        context.startActivity(intent)
    }

    private fun startImagePicker() {
        val bundle = imagePicker.getIntent(context).extras
        val intent = getStartIntent(context, bundle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(intent)
    }
}