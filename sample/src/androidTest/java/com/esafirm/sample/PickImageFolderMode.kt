package com.esafirm.sample


import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adevinta.android.barista.internal.util.resourceMatcher
import com.esafirm.sample.utils.Rules
import com.esafirm.sample.utils.ViewAsserts
import com.esafirm.sample.utils.Views
import com.esafirm.sample.utils.withViewCount
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PickImageFolderMode {

    @Rule
    @JvmField
    val activityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val grantPermissionRule = Rules.AIP_PERMISSIONS

    @Test
    fun pickImage() {
        // Activate switch
        clickOn(R.id.switch_folder_mode)

        Views.pickImageButton().perform(click())

        val recyclerView = Views.recyclersView()
        // Click folder
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))
        // Click image
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Click done button
        val actionMenuItemView = Views.pickerDoneButton()
        actionMenuItemView.perform(click())

        // Check image is picked
        Views.waitFor(1_000)
        clickOn(R.id.text_view)

        val imageView = Views.imageDetail()
        ViewAsserts.imagePicked(imageView)
    }

    @Test
    fun selectAll() {
        // Activate switch
        clickOn(R.id.switch_select_all)

        Views.pickImageButton().perform(click())

        val recyclerView = Views.recyclersView()
        // Click folder
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        // Click all button
        clickOn(R.id.menu_select_all)
        clickOn(R.id.menu_done)

        // Check image is picked
        Views.waitFor(1_000)
        Espresso.onView(R.id.text_view.resourceMatcher())
            .check(matches(object : TypeSafeMatcher<View?>() {
                override fun matchesSafely(item: View?): Boolean {
                    if (item !is TextView) return false
                    return item.text.toString().count { it == '\n' } == 9
                }

                override fun describeTo(description: Description) {
                    description.appendText("10 selected images expected")
                }
            }))
    }
}
