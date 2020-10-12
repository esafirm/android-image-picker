package com.esafirm.rximagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ImagePickerActivity

class ShadowActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, ImagePickerActivity::class.java).apply {
            intent.extras?.let { putExtras(it) }
        }
        startActivityForResult(intent, RC_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val images = ImagePicker.getImages(data)
        RxImagePicker.instance.onHandleResult(images)
        finish()
    }

    companion object {
        private const val RC_IMAGE_PICKER = 123

        /* --------------------------------------------------- */
        /* > Stater */
        /* --------------------------------------------------- */
        @JvmStatic
        fun getStartIntent(context: Context?, bundle: Bundle?): Intent {
            return Intent(context, ShadowActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                bundle?.let { putExtras(it) }
            }
        }
    }
}