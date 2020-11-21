package com.duke.elliot.kim.kotlin.photodiary.tab.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.duke.elliot.kim.kotlin.photodiary.MainViewModel
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.database.DiaryDatabase
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentDairiesBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.CREATE_MODE
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.EDIT_MODE
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.media.media_helper.MediaHelper
import com.duke.elliot.kim.kotlin.photodiary.export.ExportUtilities
import com.duke.elliot.kim.kotlin.photodiary.export.KakaoTalkOptionBottomSheetDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.picker.MediaPickerBottomSheetDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.tab.TabFragment
import com.duke.elliot.kim.kotlin.photodiary.tab.TabFragmentDirections
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import kotlin.Comparator


class DiariesFragment: Fragment(), KakaoTalkOptionBottomSheetDialogFragment.KakaoTalkOptionClickListener,
    MediaPickerBottomSheetDialogFragment.OnMediaClickListener {

    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var binding: FragmentDairiesBinding
    private lateinit var viewModel: DiariesViewModel
    private lateinit var mediaPicker: MediaPickerBottomSheetDialogFragment

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
                    viewModel.delete(it)
                }
            }

            setShareClickListener {
                getCurrentDiary()?.let {
                    ExportUtilities.sendDiary(requireActivity(), it)
                }
            }

            setSendDiaryToFacebookClickListener {
                getCurrentDiary()?.let {
                    ExportUtilities.sendDiaryToFacebook(requireActivity(), it)
                }
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
                    viewModel.update(it)
                }
            }
        }

        binding.recyclerViewDiary.layoutManager = StaggeredGridLayoutManager(
            1,
            StaggeredGridLayoutManager.VERTICAL
        )
        binding.recyclerViewDiary.adapter = diaryAdapter

        viewModel.diaries.observe(requireActivity()) { diaries ->
            diaryAdapter.addHeaderAndSubmitList(diaries, false)

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
        println("KKKKKKK ${diaryAdapter.getCurrentDiary()?.title ?: "널이랑깨."}")
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
}