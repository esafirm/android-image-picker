package com.esafirm.imagepicker.helper.diff

import androidx.recyclerview.widget.DiffUtil

typealias ValueComparison<T> = (T, T) -> Boolean

class DefaultValueComparison<T> : ValueComparison<T> {
    override fun invoke(p1: T, p2: T): Boolean {
        return p1 == p2
    }
}

class SimpleDiffUtilCallBack<T>(
    private val areItemTheSame: ValueComparison<T> = DefaultValueComparison(),
    private val areContentTheSame: ValueComparison<T> = DefaultValueComparison()
) : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return areItemTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return areContentTheSame(oldItem, newItem)
    }
}