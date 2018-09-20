package com.esafirm.imagepicker.features;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.camera.CameraHelper;
import com.esafirm.imagepicker.features.camera.DefaultCameraModule;
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig;
import com.esafirm.imagepicker.features.common.BaseConfig;
import com.esafirm.imagepicker.features.recyclers.RecyclerViewManager;
import com.esafirm.imagepicker.helper.ConfigUtils;
import com.esafirm.imagepicker.helper.ImagePickerPreferences;
import com.esafirm.imagepicker.helper.IpLogger;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.view.SnackBarView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.esafirm.imagepicker.helper.ImagePickerPreferences.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED;

public class ImagePickerFragment extends Fragment implements ImagePickerView {
    private static final String STATE_KEY_CAMERA_MODULE = "Key.CameraModule";
    private static final String STATE_KEY_RECYCLER = "Key.Recycler";
    private static final String STATE_KEY_SELECTED_IMAGES = "Key.SelectedImages";

    private static final int RC_CAPTURE = 2000;

    private static final int RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 23;
    private static final int RC_PERMISSION_REQUEST_CAMERA = 24;

    private IpLogger logger = IpLogger.getInstance();

    private RecyclerView recyclerView;
    private SnackBarView snackBarView;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    private RecyclerViewManager recyclerViewManager;

    private ImagePickerPresenter presenter;
    private ImagePickerPreferences preferences;
    private ImagePickerConfig config;
    private CameraOnlyConfig cameraOnlyConfig;
    private ImagePickerInteractionListener interactionListener;

    private Handler handler;
    private ContentObserver observer;

    private boolean isCameraOnly;


    public ImagePickerFragment() {
        // Required empty public constructor.
    }

    public static ImagePickerFragment newInstance(ImagePickerConfig config, @Nullable CameraOnlyConfig cameraOnlyConfig) {
        ImagePickerFragment fragment = new ImagePickerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ImagePickerConfig.class.getSimpleName(), config);
        if (cameraOnlyConfig != null) {
            args.putParcelable(CameraOnlyConfig.class.getSimpleName(), cameraOnlyConfig);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        config = getArguments().getParcelable(ImagePickerConfig.class.getSimpleName());
        cameraOnlyConfig = getArguments().getParcelable(CameraOnlyConfig.class.getSimpleName());
        isCameraOnly = cameraOnlyConfig != null;

        setupComponents();

        if (interactionListener == null) {
            throw new RuntimeException("ImagePickerFragment needs an " +
                    "ImagePickerInteractionListener. This will be set automatically if the " +
                    "activity implements ImagePickerInteractionListener, and can be set manually " +
                    "with fragment.setInteractionListener(listener).");
        }

        if (savedInstanceState != null) {
            presenter.setCameraModule((DefaultCameraModule) savedInstanceState.getSerializable(STATE_KEY_CAMERA_MODULE));
        }

        if (isCameraOnly) {
            if (savedInstanceState == null) {
                captureImageWithPermission();
            }
        } else {
            if (config != null) {
                // clone the inflater using the ContextThemeWrapper
                LayoutInflater localInflater = inflater.cloneInContext(new ContextThemeWrapper(getActivity(), config.getTheme()));

                // inflate the layout using the cloned inflater, not default inflater
                View result = localInflater.inflate(R.layout.ef_fragment_image_picker, container, false);
                setupView(result);
                if (savedInstanceState == null) {
                    setupRecyclerView(config, config.getSelectedImages());
                } else {
                    setupRecyclerView(config, savedInstanceState.getParcelableArrayList(STATE_KEY_SELECTED_IMAGES));
                    recyclerViewManager.onRestoreState(savedInstanceState.getParcelable(STATE_KEY_RECYCLER));
                }
                interactionListener.selectionChanged(recyclerViewManager.getSelectedImages());
                return result;
            }
        }
        return null;
    }

    private BaseConfig getBaseConfig() {
        return isCameraOnly ? cameraOnlyConfig : config;
    }

    private void setupView(View rootView) {
        progressBar = rootView.findViewById(R.id.progress_bar);
        emptyTextView = rootView.findViewById(R.id.tv_empty_images);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        snackBarView = rootView.findViewById(R.id.ef_snackbar);
    }

    private void setupRecyclerView(ImagePickerConfig config, ArrayList<Image> selectedImages) {
        recyclerViewManager = new RecyclerViewManager(
                recyclerView,
                config,
                getResources().getConfiguration().orientation
        );

        recyclerViewManager.setupAdapters(selectedImages, (isSelected) -> recyclerViewManager.selectImage(isSelected)
                , bucket -> setImageAdapter(bucket.getImages()));

        recyclerViewManager.setImageSelectedListener(selectedImage -> {
            updateTitle();
            interactionListener.selectionChanged(recyclerViewManager.getSelectedImages());
            if (ConfigUtils.shouldReturn(config, false) && !selectedImage.isEmpty()) {
                onDone();
            }
        });

    }

    private void setupComponents() {
        preferences = new ImagePickerPreferences(getActivity());
        presenter = new ImagePickerPresenter(new ImageFileLoader(getActivity()));
        presenter.attachView(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isCameraOnly) {
            getDataWithPermission();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_KEY_CAMERA_MODULE, presenter.getCameraModule());
        outState.putParcelable(STATE_KEY_RECYCLER, recyclerViewManager.getRecyclerState());
        outState.putParcelableArrayList(STATE_KEY_SELECTED_IMAGES, recyclerViewManager.getSelectedImages());
    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    void setImageAdapter(List<Image> images) {
        recyclerViewManager.setImageAdapter(images);
        updateTitle();
    }

    void setFolderAdapter(List<Folder> folders) {
        recyclerViewManager.setFolderAdapter(folders);
        updateTitle();
    }

    private void updateTitle() {
        interactionListener.setTitle(recyclerViewManager.getTitle());
    }

    /**
     * On finish selected image
     * Get all selected images then return image to caller activity
     */
    public void onDone() {
        presenter.onDoneSelectImages(recyclerViewManager.getSelectedImages());
    }

    /**
     * Config recyclerView when configuration changed
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (recyclerViewManager != null) {
            // recyclerViewManager can be null here if we use cameraOnly mode
            recyclerViewManager.changeOrientation(newConfig.orientation);
        }
    }

    /**
     * Check permission
     */
    private void getDataWithPermission() {
        int rc = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            getData();
        } else {
            requestWriteExternalPermission();
        }
    }

