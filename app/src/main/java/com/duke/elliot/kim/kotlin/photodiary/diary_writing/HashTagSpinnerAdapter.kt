package com.duke.elliot.kim.kotlin.photodiary.diary_writing

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RelativeLayout
import android.widget.TextView
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.utility.convertPxToDp
import kotlinx.android.synthetic.main.item_spinner_hash_tag.view.*

class HashTagSpinnerAdapter(context: Context, private val items: ArrayList<String>): BaseAdapter() {

    class ViewHolder(val view: RelativeLayout)

    private lateinit var deleteButtonClickListener: (position: Int) -> Unit

    fun setDeleteButtonClickListener(listener: (position: Int) -> Unit) {
        this.deleteButtonClickListener = listener
    }

    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_spinner_hash_tag, parent, false) as RelativeLayout)
            holder.view.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.view.visibility = View.GONE

        return holder.view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(inflater.inflate(R.layout.item_spinner_hash_tag, parent, false) as RelativeLayout)
            holder.view.tag = holder
        } else
            holder = convertView.tag as ViewHolder

        holder.view.text_item.text = items[position]
        holder.view.image_delete.setOnClickListener {
            deleteButtonClickListener.invoke(position)
        }

        if (position == 0) {
            holder.view.text_item.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_sharp_add_24, 0, 0, 0
            )
            holder.view.image_delete.visibility = View.GONE
        }

        return holder.view
    }

    override fun getItem(position: Int): Any = items[position]

    override fun getItemId(position: Int): Long = 0L

    override fun getCount(): Int = items.count()
}