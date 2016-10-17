/*
 * Created by Nguyen Hoang Lam
 * Date: ${DATE}
 */

package com.nguyenhoanglam.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nguyenhoanglam.imagepicker.features.ImagePicker;
import com.nguyenhoanglam.imagepicker.features.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.util.ArrayList;

/**
 * Created by hoanglam on 8/4/16.
 */
public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Button buttonPickImage;

    private ArrayList<Image> images = new ArrayList<>();

    private int REQUEST_CODE_PICKER = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_view);
        buttonPickImage = (Button) findViewById(R.id.button_pick_image);
        buttonPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });


    }


    // Recomended builder
    public void start() {
        ImagePicker.create(this)
                .folderMode(true) // set folder mode (false by default)
                .folderTitle("Folder") // folder selection title
                .imageTitle("Tap to select") // image selection title
                .single() // single mode
                .multi() // multi mode (default mode)
                .limit(10) // max images can be selected (99 by default)
                .showCamera(true) // show camera or not (true by default)
                .imageDirectory("Camera")   // captured image directory name ("Camera" folder by default)
                .origin(images) // original selected images, used in multi mode
                .start(REQUEST_CODE_PICKER); // start image picker activity with request code
    }

    // Traditional intent
    public void startWithIntent() {
        Intent intent = new Intent(this, ImagePickerActivity.class);

        intent.putExtra(ImagePickerActivity.EXTRA_FOLDER_MODE, true);
        intent.putExtra(ImagePickerActivity.EXTRA_MODE, ImagePickerActivity.MODE_MULTIPLE);
        intent.putExtra(ImagePickerActivity.EXTRA_LIMIT, 10);
        intent.putExtra(ImagePickerActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(ImagePickerActivity.EXTRA_SELECTED_IMAGES, images);
        intent.putExtra(ImagePickerActivity.EXTRA_FOLDER_TITLE, "Album");
        intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_TITLE, "Tap to select images");
        intent.putExtra(ImagePickerActivity.EXTRA_IMAGE_DIRECTORY, "Camera");
        startActivityForResult(intent, REQUEST_CODE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = data.getParcelableArrayListExtra(ImagePickerActivity.EXTRA_SELECTED_IMAGES);
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0, l = images.size(); i < l; i++) {
                stringBuffer.append(images.get(i).getPath() + "\n");
            }
            textView.setText(stringBuffer.toString());
        }
    }
}
