package com.esafirm.imagepicker.view

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Interpolator
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.esafirm.imagepicker.R
import kotlinx.android.synthetic.main.ef_imagepikcer_snackbar.view.*

class SnackBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private val txtCaption get() = ef_snackbar_txt_bottom_caption
    private val btnAction get() = ef_snackbar_btn_action

    init {
        inflate(context, R.layout.ef_imagepikcer_snackbar, this)
        if (!isInEditMode) {
            val height = context.resources.getDimensionPixelSize(R.dimen.ef_height_snackbar)
            translationY = height.toFloat()
            alpha = 0f
        }
    }

    fun show(@StringRes textResId: Int, onClickListener: OnClickListener) {
        txtCaption.text = context.getString(textResId)
        btnAction.setOnClickListener(onClickListener)

        animate().translationY(0f)
            .setDuration(ANIM_DURATION.toLong())
            .setInterpolator(INTERPOLATOR)
            .alpha(1f)
    }

    companion object {
        private const val ANIM_DURATION = 200
        private val INTERPOLATOR: Interpolator = FastOutLinearInInterpolator()
    }
}