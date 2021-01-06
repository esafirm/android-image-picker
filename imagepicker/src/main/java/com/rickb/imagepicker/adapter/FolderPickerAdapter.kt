package com.rickb.imagepicker.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rickb.imagepicker.R
import com.rickb.imagepicker.adapter.FolderPickerAdapter.FolderViewHolder
import com.rickb.imagepicker.features.imageloader.ImageLoader
import com.rickb.imagepicker.features.imageloader.ImageType
import com.rickb.imagepicker.listeners.OnFolderClickListener
import com.rickb.imagepicker.model.Folder
import kotlinx.android.synthetic.main.ef_imagepicker_item_folder.view.*

class FolderPickerAdapter(
    context: Context,
    imageLoader: ImageLoader,
    private val folderClickListener: OnFolderClickListener?
) : BaseListAdapter<FolderViewHolder>(context, imageLoader) {

    private val folders: MutableList<Folder> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val layout = inflater.inflate(
            R.layout.ef_imagepicker_item_folder,
            parent,
            false
        )
        return FolderViewHolder(layout)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders.getOrNull(position) ?: return

        imageLoader.loadImage(folder.images.first(), holder.image, ImageType.FOLDER)

        holder.apply {
            name.text = folder.folderName
            number.text = folder.images.size.toString()
            itemView.setOnClickListener { folderClickListener?.onFolderClick(folder) }
        }
    }

    fun setData(folders: List<Folder>?) {
        folders?.let {
            this.folders.clear()
            this.folders.addAll(folders)
        }

        notifyDataSetChanged()
    }

    override fun getItemCount() = folders.size

    class FolderViewHolder(itemView: View) : ViewHolder(itemView) {
        val image: ImageView = itemView.image
        val name: TextView = itemView.tv_name
        val number: TextView = itemView.tv_number
    }
}