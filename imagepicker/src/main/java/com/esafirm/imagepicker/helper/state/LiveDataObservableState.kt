package com.esafirm.imagepicker.helper.state

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class LiveDataObservableState<T>(
        value: T,
        private val usePostValue: Boolean = false
) : ObservableState<T> {

    private val backingField: MutableLiveData<T> = MutableLiveData(value)

    /**
     * This is only use only when [usePostValue] is true
     */
    private var valueHolder: T = value

    override fun set(value: T) {
        if (usePostValue) {
            valueHolder = value
            backingField.postValue(value)
        } else {
            backingField.value = value
        }
    }

    override fun get(): T = if (usePostValue) {
        valueHolder
    } else {
        backingField.value!!
    }

    override fun observe(owner: LifecycleOwner, observer: (T) -> Unit) {
        backingField.reObserve(owner, { observer.invoke(it) })
    }

    override fun observeForever(observer: (T) -> Unit) {
        backingField.observeForever(observer)
    }

    private fun <T> LiveData<T>.reObserve(owner: LifecycleOwner, observer: Observer<T>) {
        removeObserver(observer)
        observe(owner, observer)
    }
}