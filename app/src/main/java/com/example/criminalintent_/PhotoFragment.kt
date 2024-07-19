package com.example.criminalintent_

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

private const val ARG_PHOTO_URI = "crime_id"

class PhotoFragment : Fragment() {

    private lateinit var photoView: ImageView
    private lateinit var photoUri: Uri

    companion object {
        fun newInstance(photoUri: Uri): PhotoFragment {
            val args = Bundle().apply {
                putParcelable(ARG_PHOTO_URI, photoUri)
            }
            return PhotoFragment().apply {
                arguments = args
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_photo, container, false)

        photoView = view.findViewById(R.id.crime_photo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            photoUri = arguments?.getParcelable(ARG_PHOTO_URI, Uri::class.java)!!
        }
        else {
            photoUri = arguments?.getParcelable<Uri>(ARG_PHOTO_URI)!!
        }

        photoView.setImageURI(photoUri)

        return view
    }

}