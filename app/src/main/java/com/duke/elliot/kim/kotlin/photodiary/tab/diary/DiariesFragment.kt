package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.*
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDairiesBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.CREATE_MODE
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.EDIT_MODE
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.export.ExportUtilities
import com.duke.elliot.kim.kotlin.photodiary.export.FacebookOptionBottomSheetDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.export.KakaoTalkOptionBottomSheetDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.folder.DEFAULT_FOLDER_ID
import com.duke.elliot.kim.kotlin.photodiary.google_map.PLACE_SELECTED
import com.duke.elliot.kim.kotlin.photodiary.hashtag.HASHTAG_SELECTED
import com.duke.elliot.kim.kotlin.photodiary.picker.MediaPickerBottomSheetDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.picker.TypelessMediaPickerBottomSheetDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.tab.TabFragment
import com.duke.elliot.kim.kotlin.photodiary.tab.TabFragmentDirections
import com.duke.elliot.kim.kotlin.photodiary.utility.leftDrawable
import com.duke.elliot.kim.kotlin.photodiary.utility.setDrawableTint
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import kotlinx.coroutines.*
import timber.log.Timber

class DiariesFragment: Fragment(), KakaoTalkOptionBottomSheetDialogFragment.KakaoTalkOptionClickListener,
    MediaPickerBottomSheetDialogFragment.OnMediaClickListener,
    FacebookOptionBottomSheetDialogFragment.OnFacebookOptionClickListener,
    TypelessMediaPickerBottomSheetDialogFragment.OnMediaClickListener {

    private lateinit var binding: FragmentDairiesBinding
    private lateinit var viewModel: DiariesViewModel

    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var mediaPicker: MediaPickerBottomSheetDialogFragment
    private lateinit var typelessMediaPicker: TypelessMediaPickerBottomSheetDialogFragment

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dairies, container, false)

        val application = requireActivity().application
        val dataSource = DiaryDatabase.getInstance(requireContext()).diaryDao()
        val viewModelFactory = DiariesViewModelFactory(dataSource, application)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[DiariesViewModel::class.java]

        binding.diariesViewModel = viewModel

        /** Handler */
        (requireActivity() as MainActivity).diariesFragmentHandler = Handler(Looper.getMainLooper()) {
            when(it.what) {
                DIARIES_FRAGMENT_HANDLER_COLOR_CHANGED_MESSAGE -> {
                    if (diaryAdapter.currentBackgroundColor != MainActivity.themeColorSecondary) {
                        diaryAdapter.currentBackgroundColor = MainActivity.themeColorSecondary
                        binding.recyclerViewDiary.adapter = diaryAdapter
                    }
                }
                DIARIES_FRAGMENT_HANDLER_FOLDER_CHANGED_MESSAGE -> {
                    diaryAdapter.notifyDataSetChanged()
                }
            }
            true
        }

        /** Folder */
        (requireActivity() as MainActivity).getSelectedFolderId().observe(viewLifecycleOwner) { folderId ->
            viewModel.folderId = folderId
            val filteredDiaries = mutableListOf<DiaryModel>()

            if (viewModel.folderId == SHOW_FAVORITES) {
                /** Favorites */
                viewModel.originalDiaries?.let { diaries ->
                    for (diary in diaries) {
                        if (diary.liked)
                            filteredDiaries.add(diary)
                    }

                    diaryAdapter.addHeaderAndSubmitList(filteredDiaries)
                }

                binding.currentFolder.text = getString(R.string.favorites)
                binding.currentFolder.leftDrawable(R.drawable.ic_round_star_liked_24, R.dimen.dimen_24dp)
                binding.currentFolder.setDrawableTint(MainActivity.themeColorSecondary)
                binding.currentFolder.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_text_dark))
            } else if (viewModel.folderId == HASHTAG_SELECTED) {
                /** Hash Tags */
                val hashTag = (requireActivity() as MainActivity).viewModel.selectedHashTag
                viewModel.originalDiaries?.let { diaries ->
                    for (diary in diaries) {
                        if (diary.hashTags.contains(hashTag))
                            filteredDiaries.add(diary)
                    }

                    diaryAdapter.addHeaderAndSubmitList(filteredDiaries)
                }

                binding.currentFolder.text = hashTag
                binding.currentFolder.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorTextAccent))
                binding.currentFolder.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else if (viewModel.folderId == PLACE_SELECTED) {
                /** Place */
                val place = (requireActivity() as MainActivity).viewModel.selectedPlace
                viewModel.originalDiaries?.let { diaries ->
                    for (diary in diaries) {
                        if (diary.place == place) {
                            filteredDiaries.add(diary)
                        }
                    }

                    diaryAdapter.addHeaderAndSubmitList(filteredDiaries)
                }

                binding.currentFolder.text = place?.name
                binding.currentFolder.leftDrawable(R.drawable.ic_round_location_on_24, R.dimen.dimen_24dp)
                binding.currentFolder.setDrawableTint(MainActivity.themeColorSecondary)
                binding.currentFolder.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_text_dark))
            }
            else {
                /** Folder */
                if (viewModel.folderId != DEFAULT_FOLDER_ID) {
                    viewModel.originalDiaries?.let { diaries ->
                        for (diary in diaries) {
                            if (diary.folderId == viewModel.folderId)
                                filteredDiaries.add(diary)
                        }

                        diaryAdapter.addHeaderAndSubmitList(filteredDiaries)
                    }

                    coroutineScope.launch {
                        var name: String? = null
                        launch (Dispatchers.Default) {
                            name = viewModel.folderDao.getFolderById(viewModel.folderId)?.name
                        }.join()

                        launch {
                            name?.let { binding.currentFolder.text = name }
                            binding.currentFolder.leftDrawable(R.drawable.ic_round_folder_24, R.dimen.dimen_24dp)
                            binding.currentFolder.setDrawableTint(MainActivity.themeColorSecondary)
                            binding.currentFolder.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_text_dark))
                        }
                    }
                } else {
                    // Show all
                    viewModel.originalDiaries?.let {
                        diaryAdapter.addHeaderAndSubmitList(it, false)
                    }

                    binding.currentFolder.text = getString(R.string.show_all)
                    binding.currentFolder.leftDrawable(R.drawable.ic_round_folder_open_24, R.dimen.dimen_24dp)
                    binding.currentFolder.setDrawableTint(MainActivity.themeColorSecondary)
                    binding.currentFolder.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_text_dark))
                }
            }
        }

        mediaPicker = MediaPickerBottomSheetDialogFragment().apply {
            setMediaClickListener(this@DiariesFragment)
        }

        diaryAdapter = DiaryAdapter(requireContext()).apply {
            setViewOnClickListener {
                getCurrentDiary()?.let { diary ->
                    findNavController().navigate(
                        TabFragmentDirections
                            .actionTabFragmentToDiaryViewPagerFragment(
                                diary,
                                sortingCriteria
                            )
                    )
                } ?: run {
                    Timber.e("Diary not found.")
                    showToast(requireContext(), getString(R.string.diary_not_found))
                }
            }

            setConvertPdfClickListener {
                getCurrentDiary()?.let {
                    findNavController().navigate(
                        TabFragmentDirections
                            .actionTabFragmentToPdfPreviewFragment(it)
                    )
                }
            }

            setEditOnClickListener {
                getCurrentDiary()?.let {
                    findNavController().navigate(
                        TabFragmentDirections
                            .actionTabFragmentToDiaryWritingFragment(it, EDIT_MODE)
                    )
                }
            }

            setDeleteOnClickListener {
                getCurrentDiary()?.let {
                    (requireActivity() as MainActivity).viewModel.delete(it)
                }
            }

            setShareClickListener {
                getCurrentDiary()?.let {
                    ExportUtilities.sendDiary(requireActivity(), it)
                }
            }

            setSendDiaryToFacebookClickListener {
                val bottomSheetDialogFragment = FacebookOptionBottomSheetDialogFragment().apply {
                    setOnFacebookOptionClickListener(this@DiariesFragment)
                }

                bottomSheetDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    bottomSheetDialogFragment.tag
                )
            }

            setSendDiaryToKakaoTalkClickListener {
                val bottomSheetDialogFragment =
                    KakaoTalkOptionBottomSheetDialogFragment().apply {
                        setKakaoTalkOptionClickListener(this@DiariesFragment)
                    }

                bottomSheetDialogFragment.show(
                    requireActivity().supportFragmentManager,
                    bottomSheetDialogFragment.tag
                )
            }

            setUpdateListener {
                getCurrentDiary()?.let {
                    (binding.recyclerViewDiary.itemAnimator as SimpleItemAnimator)
                        .supportsChangeAnimations = false
                    (requireActivity() as MainActivity).updateDiary(it, null)
                }
            }
        }

        binding.recyclerViewDiary.layoutManager = StaggeredGridLayoutManager(
            1,
            StaggeredGridLayoutManager.VERTICAL
        )
        binding.recyclerViewDiary.adapter = diaryAdapter

        /**  Diaries */
        viewModel.diaries.observe(requireActivity()) { diaries ->
            val filteredDiaries = mutableListOf<DiaryModel>()

            if (viewModel.folderId != DEFAULT_FOLDER_ID) {
                /** Hashtag selected */
                if (viewModel.folderId == HASHTAG_SELECTED) {
                    diaries?.let { it ->
                        for (diary in it) {
                            if (diary.hashTags.contains((requireActivity() as MainActivity)
                                    .viewModel.selectedHashTag))
                                filteredDiaries.add(diary)
                        }
                    }
                } else if (viewModel.folderId == PLACE_SELECTED) {
                    /** Place */
                    diaries?.let { it ->
                        for (diary in it) {
                            if (diary.place == (requireActivity() as MainActivity).viewModel.selectedPlace)
                                filteredDiaries.add(diary)
                        }

                    }
                }
                else {
                    /** Folder selected */
                    diaries?.let { it ->
                        for (diary in it) {
                            if (diary.folderId == viewModel.folderId)
                                filteredDiaries.add(diary)
                        }
                    }
                }

                diaryAdapter.addHeaderAndSubmitList(filteredDiaries, false)
            } else
                diaryAdapter.addHeaderAndSubmitList(diaries, false)

            viewModel.originalDiaries = diaries.toMutableList()

            if (viewModel.status == DiariesViewModel.UNINITIALIZED) {
                binding.recyclerViewDiary.scrollToPosition(0)
                viewModel.status = DiariesViewModel.INITIALIZED
            }

            if (MainViewModel.inserted) {
                CoroutineScope(Dispatchers.Default).launch {
                    delay(200L)
                    withContext(Dispatchers.Main) {
                        binding.recyclerViewDiary.scrollToPosition(0)
                    }
                }

                MainViewModel.inserted = false
            }

            if (MainViewModel.updated) {
                diaryAdapter.notifyItemChanged(MainViewModel.selectedDiaryPosition)
                MainViewModel.updated = false
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        TabFragment.diaryWritingMode = CREATE_MODE
    }

    override fun onStop() {
        super.onStop()
        diaryAdapter.saveSortingCriteriaViewMode()
    }

    /** Kakao Talk */
    override fun onSendImagesClick() {
        diaryAdapter.getCurrentDiary()?.let {
            mediaPicker.setDiary(it)
            mediaPicker.setMediaType(MediaHelper.MediaType.PHOTO)
            mediaPicker.show(requireActivity().supportFragmentManager, mediaPicker.tag)
        }
    }

    override fun onSendVideoClick() {
        diaryAdapter.getCurrentDiary()?.let {
            mediaPicker.setDiary(it)
            mediaPicker.setMediaType(MediaHelper.MediaType.VIDEO)
            mediaPicker.show(requireActivity().supportFragmentManager, mediaPicker.tag)
        }
    }

    override fun onSendAudioClick() {
        diaryAdapter.getCurrentDiary()?.let {
            mediaPicker.setDiary(it)
            mediaPicker.setMediaType(MediaHelper.MediaType.AUDIO)
            mediaPicker.show(requireActivity().supportFragmentManager, mediaPicker.tag)
        }
    }

    override fun onSendTextClick() {
        diaryAdapter.getCurrentDiary()?.let {
            ExportUtilities.sendDiaryToKakaoTalk(
                requireActivity(),
                it, ExportUtilities.KAKAO_TALK_OPTION_SEND_TEXT
            )
        }
    }

    // Media Picker Interfaces
    /** 미디어 데이터를 선택받아 보내는 역할을 함. 인자들을 보낼 것이다. */
    override fun photoOnClick(pickedPhotoUris: List<String>) {
        diaryAdapter.getCurrentDiary()?.let {
            ExportUtilities.sendDiaryToKakaoTalk(
                requireActivity(),
                it, ExportUtilities.KAKAO_TALK_OPTION_SEND_IMAGES, pickedPhotoUris
            )
        }
    }

    override fun videoOnClick(pickedVideoUri: String) {
        diaryAdapter.getCurrentDiary()?.let {
            ExportUtilities.sendDiaryToKakaoTalk(
                requireActivity(),
                it, ExportUtilities.KAKAO_TALK_OPTION_SEND_VIDEO, mediaUri = pickedVideoUri
            )
        }
    }

    override fun audioOnClick(pickedAudioUri: String) {
        diaryAdapter.getCurrentDiary()?.let {
            ExportUtilities.sendDiaryToKakaoTalk(
                requireActivity(),
                it, ExportUtilities.KAKAO_TALK_OPTION_SEND_AUDIO, mediaUri = pickedAudioUri
            )
        }
    }

    /** Facebook */
    override fun onSendMediaClick() {
        diaryAdapter.getCurrentDiary()?.let {
            typelessMediaPicker = TypelessMediaPickerBottomSheetDialogFragment().apply {
                setMediaClickListener(this@DiariesFragment)
            }

            typelessMediaPicker.setDiary(it)
            typelessMediaPicker.show(requireActivity().supportFragmentManager, typelessMediaPicker.tag)
        }
    }

    override fun onSendOnlyTextClick() {
        diaryAdapter.getCurrentDiary()?.let {
            ExportUtilities.sendTextToFacebook(
                requireActivity(),
                it
            )
        }
    }

    // Typeless Media Picker
    override fun onClick(diary: DiaryModel, pickedMediaUris: List<Pair<Int, Uri>>) {
        ExportUtilities.sendDiaryToFacebook(requireActivity(), diary, pickedMediaUris)
    }
}