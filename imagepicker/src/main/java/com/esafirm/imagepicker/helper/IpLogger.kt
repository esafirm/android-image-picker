package com.esafirm.imagepicker.helper

import android.util.Log

object IpLogger {

    private const val TAG = "ImagePicker"

    private var isEnable = true

    fun setEnable(enable: Boolean) {
        isEnable = enable
    }

    fun d(message: String?) {
        if (isEnable && message != null) {
            Log.d(TAG, message)
        }
    }

    fun e(message: String?) {
        if (isEnable && message != null) {
            Log.e(TAG, message)
        }
    }

    fun w(message: String?) {
        if (isEnable && message != null) {
            Log.w(TAG, message)
        }
    }
}