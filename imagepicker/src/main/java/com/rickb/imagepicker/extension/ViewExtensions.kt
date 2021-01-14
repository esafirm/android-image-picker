package com.rickb.imagepicker.extension

import android.view.MenuItem
import android.view.View
import java.util.concurrent.TimeUnit
import com.jakewharton.rxbinding3.view.clicks
import com.rickb.imagepicker.extension.ViewExtensionsConfig.CLICK_DEBOUNCE_TIME
import io.reactivex.Observable

/**
 * Allows global configuration of the view extensions
 */
object ViewExtensionsConfig {
    /**
     * Amount of time to wait to receive more click emissions
     * */
    var CLICK_DEBOUNCE_TIME: Long = 600
}

/**
 * "Debounce" clicks using throttleFirst operator.
 * throttleFirst emits only the first item emitted by the source ObservableSource during
 * sequential time windows of the specified duration.
 *
 * @param windowDuration time to wait before emitting another click after emitting the last click
 * @param unit the unit of time of `windowDuration`
 * */
fun View.debounceClicks(windowDuration: Long = CLICK_DEBOUNCE_TIME, unit: TimeUnit = TimeUnit.MILLISECONDS): Observable<Unit> {
    return clicks().throttleFirst(windowDuration, unit)
}