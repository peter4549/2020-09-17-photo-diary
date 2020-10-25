package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDairiesBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.EDIT_MODE
import com.duke.elliot.kim.kotlin.photodiary.tab.TabFragmentDirections
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import kotlinx.coroutines.*
import timber.log.Timber

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
            setViewOnClickListener {
                getCurrentDiary()?.let { diary ->
                    viewModel.diaries.value?.let { diaries ->
                        findNavController().navigate(TabFragmentDirections
                            .actionTabFragmentToDiaryViewPagerFragment(diaries.toTypedArray(), diary))
                    }
                } ?: run {
                    Timber.e("Diary not found.")
                    showToast(requireContext(), getString(R.string.diary_not_found))
                }
            }

            setEditOnClickListener {
                getCurrentDiary()?.let {
                    findNavController().navigate(TabFragmentDirections
                        .actionTabFragmentToDiaryWritingFragment(it, EDIT_MODE))
                }
            }

            setDeleteOnClickListener {
                getCurrentDiary()?.let {
                    viewModel.delete(it)
                }
            }

            setUpdateListener {
                getCurrentDiary()?.let {
                    (binding.recyclerViewDiary.itemAnimator as SimpleItemAnimator)
                        .supportsChangeAnimations = false
                    viewModel.update(it)
                }
            }
        }

        binding.recyclerViewDiary.layoutManager = GridLayoutManagerWrapper(requireContext(), 1)

        binding.recyclerViewDiary.adapter = diaryAdapter

        viewModel.diaries.observe(requireActivity()) { diaries ->
            diaryAdapter.submitList(diaries)
            CoroutineScope(Dispatchers.Main).launch {
                delay(200L)
                (binding.recyclerViewDiary.itemAnimator as SimpleItemAnimator)
                    .supportsChangeAnimations = true
            }


            if (viewModel.status == DiariesViewModel.UNINITIALIZED) {
                binding.recyclerViewDiary.scrollToPosition(0)
                viewModel.status = DiariesViewModel.INITIALIZED
            }

            if (MainViewModel.inserted) {
                CoroutineScope(Dispatchers.Default).launch {
                    delay(200L)
                    withContext(Dispatchers.Main) {
                        binding.recyclerViewDiary.smoothScrollToPosition(0)
                    }
                }

                MainViewModel.inserted = false
            }
        }

        return binding.root
    }
}