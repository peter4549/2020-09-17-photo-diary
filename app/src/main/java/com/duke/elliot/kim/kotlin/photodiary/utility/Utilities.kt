package com.duke.elliot.kim.kotlin.photodiary.utility

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Surface
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

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

fun setImage(imageView: ImageView, drawableId: Int, loadFailedCallback: (() -> Unit)? = null) {
    Glide.with(imageView.context)
        .load(drawableId)
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

fun setImage(imageView: ImageView, uri: String, loadFailedCallback: (() -> Unit)? = null) {
    Glide.with(imageView.context)
        .load(uri)
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

fun getCurrentTime() = Calendar.getInstance().timeInMillis

fun Long.toDateFormat(pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(this)

fun getFont(context: Context, id: Int): Typeface? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        context.resources.getFont(id)
    else ResourcesCompat.getFont(context, id)
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
        Surface.ROTATION_90 -> if (width > height) activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        Surface.ROTATION_180 -> if (height > width) activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        Surface.ROTATION_270 -> if (width > height) activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else -> if (height > width) activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
}

fun hasPermissions(context: Context, permissionRequired: Array<String>) = permissionRequired.all {
    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}

fun replaceLast(string: String, oldString: String, newString: String): String {
    val stringBuilder = StringBuilder(string)
    stringBuilder.replace(
        string.lastIndexOf(oldString),
        string.lastIndexOf(oldString) + oldString.length,
        newString
    )
    return stringBuilder.toString()
}

fun convertPxToDp(context: Context, px: Float): Float {
    val resources = context.resources
    val displayMetrics = resources.displayMetrics
    return px / (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

fun getOutputDirectory(context: Context): File {
    val appContext = context.applicationContext
    val mediaDir = context.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS).firstOrNull()?.let {
        File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() } }

    return if (mediaDir != null && mediaDir.exists())
        mediaDir else appContext.filesDir
}

fun showInputDialog(context: Context, title: String, okEvent: (text: String) -> Unit) {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)

    alertDialog.setMessage(title)

    val editText = EditText(context)
    val id = getCurrentTime().toInt()
    editText.id = id
    alertDialog.setView(editText)

    alertDialog.setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
        if (editText.text.isNotBlank()) {
            okEvent.invoke(editText.text.toString())
        }

        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        dialog.dismiss()
    }

    alertDialog.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
        dialog.dismiss()
    }

    val dialog = alertDialog.show()

    val alertTitleId = context.resources.getIdentifier("alertTitle", "id", context.packageName)
    val okButton = dialog.findViewById<Button>(android.R.id.button1)!!
    val cancelButton = dialog.findViewById<Button>(android.R.id.button2)!!
    val inflatedEditText = dialog.findViewById<EditText>(id)!!

    if (alertTitleId > 0) {
        val color = ContextCompat.getColor(context, R.color.colorTrueBlue)
        okButton.setTextColor(color)
        cancelButton.setTextColor(color)

        val layoutParams = inflatedEditText.layoutParams as FrameLayout.LayoutParams
        val dp16 = convertDpToPx(
            context,
            context.resources.getDimension(R.dimen.spacing_large) / context.resources.displayMetrics.density
        )
        layoutParams.marginEnd = dp16.toInt()
        layoutParams.marginStart = dp16.toInt()
    }
}

@Suppress("DEPRECATION")
fun getDocumentDirectory(): String {
    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
}

@SuppressLint("PackageManagerGetSignatures")
fun printHashKey(context: Context) {
    try {
        val info: PackageInfo =
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
        for (signature in info.signatures) {
            val md: MessageDigest = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            val hashKey = String(android.util.Base64.encode(md.digest(), 0))
            println("printHashKey() Hash Key: $hashKey")
        }
    } catch (e: NoSuchAlgorithmException) {
        println("printHashKey() $e")
    } catch (e: Exception) {
        println("printHashKey() $e")
    }
}

fun String.toPath() = Uri.parse(this).path

