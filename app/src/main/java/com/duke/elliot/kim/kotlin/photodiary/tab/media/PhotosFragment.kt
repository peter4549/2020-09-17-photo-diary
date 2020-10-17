package com.duke.elliot.kim.kotlin.photodiary.tab.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.MainViewModel
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.base.BaseRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel
import kotlinx.android.synthetic.main.fragment_photos.view.*
import kotlinx.android.synthetic.main.item_diary.view.*

class PhotosFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photos, container, false)

        (requireActivity() as MainActivity).viewModel.diaries.observe(requireActivity()) { photos ->
            when((requireActivity() as MainActivity).viewModel.photosFragmentAction) {
                MainViewModel.Action.UNINITIALIZED -> {
                    view.recycler_view_photo.apply {
                        adapter = PhotoRecyclerViewAdapter(R.layout.item_diary, photos)  /**test layout. */
                        layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
                    }
                    view.recycler_view_photo.adapter?.notifyDataSetChanged()
                    (requireActivity() as MainActivity).viewModel.photosFragmentAction = MainViewModel.Action.INITIALIZED
                }
                MainViewModel.Action.ADDED -> view.recycler_view_photo.adapter?.notifyItemInserted(0)
            }
        }

        return view
    }

    class PhotoRecyclerViewAdapter(layoutId: Int, photos: ArrayList<DiaryModel>)
        : BaseRecyclerViewAdapter<DiaryModel>(layoutId, photos) {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val photo = items[position]  // TODO image만 필터링, 여기서 또는 밖에서.
            holder.view.text_title.text = photo.title
        }
    }
}