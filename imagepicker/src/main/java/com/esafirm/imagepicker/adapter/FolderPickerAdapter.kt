package com.esafirm.imagepicker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.esafirm.imagepicker.adapter.FolderPickerAdapter.FolderViewHolder
import com.esafirm.imagepicker.databinding.EfImagepickerItemFolderBinding
import com.esafirm.imagepicker.features.imageloader.ImageLoader
import com.esafirm.imagepicker.features.imageloader.ImageType
import com.esafirm.imagepicker.listeners.OnFolderClickListener
import com.esafirm.imagepicker.model.Folder
import com.esafirm.imagepicker.model.FolderType

class FolderPickerAdapter(
    context: Context,
    imageLoader: ImageLoader,
    private val folderClickListener: OnFolderClickListener
) : BaseListAdapter<FolderViewHolder>(context, imageLoader) {

    private val folders: MutableList<Folder> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = EfImagepickerItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders.getOrNull(position) ?: return

        holder.apply {
            name.text = folder.folderName

            if (folder.type == FolderType.Local) {
                imageLoader.loadImage(folder.images.first(), holder.image, ImageType.FOLDER)
                number.text = folder.images.size.toString()
            } else {
                number.text = "Pick from Google Photos, Dropbox and other storages"
            }
            itemView.setOnClickListener { folderClickListener(folder) }
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

    class FolderViewHolder(binding: EfImagepickerItemFolderBinding) : ViewHolder(binding.root) {
        val image = binding.image
        val name = binding.tvName
        val number = binding.tvNumber
    }
}