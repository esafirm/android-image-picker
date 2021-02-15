package com.esafirm.imagepicker.helper

import android.content.Context
import android.preference.PreferenceManager

class ImagePickerPreferences(private val context: Context) {
    /**
     * Set a permission is requested
     */
    fun setPermissionRequested(permission: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putBoolean(permission, true)
        editor.apply()
    }

    /**
     * Check if a permission is requestted or not (false by default)
     */
    fun isPermissionRequested(permission: String?): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getBoolean(permission, false)
    }

    companion object {
        const val PREF_WRITE_EXTERNAL_STORAGE_REQUESTED = "writeExternalRequested"
    }
}