package com.duke.elliot.kim.kotlin.photodiary.diary.media.simple_crop_view

import android.content.pm.ActivityInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentSimpleCropViewBinding
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import com.isseiaoki.simplecropview.CropImageView

class SimpleCropViewFragment: Fragment() {

    private lateinit var binding: FragmentSimpleCropViewBinding
    private lateinit var progressDialogFragment: ProgressDialogFragment
    private val frameColors = arrayOf(
        R.color.colorSimpleCropViewFrameWhite,
        R.color.colorSimpleCropViewFrameBlue,
        R.color.colorSimpleCropViewFrameGrey
    )
    private var currentFrameColorIndex = 0
    private var imageUri: Uri? = null

    private val buttonClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.button_close -> findNavController().popBackStack()
                R.id.button_frame_color -> changeFrameColor()
                R.id.button_fit_image -> binding.cropImageView.setCropMode(CropImageView.CropMode.FIT_IMAGE)
                R.id.button_1_1 -> binding.cropImageView.setCropMode(CropImageView.CropMode.SQUARE)
                R.id.button_3_4 -> binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_3_4)
                R.id.button_4_3 -> binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_4_3)
                R.id.button_9_16 -> binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_9_16)
                R.id.button_16_9 -> binding.cropImageView.setCropMode(CropImageView.CropMode.RATIO_16_9)
                R.id.button_custom -> binding.cropImageView.setCustomRatio(7, 5)
                R.id.button_free -> binding.cropImageView.setCropMode(CropImageView.CropMode.FREE)
                R.id.button_circle -> binding.cropImageView.setCropMode(CropImageView.CropMode.CIRCLE)
                R.id.button_show_circle_but_crop_as_square -> binding.cropImageView.setCropMode(
                    CropImageView.CropMode.CIRCLE_SQUARE
                )
                R.id.button_rotate_left -> binding.cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D)
                R.id.button_rotate_right -> binding.cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
                R.id.button_done -> cropImage()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_simple_crop_view,
            container,
            false
        )

        progressDialogFragment = ProgressDialogFragment.instance

        val simpleCropViewFragmentArgs by navArgs<SimpleCropViewFragmentArgs>()
        imageUri = simpleCropViewFragmentArgs.imageUri // TODO 얘도 save arg에 추가할 것.

        initializeButtons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext())
            .load(imageUri)
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
                    showToast(requireContext(), getString(R.string.failed_to_load_image))
                    e?.printStackTrace()
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
            .transform(CenterCrop())
            .into(binding.cropImageView)
    }

    override fun onStart() {
        super.onStart()
        lockActivityOrientation(requireActivity())
    }

    override fun onStop() {
        super.onStop()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    private fun initializeButtons() {
        binding.buttonClose.setOnClickListener(buttonClickListener)
        binding.buttonFrameColor.setOnClickListener(buttonClickListener)
        binding.buttonFitImage.setOnClickListener(buttonClickListener)
        binding.button11.setOnClickListener(buttonClickListener)
        binding.button34.setOnClickListener(buttonClickListener)
        binding.button43.setOnClickListener(buttonClickListener)
        binding.button916.setOnClickListener(buttonClickListener)
        binding.button169.setOnClickListener(buttonClickListener)
        binding.buttonCustom.setOnClickListener(buttonClickListener)
        binding.buttonFree.setOnClickListener(buttonClickListener)
        binding.buttonCircle.setOnClickListener(buttonClickListener)
        binding.buttonShowCircleButCropAsSquare.setOnClickListener(buttonClickListener)
        binding.buttonRotateLeft.setOnClickListener(buttonClickListener)
        binding.buttonRotateRight.setOnClickListener(buttonClickListener)
        binding.buttonDone.setOnClickListener(buttonClickListener)
    }

    private fun changeFrameColor() {
        if (currentFrameColorIndex == 2)
            currentFrameColorIndex = 0
        else
            currentFrameColorIndex += 1

        val frameColor = ContextCompat.getColor(
            requireContext(),
            frameColors[currentFrameColorIndex]
        )
        binding.cropImageView.setFrameColor(frameColor)
        binding.cropImageView.setGuideColor(frameColor)
        binding.cropImageView.setHandleColor(frameColor)
        binding.buttonFrameColor.setColorFilter(frameColor)
    }

    private fun cropImage() {
        progressDialogFragment.show(requireActivity().supportFragmentManager, tag)
        val croppedBitmap = binding.cropImageView.croppedBitmap
        croppedBitmap?.let { bitmap ->
            val imageUri = PhotoHelper.bitmapToTempImageFile(
                requireContext(),
                bitmap,
                SIMPLE_CROP_VIEW_IMAGE_FILE_NAME
            )?.toUri()
            progressDialogFragment.dismiss()
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                KEY_CROPPED_IMAGE_URI,
                imageUri
            )
            findNavController().popBackStack()
        } ?: run {
            progressDialogFragment.dismiss()
            showToast(requireContext(), getString(R.string.failed_to_save_image))
            findNavController().popBackStack()
        }
    }

    companion object {
        const val KEY_CROPPED_IMAGE_URI = "key_cropped_image_uri"
        const val SIMPLE_CROP_VIEW_IMAGE_FILE_NAME = "simple_crop_view_image_"
    }
}