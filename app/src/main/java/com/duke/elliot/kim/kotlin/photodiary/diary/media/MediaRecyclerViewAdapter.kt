package com.duke.elliot.kim.kotlin.photodiary.diary.media

import com.duke.elliot.kim.kotlin.photodiary.base.BaseRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.setImage
import kotlinx.android.synthetic.main.item_media.view.*

class MediaRecyclerViewAdapter(layoutId: Int, items: ArrayList<MediaModel>):
    BaseRecyclerViewAdapter<MediaModel>(layoutId, items) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = items[position]
        setImage(holder.view.image, media.bitmap)
    }
}