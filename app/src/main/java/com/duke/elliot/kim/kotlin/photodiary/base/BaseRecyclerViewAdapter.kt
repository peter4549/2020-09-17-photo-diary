package com.duke.elliot.kim.kotlin.photodiary.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("unused")
open class BaseRecyclerViewAdapter<T: Any?>(private val layoutId: Int,
                                            protected var items: ArrayList<T> = arrayListOf()):
    RecyclerView.Adapter<BaseRecyclerViewAdapter.ViewHolder>() {

    lateinit var recyclerView: RecyclerView

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {  }

    open fun insert(item: T, position: Int = 0) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    fun updateUi(item: T) {
        notifyItemChanged(items.indexOf(item))
    }

    fun updateUi(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemChanged(position)
        }
    }

    fun update(position: Int, item: T) {
        try {
            items[position] = item
            CoroutineScope(Dispatchers.Main).launch {
                notifyItemChanged(position)
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    fun remove(item: T) {
        val position = items.indexOf(item)
        items.remove(item)
        CoroutineScope(Dispatchers.IO).launch {
            launch(Dispatchers.Main) {
                notifyItemRemoved(position)
            }
        }
    }

    fun remove(position: Int) {
        items.removeAt(position)
        CoroutineScope(Dispatchers.IO).launch {
            launch(Dispatchers.Main) {
                notifyItemRemoved(position)
            }
        }
    }

    fun smoothScrollToEnd() {
        recyclerView.smoothScrollToPosition(itemCount)
    }
}