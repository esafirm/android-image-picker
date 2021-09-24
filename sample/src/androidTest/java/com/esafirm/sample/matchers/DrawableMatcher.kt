package com.esafirm.sample.matchers

import android.view.View
import android.widget.ImageView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class DrawableMatcher(private val expected: Int) : TypeSafeMatcher<View>() {

    override fun matchesSafely(target: View): Boolean {
        val imageView = target as ImageView
        if (expected == EMPTY) {
            return imageView.drawable == null
        }
        if (expected == ANY) {
            return imageView.drawable != null
        }
        return false
    }

    override fun describeTo(description: Description) {}

    companion object {
        const val EMPTY = -1
        const val ANY = -2
    }
}

fun hasDrawable(): Matcher<View> {
    return DrawableMatcher(DrawableMatcher.ANY)
}
