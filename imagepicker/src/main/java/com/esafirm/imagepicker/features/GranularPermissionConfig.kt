package com.esafirm.imagepicker.features

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Configuration for granular media permissions behavior in Android 14+
 */
@Parcelize
data class GranularPermissionConfig(
    /**
     * Whether to show the permission upgrade prompt when user has partial access.
     * Default: true
     */
    val showUpgradePrompt: Boolean = true,

    /**
     * Whether to automatically request full access on first launch.
     * If false, will request partial access first and show upgrade option.
     * Default: false (request partial access first for better UX)
     */
    val requestFullAccessFirst: Boolean = false,

    /**
     * Custom message to show when user has partial access.
     * If null, uses default message.
     */
    val partialAccessMessage: String? = null,

    /**
     * Whether to show a badge or indicator when in partial access mode.
     * Default: true
     */
    val showPartialAccessIndicator: Boolean = true,

    /**
     * Whether to allow the image picker to work with partial access.
     * If false, will always require full access.
     * Default: true
     */
    val allowPartialAccess: Boolean = true
) : Parcelable {

    companion object {
        /**
         * Default configuration that provides the best user experience
         */
        fun default() = GranularPermissionConfig()

        /**
         * Configuration that always requests full access (legacy behavior)
         */
        fun requireFullAccess() = GranularPermissionConfig(
            showUpgradePrompt = false,
            requestFullAccessFirst = true,
            allowPartialAccess = false
        )

        /**
         * Configuration that never shows upgrade prompts (minimal UI)
         */
        fun minimal() = GranularPermissionConfig(
            showUpgradePrompt = false,
            showPartialAccessIndicator = false
        )
    }
}