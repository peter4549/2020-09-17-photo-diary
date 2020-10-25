package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.utility.convertPxToDp

class HashTagSpinnerAdapter(context: Context, private val items: ArrayList<String>): BaseAdapter() {

    class ViewHolder(val textView: TextView)

    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_spinner_hash_tag, parent, false).findViewById(
                R.id.text_item))
            holder.textView.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.textView.text = items[position]

        if (position == 0) {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_sharp_add_24, 0, 0, 0
            )
        }

        return holder.textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_spinner_hash_tag, parent, false).findViewById(
                R.id.text_item))
            holder.textView.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.textView.text = items[position]

        if (position == 0) {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_sharp_add_24, 0, 0, 0
            )
        }

        return holder.textView
    }

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = 0L

    override fun getCount(): Int = items.count()
}