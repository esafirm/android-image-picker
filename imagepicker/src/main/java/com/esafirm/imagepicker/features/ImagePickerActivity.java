package com.esafirm.imagepicker.features;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.camera.CameraHelper;
import com.esafirm.imagepicker.features.camera.DefaultCameraModule;
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig;
import com.esafirm.imagepicker.features.common.BaseConfig;
import com.esafirm.imagepicker.features.recyclers.OnBackAction;
import com.esafirm.imagepicker.features.recyclers.RecyclerViewManager;
import com.esafirm.imagepicker.helper.ConfigUtils;
import com.esafirm.imagepicker.helper.ImagePickerPreferences;
import com.esafirm.imagepicker.helper.IpLogger;
import com.esafirm.imagepicker.helper.LocaleManager;
import com.esafirm.imagepicker.helper.ViewUtils;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.view.SnackBarView;

import java.util.ArrayList;
import java.util.List;

import static com.esafirm.imagepicker.helper.ImagePickerPreferences.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED;

public class ImagePickerActivity extends AppCompatActivity implements ImagePickerView {

    private static final String STATE_KEY_CAMERA_MODULE = "Key.CameraModule";

    private static final int RC_CAPTURE = 2000;

    private static final int RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 23;
    private static final int RC_PERMISSION_REQUEST_CAMERA = 24;

    private IpLogger logger = IpLogger.getInstance();

    private ActionBar actionBar;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private RecyclerView recyclerView;
    private SnackBarView snackBarView;

    private RecyclerViewManager recyclerViewManager;

    private ImagePickerPresenter presenter;
    private ImagePickerPreferences preferences;
    private ImagePickerConfig config;

    private Handler handler;
    private ContentObserver observer;

    private boolean isCameraOnly;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.updateResources(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        /* This should not happen */
        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            IpLogger.getInstance().e("This should not happen. Please open an issue!");
            finish();
            return;
        }

        isCameraOnly = getIntent().hasExtra(CameraOnlyConfig.class.getSimpleName());

        setupComponents();

