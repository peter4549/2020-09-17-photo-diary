package com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.duke.elliot.kim.kotlin.photodiary.R
import kotlinx.android.synthetic.main.fragment_photo_viewer.view.*

class PhotoViewerFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_viewer, container, false)

        val photoViewerFragmentArgs by navArgs<PhotoViewerFragmentArgs>()
        val uriString = photoViewerFragmentArgs.uriString

        Glide.with(view.context)
            .load(Uri.parse(uriString))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .error(R.drawable.ic_sharp_not_interested_112)
            .fallback(R.drawable.ic_sharp_not_interested_112)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(view.photo_view)

        return view
    }
}