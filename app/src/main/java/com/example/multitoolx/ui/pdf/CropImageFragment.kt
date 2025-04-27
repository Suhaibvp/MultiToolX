package com.example.multitoolx.ui.pdf

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.canhub.cropper.CropImageView
import com.example.multitoolx.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.io.FileOutputStream

/**
 * CropImageFragment.kt
 *
 * Created on: 2025-04-27
 * Author: Suhaib VP
 * Description:
 * - Fragment for cropping an image selected by the user.
 * - Provides functionality to crop and save the cropped image.
 */
class CropImageFragment(
    private val uri: Uri,
    private val onImageCropped: (Uri) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crop_image, container, false)

        // Initialize CropImageView and Crop Button
        val cropImageView = view.findViewById<CropImageView>(R.id.cropImageView)
        val cropButton = view.findViewById<Button>(R.id.cropButton)

        // Set the image URI for cropping
        cropImageView.setImageUriAsync(uri)

        // Handle crop button click
        cropButton.setOnClickListener {
            // Crop the image
            val cropped = cropImageView.croppedImage
            val file = File(requireContext().filesDir, "cropped_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            // Save the cropped image as a JPEG file
            cropped?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()

            // Notify the caller with the URI of the cropped image
            onImageCropped(Uri.fromFile(file))

            // Dismiss the fragment
            dismiss()
        }

        return view
    }
}
