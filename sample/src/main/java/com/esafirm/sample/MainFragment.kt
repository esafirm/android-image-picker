package com.esafirm.sample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_pick_fragment.setOnClickListener {
            ImagePicker.create(this@MainFragment)
                .returnMode(ReturnMode.ALL) // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                .folderMode(true) // set folder mode (false by default)
                .single()
                .toolbarFolderTitle("Folder") // folder selection title
                .toolbarImageTitle("Tap to select")
                .toolbarDoneButtonText("DONE") // done button text
                .start(0) // image selection title
        }

        button_close.setOnClickListener {
            fragmentManager?.beginTransaction()
                ?.remove(this@MainFragment)
                ?.commitAllowingStateLoss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val images = ImagePicker.getImages(data)
        if (images != null && images.isNotEmpty()) {
            Glide.with(img_fragment)
                .load(images[0].uri)
                .into(img_fragment)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}