package com.esafirm.imagepicker.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.esafirm.imagepicker.features.ImagePickerSavePath;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

public class ImagePickerUtils {

    public static boolean isStringEmpty(@Nullable String str) {
        return str == null || str.length() == 0;
    }

    public static File createImageFile(ImagePickerSavePath savePath) {
        // External sdcard location
        final String path = savePath.getPath();
        File mediaStorageDir = savePath.isFullPath()
                ? new File(path)
                : new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), path);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                IpLogger.getInstance().d("Oops! Failed create " + path);
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault()).format(new Date());
        File result = new File(mediaStorageDir, "IMG_" + timeStamp + ".jpg");
        int counter = 0;
        while (result.exists()) {
            counter++;
            result = new File(mediaStorageDir, "IMG_" + timeStamp + "(" + counter + ").jpg");
        }
        return result;
    }

    public static String getNameFromFilePath(String path) {
        if (path.contains(File.separator)) {
            return path.substring(path.lastIndexOf(File.separator) + 1);
        }
        return path;
    }

    public static void grantAppPermission(Context context, Intent intent, Uri fileUri) {
        List<ResolveInfo> resolvedIntentActivities = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
            String packageName = resolvedIntentInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, fileUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public static void revokeAppPermission(Context context, Uri fileUri) {
        context.revokeUriPermission(fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
    }

    public static boolean isGifFormat(Image image) {
        String extension = getExtension(image.getPath());
        return extension.equalsIgnoreCase("gif");
    }

    public static boolean isVideoFormat(Image image) {
        String extension = getExtension(image.getPath());
        String mimeType = TextUtils.isEmpty(extension)
                ? URLConnection.guessContentTypeFromName(image.getPath())
                : MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null && mimeType.startsWith("video");

    }

    private static String getExtension(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (!TextUtils.isEmpty(extension)) {
            return extension;
        }
        if (path.contains(".")) {
            return path.substring(path.lastIndexOf(".") + 1, path.length());
        } else {
            return "";
        }
    }
}
