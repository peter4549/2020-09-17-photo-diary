package com.duke.elliot.kim.kotlin.photodiary.tab

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.base.BaseFragment
import com.duke.elliot.kim.kotlin.photodiary.calendar.CalendarFragment
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.database.FolderDao
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentTabDrawerBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.CREATE_MODE
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DATE_OTHER_THAN_TODAY
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.lock_screen.SetLockScreenActivity
import com.duke.elliot.kim.kotlin.photodiary.folder.DEFAULT_FOLDER_ID
import com.duke.elliot.kim.kotlin.photodiary.folder.EditFolderDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderAdapter
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.DiariesFragment
import com.duke.elliot.kim.kotlin.photodiary.tab.media.PhotosFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_tab_layout.view.*
import kotlinx.coroutines.*


class TabFragment: BaseFragment() {

    // private lateinit var tabIcons: Array<Int>
    private lateinit var tabTexts: Array<String>
    private lateinit var binding: FragmentTabDrawerBinding
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var folderDao: FolderDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tab_drawer, container, false)
        // tabIcons = arrayOf(R.drawable.ic_sharp_library_books_24, R.drawable.ic_sharp_photo_library_24)
        tabTexts = arrayOf(
            getString(R.string.diary),
            getString(R.string.calendar),
            getString(R.string.media)
        )

        initializeNavigationDrawer()
        initializeTabLayoutViewPager(binding.tabFragment.tab_layout, binding.tabFragment.view_pager)

        binding.tabFragment.fab_write_diary.setOnClickListener {
            when(diaryWritingMode) {
                CREATE_MODE -> binding.tabFragment.findNavController().navigate(
                    TabFragmentDirections.actionTabFragmentToDiaryWritingFragment(
                        null,
                        diaryWritingMode
                    )
                )
                DATE_OTHER_THAN_TODAY -> {
                    calendarFragmentOnFabClick?.invoke()
                }
            }
        }

        /** onBackPressed */
        val onBackPressedCallback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                        binding.drawerLayout.closeDrawer(GravityCompat.START, true)
                    else
                        requireActivity().finish()
                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        // TODO: 안쓸듯..
        binding.tabFragment.alarm_test_btn.setOnClickListener {
            /*
            // TODO: test. 잘되면 존나 개꿀. 관건은 다이나믹하게 가능한가.. MAINACTIVITY에도 있음.
            // 설계할것..
            TypefaceUtil.overrideFont(
                requireContext(),
                "SERIF",
                "fonts/nanum_square_round_regular.otf"
            )
            //requireActivity().window?.decorView?.findViewById<View>(android.R.id.content)?.invalidate()
            //binding.invalidateAll()
            //binding.tabFragment.invalidate() // 리사이클러뷰를 노티해줘야함.
            */

        }

        /** Favorites */
        binding.showFavorites.setOnClickListener {
            (requireActivity() as MainActivity).setFolderId(SHOW_FAVORITES)
        }

        binding.textChangeTheme.setOnClickListener {
            findNavController().navigate(
                TabFragmentDirections.actionTabFragmentToChangeThemeFragment()
            )
        }

        binding.textSetLockScreen.setOnClickListener {
            val intent = Intent(requireActivity(), SetLockScreenActivity::class.java)
            MainViewModel.lockScreenException = true
            startActivity(intent)
        }

        binding.textDataBackup.setOnClickListener {
            findNavController().navigate(
                TabFragmentDirections.actionTabFragmentToBackupFragment()
            )
        }

        binding.textReminder.setOnClickListener {
            findNavController().navigate(
                TabFragmentDirections.actionTabFragmentToReminderFragment()
            )
        }

        /** Folders */
        var foldersInitialized = false

        CoroutineScope(Dispatchers.Default).launch {
            folderDao = DiaryDatabase.getInstance(requireContext()).folderDao()

            withContext(Dispatchers.Main) {
                folderDao.getAll().observe(viewLifecycleOwner) { folders ->
                    if (!foldersInitialized) {
                        val mainActivity = requireActivity() as MainActivity
                        folderAdapter =
                            FolderAdapter(mainActivity.getFolderDao(), mainActivity.getDiaryDao()) {
                                (requireActivity() as MainActivity).setFolderId(it.id)
                            }.apply {
                                setOnEditFolderClickListener {
                                    val editFolderDialog = EditFolderDialogFragment().apply {
                                        setMode(EditFolderDialogFragment.EDIT_MODE)
                                        setFolderDao(folderDao)
                                        setFolder(it)
                                        setCallbackAfterEditing {
                                            val diariesFragmentHandler = (requireActivity() as MainActivity).diariesFragmentHandler
                                            if (diariesFragmentHandler != null) {
                                                val message = diariesFragmentHandler.obtainMessage()
                                                message.what = DIARIES_FRAGMENT_HANDLER_FOLDER_CHANGED_MESSAGE
                                                diariesFragmentHandler.sendMessage(message)
                                            }
                                        }
                                    }

                                    editFolderDialog.show(requireActivity().supportFragmentManager, editFolderDialog.tag)
                                }
                            }

                        binding.folderRecyclerView.layoutManager =
                            GridLayoutManagerWrapper(requireContext(), 1)
                        binding.folderRecyclerView.adapter = folderAdapter

                        foldersInitialized = true
                    }

                    folderAdapter.submitList(folders)
                    folderAdapter.notifyDataSetChanged()
                }
            }
        }

        binding.showFolders.rotate(180F, 0)
        binding.folderHeaderContainer.setOnClickListener {
            val isVisible = binding.folderRecyclerView.visibility == View.VISIBLE
            val itemCount = folderAdapter.itemCount

            if (isVisible) {
                if (itemCount == 0) {
                    binding.foldersEmptyMessage.crossFadeOut(200)
                    binding.folderRecyclerView.visibility = View.GONE
                } else {
                    binding.foldersEmptyMessage.visibility = View.GONE
                    binding.folderRecyclerView.crossFadeOut(200)
                }

                binding.showFolders.rotate(180F, 200)
                binding.showAll.crossFadeOut(200)
            } else {
                if (itemCount == 0) {
                    binding.foldersEmptyMessage.crossFadeIn(200)
                    binding.folderRecyclerView.visibility = View.GONE
                    binding.showFolders.rotate(180F, 200)
                } else {
                    binding.foldersEmptyMessage.visibility = View.GONE
                    binding.folderRecyclerView.crossFadeIn(200)
                    binding.showFolders.rotate(0F, 200)
                }

                binding.showAll.crossFadeIn(200)
            }
        }

        binding.addFolder.setOnClickListener {
            val editFolderDialog = EditFolderDialogFragment().apply {
                setMode(EditFolderDialogFragment.ADD_MODE)
                setFolderDao(folderDao)
            }

            editFolderDialog.show(requireActivity().supportFragmentManager, editFolderDialog.tag)
        }

        binding.showAllContainer.setOnClickListener {
            (requireActivity() as MainActivity).setFolderId(DEFAULT_FOLDER_ID)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        applyPrimaryThemeColor(binding.tabFragment.toolbar)
        binding.tabFragment.fab_write_diary.setBackgroundColor(MainActivity.themeColorPrimary)
    }

    private fun initializeNavigationDrawer() {
        (requireActivity() as MainActivity).setSupportActionBar(binding.tabFragment.toolbar)

        val toggle = ActionBarDrawerToggle(
            requireActivity(), binding.drawerLayout, binding.tabFragment.toolbar,
            R.string.ok, // TODO change string.
            R.string.cancel
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()
    }

    private fun initializeTabLayoutViewPager(tabLayout: TabLayout, viewPager: ViewPager2) {
        viewPager.adapter = FragmentStateAdapter(requireActivity())
        viewPager.isUserInputEnabled = true

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.tag = position
            // tab.icon = ContextCompat.getDrawable(requireContext(), tabIcons[position])
            tab.text = tabTexts[position]
        }.attach()
    }

    class FragmentStateAdapter(fragmentActivity: FragmentActivity):
        androidx.viewpager2.adapter.FragmentStateAdapter(fragmentActivity) {
        private val pageCount = 3

        override fun getItemCount(): Int = pageCount

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DiariesFragment()
                1 -> CalendarFragment()
                2 -> PhotosFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }

    companion object {
        var diaryWritingMode = CREATE_MODE
        var calendarFragmentOnFabClick: (() -> Unit)? = null
    }
}