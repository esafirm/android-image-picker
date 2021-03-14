package com.esafirm.imagepicker.helper

internal object IpCrasher {
    @JvmStatic
    fun openIssue(): Nothing {
        throw IllegalStateException("This should not happen. Please open an issue!")
    }
}