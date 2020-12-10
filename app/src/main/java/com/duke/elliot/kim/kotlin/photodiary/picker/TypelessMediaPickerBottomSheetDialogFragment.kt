package com.duke.elliot.kim.kotlin.photodiary.picker

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.BottomSheetFragmentTypelessMediaPickerBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.scaleUp
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.item_media_picker.view.*

class TypelessMediaPickerBottomSheetDialogFragment: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetFragmentTypelessMediaPickerBinding
    private lateinit var diary: DiaryModel
    private lateinit var mediaPickerAdapter: MediaPickerAdapter
    private lateinit var mediaClickListener: OnMediaClickListener

    interface OnMediaClickListener {
        fun onClick(diary: DiaryModel, pickedMediaUris: List<Pair<Int, Uri>>)
    }

    fun setMediaClickListener(mediaClickListener: OnMediaClickListener) {
        this.mediaClickListener = mediaClickListener
    }

    fun setDiary(diary: DiaryModel) {
        this.diary = diary
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_fragment_typeless_media_picker, container, false)
        binding.recyclerView.layoutManager = GridLayoutManagerWrapper(requireContext(), 3)

        val imageVideoArray = diary.mediaArray.filter { it.type != MediaHelper.MediaType.AUDIO }
        mediaPickerAdapter = MediaPickerAdapter(imageVideoArray)
        binding.recyclerView.adapter = mediaPickerAdapter

        binding.ok.setOnClickListener {
            mediaClickListener.onClick(diary, mediaPickerAdapter.pickedMediaUris)
            dismiss()
        }

        return binding.root
    }

    inner class MediaPickerAdapter(private val mediaList: List<MediaModel>): RecyclerView.Adapter<MediaPickerAdapter.ViewHolder>() {

        val pickedMediaUris = mutableListOf<Pair<Int, Uri>>()

        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
            fun bind(media: MediaModel) {

                if (media.type == MediaHelper.MediaType.PHOTO) {
                    view.image_play.visibility = View.GONE
                    view.image_audio.visibility = View.GONE
                }

                if (media.type == MediaHelper.MediaType.VIDEO) {
                    view.image_play.visibility = View.VISIBLE
                    view.image_audio.visibility = View.GONE
                }

                setImage(view.imageContent, media.uriString)

                view.setOnClickListener {
                    val pickedUri = media.type to Uri.parse(media.uriString)
                    if (pickedMediaUris.contains(pickedUri)) {
                        pickedMediaUris.remove(pickedUri)
                        view.imagePicker.scaleUp(0.8F, 100L) {
                            view.imagePicker.setBackgroundResource(R.drawable.circle_border)
                            view.imagePicker.setImageResource(android.R.color.transparent)
                            view.imagePicker.scaleUp(1F, 100L)
                        }
                    } else {
                        pickedMediaUris.add(pickedUri)

                        view.imagePicker.scaleUp(0.8F, 100L) {
                            view.imagePicker.setBackgroundResource(R.drawable.circle_transparent_background)
                            view.imagePicker.setImageResource(R.drawable.ic_sharp_check_circle_24)
                            view.imagePicker.scaleUp(1F, 100L)
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_picker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val media = mediaList[position]
            holder.bind(media)
        }

        override fun getItemCount(): Int = mediaList.count()
    }
}