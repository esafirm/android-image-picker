package com.esafirm.imagepicker.helper

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import com.esafirm.imagepicker.R

object ViewUtils {
    fun getArrowIcon(context: Context): Drawable? {
        val backResourceId: Int = if (Build.VERSION.SDK_INT >= 17 && context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            // For right-to-left layouts, pick the drawable that points to the right (forward).
            R.drawable.ef_ic_arrow_forward
        } else {
            // For left-to-right layouts, pick the drawable that points to the left (back).
            R.drawable.ef_ic_arrow_back
        }
        return ContextCompat.getDrawable(context.applicationContext, backResourceId)
    }
}