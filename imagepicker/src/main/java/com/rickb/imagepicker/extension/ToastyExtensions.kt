package com.rickb.imagepicker.extension

import android.content.Context
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.rickb.imagepicker.R
import es.dmoral.toasty.Toasty
import java.util.concurrent.TimeUnit

val DEFAULT_DURATION_LONG = TimeUnit.SECONDS.toMillis(3).toInt()

/**
 * @return a toast with colorPrimary as backgorund color.
 */
@JvmOverloads
fun primaryToast(context: Context, message: String, duration: Int = DEFAULT_DURATION_LONG): Toast {
    val blueColor = ResourcesCompat.getColor(context.resources, R.color.ef_colorPrimary, null)
    return coloredToast(context, message, blueColor, duration)
}

private fun coloredToast(context: Context, message: String, color: Int, duration: Int = DEFAULT_DURATION_LONG): Toast {
    return Toasty.custom(context, message, null, color, duration, false, true)
}
