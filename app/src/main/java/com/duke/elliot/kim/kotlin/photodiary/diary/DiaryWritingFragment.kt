package com.duke.elliot.kim.kotlin.photodiary.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.*
import kotlinx.android.synthetic.main.fragment_diary_writing.*
import kotlinx.android.synthetic.main.fragment_diary_writing.view.*


class DiaryWritingFragment: Fragment() {
    private var bottomNavigationViewIsShown = true
    private var bottomNavigationViewOptionsMenuIsShown = false
    private var linearLayoutOptionsMenuHeight = 0F
    private var mediumAnimationDuration = 0
    private var shortAnimationDuration = 0
    private val bottomNavigationViewChildClickListener = View.OnClickListener { view ->
        showOptionsMenu(view)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_writing, container, false)

        mediumAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        linearLayoutOptionsMenuHeight = dpToPx(requireContext(),
            resources.getDimension(R.dimen.dimen_linear_layout_options_menu_height)) * 0.5F

        view.frame_layout_dropdown.setOnClickListener {
            when {
                bottomNavigationViewOptionsMenuIsShown -> {
                    view.linear_layout_options_menu
                        .hideDown(shortAnimationDuration, linearLayoutOptionsMenuHeight)
                    linear_layout_options_container
                        .translateDown(mediumAnimationDuration, linearLayoutOptionsMenuHeight)
                    bottomNavigationViewOptionsMenuIsShown = false
                }
                bottomNavigationViewIsShown -> {
                    view.image_dropdown.rotate(180F, shortAnimationDuration)
                    view.linear_layout_options_container
                        .hideDown(shortAnimationDuration, view.linear_layout_options.height.toFloat())
                    bottomNavigationViewIsShown = false
                }
                else -> {
                    view.image_dropdown.rotate(0F, shortAnimationDuration)
                    view.linear_layout_options_container
                        .showUp(shortAnimationDuration, view.linear_layout_options.height.toFloat())
                    bottomNavigationViewIsShown = true
                }
            }
        }

        view.image_photo.setOnClickListener(bottomNavigationViewChildClickListener)
        view.image_video.setOnClickListener(bottomNavigationViewChildClickListener)
        view.image_audio.setOnClickListener(bottomNavigationViewChildClickListener)
        view.image_drawing.setOnClickListener(bottomNavigationViewChildClickListener)

        return view
    }

    private fun showOptionsMenu(view: View) {
        linear_layout_photo_options_menu.visibility = View.GONE
        linear_layout_video_options_menu.visibility = View.GONE
        linear_layout_audio_options_menu.visibility = View.GONE
        bottomNavigationViewOptionsMenuIsShown = true

        linear_layout_options_container.translateUp(shortAnimationDuration, linearLayoutOptionsMenuHeight)

        when(view.id) {
            R.id.image_photo -> {
                linear_layout_options_menu.showUp(mediumAnimationDuration, linearLayoutOptionsMenuHeight)
                linear_layout_photo_options_menu.visibility = View.VISIBLE
            }
        }
    }
}