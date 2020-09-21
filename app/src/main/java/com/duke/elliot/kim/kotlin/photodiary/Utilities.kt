package com.duke.elliot.kim.kotlin.photodiary

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("unused")
class GridLayoutManagerWrapper: GridLayoutManager {
    constructor(context: Context, spanCount: Int) : super(context, spanCount)
    constructor(context: Context, spanCount: Int, orientation: Int, reverseLayout: Boolean) :
            super(context, spanCount, orientation, reverseLayout)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)
    override fun supportsPredictiveItemAnimations(): Boolean { return false }
}

fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_SHORT) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(context, text, duration).show()
    }
}

fun convertDpToPx(context: Context, dp: Float)
        = dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

fun setImage(imageView: ImageView, bitmap: Bitmap) {
    Glide.with(imageView.context)
        .load(bitmap)
        .disallowHardwareConfig()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .downsample(DownsampleStrategy.AT_MOST)
        .error(R.drawable.ic_sharp_not_interested_112)
        .fallback(R.drawable.ic_sharp_not_interested_112)
        .listener(null)
        .skipMemoryCache(false)
        .transform(CenterCrop(), RoundedCorners(8))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
}

fun setImage(imageView: ImageView, uri: Uri) {
    Glide.with(imageView.context)
        .load(uri)
        .disallowHardwareConfig()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .downsample(DownsampleStrategy.AT_MOST)
        .error(R.drawable.ic_sharp_not_interested_112)
        .fallback(R.drawable.ic_sharp_not_interested_112)
        .listener(null)
        .skipMemoryCache(false)
        .transform(CenterCrop(), RoundedCorners(8))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
}