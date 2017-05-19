package com.esafirm.imagepicker.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerAdapter extends BaseListAdapter<ImagePickerAdapter.ImageViewHolder> {

    private List<Image> images = new ArrayList<>();
    private List<Image> selectedImages;

    private OnImageClickListener itemClickListener;

    public ImagePickerAdapter(Context context, ImageLoader imageLoader,
                              List<Image> selectedImages, OnImageClickListener itemClickListener) {
        super(context, imageLoader);
        this.selectedImages = selectedImages;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                getInflater().inflate(R.layout.ef_imagepicker_item_image, parent, false),
                itemClickListener
        );
    }

    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {

        final Image image = images.get(position);
        final boolean isSelected = isSelected(image);

        getImageLoader().loadImage(
                image.getPath(),
                viewHolder.imageView,
                ImageType.GALLERY
        );

        viewHolder.gifIndicator.setVisibility(ImagePickerUtils.isGifFormat(image)
                ? View.VISIBLE
                : View.GONE);

        viewHolder.alphaView.setAlpha(isSelected
                ? 0.5f
                : 0f);

        ((FrameLayout) viewHolder.itemView).setForeground(isSelected
                ? ContextCompat.getDrawable(getContext(), R.drawable.ic_done_white)
                : null);
    }

    private boolean isSelected(Image image) {
        for (Image selectedImage : selectedImages) {
            if (selectedImage.getPath().equals(image.getPath())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    public void setData(List<Image> images) {
        this.images.clear();
        this.images.addAll(images);
    }

    public void addAll(List<Image> images) {
        int startIndex = this.images.size();
        this.images.addAll(startIndex, images);
        notifyItemRangeInserted(startIndex, images.size());
    }

    public void addSelected(Image image) {
        selectedImages.add(image);
        notifyItemChanged(images.indexOf(image));
    }

    public void removeSelectedImage(Image image) {
        selectedImages.remove(image);
        notifyItemChanged(images.indexOf(image));
    }

    public void removeSelectedPosition(int position, int clickPosition) {
        selectedImages.remove(position);
        notifyItemChanged(clickPosition);
    }

    public void removeAllSelectedSingleClick() {
        selectedImages.clear();
        notifyDataSetChanged();
    }

    public Image getItem(int position) {
        return images.get(position);
    }

    public List<Image> getSelectedImages() {
        return selectedImages;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private View alphaView;
        private View gifIndicator;
        private final OnImageClickListener onImageClickListener;

        ImageViewHolder(View itemView, OnImageClickListener itemClickListener) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            alphaView = itemView.findViewById(R.id.view_alpha);
            gifIndicator = itemView.findViewById(R.id.ef_item_gif_indicator);
            onImageClickListener = itemClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            view.setSelected(true);
            onImageClickListener.onClick(view, getAdapterPosition());
        }
    }


}
