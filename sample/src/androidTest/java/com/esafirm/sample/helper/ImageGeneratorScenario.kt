package com.esafirm.sample.helper


import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.esafirm.sample.MainActivity
import com.esafirm.sample.utils.ImageGenerator
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Remove ignore for generating only
 */
@LargeTest
@Ignore
@RunWith(AndroidJUnit4::class)
class ImageGeneratorScenario {

    @Rule
    @JvmField
    var testRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    var grantPermissionRule = GrantPermissionRule.grant(
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    @Test
    fun generateImages() {
        val generator = ImageGenerator()
        generator.generateImages(30_000)
    }
}
