package com.duke.elliot.kim.kotlin.photodiary.tab.diary.diary_view

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDiaryViewPagerBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingFragmentDirections
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.EDIT_MODE
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast


class DiaryViewPagerFragment: Fragment() {

    private lateinit var binding: FragmentDiaryViewPagerBinding
    private lateinit var viewModel: DiaryViewPagerViewModel
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_diary_view_pager,
            container,
            false
        )

        val diaryViewPagerFragmentArgs by navArgs<DiaryViewPagerFragmentArgs>()

        val database = DiaryDatabase.getInstance(requireContext()).dao()

        val viewModelFactory = DiaryViewPagerViewModelFactory(database)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiaryViewPagerViewModel::class.java]
        viewModel.initialDiary = diaryViewPagerFragmentArgs.selectedDiary

        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        viewModel.initialized = false
        viewModel.diaries.observe(viewLifecycleOwner) { diaries ->
            if (!viewModel.initialized) {
                viewPagerAdapter = ViewPagerAdapter(requireActivity(), diaries as ArrayList<DiaryModel>)
                binding.viewPager.apply {
                    adapter = viewPagerAdapter
                }

                binding.viewPager.setCurrentItem(viewModel.getInitialDiaryPosition(), false)
                viewModel.initialized = true
            } else {
                println("HOHOHO : ${diaries.map { it.title }}")
                viewPagerAdapter.removeFragment(0) // TODO change!
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        (requireActivity() as MainActivity).menuInflater.inflate(
            R.menu.diary_view_pager, menu
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
            R.id.edit -> navigateToDiaryWritingFragment()
            R.id.set_category -> {
            }
            R.id.export -> {
            }
            R.id.delete -> viewModel.getItem(binding.viewPager.currentItem)?.let {
                (requireActivity() as MainActivity).viewModel
                    .delete(it)
            } ?: run {
                showToast(requireContext(), getString(R.string.diary_not_found))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun navigateToDiaryWritingFragment() {
        val diary = viewModel.getItem(binding.viewPager.currentItem)
        diary?.let {
            findNavController().navigate(DiaryViewPagerFragmentDirections
                .actionDiaryViewPagerFragmentToDiaryWritingFragment(it, EDIT_MODE))
        } ?: run {
            showToast(requireContext(), getString(R.string.diary_not_found))
        }
    }

    class ViewPagerAdapter(
        fragmentActivity: FragmentActivity,
        private val diaries: ArrayList<DiaryModel>
    ) : FragmentStateAdapter(fragmentActivity) {
        private val pageIds= diaries.map { it.hashCode().toLong() }

        override fun getItemCount(): Int {
            return diaries.count()
        }

        fun removeFragment(position: Int) {
            diaries.removeAt(position)
            notifyItemRangeChanged(position, diaries.size)
            notifyDataSetChanged()
        }

        override fun createFragment(position: Int): Fragment {
            val diaryViewFragment = DiaryViewFragment()
            diaryViewFragment.setDiary(diaries[position])
            return diaryViewFragment
        }

        override fun getItemId(position: Int): Long {
            return diaries[position].hashCode().toLong()
        }

        override fun containsItem(itemId: Long): Boolean {
            return pageIds.contains(itemId)
        }
    }
}
