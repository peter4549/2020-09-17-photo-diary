package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.utility.getFont

class FontSpinnerAdapter(context: Context, private val items: Array<String>): BaseAdapter() {

    private val fontMap: MutableMap<String, Typeface?> = mutableMapOf()

    init {
        setFontMap(context)
    }

    class ViewHolder(val textView: TextView)

    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_spinner, parent, false).findViewById(R.id.text_item))
            holder.textView.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.textView.text = items[position]

        val font = fontMap[items[position]]

        holder.textView.typeface = font

        return holder.textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_spinner, parent, false).findViewById(R.id.text_item))
            holder.textView.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.textView.text = items[position]

        val font = fontMap[items[position]]

        holder.textView.typeface = font

        return holder.textView
    }

    private fun setFontMap(context: Context) {
        for (font in items) {
            fontMap[font] = getFont(context, requireNotNull(MainActivity.fontNameIdMap[font]))
        }
    }

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = 0L

    override fun getCount(): Int = items.count()
}