package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.viewpager.widget.PagerAdapter
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.diary.media.MediaModel
import com.duke.elliot.kim.kotlin.photodiary.utility.setImage
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
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

    @SuppressLint("ClickableViewAccessibility")
    private val suppressInterceptListener =
        View.OnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN && view is ViewGroup)
                view.requestDisallowInterceptTouchEvent(true)
            false
        }

    override fun instantiateItem(collection: ViewGroup, position: Int): View {
        val media = mediaList?.get(position)

        val view = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.image_pager_view, collection, false)!!

        if (media != null)
            setImage(view.image_media, media.uriString.toUri())

        view.setOnTouchListener(suppressInterceptListener)

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
}
