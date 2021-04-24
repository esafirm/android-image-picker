package com.esafirm.sample

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.model.Image
import java.io.File
import java.util.ArrayList

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

            val textView = TextView(this).apply {
                setPadding(24)
            }

            textView.setTextColor(Color.BLACK)
            textView.text = """
                Path: ${it.path}
                Absolute Path: ${File(it.path).absolutePath}
                Uri: ${it.uri}
            """.trimIndent()
            linearLayout.addView(textView)
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