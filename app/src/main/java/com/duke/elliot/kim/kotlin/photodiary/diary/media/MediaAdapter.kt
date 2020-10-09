package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.net.Uri
import com.bumptech.glide.Glide
import com.duke.elliot.kim.kotlin.photodiary.base.BaseRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import kotlinx.android.synthetic.main.item_media.view.*

class MediaAdapter(layoutId: Int, mediaArrayList: ArrayList<MediaModel>)
    : BaseRecyclerViewAdapter<MediaModel>(layoutId, mediaArrayList) {

    private var selectedItemPosition: Int? = null
    private var itemClickListener: ((media: MediaModel) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = items[position]

        when (media.type) {
            MediaHelper.MediaType.PHOTO -> {
                Glide.with(holder.view.image.context).clear(holder.view.image)
                setImage(holder.view.image, media.uri)
            }
            MediaHelper.MediaType.VIDEO -> setImage(holder.view.image, media.uri)
            MediaHelper.MediaType.AUDIO -> {  } // media.videoUri?.let { setImage(holder.view.image, it) }
        }

        holder.view.setOnClickListener {
            selectedItemPosition = position
            itemClickListener?.invoke(media)
        }
    }

    fun getSelectedPosition() = selectedItemPosition

    fun changeImageUri(position: Int, uri: Uri) {
        items[position].uri = uri
        notifyItemChanged(position)
    }

    fun setItemClickListener(itemClickListener: ((media: MediaModel) -> Unit)) {
        this.itemClickListener = itemClickListener
    }
}