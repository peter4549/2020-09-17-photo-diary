package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDairiesBinding
import com.duke.elliot.kim.kotlin.photodiary.diary.EDIT_MODE
import com.duke.elliot.kim.kotlin.photodiary.tab.TabFragmentDirections
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper

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

        diaryAdapter = DiaryAdapter().apply {
            setViewClickListener {
                findNavController().navigate(TabFragmentDirections
                    .actionTabFragmentToDiaryWritingFragment(getCurrentDiary(), EDIT_MODE))
            }
        }
        binding.recyclerViewDiary.layoutManager = GridLayoutManagerWrapper(requireContext(), 1)
        binding.recyclerViewDiary.adapter = diaryAdapter

        viewModel.diaries.observe(requireActivity()) { diaries ->
            diaryAdapter.submitList(diaries)
        }

        return binding.root
    }
}