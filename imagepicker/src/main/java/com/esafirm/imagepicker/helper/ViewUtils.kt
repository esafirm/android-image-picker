package com.esafirm.imagepicker.helper

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.esafirm.imagepicker.R

object ViewUtils {
    fun getArrowIcon(context: Context): Drawable? {
        val backResourceId: Int =
            if (context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                // For right-to-left layouts, pick the drawable that points to the right (forward).
                R.drawable.ef_ic_arrow_forward
            } else {
                // For left-to-right layouts, pick the drawable that points to the left (back).
                R.drawable.ef_ic_arrow_back
            }
        return ContextCompat.getDrawable(context.applicationContext, backResourceId)
    }
}


fun Context.getColorPrimary(): Int {
    val typedValue = TypedValue()
    val theme = this.theme
    val resolved = theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
    return if (resolved) {
        if (typedValue.resourceId != 0) {
            ContextCompat.getColor(this, typedValue.resourceId)
        } else {
            typedValue.data
        }
    } else {
        return 0
    }
}

fun Context.isActionBarEnabled(): Boolean {
    val typedValue = TypedValue()
    val hasAttribute =
        theme.resolveAttribute(androidx.appcompat.R.attr.windowActionBar, typedValue, true)
    return hasAttribute && typedValue.data != 0
}