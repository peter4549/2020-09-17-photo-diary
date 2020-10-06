package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R
import ja.burhanrashid52.photoeditor.PhotoFilter
import java.io.IOException
import java.io.InputStream

class FilterViewAdapter(private val filterListener: FilterListener) :
    RecyclerView.Adapter<FilterViewAdapter.ViewHolder>() {

    interface FilterListener {
        fun onFilterSelected(photoFilter: PhotoFilter?)
    }

    private val pairs: ArrayList<Pair<String, PhotoFilter>> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_filter_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (first, second) = pairs[position]
        val fromAsset = getBitmapFromAsset(holder.itemView.context, first)
        holder.imageFilter.setImageBitmap(fromAsset)
        holder.textFilterName.text = second.name.replace("_", " ")
    }

    override fun getItemCount(): Int {
        return pairs.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageFilter: ImageView = itemView.findViewById(R.id.image_filter)
        var textFilterName: TextView = itemView.findViewById(R.id.text_filter_name)

        init {
            itemView.setOnClickListener { filterListener.onFilterSelected(pairs[layoutPosition].second) }
        }
    }

    private fun getBitmapFromAsset(context: Context, strName: String): Bitmap? {
        val assetManager: AssetManager = context.assets
        val inputStream: InputStream?
        return try {
            inputStream = assetManager.open(strName)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun setupFilters() {
        pairs.add(Pair("filters/original.jpg", PhotoFilter.NONE))
        pairs.add(Pair("filters/auto_fix.png", PhotoFilter.AUTO_FIX))
        pairs.add(Pair("filters/brightness.png", PhotoFilter.BRIGHTNESS))
        pairs.add(Pair("filters/contrast.png", PhotoFilter.CONTRAST))
        pairs.add(Pair("filters/documentary.png", PhotoFilter.DOCUMENTARY))
        pairs.add(Pair("filters/dual_tone.png", PhotoFilter.DUE_TONE))
        pairs.add(Pair("filters/fill_light.png", PhotoFilter.FILL_LIGHT))
        pairs.add(Pair("filters/fish_eye.png", PhotoFilter.FISH_EYE))
        pairs.add(Pair("filters/grain.png", PhotoFilter.GRAIN))
        pairs.add(Pair("filters/gray_scale.png", PhotoFilter.GRAY_SCALE))
        pairs.add(Pair("filters/lomish.png", PhotoFilter.LOMISH))
        pairs.add(Pair("filters/negative.png", PhotoFilter.NEGATIVE))
        pairs.add(Pair("filters/posterize.png", PhotoFilter.POSTERIZE))
        pairs.add(Pair("filters/saturate.png", PhotoFilter.SATURATE))
        pairs.add(Pair("filters/sepia.png", PhotoFilter.SEPIA))
        pairs.add(Pair("filters/sharpen.png", PhotoFilter.SHARPEN))
        pairs.add(Pair("filters/temprature.png", PhotoFilter.TEMPERATURE))
        pairs.add(Pair("filters/tint.png", PhotoFilter.TINT))
        pairs.add(Pair("filters/vignette.png", PhotoFilter.VIGNETTE))
        pairs.add(Pair("filters/cross_process.png", PhotoFilter.CROSS_PROCESS))
        pairs.add(Pair("filters/b_n_w.png", PhotoFilter.BLACK_WHITE))
        pairs.add(Pair("filters/flip_horizental.png", PhotoFilter.FLIP_HORIZONTAL))
        pairs.add(Pair("filters/flip_vertical.png", PhotoFilter.FLIP_VERTICAL))
        pairs.add(Pair("filters/rotate.png", PhotoFilter.ROTATE))
    }

    init {
        setupFilters()
    }
}