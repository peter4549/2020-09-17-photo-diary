package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.base.BaseRecyclerViewAdapter
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDairiesBinding
import com.duke.elliot.kim.kotlin.photodiary.diary.DiaryModel
import kotlinx.android.synthetic.main.item_diary.view.*

class DiariesFragment: Fragment() {

    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var binding: FragmentDairiesBinding
    private lateinit var viewModel: DiariesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dairies, container, false)

        val application = requireActivity().application
        val dataSource = DiaryDatabase.getInstance(requireContext()).dao()
        val viewModelFactory = DiariesViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiariesViewModel::class.java]

        binding.diariesViewModel = viewModel

        diaryAdapter = DiaryAdapter()
        binding.recyclerViewDiary.adapter = diaryAdapter

        viewModel.diaries.observe(requireActivity()) { diaries ->
            diaryAdapter.submitList(diaries)
        }

        return binding.root
    }

    class DiaryRecyclerViewAdapter(layoutId: Int, diaries: ArrayList<DiaryModel>)
        : BaseRecyclerViewAdapter<DiaryModel>(layoutId, diaries) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val diary = items[position]
            holder.view.text_title.text = diary.title
        }
    }
}