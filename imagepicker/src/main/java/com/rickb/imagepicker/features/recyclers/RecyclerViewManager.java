package com.rickb.imagepicker.features.recyclers;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rickb.imagepicker.R;
import com.rickb.imagepicker.adapter.FolderPickerAdapter;
import com.rickb.imagepicker.adapter.ImagePickerAdapter;
import com.rickb.imagepicker.features.ImagePickerComponentHolder;
import com.rickb.imagepicker.features.ImagePickerConfig;
import com.rickb.imagepicker.features.ReturnMode;
import com.rickb.imagepicker.features.imageloader.ImageLoader;
import com.rickb.imagepicker.helper.ConfigUtils;
import com.rickb.imagepicker.helper.ImagePickerUtils;
import com.rickb.imagepicker.listeners.OnFolderClickListener;
import com.rickb.imagepicker.listeners.OnImageClickListener;
import com.rickb.imagepicker.listeners.OnImageSelectedListener;
import com.rickb.imagepicker.listeners.OnTotalSizeLimitReachedListener;
import com.rickb.imagepicker.model.Folder;
import com.rickb.imagepicker.model.Image;
import com.rickb.imagepicker.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import static com.rickb.imagepicker.features.IpCons.MAX_LIMIT;
import static com.rickb.imagepicker.features.IpCons.MODE_MULTIPLE;
import static com.rickb.imagepicker.features.IpCons.MODE_SINGLE;

public class RecyclerViewManager {

    private final Context context;
    private final RecyclerView recyclerView;
    private final ImagePickerConfig config;

    private GridLayoutManager layoutManager;
    private GridSpacingItemDecoration itemOffsetDecoration;

    private ImagePickerAdapter imageAdapter;
    private FolderPickerAdapter folderAdapter;

    private Parcelable foldersState;

    private int imageColumns;
    private int folderColumns;

    public RecyclerViewManager(RecyclerView recyclerView, ImagePickerConfig config, int orientation) {
        this.recyclerView = recyclerView;
        this.config = config;
        this.context = recyclerView.getContext();
        changeOrientation(orientation);
    }

    public void onRestoreState(Parcelable recyclerState) {
        layoutManager.onRestoreInstanceState(recyclerState);
    }

    public Parcelable getRecyclerState() {
        return layoutManager.onSaveInstanceState();
    }

