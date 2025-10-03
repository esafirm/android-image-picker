package com.esafirm.imagepicker.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Helper class to handle media permissions across different Android versions,
 * with special support for Android 14's granular media permissions.
 */
class MediaPermissionHelper(private val context: Context) {

    /**
     * Represents the different states of media permission access
     */
    enum class MediaPermissionState {
        /** Full access to all media (READ_MEDIA_IMAGES + READ_MEDIA_VIDEO granted) */
        FULL_ACCESS,

        /** Partial access to user-selected media only (READ_MEDIA_VISUAL_USER_SELECTED granted) */
        PARTIAL_ACCESS,

        /** No media access permissions granted */
        NO_ACCESS,

        /** Legacy permission model for Android < 14 */
        LEGACY_ACCESS
    }

    /**
     * Get the current media permission state based on Android version and granted permissions
     */
    fun getMediaPermissionState(): MediaPermissionState {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                checkApi34Permissions()
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                checkApi33Permissions()
            }

            else -> {
                checkLegacyPermissions()
            }
        }
    }

    /**
     * Get the appropriate permissions to request based on Android version
     */
    fun getPermissionsToRequest(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // For Android 14+, request both full access permissions
                // The system will show the granular permission dialog
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }

            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                    android.os.Environment.isExternalStorageLegacy() -> {
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            else -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Check if we can request an upgrade from partial to full access
     */
    fun canRequestPermissionUpgrade(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                getMediaPermissionState() == MediaPermissionState.PARTIAL_ACCESS
    }

    /**
     * Get permissions to request for upgrading from partial to full access
     */
    fun getUpgradePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            emptyArray()
        }
    }

    /**
     * Check if all requested permissions are granted
     */
    fun arePermissionsGranted(permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkApi34Permissions(): MediaPermissionState {
        val hasFullImages = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        val hasFullVideo = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_VIDEO
        ) == PackageManager.PERMISSION_GRANTED

        val hasPartialAccess = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) == PackageManager.PERMISSION_GRANTED

        return when {
            hasFullImages && hasFullVideo -> MediaPermissionState.FULL_ACCESS
            hasPartialAccess -> MediaPermissionState.PARTIAL_ACCESS
            else -> MediaPermissionState.NO_ACCESS
        }
    }

    private fun checkApi33Permissions(): MediaPermissionState {
        val hasImages = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        val hasVideo = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_VIDEO
        ) == PackageManager.PERMISSION_GRANTED

        return if (hasImages && hasVideo) {
            MediaPermissionState.LEGACY_ACCESS
        } else {
            MediaPermissionState.NO_ACCESS
        }
    }

    private fun checkLegacyPermissions(): MediaPermissionState {
        val legacyPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            android.os.Environment.isExternalStorageLegacy()
        ) {
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return if (arePermissionsGranted(legacyPermissions)) {
            MediaPermissionState.LEGACY_ACCESS
        } else {
            MediaPermissionState.NO_ACCESS
        }
    }

    companion object {
        /**
         * Check if the device supports granular media permissions (Android 14+)
         */
        fun supportsGranularPermissions(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        }
    }
}