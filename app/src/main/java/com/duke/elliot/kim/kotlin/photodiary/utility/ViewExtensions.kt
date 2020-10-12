package com.duke.elliot.kim.kotlin.photodiary.utility

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

fun View.hideDown(duration: Number, height: Float) {
    this.apply {
        translationY = 0F

        animate().translationY(height)
            .setDuration(duration.toLong())
            .setListener(null)
    }
}

fun View.hideDownWithFading(duration: Number, height: Float) {
    this.apply {
        translationY = 0F

        animate()
            .alpha(0F)
            .translationY(height)
            .setDuration(duration.toLong())
            .setListener(null)
    }
}

fun View.crossFadeIn(duration: Number) {
    this.apply {
        alpha = 0F
        visibility = View.VISIBLE

        animate()
            .alpha(1F)
            .setDuration(duration.toLong())
            .setListener(null)
    }
}

fun View.hideDown(duration: Number) {
    this.apply {
        translationY = 0F

        animate().translationY(this.height.toFloat())
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    this@hideDown.visibility = View.GONE
                    super.onAnimationEnd(animation)
                }
            })
    }
}

fun View.showUp(duration: Number, height: Float = 0F) {
    this.apply {
        alpha = 1F
        translationY = height
        visibility = View.VISIBLE

        animate().translationY(0F)
            .setDuration(duration.toLong())
            .setListener(null)
    }
}

fun View.showUp(duration: Number) {
    this.apply {
        visibility = View.VISIBLE
        translationY = this.height.toFloat()

        animate()
            .translationY(0F)
            .setDuration(duration.toLong())
            .setListener(null)
    }
}

fun View.translateDown(duration: Number, height: Float) {
    this.apply {
        translationY = -height

        animate().translationY(0F)
            .setDuration(duration.toLong())
            .setListener(null)
    }
}

fun View.translateDown(duration: Number, height: Float, animationEndCallback: (() -> Unit)? = null) {
    this.apply {
        translationY = -height

        animate().translationY(0F)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    animationEndCallback?.invoke()
                    super.onAnimationEnd(animation)
                }
            })
    }
}

fun View.translateUp(duration: Number, height: Float) {
    this.apply {
        translationY = 0F

        animate().translationY(-height)
            .setDuration(duration.toLong())
            .setListener(null)
    }
}

fun ImageView.rotate(degrees: Float, duration: Number,
                     animationListenerAdapter: AnimatorListenerAdapter? = null) {
    this.animate().rotation(degrees)
        .setDuration(duration.toLong())
        .setListener(animationListenerAdapter)
        .start()
}

fun ImageButton.setTintById(id: Int) {
    DrawableCompat.setTint(
        DrawableCompat.wrap(this.drawable),
        ContextCompat.getColor(this.context, id)
    )
}

fun ImageButton.setTintByColor(color: Int) {
    DrawableCompat.setTint(
        DrawableCompat.wrap(this.drawable),
        color
    )
}
