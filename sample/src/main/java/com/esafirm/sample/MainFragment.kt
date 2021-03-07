package com.esafirm.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePickerConfigFactory
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.features.registerImagePicker
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment() {

    private val startImagePicker = registerImagePicker {
        val firstImage = it.firstOrNull() ?: return@registerImagePicker
        Glide.with(img_fragment)
            .load(firstImage.uri)
            .into(img_fragment)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_pick_fragment.setOnClickListener {
            startImagePicker(ImagePickerConfigFactory.create {
                mode = ImagePickerMode.SINGLE
                returnMode = ReturnMode.ALL // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                isFolderMode = true // set folder mode (false by default)
                folderTitle = "Folder" // folder selection title
                imageTitle = "Tap to select" // image selection title
                doneButtonText = "DONE" // done button text
            })
        }

        button_close.setOnClickListener {
            fragmentManager?.beginTransaction()
                ?.remove(this@MainFragment)
                ?.commitAllowingStateLoss()
        }
    }
}