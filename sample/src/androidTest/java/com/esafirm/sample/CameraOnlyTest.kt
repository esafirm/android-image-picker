package com.esafirm.sample


import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.MediaStore
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.esafirm.sample.matchers.hasDrawable
import com.esafirm.sample.utils.ViewAsserts
import com.esafirm.sample.utils.Views
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.intents.BaristaIntents.mockAndroidCamera
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CameraOnlyTest {

    @Rule
    @JvmField
    var testRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var grantPermissionRule = GrantPermissionRule.grant(
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    private fun runCameraOnly() {
        Intents.init()
        mockAndroidCamera()
        clickOn(R.id.button_camera)
        Intents.release()
    }

    @Test
    fun cameraOnlyTest() {
        runCameraOnly()

        Views.waitFor(1_000)
        clickOn(R.id.text_view)

        ViewAsserts.imagePicked(Views.imageDetail())
        val imageView = Views.imageDetail()
        imageView.check(ViewAssertions.matches(isDisplayed()))
        imageView.check(ViewAssertions.matches(hasDrawable()))
    }

    @Test
    fun cameraOnlyCancelTest() {
        Intents.init()
        val cancelResult = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, Intent())
        intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE)).respondWith(cancelResult)
        clickOn(R.id.button_camera)
        Intents.release()

        assertDisplayed(R.id.button_camera)
    }
}
