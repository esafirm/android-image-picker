package com.esafirm.sample

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode

class MainFragment : Fragment() {
    private var imageView: ImageView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageView = view.findViewById(R.id.img_fragment)
        view.findViewById<View>(R.id.button_pick_fragment)
            .setOnClickListener { view1: View? ->
                ImagePicker.create(this@MainFragment)
                    .returnMode(ReturnMode.ALL) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                    .folderMode(true) // set folder mode (false by default)
                    .single()
                    .toolbarFolderTitle("Folder") // folder selection title
                    .toolbarImageTitle("Tap to select")
                    .toolbarDoneButtonText("DONE") // done button text
                    .start(0) // image selection title
            }
        view.findViewById<View>(R.id.button_close)
            .setOnClickListener { view12: View? ->
                fragmentManager!!.beginTransaction()
                    .remove(this@MainFragment)
                    .commitAllowingStateLoss()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val images = ImagePicker.getImages(data)
        if (images != null && !images.isEmpty()) {
            imageView!!.setImageBitmap(BitmapFactory.decodeFile(images[0].path))
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}