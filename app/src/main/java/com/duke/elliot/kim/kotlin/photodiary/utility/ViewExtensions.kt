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

fun ImageView.rotate(
    degrees: Float, duration: Number,
    animationListenerAdapter: AnimatorListenerAdapter? = null
) {
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

fun View.scaleDown(duration: Long = 200L) {
    this.animate()
        .scaleX(0.0F)
        .scaleY(0.0F)
        .alpha(0F)
        .setDuration(duration)
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator?) {}
            override fun onAnimationEnd(animator: Animator?) {
                this@scaleDown.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(animator: Animator?) {}
            override fun onAnimationRepeat(animator: Animator?) {}
        })
        .start()
}

fun View.scaleUp(
    scale: Float = 0.8F,
    duration: Long = 200L,
    animationEndCallback: (() -> Unit)? = null
) {
    this.animate()
        .scaleX(scale)
        .scaleY(scale)
        .alpha(1F)
        .setDuration(duration)
        .setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator?) {
                this@scaleUp.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animator: Animator?) {
                animationEndCallback?.invoke()
            }

            override fun onAnimationCancel(animator: Animator?) {}
            override fun onAnimationRepeat(animator: Animator?) {}
        })
        .start()
}

fun View.scaleShowUp(from: Float = 0.0F, to: Float = 1.0F, duration: Long = 200L) {
    this.apply {
        scaleX = from
        scaleY = from
        visibility = View.GONE

        animate()
            .scaleX(to)
            .scaleY(to)
            .alpha(1F)
            .setDuration(duration)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator?) {
                    this@scaleShowUp.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animator: Animator?) {}
                override fun onAnimationCancel(animator: Animator?) {}
                override fun onAnimationRepeat(animator: Animator?) {}
            })
            .start()
    }
}

fun View.scaleHideDown(from: Float = 1.0F, to: Float = 0.0F, duration: Long = 200L) {
    this.apply {
        scaleX = from
        scaleY = from
        visibility = View.VISIBLE

        animate()
            .scaleX(to)
            .scaleY(to)
            .alpha(1F)
            .setDuration(duration)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator?) {
                    this@scaleHideDown.visibility = View.GONE
                }

                override fun onAnimationEnd(animator: Animator?) {}
                override fun onAnimationCancel(animator: Animator?) {}
                override fun onAnimationRepeat(animator: Animator?) {}
            })
            .start()
    }
}
