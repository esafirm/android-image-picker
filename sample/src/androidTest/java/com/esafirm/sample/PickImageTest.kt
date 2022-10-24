package com.esafirm.sample


import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.esafirm.sample.matchers.hasDrawable
import com.esafirm.sample.utils.Views
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PickImageTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule = GrantPermissionRule.grant("android.permission.WRITE_EXTERNAL_STORAGE")

    @Test
    fun pickImage() {
        val appCompatButton = Views.pickImageButton()
        appCompatButton.perform(click())

        val recyclerView = Views.recyclersView()
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        val actionMenuItemView = Views.pickerDoneButton()
        actionMenuItemView.perform(click())

        Views.waitFor(500)

        val appCompatTextView = onView(allOf(withId(R.id.text_view)))
        appCompatTextView.check(matches(isClickable()))
        appCompatTextView.perform(scrollTo(), click())

        val imageView = Views.imageDetail()
        imageView.check(matches(isDisplayed()))
        imageView.check(matches(hasDrawable()))
    }
}
