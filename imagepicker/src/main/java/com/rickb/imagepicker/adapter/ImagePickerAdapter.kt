package com.rickb.imagepicker.adapter

import android.content.Context
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rickb.imagepicker.R
import com.rickb.imagepicker.features.imageloader.ImageLoader
import com.rickb.imagepicker.features.imageloader.ImageType
import com.rickb.imagepicker.helper.ImagePickerUtils
import com.rickb.imagepicker.listeners.OnImageClickListener
import com.rickb.imagepicker.listeners.OnImageSelectedListener
import com.rickb.imagepicker.listeners.OnTotalSizeLimitReachedListener
import com.rickb.imagepicker.model.Image
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.ef_imagepicker_header.view.*
import kotlinx.android.synthetic.main.ef_imagepicker_item_image.view.*
import java.io.File
import java.util.*

class ImagePickerAdapter(
        context: Context,
        imageLoader: ImageLoader,
        selectedImages: List<Image>,
        private val itemClickListener: OnImageClickListener,
        private val maxTotalSizeLimit: Double?,
        private val maxTotalSelectionsLimit: Int?,
) : BaseListAdapter<ImagePickerAdapter.BaseImagePickerViewHolder>(context, imageLoader) {

    private val items: MutableList<PickerItem> = mutableListOf()
    val selectedImages: MutableList<Image> = mutableListOf()

    private var imageSelectedListener: OnImageSelectedListener? = null
    private var onTotalSizeLimitReachedListener: OnTotalSizeLimitReachedListener? = null
    private val videoDurationHolder = HashMap<Long, String?>()

    private var wasTotalSizeLimitReached = false
    private var wasTotalSelectionLimitReached = false

    init {
        if (selectedImages.isNotEmpty()) {
            this.selectedImages.addAll(selectedImages)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseImagePickerViewHolder {
        if (viewType == VIEW_TYPE_HEADER) {
            val layout = inflater.inflate(
                    R.layout.ef_imagepicker_header,
                    parent,
                    false
            )
            return HeaderViewHolder(layout)
        } else {
            val layout = inflater.inflate(
                    R.layout.ef_imagepicker_item_image,
                    parent,
                    false
            )
            return ImageViewHolder(layout)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is ImageHeaderPlaceHolder) {
            VIEW_TYPE_HEADER
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(viewHolder: BaseImagePickerViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        if (viewType == VIEW_TYPE_HEADER) {
            onBindHeaderViewHolder(viewHolder as HeaderViewHolder, position)
        } else if (viewType == VIEW_TYPE_ITEM) {
            val image = items[position] as Image
            onBindImageViewHolder(viewHolder as ImageViewHolder, image)
        } else throw IllegalStateException("Unimplemented viewType for item at position $position.")
    }

    private fun onBindHeaderViewHolder(viewHolder: HeaderViewHolder, position: Int) {
        val headerItem = items[position] as ImageHeaderPlaceHolder
        val context = viewHolder.textView.context
        viewHolder.apply {
            val weeksAgo = headerItem.weeksAgo
            textView.text = when (weeksAgo) {
                0 -> {
                    if (headerItem.isRecent) {
                        context.getString(R.string.ef_header_recent)
                    } else {
                        context.getString(R.string.ef_header_last_week)
                    }
                }
                1 -> context.getString(R.string.ef_header_1_week_ago)
                else -> context.getString(R.string.ef_header_weeks_ago, weeksAgo)
            }
        }
    }

    private fun onBindImageViewHolder(viewHolder: ImageViewHolder, image: Image) {
        val isSelected = isSelected(image)
        imageLoader.loadImage(image, viewHolder.imageView, ImageType.GALLERY)

        var showFileTypeIndicator = false
        var fileTypeLabel: String? = ""

        if (ImagePickerUtils.isGifFormat(image)) {
            fileTypeLabel = context.resources.getString(R.string.ef_gif)
            showFileTypeIndicator = true
        }

        if (ImagePickerUtils.isVideoFormat(image)) {
            if (!videoDurationHolder.containsKey(image.id)) {
                videoDurationHolder[image.id] = ImagePickerUtils.getVideoDurationLabel(
                        context, File(image.path)
                )
            }

            fileTypeLabel = videoDurationHolder[image.id]
            showFileTypeIndicator = true
        }

        viewHolder.apply {
            fileTypeIndicator.text = fileTypeLabel
            fileTypeIndicator.visibility = if (showFileTypeIndicator) View.VISIBLE else View.GONE

            val backgroundColorResId = if (isSelected) R.color.ef_black else R.color.ef_white
            alphaView.setBackgroundColor(ResourcesCompat.getColor(context.resources, backgroundColorResId, null))
            alphaView.alpha = if (isSelected || isMaxTotalSizeReached() || isMaxTotalSelectionsReached()) 0.5f else 0f

            itemView.setOnClickListener {
                val shouldSelect = itemClickListener.onImageClick(isSelected)

                if (isSelected) {
                    removeSelectedImage(image, position)
                } else if (shouldSelect) {
                    addSelected(image, position)
                }
            }
            container?.foreground = if (isSelected) ContextCompat.getDrawable(
                    context,
                    R.drawable.ef_ic_done_white
            ) else null
        }
    }

    private fun isSelected(image: Image): Boolean {
        return selectedImages.any { it.path == image.path }
    }

    override fun getItemCount() = items.size

    fun setData(images: List<Image>) {
        val imagesAndHeaders = addHeaders(images)

        this.items.clear()
        this.items.addAll(imagesAndHeaders)
    }

    private fun addHeaders(images: List<Image>): List<PickerItem> = mutableListOf<PickerItem>()
            .apply {
                var lastAddedHeaderTimeStamp = 0

                // If there are recent images this will be set to true until the first not-recent image is added (and so the 'last week' header is added).
                var needsAdditionalHeader = false

                images
                        .sortedByDescending { it.lastChangedTimestamp }
                        .forEachIndexed { index, image ->
                            val weeksAgo = image.weeksAgo
                            val isDifferentWeeksAgo = weeksAgo != lastAddedHeaderTimeStamp

                            // Add a header item before index 0 and for each image that is more than the amount of weeks ago of the previopusly added header.
                            if (index == 0) {
                                if (image.isRecent) {
                                    needsAdditionalHeader = true
                                }
                                add(ImageHeaderPlaceHolder(image.lastChangedTimestamp))
                                lastAddedHeaderTimeStamp = weeksAgo
                            } else if (isDifferentWeeksAgo) {
                                add(ImageHeaderPlaceHolder(image.lastChangedTimestamp))
                                lastAddedHeaderTimeStamp = weeksAgo
                            }

                            if (needsAdditionalHeader && !image.isRecent) {
                                // the first header will be the 'Recent' header. Add another header before the first item that is not recent.
                                add(ImageHeaderPlaceHolder(image.lastChangedTimestamp))
                                lastAddedHeaderTimeStamp = weeksAgo
                                needsAdditionalHeader = false
                            }

                            add(image)
                        }
            }

    private fun addSelected(image: Image, position: Int) {
        mutateSelection {
            selectedImages.add(image)
            notifyItemChanged(position)
        }
    }

    private fun removeSelectedImage(image: Image, position: Int) {
        mutateSelection {
            selectedImages.remove(image)
            notifyItemChanged(position)
        }
    }

    fun removeAllSelectedSingleClick() {
        mutateSelection {
            selectedImages.clear()
            notifyDataSetChanged()
        }
    }

    private fun mutateSelection(runnable: Runnable) {
        runnable.run()
        if (!isMaxTotalSizeReached()) {
            imageSelectedListener?.onSelectionUpdate(selectedImages)
        } else {
            onTotalSizeLimitReachedListener?.onTotalSizeLimitReached()
        }
        // Update all items after a selection change, only if the total limit was reached and not is not anymore OR the total limit was not reached but now is.
        if (wasTotalSizeLimitReached xor isMaxTotalSizeReached()) {
            wasTotalSizeLimitReached = isMaxTotalSizeReached()
            notifyDataSetChanged()
        }
        if (wasTotalSelectionLimitReached xor isMaxTotalSelectionsReached()) {
            wasTotalSelectionLimitReached = isMaxTotalSelectionsReached()
            notifyDataSetChanged()
        }
    }

    fun isMaxTotalSizeReached() =
            getTotalSelectedFileSize() > (maxTotalSizeLimit
                    ?: Double.MAX_VALUE) * MB_TO_BYTES_CONVERSION_MULTIPLIER

    fun isMaxTotalSelectionsReached() =
            selectedImages.size >= (maxTotalSelectionsLimit ?: Integer.MAX_VALUE)

    /**
     * @return the file size in bytes of all selected media together.
     */
    private fun getTotalSelectedFileSize(): Long {
        var totalFileSize: Long = 0
        for (image in selectedImages) {
            val file = File(image.path)
            totalFileSize += file.length()
        }
        return totalFileSize
    }

    fun setImageSelectedListener(imageSelectedListener: OnImageSelectedListener?) {
        this.imageSelectedListener = imageSelectedListener
    }

    fun setOnTotalSizeLimitReachedListener(onTotalSizeLimitReachedListener: OnTotalSizeLimitReachedListener?) {
        this.onTotalSizeLimitReachedListener = onTotalSizeLimitReachedListener
    }

    class HeaderViewHolder(itemView: View) : BaseImagePickerViewHolder(itemView) {
        val textView: TextView = itemView.header_text_view
    }

    class ImageViewHolder(itemView: View) : BaseImagePickerViewHolder(itemView) {
        val imageView: ImageView = itemView.image_view
        val alphaView: View = itemView.view_alpha
        val fileTypeIndicator: TextView = itemView.ef_item_file_type_indicator
        val container: FrameLayout? = itemView as? FrameLayout
    }

    open class BaseImagePickerViewHolder(itemView: View) : ViewHolder(itemView)

    companion object {
        // 1 MB = 1048576 Bytes.
        const val MB_TO_BYTES_CONVERSION_MULTIPLIER = 1048576

        const val VIEW_TYPE_ITEM = 1
        const val VIEW_TYPE_HEADER = 2

        private const val MILLIS_IN_DAY = 1000 * 60 * 60 * 24
        private const val MILLIS_IN_WEEK = MILLIS_IN_DAY * 7

        @Parcelize
        open class PickerItem(private val lastChangedTimestamp: Long) : Parcelable {
            /**
             * How many weeks ago this image's file was last edited.
             */
            val weeksAgo: Int
                get() {
                    return ((System.currentTimeMillis() - lastChangedTimestamp) / MILLIS_IN_WEEK).toInt()
                }

            val isRecent: Boolean
                get() {
                    val daysAgo = ((System.currentTimeMillis() - lastChangedTimestamp) / MILLIS_IN_DAY).toInt()
                    return daysAgo <= Image.MAX_DAYS_AGO_FOR_RECENT
                }
        }

        @Parcelize
        class ImageHeaderPlaceHolder(val timestamp: Long) : PickerItem(timestamp)
    }
}