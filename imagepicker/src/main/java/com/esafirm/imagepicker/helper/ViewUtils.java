package com.esafirm.imagepicker.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.ViewTreeObserver;

import com.esafirm.imagepicker.R;

public class ViewUtils {

    public static Drawable getArrowIcon(Context context) {
        final int backResourceId;
        if (Build.VERSION.SDK_INT >= 17 && context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            // For right-to-left layouts, pick the drawable that points to the right (forward).
            backResourceId = R.drawable.ef_ic_arrow_forward;
        } else {
            // For left-to-right layouts, pick the drawable that points to the left (back).
            backResourceId = R.drawable.ef_ic_arrow_back;
        }
        return ContextCompat.getDrawable(context.getApplicationContext(), backResourceId);
    }

    public static void onPreDraw(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                runnable.run();
                return false;
            }
        });
    }
}
