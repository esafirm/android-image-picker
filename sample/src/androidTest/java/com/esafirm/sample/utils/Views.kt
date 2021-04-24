package com.esafirm.sample.utils

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.esafirm.sample.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf

object Views {
    fun waitFor(timeInMs: Long) {
        Thread.sleep(timeInMs)
    }

    /* --------------------------------------------------- */
    /* > Specific view */
    /* --------------------------------------------------- */

    fun pickImageButton(): ViewInteraction {
        return onView(
            allOf(withId(R.id.button_pick_image), withText("PICK IMAGE"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.bottom_container),
                        0),
                    0),
                isDisplayed()))
    }

    fun recyclersView(): ViewInteraction {
        return onView(
            allOf(withId(R.id.recyclerView),
                childAtPosition(
                    ViewMatchers.withClassName(Matchers.`is`("android.widget.RelativeLayout")),
                    2)))
    }

    fun pickerDoneButton(): ViewInteraction {
        return onView(
            allOf(withId(R.id.menu_done), withText("DONE"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.toolbar),
                        2),
                    1),
                isDisplayed()))
    }

    fun imageDetail(): ViewInteraction {
        return onView(
            allOf(ViewMatchers.withParent(allOf(withId(R.id.container),
                ViewMatchers.withParent(IsInstanceOf.instanceOf(ScrollView::class.java)))),
                isDisplayed(),
                isAssignableFrom(ImageView::class.java)
            ))
    }

    fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position)
            }
        }
    }
}