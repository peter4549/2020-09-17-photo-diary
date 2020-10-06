package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.PhotoEditor
import kotlinx.android.synthetic.main.fragment_emoji_bottom_sheet_dialog.view.*


@Suppress("SpellCheckingInspection")
class EmojiBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var emojiListener: EmojiListener

    interface EmojiListener {
        fun onEmojiClick(emojiUnicode: String?)
    }

    private val bottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {  }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView: View =
            View.inflate(context, R.layout.fragment_emoji_bottom_sheet_dialog, null)
        dialog.setContentView(contentView)

        (((contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams?)
            ?.behavior as BottomSheetBehavior<*>?).let {
            if (it != null) {
                behavior = it
                behavior.addBottomSheetCallback(bottomSheetBehaviorCallback)
            }
        }

        context?.let { ContextCompat.getColor(it, android.R.color.transparent) }?.let {
            (contentView.parent as View).setBackgroundColor(it)
        }

        contentView.recycler_view_emoji.apply {
            layoutManager = GridLayoutManagerWrapper(context, 5)
            adapter = EmojiAdapter()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::behavior.isInitialized)
            behavior.removeBottomSheetCallback(bottomSheetBehaviorCallback)
    }

    fun setEmojiListener(emojiListener: EmojiListener) {
        this.emojiListener = emojiListener
    }

    inner class EmojiAdapter : RecyclerView.Adapter<EmojiAdapter.ViewHolder>() {
        var emojis: ArrayList<String> = PhotoEditor.getEmojis(activity)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_emoji, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.txtEmoji.text = emojis[position]
        }

        override fun getItemCount(): Int {
            return emojis.count()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var txtEmoji: TextView = itemView.findViewById(R.id.text_emoji)

            init {
                itemView.setOnClickListener {
                    emojiListener.onEmojiClick(emojis[layoutPosition])
                    dismiss()
                }
            }
        }
    }
}