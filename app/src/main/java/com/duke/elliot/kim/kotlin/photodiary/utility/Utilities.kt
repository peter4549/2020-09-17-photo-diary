package com.duke.elliot.kim.kotlin.photodiary.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Surface
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor.PhotoEditorFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val PROGRESS_DIALOG = "ProgressDialog"

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

fun setImage(imageView: ImageView, bitmap: Bitmap, loadFailedCallback: (() -> Unit)? = null) {
    Glide.with(imageView.context)
        .load(bitmap)
        .disallowHardwareConfig()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .downsample(DownsampleStrategy.AT_MOST)
        .error(R.drawable.ic_sharp_not_interested_112)
        .fallback(R.drawable.ic_sharp_not_interested_112)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                e?.printStackTrace()
                loadFailedCallback?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        })
        .skipMemoryCache(false)
        .transform(CenterCrop(), RoundedCorners(8))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
}

fun setImage(imageView: ImageView, uri: Uri, loadFailedCallback: (() -> Unit)? = null) {
    Glide.with(imageView.context)
        .load(uri)
        .disallowHardwareConfig()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .error(R.drawable.ic_sharp_not_interested_112)
        .fallback(R.drawable.ic_sharp_not_interested_112)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                e?.printStackTrace()
                loadFailedCallback?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        })
        .skipMemoryCache(false)
        .transform(CenterCrop(), RoundedCorners(8))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
}

fun getCurrentDateString(): String = SimpleDateFormat("yyyy년 M월 d일 EEEE", Locale.getDefault()).format(
    Date()
)
fun getCurrentTimeString(): String = SimpleDateFormat("aa h:m", Locale.getDefault()).format(Date())


fun showProgress(fragment: Fragment) {
    val progressDialogFragment: ProgressDialogFragment = ProgressDialogFragment.instance
    fragment.requireActivity().supportFragmentManager
        .beginTransaction()
        .add(progressDialogFragment, PROGRESS_DIALOG).commitAllowingStateLoss()
}

fun dismissProgress(fragment: Fragment) {
    if (!fragment.isResumed)
        return
    val fragmentManager = fragment.activity?.supportFragmentManager ?: return

    val progressDialogFragment: ProgressDialogFragment? =
        fragmentManager.findFragmentByTag(PROGRESS_DIALOG) as? ProgressDialogFragment

    if (progressDialogFragment != null) {
        fragmentManager.beginTransaction()
            .remove(progressDialogFragment)
            .commitAllowingStateLoss()
    }
}

fun Drawable.setColorFilter(color: Int, mode: Mode = Mode.SRC_ATOP) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        colorFilter = BlendModeColorFilter(color, mode.getBlendMode())
    } else {
        @Suppress("DEPRECATION")
        setColorFilter(color, mode.getPorterDuffMode())
    }
}

// This class is needed to call the setColorFilter
// with different BlendMode on older API (before 29).
enum class Mode {
    CLEAR,
    SRC,
    DST,
    SRC_OVER,
    DST_OVER,
    SRC_IN,
    DST_IN,
    SRC_OUT,
    DST_OUT,
    SRC_ATOP,
    DST_ATOP,
    XOR,
    DARKEN,
    LIGHTEN,
    MULTIPLY,
    SCREEN,
    ADD,
    OVERLAY;

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getBlendMode(): BlendMode =
        when (this) {
            CLEAR -> BlendMode.CLEAR
            SRC -> BlendMode.SRC
            DST -> BlendMode.DST
            SRC_OVER -> BlendMode.SRC_OVER
            DST_OVER -> BlendMode.DST_OVER
            SRC_IN -> BlendMode.SRC_IN
            DST_IN -> BlendMode.DST_IN
            SRC_OUT -> BlendMode.SRC_OUT
            DST_OUT -> BlendMode.DST_OUT
            SRC_ATOP -> BlendMode.SRC_ATOP
            DST_ATOP -> BlendMode.DST_ATOP
            XOR -> BlendMode.XOR
            DARKEN -> BlendMode.DARKEN
            LIGHTEN -> BlendMode.LIGHTEN
            MULTIPLY -> BlendMode.MULTIPLY
            SCREEN -> BlendMode.SCREEN
            ADD -> BlendMode.PLUS
            OVERLAY -> BlendMode.OVERLAY
        }

    fun getPorterDuffMode(): PorterDuff.Mode =
        when (this) {
            CLEAR -> PorterDuff.Mode.CLEAR
            SRC -> PorterDuff.Mode.SRC
            DST -> PorterDuff.Mode.DST
            SRC_OVER -> PorterDuff.Mode.SRC_OVER
            DST_OVER -> PorterDuff.Mode.DST_OVER
            SRC_IN -> PorterDuff.Mode.SRC_IN
            DST_IN -> PorterDuff.Mode.DST_IN
            SRC_OUT -> PorterDuff.Mode.SRC_OUT
            DST_OUT -> PorterDuff.Mode.DST_OUT
            SRC_ATOP -> PorterDuff.Mode.SRC_ATOP
            DST_ATOP -> PorterDuff.Mode.DST_ATOP
            XOR -> PorterDuff.Mode.XOR
            DARKEN -> PorterDuff.Mode.DARKEN
            LIGHTEN -> PorterDuff.Mode.LIGHTEN
            MULTIPLY -> PorterDuff.Mode.MULTIPLY
            SCREEN -> PorterDuff.Mode.SCREEN
            ADD -> PorterDuff.Mode.ADD
            OVERLAY -> PorterDuff.Mode.OVERLAY
        }
}

@Suppress("DEPRECATION")
@SuppressLint("ObsoleteSdkInt")
fun lockActivityOrientation(activity: Activity) {
    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.display
    } else {
        activity.windowManager.defaultDisplay
    }
    val rotation = display?.rotation
    val height: Int?
    val width: Int?
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
        height = display?.height
        width = display?.width
    } else {
        val size = Point()
        display?.getSize(size)
        height = size.y
        width = size.x
    }

    if (width == null || height == null)
        return

    when (rotation) {
        Surface.ROTATION_90 -> if (width > height) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        Surface.ROTATION_180 -> if (height > width) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        Surface.ROTATION_270 -> if (width > height) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else -> if (height > width) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

fun bitmapToImageFile(context: Context, bitmap: Bitmap, fileName: String): File? {
    val imageFile: File? = try {
        MediaHelper.photoHelper.createImageFile(context,
            fileName
        )
    } catch (e: IOException) {
        return null
    }

    var outputStream: FileOutputStream? = null

    try {
        outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    } finally {
        try {
            outputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    return imageFile
}