        if (isCameraOnly) {
            if (savedInstanceState == null) {
                captureImageWithPermission();
            }
        } else {
            ImagePickerConfig config = getImagePickerConfig();
            if (config != null) {
                setTheme(config.getTheme());
                setContentView(R.layout.ef_activity_image_picker);
                setupView(config);
                setupRecyclerView(config);
            }
        }
    }

    private BaseConfig getBaseConfig() {
        return isCameraOnly
                ? getCameraOnlyConfig()
                : getImagePickerConfig();
    }

    private CameraOnlyConfig getCameraOnlyConfig() {
        return getIntent().getParcelableExtra(CameraOnlyConfig.class.getSimpleName());
    }

    @Nullable
    private ImagePickerConfig getImagePickerConfig() {
        if (config == null) {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                throw new IllegalStateException("This should not happen. Please open an issue!");
            }
            config = bundle.getParcelable(ImagePickerConfig.class.getSimpleName());
        }
        return config;
    }

    private void setupView(ImagePickerConfig config) {
        progressBar = findViewById(R.id.progress_bar);
        emptyTextView = findViewById(R.id.tv_empty_images);
        recyclerView = findViewById(R.id.recyclerView);
        snackBarView = findViewById(R.id.ef_snackbar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {

            final Drawable arrowDrawable = ViewUtils.getArrowIcon(this);
            final int arrowColor = config.getArrowColor();
            if (arrowColor != ImagePickerConfig.NO_COLOR && arrowDrawable != null) {
                arrowDrawable.setColorFilter(arrowColor, PorterDuff.Mode.SRC_ATOP);
            }
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(arrowDrawable);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    private void setupRecyclerView(ImagePickerConfig config) {
        recyclerViewManager = new RecyclerViewManager(
                recyclerView,
                config,
                getResources().getConfiguration().orientation
        );

        recyclerViewManager.setupAdapters((isSelected) -> recyclerViewManager.selectImage(isSelected)
                , bucket -> setImageAdapter(bucket.getImages()));

        recyclerViewManager.setImageSelectedListener(selectedImage -> {
            invalidateTitle();
            if (ConfigUtils.shouldReturn(config, false) && !selectedImage.isEmpty()) {
                onDone();
            }
        });

    }

    private void setupComponents() {
        preferences = new ImagePickerPreferences(this);
        presenter = new ImagePickerPresenter(new ImageFileLoader(this));
        presenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCameraOnly) {
            getDataWithPermission();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_KEY_CAMERA_MODULE, presenter.getCameraModule());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        presenter.setCameraModule((DefaultCameraModule) savedInstanceState.getSerializable(STATE_KEY_CAMERA_MODULE));
    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private void setImageAdapter(List<Image> images) {
        recyclerViewManager.setImageAdapter(images);
        invalidateTitle();
    }

    private void setFolderAdapter(List<Folder> folders) {
        recyclerViewManager.setFolderAdapter(folders);
        invalidateTitle();
    }

    private void invalidateTitle() {
        supportInvalidateOptionsMenu();
        actionBar.setTitle(recyclerViewManager.getTitle());
    }

    /**
     * Create option menus and update title
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ef_image_picker_menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuCamera = menu.findItem(R.id.menu_camera);
        if (menuCamera != null) {
            ImagePickerConfig imagePickerConfig = getImagePickerConfig();
            if (imagePickerConfig != null) {
                menuCamera.setVisible(imagePickerConfig.isShowCamera());
            }
        }

        MenuItem menuDone = menu.findItem(R.id.menu_done);
        if (menuDone != null) {
            menuDone.setTitle(ConfigUtils.getDoneButtonText(this, config));
            menuDone.setVisible(recyclerViewManager.isShowDoneButton());
        }
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Handle option menu's click event
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (id == R.id.menu_done) {
            onDone();
            return true;
        }
        if (id == R.id.menu_camera) {
            captureImageWithPermission();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * On finish selected image
     * Get all selected images then return image to caller activity
     */
    private void onDone() {
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
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            getData();
        } else {
            requestWriteExternalPermission();
        }
    }

    private void getData() {
        ImagePickerConfig config = getImagePickerConfig();
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

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            final String permission = PREF_WRITE_EXTERNAL_STORAGE_REQUESTED;
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission);
                ActivityCompat.requestPermissions(this, permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                snackBarView.show(R.string.ef_msg_no_write_external_permission, v -> openAppSettings());
            }
        }
    }

    private void requestCameraPermissions() {
        logger.w("Write External permission is not granted. Requesting permission");

        ArrayList<String> permissions = new ArrayList<>(2);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (checkForRationale(permissions)) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), RC_PERMISSION_REQUEST_CAMERA);
        } else {
            final String permission = ImagePickerPreferences.PREF_CAMERA_REQUESTED;
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission);
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), RC_PERMISSION_REQUEST_CAMERA);
            } else {
                if (isCameraOnly) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.ef_msg_no_camera_permission), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    snackBarView.show(R.string.ef_msg_no_camera_permission, v -> openAppSettings());
                }
            }
        }
    }

    private boolean checkForRationale(List<String> permissions) {
        for (int i = 0, size = permissions.size(); i < size; i++) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions.get(i))) {
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
                finish();
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
                finish();
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
                Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Check if the captured image is stored successfully
     * Then reload data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAPTURE) {
            if (resultCode == RESULT_OK) {
                presenter.finishCaptureImage(this, data, getBaseConfig());
            } else if (resultCode == RESULT_CANCELED && isCameraOnly) {
                presenter.abortCaptureImage();
                finish();
            }
        }
    }

    /**
     * Request for camera permission
     */
    private void captureImageWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final boolean isCameraGranted = ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            final boolean isWriteGranted = ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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
        if (!CameraHelper.checkCameraAvailability(this)) {
            return;
        }
        presenter.captureImage(this, getBaseConfig(), RC_CAPTURE);
    }


    @Override
    protected void onStart() {
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
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.abortLoad();
            presenter.detachView();
        }

        if (observer != null) {
            getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isCameraOnly) {
            super.onBackPressed();
            return;
        }

        recyclerViewManager.handleBack(new OnBackAction() {
            @Override
            public void onBackToFolder() {
                invalidateTitle();
            }

            @Override
            public void onFinishImagePicker() {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    /* --------------------------------------------------- */
    /* > View Methods */
    /* --------------------------------------------------- */

    @Override
    public void finishPickImages(List<Image> images) {
        Intent data = new Intent();
        data.putExtras(getIntent());
        data.putParcelableArrayListExtra(IpCons.EXTRA_SELECTED_IMAGES, (ArrayList<? extends Parcelable>) images);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void showCapturedImage() {
        getDataWithPermission();
    }

    @Override
    public void showFetchCompleted(List<Image> images, List<Folder> folders) {
        ImagePickerConfig config = getImagePickerConfig();
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
            message = "Images not exist";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
