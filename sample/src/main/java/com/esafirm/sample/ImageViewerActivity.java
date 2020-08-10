package com.esafirm.sample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        LinearLayout linearLayout = findViewById(R.id.container);

        List<Image> images = getIntent().getParcelableArrayListExtra("images");
        for (Image image : images) {
            ImageView imageView = new ImageView(this);
            Glide.with(imageView)
                    .load(image.getUri())
                    .into(imageView);

            linearLayout.addView(imageView);
        }
    }

    public static void start(Context context, List<Image> images) {
        Intent intent = new Intent(context, ImageViewerActivity.class);
        intent.putParcelableArrayListExtra("images", (ArrayList<? extends Parcelable>) images);
        context.startActivity(intent);
    }
}
