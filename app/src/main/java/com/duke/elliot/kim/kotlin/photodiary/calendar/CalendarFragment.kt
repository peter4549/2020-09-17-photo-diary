package com.duke.elliot.kim.kotlin.photodiary.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.CalendarDayBinding
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentCalendarBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.CREATE_MODE
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DATE_OTHER_THAN_TODAY
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.EDIT_MODE
import com.duke.elliot.kim.kotlin.photodiary.tab.TabFragment
import com.duke.elliot.kim.kotlin.photodiary.tab.TabFragmentDirections
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.BRIEF_VIEW_MODE
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.DiaryAdapter
import com.duke.elliot.kim.kotlin.photodiary.tab.diary.SORT_BY_OLDEST
import com.duke.elliot.kim.kotlin.photodiary.utility.ColorUtilities
import com.duke.elliot.kim.kotlin.photodiary.utility.GridLayoutManagerWrapper
import com.duke.elliot.kim.kotlin.photodiary.utility.OkCancelDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.yearMonth
import timber.log.Timber
import java.sql.Date
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

const val REGISTER_ANNIVERSARY = 0
const val UPDATE_ANNIVERSARY = 1

class CalendarFragment : Fragment(), RegisterAnniversaryDialogFragment.OnButtonClickListener {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var viewModel: CalendarViewModel
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")
    private var anniversaryFlag = REGISTER_ANNIVERSARY
    private var selectedDate: LocalDate? = null
    private var selectedAnniversary: AnniversaryModel? = null

