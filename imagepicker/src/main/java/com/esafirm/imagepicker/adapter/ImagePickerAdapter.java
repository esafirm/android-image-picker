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
import com.esafirm.imagepicker.listeners.OnImageSelectedListener;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerAdapter extends BaseListAdapter<ImagePickerAdapter.ImageViewHolder> {

    private List<Image> images = new ArrayList<>();
    private List<Image> selectedImages = new ArrayList<>();

    private OnImageClickListener itemClickListener;
    private OnImageSelectedListener imageSelectedListener;

    public ImagePickerAdapter(Context context, ImageLoader imageLoader,
                              List<Image> selectedImages, OnImageClickListener itemClickListener) {
        super(context, imageLoader);
        this.itemClickListener = itemClickListener;

        if (selectedImages != null && !selectedImages.isEmpty()) {
            this.selectedImages.addAll(selectedImages);
        }
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                getInflater().inflate(R.layout.ef_imagepicker_item_image, parent, false)
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

        viewHolder.itemView.setOnClickListener(v -> {
            boolean shouldSelect = itemClickListener.onImageClick(
                    viewHolder.getAdapterPosition(),
                    !isSelected
            );

            if (isSelected) {
                removeSelectedImage(image, position);
            } else if (shouldSelect) {
                addSelected(image, position);
            }
        });

        viewHolder.container.setForeground(isSelected
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

    private void addSelected(final Image image, final int position) {
        mutateSelection(() -> {
            selectedImages.add(image);
            notifyItemChanged(position);
        });
    }

    private void removeSelectedImage(final Image image, final int position) {
        mutateSelection(() -> {
            selectedImages.remove(image);
            notifyItemChanged(position);
        });
    }

    public void removeAllSelectedSingleClick() {
        mutateSelection(() -> {
            selectedImages.clear();
            notifyDataSetChanged();
        });
    }

    private void mutateSelection(Runnable runnable) {
        runnable.run();
        if (imageSelectedListener != null) {
            imageSelectedListener.onSelectionUpdate(selectedImages);
        }
    }

    public void setImageSelectedListener(OnImageSelectedListener imageSelectedListener) {
        this.imageSelectedListener = imageSelectedListener;
    }

    public Image getItem(int position) {
        return images.get(position);
    }

    public List<Image> getSelectedImages() {
        return selectedImages;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private View alphaView;
        private View gifIndicator;
        private FrameLayout container;

        ImageViewHolder(View itemView) {
            super(itemView);

            container = (FrameLayout) itemView;
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            alphaView = itemView.findViewById(R.id.view_alpha);
            gifIndicator = itemView.findViewById(R.id.ef_item_gif_indicator);
        }
    }

}
