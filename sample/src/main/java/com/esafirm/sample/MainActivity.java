package com.esafirm.sample;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Switch;
import android.widget.TextView;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.IpCons;
import com.esafirm.imagepicker.features.ReturnMode;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.rximagepicker.RxImagePicker;
import rx.Observable;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_CAMERA = 3000;

    private TextView textView;
    private ArrayList<Image> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.text_view);

        findViewById(R.id.button_pick_image).setOnClickListener(view -> start());
        findViewById(R.id.button_pick_image_rx).setOnClickListener(view -> getImagePickerObservable().forEach(action));
        findViewById(R.id.button_intent).setOnClickListener(v -> startWithIntent());
        findViewById(R.id.button_camera).setOnClickListener(v -> {
            captureImage();
        });

        findViewById(R.id.button_launch_fragment)
                .setOnClickListener(view -> getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new MainFragment())
                        .commitAllowingStateLoss());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_CAMERA) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void captureImage() {
        ImagePicker.cameraOnly().start(this);
    }

    Action1<List<Image>> action = this::printImages;

    private Observable<List<Image>> getImagePickerObservable() {
        return RxImagePicker.getInstance()
                .start(this, ImagePicker.create(this));
    }

    private void startWithIntent() {
        startActivityForResult(ImagePicker.create(this)
                .single()
                .returnMode(ReturnMode.ALL)
                .getIntent(this), IpCons.RC_IMAGE_PICKER);
    }

    public void start() {
        final boolean returnAfterCapture = ((Switch) findViewById(R.id.ef_switch_return_after_capture)).isChecked();
        final boolean isSingleMode = ((Switch) findViewById(R.id.ef_switch_single)).isChecked();
        final boolean useCustomImageLoader = ((Switch) findViewById(R.id.ef_switch_imageloader)).isChecked();
        final boolean folderMode = ((Switch) findViewById(R.id.ef_switch_folder_mode)).isChecked();
        final boolean includeVideo = ((Switch) findViewById(R.id.ef_switch_include_video)).isChecked();
        final boolean isExclude = ((Switch) findViewById(R.id.ef_switch_include_exclude)).isChecked();

        ImagePicker imagePicker = ImagePicker.create(this)
                .language("in") // Set image picker language
                .theme(R.style.ImagePickerTheme)
                .returnMode(returnAfterCapture
                        ? ReturnMode.ALL
                        : ReturnMode.NONE) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                .folderMode(folderMode) // set folder mode (false by default)
                .includeVideo(includeVideo) // include video (false by default)
                .toolbarArrowColor(Color.RED) // set toolbar arrow up color
                .toolbarFolderTitle("Folder") // folder selection title
                .toolbarImageTitle("Tap to select"); // image selection title

        if (useCustomImageLoader) {
            imagePicker.imageLoader(new GrayscaleImageLoader());
        }

        if (isSingleMode) {
            imagePicker.single();
        } else {
            imagePicker.multi(); // multi mode (default mode)
        }

        if (isExclude) {
            imagePicker.exclude(images); // don't show anything on this selected images
        } else {
            imagePicker.origin(images); // original selected images, used in multi mode
        }

        imagePicker.limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .imageFullDirectory(Environment.getExternalStorageDirectory().getPath()) // can be full path
                .start(); // start image picker activity with request code
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            images = (ArrayList<Image>) ImagePicker.getImages(data);
            printImages(images);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void printImages(List<Image> images) {
        if (images == null) return;

        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0, l = images.size(); i < l; i++) {
            stringBuffer.append(images.get(i).getPath()).append("\n");
        }
        textView.setText(stringBuffer.toString());
    }
}
