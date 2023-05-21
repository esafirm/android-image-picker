package com.esafirm.sample

import android.app.Application
import android.os.Build
import android.os.StrictMode

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Check if we ever get this issue
        // https://github.com/esafirm/android-image-picker/issues/394
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectNonSdkApiUsage()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
