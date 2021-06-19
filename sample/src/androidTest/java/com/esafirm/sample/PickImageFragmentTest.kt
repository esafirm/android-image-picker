package com.esafirm.sample

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.esafirm.sample.utils.Views
import com.schibsted.spain.barista.assertion.BaristaImageViewAssertions.assertHasAnyDrawable
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4ClassRunner::class)
class PickImageFragmentTest {

    @Rule
    @JvmField
    val mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    val mGrantPermissionRule =
        GrantPermissionRule.grant("android.permission.WRITE_EXTERNAL_STORAGE")

    @Test
    fun pickImage() {
        clickOn(R.id.button_launch_fragment)
        clickOn(R.id.button_pick_fragment)

        // Click image on picker with folder mode enabled
        val recyclerView = Views.recyclersView()
        recyclerView.perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))
        recyclerView.perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        Views.waitFor(1_000)
        assertDisplayed(R.id.img_fragment)
        assertHasAnyDrawable(R.id.img_fragment)
    }

}