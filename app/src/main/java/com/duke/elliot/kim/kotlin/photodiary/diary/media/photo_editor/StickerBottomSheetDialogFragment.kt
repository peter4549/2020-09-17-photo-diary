package com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_sticker_bottom_sheet_dialog.view.*

class StickerBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var behavior: BottomSheetBehavior<*>
    private lateinit var stickerListener: StickerListener
    fun setStickerListener(stickerListener: StickerListener) {
        this.stickerListener = stickerListener
    }

    interface StickerListener {
        fun onStickerClick(bitmap: Bitmap?)
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
            View.inflate(context, R.layout.fragment_sticker_bottom_sheet_dialog, null)
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

        contentView.recycler_view_sticker.apply {
            layoutManager = GridLayoutManagerWrapper(context, 3)
            adapter = StickerAdapter()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::behavior.isInitialized)
            behavior.removeBottomSheetCallback(bottomSheetBehaviorCallback)
    }

    inner class StickerAdapter : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {
        var stickerList = intArrayOf(R.drawable.ic_sharp_not_interested_112, R.drawable.ic_sharp_photo_library_24)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imageSticker.setImageResource(stickerList[position])
        }

        override fun getItemCount(): Int {
            return stickerList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imageSticker: ImageView = itemView.findViewById(R.id.image_sticker)

            init {
                itemView.setOnClickListener {
                    if (::stickerListener.isInitialized) {
                        stickerListener.onStickerClick(
                            BitmapFactory.decodeResource(
                                resources,
                                stickerList[layoutPosition]
                            )
                        )
                    }
                    dismiss()
                }
            }
        }
    }

    private fun convertEmoji(emoji: String): String {
        var returnedEmoji = ""
        returnedEmoji = try {
            val convertEmojiToInt = emoji.substring(2).toInt(16)
            getEmojiByUnicode(convertEmojiToInt)
        } catch (e: NumberFormatException) {
            ""
        }
        return returnedEmoji
    }

    private fun getEmojiByUnicode(unicode: Int): String {
        return String(Character.toChars(unicode))
    }
}