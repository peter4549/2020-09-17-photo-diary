package com.duke.elliot.kim.kotlin.photodiary.diary.media

import android.net.Uri
import android.view.View
import com.bumptech.glide.Glide
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.base.BaseRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.diary.media.media_helper.PhotoHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import kotlinx.android.synthetic.main.item_media.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaAdapter(layoutId: Int, mediaArrayList: ArrayList<MediaModel>)
    : BaseRecyclerViewAdapter<MediaModel>(layoutId, mediaArrayList) {

    private lateinit var fileUtilities: FileUtilities
    private var selectedItemPosition: Int? = null
    private var itemClickListener: ((media: MediaModel) -> Unit)? = null

    fun setFileUtilities(fileUtilities: FileUtilities) {
        this.fileUtilities = fileUtilities
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = items[position]

        when (media.type) {
            MediaHelper.MediaType.PHOTO -> {
                Glide.with(holder.view.image.context).clear(holder.view.image)
                setImage(holder.view.image, media.uri)
            }
            MediaHelper.MediaType.VIDEO -> {
                Glide.with(holder.view.image.context).clear(holder.view.image)
                setImage(holder.view.image, media.uri)
                holder.view.image_play.visibility = View.VISIBLE
            }
            MediaHelper.MediaType.AUDIO -> {
                Glide.with(holder.view.image.context).clear(holder.view.image)
                holder.view.image_audio.visibility = View.VISIBLE
                //setImage
            } // media.videoUri?.let { setImage(holder.view.image, it) }
        }

        holder.view.setOnClickListener {
            selectedItemPosition = position
            itemClickListener?.invoke(media)
        }

        holder.view.image_delete.setOnClickListener {
            remove(media)
        }
    }

    override fun remove(position: Int) {
        val path = fileUtilities.getPath(items[position].uri)
        super.remove(position)
        CoroutineScope(Dispatchers.IO).launch {
            if (path != null)
                PhotoHelper.deleteImageFile(path)
            else
                Timber.e("File path not found")
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