package com.duke.elliot.kim.kotlin.photodiary.picker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.BottomSheetFragmentMediaPickerBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.AudioHelper
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.scaleDown
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
                } else
                    showToast(requireContext(), getString(R.string.photo_not_found))
            }
            MediaHelper.MediaType.VIDEO -> {
                val videos = diary.mediaArray.filter { it.type == MediaHelper.MediaType.VIDEO }

                if (videos.isNotEmpty()) {
                    mediaPickerAdapter = MediaPickerAdapter(videos)
                    binding.recyclerView.adapter = mediaPickerAdapter
                } else
                    showToast(requireContext(), getString(R.string.video_not_found))
            }
            MediaHelper.MediaType.AUDIO -> {
                val audios = diary.mediaArray.filter { it.type == MediaHelper.MediaType.AUDIO }

                if (audios.isNotEmpty()) {
                    mediaPickerAdapter = MediaPickerAdapter(audios)
                    binding.recyclerView.adapter = mediaPickerAdapter
                } else
                    showToast(requireContext(), getString(R.string.audio_not_found))
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
                    if (mediaPickerAdapter.pickedPhotoUris.isNotEmpty())
                        mediaClickListener.photoOnClick(mediaPickerAdapter.pickedPhotoUris)
                    else
                        showToast(requireContext(), getString(R.string.no_picture_is_selected))
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

        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view)

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

            if (mediaType == MediaHelper.MediaType.PHOTO) {
                holder.view.setOnClickListener {
                    if (pickedPhotoUris.contains(media.uriString)) {
                        pickedPhotoUris.remove(media.uriString)
                        holder.view.imagePicker.scaleUp(0.8F, 100L) {
                            holder.view.imagePicker.setBackgroundResource(R.drawable.circle_border)
                            holder.view.imagePicker.setImageResource(android.R.color.transparent)
                            holder.view.imagePicker.scaleUp(1F, 100L)
                        }
                        //holder.view.imagePicker.scaleUp(1F, 400L)
                    } else {
                        pickedPhotoUris.add(media.uriString)

                        println("ADDED: ${pickedPhotoUris}")
                        holder.view.imagePicker.scaleUp(0.8F, 100L) {
                            holder.view.imagePicker.setBackgroundResource(R.drawable.circle_transparent_background)
                            holder.view.imagePicker.setImageResource(R.drawable.ic_sharp_check_circle_24)
                            holder.view.imagePicker.scaleUp(1F, 100L)
                        }
                    }
                }
            }

            when(mediaType) {
                MediaHelper.MediaType.PHOTO -> setImage(holder.view.imageContent, media.uriString)
                MediaHelper.MediaType.VIDEO -> {
                    setImage(holder.view.imageContent, media.uriString)
                    holder.view.setOnClickListener {
                        mediaClickListener.videoOnClick(media.uriString)
                    }
                }
                MediaHelper.MediaType.AUDIO -> {
                    val albumArt = AudioHelper.getAudioAlbumArt(holder.view.context, media.uriString)
                    albumArt?.let {
                        setImage(holder.view.imageContent, it)
                        holder.view.setOnClickListener {
                            mediaClickListener.audioOnClick(media.uriString)
                        }
                    }
                }
            }
        }

        override fun getItemCount(): Int = mediaList.count()
    }
}