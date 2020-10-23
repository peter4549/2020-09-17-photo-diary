package com.duke.elliot.kim.kotlin.photodiary.utility

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.util.TypedValue
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

object ColorUtilities {

    fun setCursorDrawableColor(editText: TextView, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(color, color))
            // gradientDrawable.setSize(2.spToPx(editText.context).toInt(), editText.textSize.toInt())
            // editText.textCursorDrawable = gradientDrawable

            editText.textCursorDrawable?.setColorFilter(color)

            return
        }

        try {
            val editorField = try {
                TextView::class.java.getDeclaredField("mEditor").apply { isAccessible = true }
            } catch (t: Throwable) {
                null
            }

            val editor = editorField?.get(editText) ?: editText
            val editorClass: Class<*> = if (editorField == null) TextView::class.java else editor.javaClass

            val tintedCursorDrawable = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                .apply { isAccessible = true }
                .getInt(editText)
                .let { ContextCompat.getDrawable(editText.context, it) ?: return }
                .let { tintDrawable(it, color) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                editorClass
                    .getDeclaredField("mDrawableForCursor")
                    .apply { isAccessible = true }
                    .run { set(editor, tintedCursorDrawable) }
            } else {
                editorClass
                    .getDeclaredField("mCursorDrawable")
                    .apply { isAccessible = true }
                    .run { set(editor, arrayOf(tintedCursorDrawable, tintedCursorDrawable)) }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun Number.spToPx(context: Context? = null): Float {
        val res = context?.resources ?: android.content.res.Resources.getSystem()
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this.toFloat(),
            res.displayMetrics
        )
    }

    fun tintDrawable(drawable: Drawable, @ColorInt color: Int): Drawable {
        (drawable as? VectorDrawableCompat)
            ?.apply { setTintList(ColorStateList.valueOf(color)) }
            ?.let { return it }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            (drawable as? VectorDrawable)
                ?.apply { setTintList(ColorStateList.valueOf(color)) }
                ?.let { return it }
        }

        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrappedDrawable, color)
        return DrawableCompat.unwrap(wrappedDrawable)
    }

    fun setCursorPointerColor(editText: EditText, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            editText.textSelectHandle?.setColorFilter(color)
            editText.textSelectHandleLeft?.setColorFilter(color)
            editText.textSelectHandleRight?.setColorFilter(color)

            return
        }

        try {
            var textSelectHandleResField = TextView::class.java.getDeclaredField("mTextSelectHandleRes")
            textSelectHandleResField.isAccessible = true
            val drawableResId = textSelectHandleResField.getInt(editText)

            var textSelectHandleLeftResField = TextView::class.java.getDeclaredField("mTextSelectHandleLeftRes")
            textSelectHandleLeftResField.isAccessible = true
            val drawableLeftResId = textSelectHandleLeftResField.getInt(editText)

            var textSelectHandleRightResField = TextView::class.java.getDeclaredField("mTextSelectHandleRightRes")
            textSelectHandleRightResField.isAccessible = true
            val drawableRightResId = textSelectHandleRightResField.getInt(editText)

            textSelectHandleResField = TextView::class.java.getDeclaredField("mEditor")
            textSelectHandleResField.isAccessible = true

            textSelectHandleLeftResField = TextView::class.java.getDeclaredField("mEditor")
            textSelectHandleLeftResField.isAccessible = true

            textSelectHandleRightResField = TextView::class.java.getDeclaredField("mEditor")
            textSelectHandleRightResField.isAccessible = true

            val editor = textSelectHandleResField.get(editText)
            val editorLeft = textSelectHandleLeftResField.get(editText)
            val editorRight = textSelectHandleRightResField.get(editText)

            val drawable = ContextCompat.getDrawable(editText.context, drawableResId)!!
            drawable.setColorFilter(color, Mode.SRC_IN)

            val drawableLeft = ContextCompat.getDrawable(editText.context, drawableLeftResId)!!
            drawableLeft.setColorFilter(color, Mode.SRC_IN)

            val drawableRight = ContextCompat.getDrawable(editText.context, drawableRightResId)!!
            drawableRight.setColorFilter(color, Mode.SRC_IN)

            textSelectHandleResField = editor.javaClass.getDeclaredField("mSelectHandleCenter")
            textSelectHandleLeftResField = editor.javaClass.getDeclaredField("mSelectHandleLeft")
            textSelectHandleRightResField = editor.javaClass.getDeclaredField("mSelectHandleRight")

            textSelectHandleResField.isAccessible = true
            textSelectHandleResField.set(editor, drawable)
            textSelectHandleLeftResField.isAccessible = true
            textSelectHandleLeftResField.set(editorLeft, drawableLeft)
            textSelectHandleRightResField.isAccessible = true
            textSelectHandleRightResField.set(editorRight, drawableRight)

        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    @Size(3)
    private fun colorToHSL(
        @ColorInt color: Int,
        @Size(3) hsl: FloatArray = FloatArray(3)
    ): FloatArray {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f

        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        hsl[2] = (max + min) / 2

        if (max == min) {
            hsl[1] = 0f
            hsl[0] = hsl[1]
        } else {
            val d = max - min
            hsl[1] = if (hsl[2] > 0.5f) d / (2f - max - min) else d / (max + min)

            when (max) {
                r -> hsl[0] = (g - b) / d + (if (g < b) 6 else 0)
                g -> hsl[0] = (b - r) / d + 2
                b -> hsl[0] = (r - g) / d + 4
            }

            hsl[0] /= 6f
        }

        return hsl
    }

    @ColorInt
    private fun hslToColor(@Size(3) hsl: FloatArray): Int {
        val r: Float
        val g: Float
        val b: Float

        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]

        if (s == 0f) {
            b = l
            g = b
            r = g
        } else {
            val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
            val p = 2 * l - q
            r = hue2rgb(p, q, h + 1f / 3)
            g = hue2rgb(p, q, h)
            b = hue2rgb(p, q, h - 1f / 3)
        }

        return Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    private fun hue2rgb(p: Float, q: Float, t: Float): Float {
        var valueT = t
        if (valueT < 0) valueT += 1f
        if (valueT > 1) valueT -= 1f
        if (valueT < 1f / 6) return p + (q - p) * 6f * valueT
        if (valueT < 1f / 2) return q
        return if (valueT < 2f / 3) p + (q - p) * (2f / 3 - valueT) * 6f else p
    }

    @ColorInt
    fun lightenColor(
        @ColorInt color: Int,
        value: Float
    ): Int {
        val hsl = colorToHSL(color)
        hsl[2] += value
        hsl[2] = max(0f, min(hsl[2], 1f))
        return hslToColor(hsl)
    }

    @ColorInt
    fun darkenColor(
        @ColorInt color: Int,
        value: Float
    ): Int {
        val hsl = colorToHSL(color)
        hsl[2] -= value
        hsl[2] = max(0f, min(hsl[2], 1f))
        return hslToColor(hsl)
    }

    @ColorInt
    fun getComplementaryColor(@ColorInt color: Int): Int {
        val r = 255 - Color.red(color)
        val g = 255 - Color.green(color)
        val b = 255 - Color.blue(color)

        return Color.argb(255, r, g, b)
    }

    @ColorInt
    fun whiteToGrey(@ColorInt color: Int): Int {
        val limit = 224

        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)

        if (r > limit && g > limit && b > limit)
            return Color.argb(color.alpha, limit, limit, limit)

        return color
    }
}
