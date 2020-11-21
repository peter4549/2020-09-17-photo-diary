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
import androidx.viewpager2.widget.ViewPager2
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDiaryViewPagerBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.EDIT_MODE
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.SORT_BY_LATEST
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.SORT_BY_OLDEST
import com.duke.elliot.kim.kotlin.photodiary.utility.FileUtilities
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

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
        val database = DiaryDatabase.getInstance(requireContext()).diaryDao()
        val viewModelFactory = DiaryViewPagerViewModelFactory(database, FileUtilities.getInstance(requireActivity().application))
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiaryViewPagerViewModel::class.java]

        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        val sortingCriteria = diaryViewPagerFragmentArgs.sortingCriteria

        viewModel.diaries.observe(viewLifecycleOwner) { diaries ->
            Collections.sort(diaries,
                Comparator { o1: DiaryModel, o2: DiaryModel ->
                    when (sortingCriteria) {
                        SORT_BY_LATEST -> {
                            return@Comparator (o1.time - o2.time).toInt()
                        }
                        SORT_BY_OLDEST -> {
                            return@Comparator (o2.time - o1.time).toInt()
                        }
                        else -> 0
                    }
                }
            )

            viewPagerAdapter =
                ViewPagerAdapter(requireActivity(), diaries as ArrayList<DiaryModel>)
            binding.viewPager.apply {
                adapter = viewPagerAdapter
            }

            if (!viewModel.initialized) {
                viewModel.currentDiary = diaryViewPagerFragmentArgs.selectedDiary
                viewModel.initialized = true
            }

            if (viewModel.status == DiaryViewPagerViewModel.DELETED) {
                if (viewModel.deletedDiaryPosition >= diaries.size)
                    viewModel.deletedDiaryPosition -= 1

                binding.viewPager.setCurrentItem(viewModel.deletedDiaryPosition, false)
                viewModel.status = DiaryViewPagerViewModel.DEFAULT
            } else
                binding.viewPager.setCurrentItem(viewModel.getCurrentDiaryPosition(), false)
        }

        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val page = "${binding.viewPager.currentItem + 1}/${viewModel.diaries.value?.count() ?: 0}"
                binding.textPage.text = page
            }
        })

        binding.imageBackArrow.setOnClickListener {
            val previousPosition = binding.viewPager.currentItem - 1
            if (previousPosition >= 0) {
                binding.viewPager.setCurrentItem(previousPosition, true)
            }
        }

        binding.imageForwardArrow.setOnClickListener {
            val nextPosition = binding.viewPager.currentItem + 1
            if (nextPosition < viewModel.diaries.value?.count() ?: 0)
                binding.viewPager.setCurrentItem(nextPosition, true)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        (requireActivity() as MainActivity).menuInflater.inflate(
            R.menu.diary_options, menu
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
            R.id.edit -> navigateToDiaryWritingFragment()
            R.id.set_category -> {
            }
            R.id.export -> {
                // PdfUtilities.viewToPdf(binding.viewPager, binding.root.context) // TODO test,, 미리보기 페이지 이동 같은걸로 변경. scroll text view
            }
            R.id.delete -> viewModel.getItem(binding.viewPager.currentItem)?.let {
                viewModel.delete(it)
            } ?: run {
                showToast(requireContext(), getString(R.string.diary_not_found))
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun navigateToDiaryWritingFragment() {
        val diary = viewModel.getItem(binding.viewPager.currentItem)
        diary?.let {
            viewModel.currentDiary = diary
            // viewModel.status = DiaryViewPagerViewModel.UPDATED
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

        /*
        fun removeFragment(position: Int) {
            diaries.removeAt(position)
            notifyItemRangeChanged(position, diaries.size)
            notifyDataSetChanged()
        }

        fun updateFragment(position: Int) {
            notifyItemRangeChanged(position, diaries.size)
            notifyDataSetChanged()
        }
         */

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