    @ColorInt
    private var colorCalendarBackground = MainActivity.themeColorDark

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calendar, container, false)

        val viewModelFactory = CalendarViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[CalendarViewModel::class.java]

        viewModel.anniversaryList.observe(viewLifecycleOwner, { anniversaryList ->
            when (viewModel.status) {
                CalendarViewModel.UNINITIALIZED -> {
                    for (anniversary in anniversaryList) {
                        binding.calendarView.notifyDateChanged(anniversary.getLocalDate())
                    }

                    viewModel.status = CalendarViewModel.INITIALIZED
                }
                CalendarViewModel.DELETED -> {
                    binding.anniversaryContainer.visibility = View.GONE
                }
            }
        })

        var oldDiaries: List<DiaryModel>? = null
        viewModel.diaries.observe(viewLifecycleOwner, { diaries ->
            if (viewModel.hashMapStatus == CalendarViewModel.HASH_MAP_UNINITIALIZED) {
                oldDiaries = diaries.map { it.copy() }
                viewModel.createDateDiaryHashMap(diaries) {
                    binding.calendarView.notifyDateChanged(it)
                }

                viewModel.hashMapStatus = CalendarViewModel.HASH_MAP_INITIALIZED
            } else if (viewModel.hashMapStatus == CalendarViewModel.HASH_MAP_INITIALIZED) {
                oldDiaries?.let { oldDiaries ->
                    val newDiaries = diaries.filterNot { oldDiaries.contains(it) }

                    if (newDiaries.isNotEmpty()) {
                        val newDiary = newDiaries[0]
                        viewModel.putDiaryToDataDiaryHashMap(newDiary)
                        binding.calendarView.notifyDateChanged(newDiary.getLocalDate())
                    }
                }
                showSelectedDateDiaries(diaries)
            }
        })

        diaryAdapter = DiaryAdapter(requireContext(), true).apply {
            setViewOnClickListener {
                getCurrentDiary()?.let { diary ->
                    findNavController().navigate(
                        TabFragmentDirections
                            .actionTabFragmentToDiaryViewPagerFragment(
                                diary,
                                SORT_BY_OLDEST,
                                diary.getLocalDate()
                            )
                    )
                } ?: run {
                    Timber.e("Diary not found.")
                    showToast(requireContext(), getString(R.string.diary_not_found))
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
        }
        diaryAdapter.viewMode = BRIEF_VIEW_MODE
        binding.diaryRecyclerView.layoutManager =
            GridLayoutManagerWrapper(requireContext(), 1)
        binding.diaryRecyclerView.adapter = diaryAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val daysOfWeek = daysOfWeekFromLocale()

        binding.calendarAppBarLayout.setBackgroundColor(ColorUtilities.lightenColor(colorCalendarBackground, 0.13F))
        binding.calendarView.setBackgroundColor(ColorUtilities.lightenColor(colorCalendarBackground, 0.04F))
        // binding.calendarAppBarLayout.setBackgroundColor(colorCalendarBackgroundLight)

        (binding.legendLayout as ViewGroup).children.forEachIndexed { index, textView ->
            (textView as TextView).apply {
                text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase(Locale.ENGLISH)
                setTextColorRes(R.color.calendarWhiteLight)
            }
        }

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(10)
        val endMonth = currentMonth.plusMonths(10)
        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = CalendarDayBinding.bind(view).textCalendarDay

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate == day.date) {
                            selectedDate = null

                            binding.calendarView.notifyDayChanged(day)

                            // TODO: 여기서 기념일 체크 후 노티.. 그러면 바인딩파트에서 추가 디자인 작업.
                            // 기념일이면, 아래에 표시.
                        } else {
                            val canceledDate = selectedDate
                            selectedDate = day.date
                            binding.calendarView.notifyDateChanged(day.date)
                            canceledDate?.let { binding.calendarView.notifyDateChanged(canceledDate) }

                            val anniversary = viewModel.getAnniversary(day.date)
                            selectedAnniversary = anniversary

                            val diaries = viewModel.dateDiaryHashMap[selectedDate]
                            showSelectedDateDiaries(diaries)

                            if (anniversary != null) {
                                setAnniversaryText(anniversary)
                                binding.textButtonRegisterAnniversary.text = getString(R.string.edit_anniversary)
                                anniversaryFlag = UPDATE_ANNIVERSARY
                            } else {
                                binding.anniversaryContainer.visibility = View.GONE
                                binding.textButtonRegisterAnniversary.text = getString(R.string.anniversary)
                                anniversaryFlag = REGISTER_ANNIVERSARY
                            }

                            if (selectedDate != today) {
                                TabFragment.diaryWritingMode = DATE_OTHER_THAN_TODAY
                                TabFragment.calendarFragmentOnFabClick = {
                                    val defaultZoneId = ZoneId.systemDefault()
                                    val date = Date.from(
                                        selectedDate?.atStartOfDay(defaultZoneId)?.toInstant()
                                    )

                                    navigateToDiaryWritingFragment(date.time)
                                    TabFragment.diaryWritingMode = CREATE_MODE
                                }
                            } else
                                TabFragment.diaryWritingMode = CREATE_MODE
                        }
                    }
                }
            }
        }

        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day

                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    val anniversary = viewModel.getAnniversary(day.date)
                    val diaries = viewModel.dateDiaryHashMap[day.date]

                    when {
                        selectedDate == day.date -> {
                            textView.setTextColor(colorCalendarBackground)
                            textView.setBackgroundResource(R.drawable.calendar_selected_day_background)
                        }
                        today == day.date -> {
                            textView.setTextColorRes(R.color.colorWhite)
                            textView.setBackgroundResource(R.drawable.calendar_today_background)
                        }
                        else -> {
                            textView.setTextColorRes(R.color.colorWhite)
                            textView.background = null
                        }
                    }

                    if (anniversary != null)
                        textView.setTextColor(anniversary.color)

                    if (diaries != null) {
                        textView.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            0,
                            R.drawable.ic_quill_pen_16
                        )
                        // Bottom
                        textView.compoundDrawables.getOrNull(3)?.setTint(ContextCompat.getColor(requireContext(), R.color.colorBabyBlue))
                    }
                    else
                        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                } else {
                    textView.setTextColorRes(R.color.calendarWhiteLight)
                    textView.background = null
                }
            }
        }

        binding.calendarView.monthScrollListener = {
            if (binding.calendarView.maxRowCount == 6) {
                binding.textYear.text = it.yearMonth.year.toString()
                binding.textMonth.text = monthTitleFormatter.format(it.yearMonth)
            } else {
                // In week mode, we show the header a bit differently.
                // We show indices with dates from different months since
                // dates overflow and cells in one index can belong to different
                // months/years.

                val firstDate = it.weekDays.first().first().date
                val lastDate = it.weekDays.last().last().date
                if (firstDate.yearMonth == lastDate.yearMonth) {
                    binding.textYear.text = firstDate.yearMonth.year.toString()
                    binding.textMonth.text = monthTitleFormatter.format(firstDate)
                } else {
                    binding.textMonth.text =
                        "${monthTitleFormatter.format(firstDate)} - ${monthTitleFormatter.format(lastDate)}"
                    if (firstDate.year == lastDate.year) {
                        binding.textYear.text = firstDate.yearMonth.year.toString()
                    } else {
                        binding.textYear.text = "${firstDate.yearMonth.year} - ${lastDate.yearMonth.year}"
                    }
                }
            }
        }

        // Anniversary
        binding.textButtonRegisterAnniversary.setOnClickListener {
            if (selectedDate != null) {
                val registerAnniversaryDialogFragment = RegisterAnniversaryDialogFragment().apply {
                    setLocalDate(selectedDate!!)
                    setButtonClickListener(this@CalendarFragment)
                    setAnniversaryFlag(anniversaryFlag)
                }

                if (anniversaryFlag == UPDATE_ANNIVERSARY)
                    registerAnniversaryDialogFragment.setAnniversary(selectedAnniversary ?: return@setOnClickListener)

                registerAnniversaryDialogFragment.show(requireActivity().supportFragmentManager, registerAnniversaryDialogFragment.tag)
            } else
                showToast(requireContext(), getString(R.string.no_selected_date))
        }

        binding.imageDeleteAnniversary.setOnClickListener {
            val title = getString(R.string.delete_anniversary_title)
            val message = getString(R.string.delete_anniversary_message)

            selectedDate?.let { localDate ->
                val anniversary = viewModel.getAnniversary(localDate) ?: return@setOnClickListener
                val confirmDeletionDialogFragment = OkCancelDialogFragment().apply {
                    setDialogParameters(title, message) {
                        viewModel.unregisterAnniversary(anniversary)
                    }
                }

                confirmDeletionDialogFragment.show(requireActivity().supportFragmentManager, confirmDeletionDialogFragment.tag)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.calendarBackgroundLight)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
    }

    override fun onOkClick(anniversary: AnniversaryModel) {
        if (anniversaryFlag == REGISTER_ANNIVERSARY)
            viewModel.registerAnniversary(anniversary)
        else if (anniversaryFlag == UPDATE_ANNIVERSARY)
            viewModel.updateAnniversary(anniversary)

        val defaultZoneId = ZoneId.systemDefault()
        val localDate = anniversary.getLocalDate()
        val date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant())

        setAnniversaryText(anniversary)
    }

    fun setAnniversaryText(anniversary: AnniversaryModel) {
        binding.anniversaryContainer.visibility = View.VISIBLE
        binding.textAnniversary.text = anniversary.title
        binding.textAnniversary.setTextColor(anniversary.color)
        binding.imageAnniversary.setColorFilter(anniversary.color)
    }

    private fun showSelectedDateDiaries(diaries: List<DiaryModel>?) {
        diaries?.let {
            diaryAdapter.submitListWithoutHeader(it)
            binding.textNoRegisteredDiaries.visibility = View.GONE
            binding.diaryRecyclerView.visibility = View.VISIBLE
        } ?: run {
            binding.textNoRegisteredDiaries.visibility = View.VISIBLE
            binding.diaryRecyclerView.visibility = View.GONE
        }
    }

    private fun navigateToDiaryWritingFragment(time: Long) {
        findNavController().navigate(TabFragmentDirections.actionTabFragmentToDiaryWritingFragment(null, DATE_OTHER_THAN_TODAY, time))
    }
}