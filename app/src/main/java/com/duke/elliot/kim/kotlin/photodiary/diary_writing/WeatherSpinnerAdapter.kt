package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.duke.elliot.kim.kotlin.photodiary.R

class WeatherSpinnerAdapter(context: Context, private val weatherIconIds: Array<Int>): BaseAdapter() {

    class ViewHolder(val imageView: ImageView)

    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_weather_icon, parent, false).findViewById(R.id.image_weather_icon))
            holder.imageView.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.imageView.setImageResource(weatherIconIds[position])

        return holder.imageView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_weather_icon, parent, false).findViewById(R.id.image_weather_icon))
            holder.imageView.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.imageView.setImageResource(weatherIconIds[position])

        return holder.imageView
    }

    override fun getItem(position: Int): Any = weatherIconIds[position]

    override fun getItemId(position: Int): Long = 0L

    override fun getCount(): Int = weatherIconIds.count()
}