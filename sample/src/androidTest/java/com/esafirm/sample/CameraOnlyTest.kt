package com.esafirm.sample


import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import com.esafirm.sample.matchers.hasDrawable
import com.esafirm.sample.utils.Views
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

    @Test
    fun cameraOnlyTestTwo() {
        Intents.init()
        mockAndroidCamera()
        clickOn(R.id.button_camera)
        Intents.release()

        Views.waitFor(1_000)
        clickOn(R.id.text_view)

        val imageView = Views.imageDetail()
        imageView.check(ViewAssertions.matches(isDisplayed()))
        imageView.check(ViewAssertions.matches(hasDrawable()))
    }
}