    private void getData() {
        presenter.abortLoad();
        if (config != null) {
            presenter.loadImages(config);
        }
    }

    /**
     * Request for permission
     * If permission denied or app is first launched, request for permission
     * If permission denied and user choose 'Never Ask Again', show snackbar with an action that navigate to app settings
     */
    private void requestWriteExternalPermission() {
        logger.w("Write External permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissions(permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            final String permission = PREF_WRITE_EXTERNAL_STORAGE_REQUESTED;
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission);
                requestPermissions(permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                snackBarView.show(R.string.ef_msg_no_write_external_permission, v -> openAppSettings());
            }
        }
    }

    private void requestCameraPermissions() {
        logger.w("Write External permission is not granted. Requesting permission");

        ArrayList<String> permissions = new ArrayList<>(2);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (checkForRationale(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), RC_PERMISSION_REQUEST_CAMERA);
        } else {
            final String permission = ImagePickerPreferences.PREF_CAMERA_REQUESTED;
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission);
                requestPermissions(permissions.toArray(new String[permissions.size()]), RC_PERMISSION_REQUEST_CAMERA);
            } else {
                if (isCameraOnly) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            getString(R.string.ef_msg_no_camera_permission), Toast.LENGTH_SHORT).show();
                    interactionListener.cancel();
                } else {
                    snackBarView.show(R.string.ef_msg_no_camera_permission, v -> openAppSettings());
                }
            }
        }
    }

    private boolean checkForRationale(List<String> permissions) {
        for (int i = 0, size = permissions.size(); i < size; i++) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logger.d("Write External permission granted");
                    getData();
                    return;
                }
                logger.e("Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                interactionListener.cancel();
            }
            break;
            case RC_PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logger.d("Camera permission granted");
                    captureImage();
                    return;
                }
                logger.e("Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                interactionListener.cancel();
                break;
            }
            default: {
                logger.d("Got unexpected permission result: " + requestCode);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
            }
        }
    }

    /**
     * Open app settings screen
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getActivity().getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Check if the captured image is stored successfully
     * Then reload data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAPTURE) {
            if (resultCode == RESULT_OK) {
                presenter.finishCaptureImage(getActivity(), data, getBaseConfig());
            } else if (resultCode == RESULT_CANCELED && isCameraOnly) {
                presenter.abortCaptureImage();
                interactionListener.cancel();
            }
        }
    }

    /**
     * Request for camera permission
     */
    public void captureImageWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final boolean isCameraGranted = ActivityCompat
                    .checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            final boolean isWriteGranted = ActivityCompat
                    .checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (isCameraGranted && isWriteGranted) {
                captureImage();
            } else {
                logger.w("Camera permission is not granted. Requesting permission");
                requestCameraPermissions();
            }
        } else {
            captureImage();
        }
    }

    /**
     * Start camera intent
     * Create a temporary file and pass file Uri to camera intent
     */
    private void captureImage() {
        if (!CameraHelper.checkCameraAvailability(getActivity())) {
            return;
        }
        presenter.captureImage(this, getBaseConfig(), RC_CAPTURE);
    }


    @Override
    public void onStart() {
        super.onStart();

        if (isCameraOnly) {
            return;
        }

        if (handler == null) {
            handler = new Handler();
        }
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                getData();
            }
        };
        getActivity().getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.abortLoad();
            presenter.detachView();
        }

        if (observer != null) {
            getActivity().getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    // Returns true if this Fragment can do anything with a "back" event, such as the containing
    // Activity receiving onBackPressed(). Returns false if the containing Activity should handle
    // it.
    // This Fragment might handle a "back" event by, for example, going back to the list of folders.
    // Or it might have no "back" to go, and return false.
    public boolean handleBack() {
        if (isCameraOnly) {
            return false;
        }
        if (recyclerViewManager.handleBack()) {
            // Handled.
            updateTitle();
            return true;
        }
        return false;
    }

    public boolean isShowDoneButton() {
        return recyclerViewManager.isShowDoneButton();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ImagePickerInteractionListener) {
            interactionListener = (ImagePickerInteractionListener) context;
        }
    }

    public void setInteractionListener(ImagePickerInteractionListener listener) {
        interactionListener = listener;
    }

    /* --------------------------------------------------- */
    /* > View Methods */
    /* --------------------------------------------------- */

    @Override
    public void finishPickImages(List<Image> images) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(IpCons.EXTRA_SELECTED_IMAGES, (ArrayList<? extends Parcelable>) images);
        interactionListener.finishPickImages(data);
    }

    @Override
    public void showCapturedImage() {
        getDataWithPermission();
    }

    @Override
    public void showFetchCompleted(List<Image> images, List<Folder> folders) {
        if (config != null && config.isFolderMode()) {
            setFolderAdapter(folders);
        } else {
            setImageAdapter(images);
        }
    }

    @Override
    public void showError(Throwable throwable) {
        String message = "Unknown Error";
        if (throwable != null && throwable instanceof NullPointerException) {
            message = "Images do not exist";
        }
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    @Override
    public void showEmpty() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
    }
}
