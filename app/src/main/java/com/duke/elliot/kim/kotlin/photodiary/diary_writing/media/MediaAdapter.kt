package com.duke.elliot.kim.kotlin.photodiary.diary_writing.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.View
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.base.BaseRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.AudioHelper
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import kotlinx.android.synthetic.main.item_media.view.*
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
                holder.view.image_audio.visibility = View.GONE
                holder.view.image_play.visibility = View.GONE

                Glide.with(holder.view.image.context).clear(holder.view.image)
                setImage(holder.view.image, media.uriString.toUri())
            }
            MediaHelper.MediaType.VIDEO -> {
                holder.view.image_audio.visibility = View.GONE

                Glide.with(holder.view.image.context).clear(holder.view.image)
                setImage(holder.view.image, media.uriString.toUri())
                holder.view.image_play.visibility = View.VISIBLE
            }
            MediaHelper.MediaType.AUDIO -> {
                holder.view.image_play.visibility = View.GONE

                Glide.with(holder.view.image.context).clear(holder.view.image)

                val albumArt = AudioHelper.getAudioAlbumArt(holder.view.context, media.uriString)
                albumArt?.let {
                    setImage(holder.view.image, it)
                } ?: run {
                    setImage(holder.view.image, R.drawable.blue_gradation_background)
                }

                holder.view.image_audio.visibility = View.VISIBLE
            }
        }

        holder.view.setOnClickListener {
            selectedItemPosition = position
            itemClickListener?.invoke(media)
        }

        holder.view.image_delete.setOnClickListener {
            remove(holder.absoluteAdapterPosition)
            holder.view.image_audio.visibility = View.GONE
            holder.view.image_play.visibility = View.GONE
        }
    }

    override fun remove(position: Int) {
        val path = fileUtilities.getPath(items[position].uriString.toUri())
        super.remove(position)
        if (path != null)
            MediaHelper.deleteFile(path)
        else
            Timber.e("File path not found")
    }

    fun getSelectedPosition() = selectedItemPosition

    fun changeImageUri(position: Int, uri: Uri) {
        items[position].uriString = uri.toString()
        notifyItemChanged(position)
    }

    fun setItemClickListener(itemClickListener: ((media: MediaModel) -> Unit)) {
        this.itemClickListener = itemClickListener
    }

    fun getMediaArray() = items.map { it }.toTypedArray()
}