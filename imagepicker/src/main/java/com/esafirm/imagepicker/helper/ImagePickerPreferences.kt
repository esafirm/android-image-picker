package com.esafirm.imagepicker.helper

import android.content.Context
import android.preference.PreferenceManager

class ImagePickerPreferences(context: Context) {

    companion object {
        private const val KEY_PERMISSION_GRANTED = "Key.WritePermissionGranted"
    }

    // TODO: Change this with data store
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Set a permission is requested
     */
    fun setPermissionIsRequested() {
        preferences.edit()
            .putBoolean(KEY_PERMISSION_GRANTED, true)
            .apply()
    }

    /**
     * Check if a permission is requestted or not (false by default)
     */
    fun isPermissionRequested(): Boolean {
        return preferences.getBoolean(KEY_PERMISSION_GRANTED, false)
    }
}