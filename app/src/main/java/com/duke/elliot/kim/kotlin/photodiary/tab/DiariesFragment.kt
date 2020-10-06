package com.duke.elliot.kim.kotlin.photodiary.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.base.BaseRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDairiesBinding
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import kotlinx.android.synthetic.main.fragment_dairies.view.*
import kotlinx.android.synthetic.main.item_diary.view.*

class DiariesFragment: Fragment() {

    private lateinit var binding: FragmentDairiesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dairies, container, false)

        (requireActivity() as MainActivity).viewModel.diaries.observe(requireActivity()) { diaries ->
            when((requireActivity() as MainActivity).viewModel.diariesFragmentAction) {
                MainViewModel.Action.UNINITIALIZED -> {
                    view.recycler_view_diary.apply {
                        adapter = DiaryRecyclerViewAdapter(R.layout.item_diary, diaries) /**test layout. */
                        layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
                    }
                    view.recycler_view_diary.adapter?.notifyDataSetChanged()
                    (requireActivity() as MainActivity).viewModel.diariesFragmentAction = MainViewModel.Action.INITIALIZED
                }
                MainViewModel.Action.ADDED -> view.recycler_view_diary.adapter?.notifyItemInserted(0)
            }
        }

        return view
    }

    class DiaryRecyclerViewAdapter(layoutId: Int, diaries: ArrayList<DiaryModel>)
        : BaseRecyclerViewAdapter<DiaryModel>(layoutId, diaries) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val diary = items[position]
            holder.view.text_title.text = diary.title
        }
    }
}