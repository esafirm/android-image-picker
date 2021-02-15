package com.esafirm.imagepicker.helper

object IpCrasher {
    @JvmStatic
    fun openIssue(): Nothing {
        throw IllegalStateException("This should not happen. Please open an issue!")
    }
}