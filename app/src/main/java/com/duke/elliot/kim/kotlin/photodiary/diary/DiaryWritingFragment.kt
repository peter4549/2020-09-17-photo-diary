package com.duke.elliot.kim.kotlin.photodiary.diary

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.diary.media.PhotoHelper
import kotlinx.android.synthetic.main.fragment_diary_writing.*
import kotlinx.android.synthetic.main.fragment_diary_writing.view.*

class DiaryWritingFragment: Fragment() {
    private var bottomNavigationViewIsShown = true
    private var bottomNavigationViewOptionsMenuIsShown = false
    private var linearLayoutOptionsHeight = 0F
    private var linearLayoutOptionsMenuHeight = 0F
    private var mediumAnimationDuration = 0
    private var recyclerViewMediaIsShown = false
    private var shortAnimationDuration = 0
    private val bottomNavigationViewOptionsClickListener = View.OnClickListener { view ->
        showOptionsMenu(view)
    }
    private val bottomNavigationViewOptionsMenuClickListener = View.OnClickListener { view ->
        when(view.id) {
            R.id.linear_layout_photo_shoot -> PhotoHelper.dispatchTakePictureIntent(requireActivity())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_diary_writing, container, false)

        mediumAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime)
        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        linearLayoutOptionsHeight = convertDpToPixel(requireContext(),
            resources.getDimension(R.dimen.dimen_linear_layout_options_height) / resources.displayMetrics.density)
        linearLayoutOptionsMenuHeight = convertDpToPixel(requireContext(),
            resources.getDimension(R.dimen.dimen_linear_layout_options_menu_height) / resources.displayMetrics.density)

        showToast(requireContext(), linearLayoutOptionsHeight.toString())

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
                        .hideDown(shortAnimationDuration, linearLayoutOptionsHeight)
                    bottomNavigationViewIsShown = false
                }
                else -> {
                    view.image_dropdown.rotate(0F, shortAnimationDuration)
                    view.linear_layout_options_container
                        .showUp(shortAnimationDuration, linearLayoutOptionsHeight)
                    bottomNavigationViewIsShown = true
                }
            }
        }

        view.image_photo.setOnClickListener(bottomNavigationViewOptionsClickListener)
        view.image_video.setOnClickListener(bottomNavigationViewOptionsClickListener)
        view.image_audio.setOnClickListener(bottomNavigationViewOptionsClickListener)
        view.image_drawing.setOnClickListener(bottomNavigationViewOptionsClickListener)

        view.linear_layout_photo_shoot.setOnClickListener(bottomNavigationViewOptionsMenuClickListener)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PhotoHelper.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
        }
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