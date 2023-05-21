package com.esafirm.sample.utils

import android.os.Build
import androidx.test.rule.GrantPermissionRule

object Rules {
    val AIP_PERMISSIONS: GrantPermissionRule by lazy {
        // Tiramisu
        if (Build.VERSION.SDK_INT >= 31) {
            GrantPermissionRule.grant(
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VIDEO"
            )
        } else {
            GrantPermissionRule.grant(
                "android.permission.READ_EXTERNAL_STORAGE"
            )
        }
    }
}