    /**
     * Set item size, column size base on the screen orientation
     */
    public void changeOrientation(int orientation) {
        imageColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 4 : 5;
        folderColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;

        boolean shouldShowFolder = config.isFolderMode() && isDisplayingFolderView();
        int columns = shouldShowFolder ? folderColumns : imageColumns;
        layoutManager = new GridLayoutManager(context, columns);

        if (! shouldShowFolder) {
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (imageAdapter.getItemViewType(position) == imageAdapter.VIEW_TYPE_HEADER) {
                        // Headers should span the whole column width.
                        return columns;
                    } else {
                        return 1;
                    }
                }
            });
        }

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);
        setItemDecoration(columns);
    }

    public void setupAdapters(ArrayList<Image> selectedImages, OnImageClickListener onImageClickListener, OnFolderClickListener onFolderClickListener) {
        if (config.getMode() == MODE_SINGLE && selectedImages != null && selectedImages.size() > 1) {
            selectedImages = null;
        }

        /* Init folder and image adapter */
        final ImageLoader imageLoader = ImagePickerComponentHolder.getInstance().getImageLoader();
        imageAdapter = new ImagePickerAdapter(context, imageLoader, selectedImages, onImageClickListener, config.totalSizeLimit(), config.getLimit());
        folderAdapter = new FolderPickerAdapter(context, imageLoader, bucket -> {
            foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
            onFolderClickListener.onFolderClick(bucket);
        });
    }

    private void setItemDecoration(int columns) {
        if (itemOffsetDecoration != null) {
            recyclerView.removeItemDecoration(itemOffsetDecoration);
        }
        itemOffsetDecoration = new GridSpacingItemDecoration(
                columns,
                context.getResources().getDimensionPixelSize(R.dimen.ef_item_padding),
                false
        );
        recyclerView.addItemDecoration(itemOffsetDecoration);

        layoutManager.setSpanCount(columns);
    }

    // Returns true if a back action was handled by going back a folder; false otherwise.
    public boolean handleBack() {
        if (config.isFolderMode() && !isDisplayingFolderView()) {
            setFolderAdapter(null);
            return true;
        }
        return false;
    }

    private boolean isDisplayingFolderView() {
        return recyclerView.getAdapter() == null || recyclerView.getAdapter() instanceof FolderPickerAdapter;
    }

    public String getTitle() {
        if (isDisplayingFolderView()) {
            return ConfigUtils.getFolderTitle(context, config);
        }

        if (config.getMode() == MODE_SINGLE) {
            return ConfigUtils.getImageTitle(context, config);
        }

        final int imageSize = imageAdapter.getSelectedImages().size();
        final boolean useDefaultTitle = !ImagePickerUtils.isStringEmpty(config.getImageTitle()) && imageSize == 0;

        if (useDefaultTitle) {
            return ConfigUtils.getImageTitle(context, config);
        }
        return String.format(context.getString(R.string.ef_selected), imageSize);
    }

    /**
     * Returns the 'X/X selected' text for in the bottom toast.
     *
     * @return
     */
    public String getAmountSelectedText() {
        final int imageSize = imageAdapter.getSelectedImages().size();

        return config.getLimit() == MAX_LIMIT
                ? String.format(context.getString(R.string.ef_selected), imageSize)
                : String.format(context.getString(R.string.ef_selected_with_limit), imageSize, config.getLimit());
    }

    public void setImageAdapter(List<Image> images) {
        imageAdapter.setData(images);
        setItemDecoration(imageColumns);
        recyclerView.setAdapter(imageAdapter);
    }

    public void setFolderAdapter(List<Folder> folders) {
        folderAdapter.setData(folders);
        setItemDecoration(folderColumns);
        recyclerView.setAdapter(folderAdapter);

        if (foldersState != null) {
            layoutManager.setSpanCount(folderColumns);
            recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
        }
    }

    /* --------------------------------------------------- */
    /* > Images */
    /* --------------------------------------------------- */

    private void checkAdapterIsInitialized() {
        if (imageAdapter == null) {
            throw new IllegalStateException("Must call setupAdapters first!");
        }
    }

    public List<Image> getSelectedImages() {
        checkAdapterIsInitialized();
        return imageAdapter.getSelectedImages();
    }

    public void setImageSelectedListener(OnImageSelectedListener listener) {
        checkAdapterIsInitialized();
        imageAdapter.setImageSelectedListener(listener);
    }

    public void setTotalSizeLimitReachedListener(OnTotalSizeLimitReachedListener listener) {
        checkAdapterIsInitialized();
        imageAdapter.setOnTotalSizeLimitReachedListener(listener);
    }

    public boolean selectImage(boolean isSelected) {
        if (config.getMode() == MODE_MULTIPLE) {
            boolean isSelectionAmountLimitReached = imageAdapter.getSelectedImages().size() >= config.getLimit() && !isSelected;
            boolean shouldBlockSelection = imageAdapter.isMaxTotalSizeReached() || isSelectionAmountLimitReached;
            if (shouldBlockSelection) {
                return false;
            }
        } else if (config.getMode() == MODE_SINGLE) {
            if (imageAdapter.getSelectedImages().size() > 0) {
                imageAdapter.removeAllSelectedSingleClick();
            }
        }
        return true;
    }

    public boolean isShowDoneButton() {
        return !isDisplayingFolderView()
                && !imageAdapter.getSelectedImages().isEmpty()
                && (config.getReturnMode() != ReturnMode.ALL && config.getReturnMode() != ReturnMode.GALLERY_ONLY);
    }

}
