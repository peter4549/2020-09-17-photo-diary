package com.duke.elliot.kim.kotlin.photodiary.diary.media

import com.duke.elliot.kim.kotlin.photodiary.base.BaseRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import kotlinx.android.synthetic.main.item_media.view.*

class MediaAdapter(layoutId: Int, mediaArrayList: ArrayList<MediaModel>,
                   private val itemClickListener: (media: MediaModel) -> Unit)
    : BaseRecyclerViewAdapter<MediaModel>(layoutId, mediaArrayList) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = items[position]

        when (media.type) {
            MediaHelper.MediaType.PHOTO -> media.bitmap?.let { setImage(holder.view.image, it) }
            MediaHelper.MediaType.VIDEO -> media.videoUri?.let { setImage(holder.view.image, it) }
            MediaHelper.MediaType.AUDIO -> {  } // media.videoUri?.let { setImage(holder.view.image, it) }
        }

        holder.view.setOnClickListener {
            itemClickListener.invoke(media)
        }
    }
}