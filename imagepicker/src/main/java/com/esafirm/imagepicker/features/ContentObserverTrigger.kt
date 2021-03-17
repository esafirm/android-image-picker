package com.esafirm.imagepicker.features

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class ContentObserverTrigger(
    private val contentResolver: ContentResolver,
    private val loadData: () -> Unit
) : LifecycleEventObserver {

    private var handler: Handler? = null
    private var observer: ContentObserver? = null

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            else -> {
                // Ignore others event
            }
        }
    }

    private fun onCreate() {
        if (handler == null) {
            handler = Handler()
        }

        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                loadData()
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            false,
            observer!!
        )
    }

    private fun onDestroy() {
        if (observer != null) {
            contentResolver.unregisterContentObserver(observer!!)
            observer = null
        }

        handler?.removeCallbacksAndMessages(null)
        handler = null
    }
}