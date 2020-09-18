package com.duke.elliot.kim.kotlin.photodiary

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.widget.ImageView

fun View.hideDown(duration: Number, height: Float) {
    this.apply {
        translationY = 0F

        animate().translationY(height)
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