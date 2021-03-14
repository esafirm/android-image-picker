package com.esafirm.imagepicker.helper

import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.shouldBe
import org.junit.jupiter.api.Test

class ConfigUtilsTest {

    @Test
    fun `camera action return mode should be valid`() {
        val cameraConfig = CameraOnlyConfig()
        ConfigUtils.shouldReturn(cameraConfig, true) shouldBe true

        val config = ImagePickerConfig(
            returnMode = ReturnMode.CAMERA_ONLY
        )
        ConfigUtils.shouldReturn(config, true) shouldBe true
        ConfigUtils.shouldReturn(config, false) shouldBe false
    }
}