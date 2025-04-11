package com.example.simple_world.ui.pdf

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.canhub.cropper.CropImageView
import com.example.simple_world.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.io.FileOutputStream

class CropImageFragment(private val uri: Uri, val onImageCropped: (Uri) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crop_image, container, false)

        val cropImageView = view.findViewById<CropImageView>(R.id.cropImageView)
        val cropButton = view.findViewById<Button>(R.id.cropButton)

        cropImageView.setImageUriAsync(uri)

        cropButton.setOnClickListener {
            val cropped = cropImageView.croppedImage
            val file = File(requireContext().filesDir, "cropped_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            if (cropped != null) {
                cropped.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            outputStream.close()

            onImageCropped(Uri.fromFile(file))
            dismiss()
        }

        return view
    }
}
