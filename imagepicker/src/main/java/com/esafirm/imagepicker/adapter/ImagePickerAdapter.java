package com.esafirm.imagepicker.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerAdapter extends RecyclerView.Adapter<ImagePickerAdapter.ImageViewHolder> {

    private List<Image> images = new ArrayList<>();
    private List<Image> selectedImages;

    private Context context;
    private LayoutInflater inflater;
    private OnImageClickListener itemClickListener;

    public ImagePickerAdapter(Context context, List<Image> selectedImages, OnImageClickListener itemClickListener) {
        this.context = context;
        this.selectedImages = selectedImages;
        this.itemClickListener = itemClickListener;
        inflater = LayoutInflater.from(this.context);
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.ef_imagepicker_item_image, parent, false);
        return new ImageViewHolder(itemView, itemClickListener);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {

        Image image = images.get(position);

        Glide.with(context)
                .load(image.getPath()).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(viewHolder.imageView);

        if (isSelected(image)) {
            viewHolder.alphaView.setAlpha(0.5f);
            ((FrameLayout) viewHolder.itemView).setForeground(ContextCompat.getDrawable(context, R.drawable.ic_done_white));
        } else {
            viewHolder.alphaView.setAlpha(0.0f);
            ((FrameLayout) viewHolder.itemView).setForeground(null);
        }

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

    public List<Image> getSelectedImages(){
        return selectedImages;
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private View alphaView;
        private final OnImageClickListener itemClickListener;

        public ImageViewHolder(View itemView, OnImageClickListener itemClickListener) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            alphaView = itemView.findViewById(R.id.view_alpha);
            this.itemClickListener = itemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            view.setSelected(true);
            itemClickListener.onClick(view, getAdapterPosition());
        }
    }


}
