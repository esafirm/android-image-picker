package com.rickb.imagepicker.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import com.rickb.imagepicker.R
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
        val padding = context.resources.getDimensionPixelSize(R.dimen.ef_spacing_double)
        setPadding(padding, 0, padding, 0)

        if (!isInEditMode) {
            val height = context.resources.getDimensionPixelSize(R.dimen.ef_height_snackbar)
            translationY = height.toFloat()
            alpha = 0f
        }
    }

    fun setText(@StringRes textResId: Int) {
        txtCaption?.setText(textResId)
    }

    fun setOnActionClickListener(@StringRes textId: Int, onClickListener: OnClickListener) {
        val mTextId = if (textId == 0) R.string.ef_ok else textId
        btnAction?.let {
            setText(mTextId)
            setOnClickListener { v: View? -> hide { onClickListener.onClick(v) } }
        }
    }

    fun show(@StringRes textResId: Int, onClickListener: OnClickListener) {
        setText(textResId)
        setOnActionClickListener(0, onClickListener)
        ViewCompat.animate(this)
            .translationY(0f)
            .setDuration(ANIM_DURATION.toLong())
            .setInterpolator(INTERPOLATOR)
            .alpha(1f)
    }

    fun hide() {
        hide(null)
    }

    private fun hide(runnable: Runnable?) {
        ViewCompat.animate(this)
            .translationY(height.toFloat())
            .setDuration(ANIM_DURATION.toLong())
            .alpha(0.5f)
            .withEndAction(runnable)
    }

    companion object {
        private const val ANIM_DURATION = 200
        private val INTERPOLATOR: Interpolator = FastOutLinearInInterpolator()
    }
}