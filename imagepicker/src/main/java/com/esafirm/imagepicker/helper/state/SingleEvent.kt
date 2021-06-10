package com.esafirm.imagepicker.helper.state

class SingleEvent<out T>(private val value: T) {
    private var fetched: Boolean = false

    fun get(): T? {
        if (fetched) return null
        fetched = true
        return value
    }
}

inline fun <T> SingleEvent<T>?.fetch(block: T.() -> Unit) {
    val value = this?.get()
    if (value != null) {
        block.invoke(value)
    }
}

fun <T> T.asSingleEvent() = SingleEvent(this)