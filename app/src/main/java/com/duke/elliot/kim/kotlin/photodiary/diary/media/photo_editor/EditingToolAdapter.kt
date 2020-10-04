package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.R

@Suppress("SpellCheckingInspection")
enum class ToolType {
    BRUSH, TEXT, ERASER, FILTER, EMOJI, STICKER
}

class EditingToolRecyclerViewAdapter(private val onItemSelected: OnItemSelected) :
    RecyclerView.Adapter<EditingToolRecyclerViewAdapter.ViewHolder>() {
    private val tools: ArrayList<ToolModel> = ArrayList()

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType?)
    }

    internal class ToolModel(
        val toolName: Int,
        val toolIcon: Int,
        val toolType: ToolType
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_editing_tool, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tool = tools[position]
        holder.imageToolIcon.setImageResource(tool.toolIcon)
        holder.textTool.setText(tool.toolName)
    }

    override fun getItemCount(): Int {
        return tools.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageToolIcon: ImageView = itemView.findViewById(R.id.image_tool_icon)
        var textTool: TextView = itemView.findViewById(R.id.text_tool_name)

        init {
            itemView.setOnClickListener { onItemSelected.onToolSelected(tools[layoutPosition].toolType) }
        }
    }

    init {
        tools.add(ToolModel(R.string.editing_tool_brush, R.drawable.ic_round_brush_40, ToolType.BRUSH))
        tools.add(ToolModel(R.string.editing_tool_text, R.drawable.ic_sharp_text_fields_40, ToolType.TEXT))
        tools.add(ToolModel(R.string.editing_tool_eraser, R.drawable.ic_eraser_black_24dp, ToolType.ERASER))
        tools.add(ToolModel(R.string.editing_tool_filter, R.drawable.ic_round_photo_filter_40, ToolType.FILTER))
        @Suppress("SpellCheckingInspection")
        tools.add(ToolModel(R.string.editing_tool_emoji, R.drawable.ic_round_insert_emoticon_40, ToolType.EMOJI))
        tools.add(ToolModel(R.string.editing_tool_sticker, R.drawable.ic_round_image_40, ToolType.STICKER))
    }
}