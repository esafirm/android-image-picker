package com.esafirm.sample

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class GrayscaleTransformation : BitmapTransformation() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        val config = if (toTransform.config != null) toTransform.config else Bitmap.Config.ARGB_8888
        val bitmap = pool[width, height, config]
        val canvas = Canvas(bitmap)
        val saturation = ColorMatrix()
        saturation.setSaturation(0f)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(saturation)
        canvas.drawBitmap(toTransform, 0f, 0f, paint)
        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID.toByteArray())
    }

    companion object {
        private const val ID = "GrayscaleTransformation()"
    }
}