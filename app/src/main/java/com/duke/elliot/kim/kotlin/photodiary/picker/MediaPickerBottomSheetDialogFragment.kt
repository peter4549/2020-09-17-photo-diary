package com.duke.elliot.kim.kotlin.photodiary.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.BottomSheetFragmentMediaPickerBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.AudioHelper
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.scaleUp
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.item_media_picker.view.*

class MediaPickerBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private lateinit var mediaClickListener: OnMediaClickListener

    interface OnMediaClickListener {
        fun photoOnClick(pickedPhotoUris: List<String>)
        fun videoOnClick(pickedVideoUri: String)
        fun audioOnClick(pickedAudioUri: String)
    }

    fun setMediaClickListener(mediaClickListener: OnMediaClickListener) {
        this.mediaClickListener = mediaClickListener
    }

    private lateinit var binding: BottomSheetFragmentMediaPickerBinding
    private lateinit var diary: DiaryModel
    private lateinit var mediaPickerAdapter: MediaPickerAdapter
    private var mediaType = MediaHelper.MediaType.NULL

    fun setDiary(diary: DiaryModel) {
        this.diary = diary
    }

    fun setMediaType(mediaType: Int) {
        this.mediaType = mediaType
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_fragment_media_picker, container, false)
        binding.recyclerView.layoutManager = GridLayoutManagerWrapper(requireContext(), 3)

        uiUpdate(mediaType)

        when(mediaType) {
            MediaHelper.MediaType.PHOTO ->  {
                val photos = diary.mediaArray.filter { it.type == MediaHelper.MediaType.PHOTO }

                if (photos.isNotEmpty()) {
                    mediaPickerAdapter = MediaPickerAdapter(photos)
                    binding.recyclerView.adapter = mediaPickerAdapter
                } else {
                    showToast(requireContext(), getString(R.string.photo_not_found))
                    dismiss()
                }
            }
            MediaHelper.MediaType.VIDEO -> {
                val videos = diary.mediaArray.filter { it.type == MediaHelper.MediaType.VIDEO }

                if (videos.isNotEmpty()) {
                    mediaPickerAdapter = MediaPickerAdapter(videos)
                    binding.recyclerView.adapter = mediaPickerAdapter
                } else {
                    showToast(requireContext(), getString(R.string.video_not_found))
                    dismiss()
                }
            }
            MediaHelper.MediaType.AUDIO -> {
                val audios = diary.mediaArray.filter { it.type == MediaHelper.MediaType.AUDIO }

                if (audios.isNotEmpty()) {
                    mediaPickerAdapter = MediaPickerAdapter(audios)
                    binding.recyclerView.adapter = mediaPickerAdapter
                } else {
                    showToast(requireContext(), getString(R.string.audio_not_found))
                    dismiss()
                }
            }
            else -> showToast(requireContext(), getString(R.string.media_not_found))
        }

        return binding.root
    }

    private fun uiUpdate(mediaType: Int) {
        when (mediaType) {
            MediaHelper.MediaType.PHOTO -> {
                binding.textTitle.text = getString(R.string.select_photos)
                binding.textButtonContainer.visibility = View.VISIBLE
                binding.ok.setOnClickListener {
                    if (mediaPickerAdapter.pickedPhotoUris.isNotEmpty()) {
                        mediaClickListener.photoOnClick(mediaPickerAdapter.pickedPhotoUris)
                        dismiss()
                    } else
                        showToast(requireContext(), getString(R.string.no_picture_is_selected))
                }

                binding.selectAll.setOnClickListener {
                    mediaPickerAdapter.selectAllPhotos()
                }
            }
            MediaHelper.MediaType.VIDEO -> {
                binding.textTitle.text = getString(R.string.select_video)
                binding.textButtonContainer.visibility = View.GONE
            }
            MediaHelper.MediaType.AUDIO -> {
                binding.textTitle.text = getString(R.string.select_audio)
                binding.textButtonContainer.visibility = View.GONE
            }
        }
    }

    inner class MediaPickerAdapter(private val mediaList: List<MediaModel>): RecyclerView.Adapter<MediaPickerAdapter.ViewHolder>() {

        val pickedPhotoUris = mutableListOf<String>()

        fun selectAllPhotos() {
            for (media in mediaList) {
                if (!pickedPhotoUris.contains(media.uriString))
                    pickedPhotoUris.add(media.uriString)
            }

            notifyDataSetChanged()
        }

        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
            fun bind(media: MediaModel) {
                when(mediaType) {
                    MediaHelper.MediaType.PHOTO -> {
                        view.image_play.visibility = View.GONE
                        view.image_audio.visibility = View.GONE

                        // Select all
                        if (pickedPhotoUris.contains(media.uriString)) {
                            view.imagePicker.scaleUp(0.8F, 100L) {
                                view.imagePicker.setBackgroundResource(R.drawable.circle_transparent_background)
                                view.imagePicker.setImageResource(R.drawable.ic_sharp_check_circle_24)
                                view.imagePicker.scaleUp(1F, 100L)
                            }
                        }

                        setImage(view.imageContent, media.uriString)
                        view.setOnClickListener {
                            if (pickedPhotoUris.contains(media.uriString)) {
                                pickedPhotoUris.remove(media.uriString)
                                view.imagePicker.scaleUp(0.8F, 100L) {
                                    view.imagePicker.setBackgroundResource(R.drawable.circle_border)
                                    view.imagePicker.setImageResource(android.R.color.transparent)
                                    view.imagePicker.scaleUp(1F, 100L)
                                }
                            } else {
                                pickedPhotoUris.add(media.uriString)

                                view.imagePicker.scaleUp(0.8F, 100L) {
                                    view.imagePicker.setBackgroundResource(R.drawable.circle_transparent_background)
                                    view.imagePicker.setImageResource(R.drawable.ic_sharp_check_circle_24)
                                    view.imagePicker.scaleUp(1F, 100L)
                                }
                            }
                        }
                    }
                    MediaHelper.MediaType.VIDEO -> {
                        view.image_play.visibility = View.VISIBLE
                        view.image_audio.visibility = View.GONE

                        setImage(view.imageContent, media.uriString)
                        view.setOnClickListener {
                            mediaClickListener.videoOnClick(media.uriString)
                            dismiss()
                        }
                    }
                    MediaHelper.MediaType.AUDIO -> {
                        view.image_play.visibility = View.GONE
                        view.image_audio.visibility = View.VISIBLE

                        val albumArt = AudioHelper.getAudioAlbumArt(view.context, media.uriString)
                        albumArt?.let {
                            setImage(view.imageContent, it)
                            view.setOnClickListener {
                                mediaClickListener.audioOnClick(media.uriString)
                                dismiss()
                            }
                        } ?: run {
                            setImage(view.imageContent, R.drawable.blue_gradation_background)
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_picker, parent, false)

            if (mediaType == MediaHelper.MediaType.VIDEO || mediaType == MediaHelper.MediaType.AUDIO) {
                view.imagePicker.visibility = View.GONE
            } else {
                view.imagePicker.visibility = View.VISIBLE
            }

            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val media = mediaList[position]
            holder.bind(media)
        }

        override fun getItemCount(): Int = mediaList.count()
    }
}