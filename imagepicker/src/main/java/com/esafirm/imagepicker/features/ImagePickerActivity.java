package com.esafirm.imagepicker.features;

import android.Manifest;
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
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.adapter.FolderPickerAdapter;
import com.esafirm.imagepicker.adapter.ImagePickerAdapter;
import com.esafirm.imagepicker.features.camera.CameraHelper;
import com.esafirm.imagepicker.helper.ImagePickerPreferences;
import com.esafirm.imagepicker.helper.IntentHelper;
import com.esafirm.imagepicker.listeners.OnFolderClickListener;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.model.Folder;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.imagepicker.view.GridSpacingItemDecoration;
import com.esafirm.imagepicker.view.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import static com.esafirm.imagepicker.features.ImagePicker.EXTRA_SELECTED_IMAGES;
import static com.esafirm.imagepicker.features.ImagePicker.MODE_MULTIPLE;
import static com.esafirm.imagepicker.helper.ImagePickerPreferences.PREF_WRITE_EXTERNAL_STORAGE_REQUESTED;

public class ImagePickerActivity extends AppCompatActivity
        implements ImagePickerView, OnImageClickListener {

    private static final int RC_CAPTURE = 2000;

    private static final String TAG = "ImagePickerActivity";

    public static final int RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 23;
    public static final int RC_PERMISSION_REQUEST_CAMERA = 24;

    private ActionBar actionBar;
    private RelativeLayout mainLayout;
    private ProgressWheel progressBar;
    private TextView emptyTextView;
    private RecyclerView recyclerView;

    private GridLayoutManager layoutManager;
    private GridSpacingItemDecoration itemOffsetDecoration;

    private ImagePickerPresenter presenter;
    private ImagePickerPreferences preferences;
    private ImagePickerAdapter imageAdapter;
    private ImagePickerConfig config;
    private FolderPickerAdapter folderAdapter;

    private Handler handler;
    private ContentObserver observer;

    private Parcelable foldersState;

    private int imageColumns;
    private int folderColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }

        preferences = new ImagePickerPreferences(this);
        presenter = new ImagePickerPresenter(new ImageLoader(this));
        presenter.attachView(this);

        setupExtras();
        setupView();

        orientationBasedUI(getResources().getConfiguration().orientation);
    }

    private void setupView() {
        mainLayout = (RelativeLayout) findViewById(R.id.main);
        progressBar = (ProgressWheel) findViewById(R.id.progress_bar);
        emptyTextView = (TextView) findViewById(R.id.tv_empty_images);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(config.isFolderMode() ? config.getFolderTitle() : config.getImageTitle());
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            actionBar.setDisplayShowTitleEnabled(true);
        }
    }

    private void setupExtras() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        config = bundle.getParcelable(ImagePickerConfig.class.getSimpleName());
        if (config == null) {
            config = IntentHelper.makeConfigFromIntent(this, intent);
        }

        ArrayList<Image> selectedImages = null;
        if (config.getMode() == MODE_MULTIPLE && !config.getSelectedImages().isEmpty()) {
            selectedImages = config.getSelectedImages();
        }
        if (selectedImages == null) {
            selectedImages = new ArrayList<>();
        }

        /** Init folder and image adapter */
        imageAdapter = new ImagePickerAdapter(this, selectedImages, this);
        folderAdapter = new FolderPickerAdapter(this, new OnFolderClickListener() {
            @Override
            public void onFolderClick(Folder bucket) {
                foldersState = recyclerView.getLayoutManager().onSaveInstanceState();
                setImageAdapter(bucket.getImages());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDataWithPermission();
    }

    /**
     * Set image adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private void setImageAdapter(List<Image> images) {
        imageAdapter.setData(images);
        setItemDecoration(imageColumns);
        recyclerView.setAdapter(imageAdapter);
        updateTitle();
    }

    /**
     * Set folder adapter
     * 1. Set new data
     * 2. Update item decoration
     * 3. Update title
     */
    private void setFolderAdapter(List<Folder> folders) {
        if (folders != null) {
            folderAdapter.setData(folders);
        }
        setItemDecoration(folderColumns);
        recyclerView.setAdapter(folderAdapter);

        if (foldersState != null) {
            layoutManager.setSpanCount(folderColumns);
            recyclerView.getLayoutManager().onRestoreInstanceState(foldersState);
        }
        updateTitle();
    }

    /**
     * Create option menus and update title
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_picker_menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuCamera = menu.findItem(R.id.menu_camera);
        if (menuCamera != null) {
            menuCamera.setVisible(config.isShowCamera());
        }

        MenuItem menuDone = menu.findItem(R.id.menu_done);
        if (menuDone != null) {
            menuDone.setVisible(!isDisplayingFolderView() && !imageAdapter.getSelectedImages().isEmpty());
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
            List<Image> selectedImages = imageAdapter.getSelectedImages();
            presenter.onDoneSelectImages(selectedImages);
            return true;
        }
        if (id == R.id.menu_camera) {
            captureImageWithPermission();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Config recyclerView when configuration changed
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    /**
     * Set item size, column size base on the screen orientation
     */
    private void orientationBasedUI(int orientation) {
        imageColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5;
        folderColumns = orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;

        int columns = isDisplayingFolderView() ? folderColumns : imageColumns;
        layoutManager = new GridLayoutManager(this, columns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        setItemDecoration(columns);
    }

    /**
     * Set item decoration
     */
    private void setItemDecoration(int columns) {
        layoutManager.setSpanCount(columns);
        if (itemOffsetDecoration != null)
            recyclerView.removeItemDecoration(itemOffsetDecoration);
        itemOffsetDecoration = new GridSpacingItemDecoration(columns, getResources().getDimensionPixelSize(R.dimen.item_padding), false);
        recyclerView.addItemDecoration(itemOffsetDecoration);
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
        presenter.abortLoad();
        presenter.loadImages(config.isFolderMode());
    }

    /**
     * Request for permission
     * If permission denied or app is first launched, request for permission
     * If permission denied and user choose 'Nerver Ask Again', show snackbar with an action that navigate to app settings
     */
    private void requestWriteExternalPermission() {
        Log.w(TAG, "Write External permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            final String permission = PREF_WRITE_EXTERNAL_STORAGE_REQUESTED;
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission);
                ActivityCompat.requestPermissions(this, permissions, RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                Snackbar snackbar = Snackbar.make(mainLayout, R.string.msg_no_write_external_permission,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAppSettings();
                    }
                });
                snackbar.show();
            }
        }

    }


    private void requestCameraPermission() {
        Log.w(TAG, "Write External permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_PERMISSION_REQUEST_CAMERA);
        } else {
            final String permission = ImagePickerPreferences.PREF_CAMERA_REQUESTED;
            if (!preferences.isPermissionRequested(permission)) {
                preferences.setPermissionRequested(permission);
                ActivityCompat.requestPermissions(this, permissions, RC_PERMISSION_REQUEST_CAMERA);
            } else {
                Snackbar snackbar = Snackbar.make(mainLayout, R.string.msg_no_camera_permission,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openAppSettings();
                    }
                });
                snackbar.show();
            }
        }
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case RC_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Write External permission granted");
                    getData();
                    return;
                }
                Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                finish();
            }
            break;
            case RC_PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Camera permission granted");
                    captureImage();
                    return;
                }
                Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                        " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
                break;
            }
            default: {
                Log.d(TAG, "Got unexpected permission result: " + requestCode);
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

    @Override
    public void onClick(View view, int position) {
        clickImage(position);
    }

    /**
     * Handle image selection event: add or remove selected image, change title
     */
    private void clickImage(int position) {
        Image image = imageAdapter.getItem(position);
        int selectedItemPosition = selectedImagePosition(image);
        if (config.getMode() == ImagePicker.MODE_MULTIPLE) {
            if (selectedItemPosition == -1) {
                if (imageAdapter.getSelectedImages().size() < config.getLimit()) {
                    imageAdapter.addSelected(image);
                } else {
                    Toast.makeText(this, R.string.msg_limit_images, Toast.LENGTH_SHORT).show();
                }
            } else {
                imageAdapter.removeSelectedPosition(selectedItemPosition, position);
            }
        } else {
            if (selectedItemPosition != -1)
                imageAdapter.removeSelectedPosition(selectedItemPosition, position);
            else {
                if (imageAdapter.getSelectedImages().size() > 0) {
                    imageAdapter.removeAllSelectedSingleClick();
                }
                imageAdapter.addSelected(image);
            }
        }
        updateTitle();
    }

    private int selectedImagePosition(Image image) {
        List<Image> selectedImages = imageAdapter.getSelectedImages();
        for (int i = 0; i < selectedImages.size(); i++) {
            if (selectedImages.get(i).getPath().equals(image.getPath())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check if the captured image is stored successfully
     * Then reload data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CAPTURE && resultCode == RESULT_OK) {
            presenter.finishCaptureImage(this, data, config);
        }
    }

    /**
     * Request for camera permission
     */
    private void captureImageWithPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            } else {
                Log.w(TAG, "Camera permission is not granted. Requesting permission");
                requestCameraPermission();
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
        presenter.captureImage(this, config, RC_CAPTURE);
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (handler == null) {
            handler = new Handler();
        }
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                getData();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);
    }

    /**
     * Update activity title
     * If we're displaying folder, set folder title
     * If we're displaying images, show number of selected images
     */
    private void updateTitle() {
        supportInvalidateOptionsMenu();

        if (isDisplayingFolderView()) {
            actionBar.setTitle(config.getFolderTitle());
            return;
        }

        if (imageAdapter.getSelectedImages().isEmpty()) {
            actionBar.setTitle(config.getImageTitle());
        } else if (config.getMode() == ImagePicker.MODE_MULTIPLE) {
            int imageSize = imageAdapter.getSelectedImages().size();
            actionBar.setTitle(config.getLimit() == ImagePicker.MAX_LIMIT
                    ? String.format(getString(R.string.selected), imageSize)
                    : String.format(getString(R.string.selected_with_limit), imageSize, config.getLimit()));
        }
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

    /**
     * Check if displaying folders view
     */
    private boolean isDisplayingFolderView() {
        return (config.isFolderMode() &&
                (recyclerView.getAdapter() == null || recyclerView.getAdapter() instanceof FolderPickerAdapter));
    }

    /**
     * When press back button, show folders if view is displaying images
     */
    @Override
    public void onBackPressed() {
        if (config.isFolderMode() && !isDisplayingFolderView()) {
            setFolderAdapter(null);
            return;
        }
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    /* --------------------------------------------------- */
    /* > View Methods */
    /* --------------------------------------------------- */

    @Override
    public void finishPickImages(List<Image> images) {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(EXTRA_SELECTED_IMAGES,
                (ArrayList<? extends Parcelable>) images);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void showCapturedImage() {
        getDataWithPermission();
    }

    @Override
    public void showFetchCompleted(List<Image> images, List<Folder> folders) {
        if (config.isFolderMode()) {
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
