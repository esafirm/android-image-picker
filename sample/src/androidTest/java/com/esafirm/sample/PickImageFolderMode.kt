package com.esafirm.sample


import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.esafirm.sample.utils.ViewAsserts
import com.esafirm.sample.utils.Views
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
    val grantPermissionRule = GrantPermissionRule.grant("android.permission.WRITE_EXTERNAL_STORAGE")

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

}
