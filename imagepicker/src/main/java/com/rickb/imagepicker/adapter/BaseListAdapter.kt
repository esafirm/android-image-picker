package com.rickb.imagepicker.adapter

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.rickb.imagepicker.features.imageloader.ImageLoader

abstract class BaseListAdapter<T : RecyclerView.ViewHolder>(
    val context: Context,
    val imageLoader: ImageLoader
) : RecyclerView.Adapter<T>() {
    val inflater: LayoutInflater = LayoutInflater.from(context)
}