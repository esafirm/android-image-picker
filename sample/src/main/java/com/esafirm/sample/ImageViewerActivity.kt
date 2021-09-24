package com.esafirm.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.model.Image

class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        val linearLayout = findViewById<LinearLayout>(R.id.container)
        val images: List<Image>? = intent.getParcelableArrayListExtra("images")

        images?.forEach {
            val imageView = ImageView(this)
            Glide.with(imageView)
                .load(it.uri)
                .into(imageView)
            linearLayout.addView(imageView)
        }
    }

    companion object {
        fun start(context: Context, images: List<Image?>?) {
            val intent = Intent(context, ImageViewerActivity::class.java)
            intent.putParcelableArrayListExtra("images", images as ArrayList<out Parcelable?>?)
            context.startActivity(intent)
        }
    }
}
