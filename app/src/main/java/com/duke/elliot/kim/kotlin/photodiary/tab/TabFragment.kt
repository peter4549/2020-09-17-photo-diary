package com.duke.elliot.kim.kotlin.photodiary.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.DiariesFragment
import com.duke.elliot.kim.kotlin.photodiary.tab.media.PhotosFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab_layout.view.*
import java.lang.IllegalArgumentException

class TabFragment: Fragment() {

    private lateinit var tabIcons: Array<Int>
    private lateinit var tabTexts: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab_layout, container, false)
        tabIcons = arrayOf(R.drawable.ic_sharp_library_books_24, R.drawable.ic_sharp_photo_library_24)
        tabTexts = arrayOf(getString(R.string.diary), getString(R.string.photo))
        initializeTabLayoutViewPager(view.tab_layout, view.view_pager)

        view.fab_write_diary.setOnClickListener {
            view.findNavController().navigate(TabFragmentDirections.actionTabFragmentToDiaryWritingFragment())
        }

        return view
    }

    private fun initializeTabLayoutViewPager(tabLayout: TabLayout, viewPager: ViewPager2) {
        viewPager.adapter = FragmentStateAdapter(requireActivity())
        viewPager.isUserInputEnabled = true

        // TODO tabLayout.tabIconTint

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.tag = position
            tab.icon = ContextCompat.getDrawable(requireContext(), tabIcons[position])
            tab.text = tabTexts[position]
        }.attach()
    }

    class FragmentStateAdapter(fragmentActivity: FragmentActivity):
        androidx.viewpager2.adapter.FragmentStateAdapter(fragmentActivity) {
        private val pageCount = 2

        override fun getItemCount(): Int = pageCount

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DiariesFragment()
                1 -> PhotosFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }
}