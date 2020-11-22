package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.content.Context
import android.view.*
import androidx.core.net.toUri
import androidx.viewpager.widget.PagerAdapter
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.AudioHelper
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import kotlinx.android.synthetic.main.image_pager_view.view.*

class MediaPagerAdapter : PagerAdapter() {
    private var mediaList: List<MediaModel>? = null
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    fun setMediaList(mediaList: List<MediaModel>?) {
        this.mediaList = mediaList
    }

    override fun instantiateItem(collection: ViewGroup, position: Int): View {
        val media = mediaList?.get(position)

        val view = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.image_pager_view, collection, false)!!

        media?.let {
            when(it.type) {
                MediaHelper.MediaType.PHOTO -> {
                    view.image_play.visibility = View.GONE
                    view.image_audio.visibility = View.GONE

                    setImage(view.image_media, it.uriString.toUri())
                }
                MediaHelper.MediaType.VIDEO -> {
                    view.image_play.visibility = View.VISIBLE
                    view.image_audio.visibility = View.GONE

                    setImage(view.image_media, it.uriString.toUri())

                }
                MediaHelper.MediaType.AUDIO -> {
                    view.image_play.visibility = View.GONE
                    view.image_audio.visibility = View.VISIBLE

                    AudioHelper.getAudioAlbumArt(view.context, it.uriString)?.let { bitmap ->
                        setImage(view.image_media, bitmap)
                    } ?: run {
                        setImage(view.image_media, R.drawable.blue_gradation_background)
                    }
                }
            }
        }

        collection.addView(view)

        return view
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return mediaList?.count() ?: 0
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun getItem(position: Int) = mediaList?.get(position)
}
