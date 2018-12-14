package com.esafirm.imagepicker.features;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import androidx.annotation.Nullable;

import com.esafirm.imagepicker.features.common.ImageLoaderListener;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageFileLoader {

    private Context context;
    private ExecutorService executorService;

    public ImageFileLoader(Context context) {
        this.context = context;
    }

    private final String[] projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };

    public void loadDeviceImages(final boolean isFolderMode, final boolean includeVideo, final boolean includeAnimation, final ArrayList<File> excludedImages, final ImageLoaderListener listener) {
        getExecutorService().execute(new ImageLoadRunnable(isFolderMode, includeVideo, includeAnimation, excludedImages, listener));
    }

    public void abortLoadImages() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }

    private class ImageLoadRunnable implements Runnable {

        private boolean isFolderMode;
        private boolean includeVideo;
        private boolean includeAnimation;
        private ArrayList<File> exlucedImages;
        private ImageLoaderListener listener;

        public ImageLoadRunnable(boolean isFolderMode, boolean includeVideo, boolean includeAnimation, ArrayList<File> excludedImages, ImageLoaderListener listener) {
            this.isFolderMode = isFolderMode;
            this.includeVideo = includeVideo;
            this.includeAnimation = includeAnimation;
            this.exlucedImages = excludedImages;
            this.listener = listener;
        }

        @Override
        public void run() {
            Cursor cursor;
            if (includeVideo) {
                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR "
                        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

                cursor = context.getContentResolver().query(MediaStore.Files.getContentUri("external"), projection,
                        selection, null, MediaStore.Images.Media.DATE_ADDED);
            } else {
                cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                        null, null, MediaStore.Images.Media.DATE_ADDED);
            }

            if (cursor == null) {
                listener.onFailed(new NullPointerException());
                return;
            }

            List<Image> temp = new ArrayList<>();
            Map<String, Folder> folderMap = null;
            if (isFolderMode) {
                folderMap = new HashMap<>();
            }

            if (cursor.moveToLast()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                    String bucket = cursor.getString(cursor.getColumnIndex(projection[3]));

                    File file = makeSafeFile(path);
                    if (file != null) {
                        if (exlucedImages != null && exlucedImages.contains(file))
                            continue;

                        Image image = new Image(id, name, path);

                        if (!includeAnimation) {
                            if (ImagePickerUtils.isGifFormat(image))
                                continue;
                        }

                        temp.add(image);

                        if (folderMap != null) {
                            Folder folder = folderMap.get(bucket);
                            if (folder == null) {
                                folder = new Folder(bucket);
                                folderMap.put(bucket, folder);
                            }
                            folder.getImages().add(image);
                        }
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            /* Convert HashMap to ArrayList if not null */
            List<Folder> folders = null;
            if (folderMap != null) {
                folders = new ArrayList<>(folderMap.values());
            }

            listener.onImageLoaded(temp, folders);
        }
    }

    @Nullable
    private static File makeSafeFile(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        try {
            return new File(path);
        } catch (Exception ignored) {
            return null;
        }
    }

}
