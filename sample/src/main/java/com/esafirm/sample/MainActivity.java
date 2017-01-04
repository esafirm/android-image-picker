package com.esafirm.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.ImagePickerActivity;
import com.esafirm.imagepicker.features.camera.CameraModule;
import com.esafirm.imagepicker.features.camera.ImmediateCameraModule;
import com.esafirm.imagepicker.features.camera.OnImageReadyListener;
import com.esafirm.imagepicker.model.Image;
import com.esafirm.rximagepicker.RxImagePicker;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    private static final int RC_CODE_PICKER = 2000;
    private static final int RC_CAMERA = 3000;

    private TextView textView;
    private ArrayList<Image> images = new ArrayList<>();
    private CameraModule cameraModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_view);

        findViewById(R.id.button_pick_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        findViewById(R.id.button_pick_image_rx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImagePickerObservable().forEach(action);
            }
        });

        findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Activity activity = MainActivity.this;
                final String[] permissions = new String[]{Manifest.permission.CAMERA};
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, permissions, RC_CAMERA);
                } else {
                    captureImage();
                }
            }
        });

        findViewById(R.id.button_launch_fragment)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new MainFragment())
                                .commitAllowingStateLoss();
                    }
                });

        findViewById(R.id.button_pick_image_intent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWithIntent();
            }
        });
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
        startActivityForResult(
                getCameraModule().getCameraIntent(MainActivity.this), RC_CAMERA);
    }

    private ImmediateCameraModule getCameraModule() {
        if (cameraModule == null) {
            cameraModule = new ImmediateCameraModule();
        }
        return (ImmediateCameraModule) cameraModule;
    }

    Action1<List<Image>> action = new Action1<List<Image>>() {
        @Override
        public void call(List<Image> images) {
            printImages(images);
        }
    };

    private Observable<List<Image>> getImagePickerObservable() {
        return RxImagePicker.getInstance()
                .start(this, ImagePicker.create(this));
    }

    // Recommended builder
    public void start() {
        boolean returnAfterCapture = ((Switch) findViewById(R.id.ef_switch_return_after_capture)).isChecked();
        boolean isSingleMode = ((Switch) findViewById(R.id.ef_switch_single)).isChecked();

        ImagePicker imagePicker = ImagePicker.create(this)
                .returnAfterFirst(returnAfterCapture) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select"); // image selection title

        if (isSingleMode) {
            imagePicker.single();
        } else {
            imagePicker.multi(); // multi mode (default mode)
        }

        imagePicker.limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(RC_CODE_PICKER); // start image picker activity with request code
    }

    // Traditional intent
    public void startWithIntent() {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        intent.putExtra(ImagePicker.EXTRA_FOLDER_MODE, true);
        intent.putExtra(ImagePicker.EXTRA_MODE, ImagePicker.MODE_MULTIPLE);
        intent.putExtra(ImagePicker.EXTRA_LIMIT, 10);
        intent.putExtra(ImagePicker.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGES, images);
        intent.putExtra(ImagePicker.EXTRA_FOLDER_TITLE, "Album");
        intent.putExtra(ImagePicker.EXTRA_IMAGE_TITLE, "Tap to select images");
        intent.putExtra(ImagePicker.EXTRA_IMAGE_DIRECTORY, "Camera");

        /* Will force ImagePicker to single pick */
        intent.putExtra(ImagePicker.EXTRA_RETURN_AFTER_FIRST, true);

        startActivityForResult(intent, RC_CODE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (requestCode == RC_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = (ArrayList<Image>) ImagePicker.getImages(data);
            printImages(images);
            return;
        }

        if (requestCode == RC_CAMERA && resultCode == RESULT_OK) {
            getCameraModule().getImage(this, data, new OnImageReadyListener() {
                @Override
                public void onImageReady(List<Image> resultImages) {
                    images = (ArrayList<Image>) resultImages;
                    printImages(images);
                }
            });
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
