package com.esafirm.imagepicker.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by hoanglam on 9/5/16.
 */
class SquareFrameLayout(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}