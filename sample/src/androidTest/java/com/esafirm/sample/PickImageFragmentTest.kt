package com.esafirm.sample

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.adevinta.android.barista.assertion.BaristaImageViewAssertions.assertHasAnyDrawable
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.esafirm.sample.utils.Views
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PickImageFragmentTest {

    @Rule
    @JvmField
    val activityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val grantPermissionRule =
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
