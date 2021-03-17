package com.esafirm.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.features.registerImagePicker
import com.esafirm.sample.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var binding: FragmentMainBinding

    private val imagePickerLauncher = registerImagePicker {
        val firstImage = it.firstOrNull() ?: return@registerImagePicker
        Glide.with(binding.imgFragment)
            .load(firstImage.uri)
            .into(binding.imgFragment)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMainBinding.bind(view)

        binding.buttonPickFragment.setOnClickListener {
            imagePickerLauncher.launch(
                ImagePickerConfig {
                    mode = ImagePickerMode.SINGLE
                    returnMode = ReturnMode.ALL // set whether pick action or camera action should return immediate result or not. Only works in single mode for image picker
                    isFolderMode = true // set folder mode (false by default)
                    folderTitle = "Folder" // folder selection title
                    imageTitle = "Tap to select" // image selection title
                    doneButtonText = "DONE" // done button text
                }
            )
        }

        binding.buttonClose.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .remove(this@MainFragment)
                .commitAllowingStateLoss()
        }
    }
}