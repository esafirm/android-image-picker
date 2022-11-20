package com.esafirm.sample


import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.esafirm.sample.utils.Rules
import com.esafirm.sample.utils.ViewAsserts
import com.esafirm.sample.utils.Views
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class PickImageSingleTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val grantPermissionRule = Rules.AIP_PERMISSIONS

    @Test
    fun pickImage() {
        clickOn(R.id.switch_return_after_capture)
        clickOn(R.id.switch_single)

        Views.pickImageButton().perform(click())

        val recyclerView = Views.recyclersView()
        recyclerView.perform(actionOnItemAtPosition<ViewHolder>(0, click()))

        Views.waitFor(1_000)
        clickOn(R.id.text_view)

        val imageView = Views.imageDetail()
        ViewAsserts.imagePicked(imageView)
    }

}
