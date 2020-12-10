package com.duke.elliot.kim.kotlin.photodiary.drawer_items.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.databinding.DataBindingUtil
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.alarm.AlarmUtil
import com.duke.elliot.kim.kotlin.photodiary.base.BaseFragment
import com.duke.elliot.kim.kotlin.photodiary.databinding.FragmentReminderBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.toDateFormat
import kotlinx.android.synthetic.main.fragment_reminder.*
import java.util.*

class ReminderFragment: BaseFragment() {

    private lateinit var binding: FragmentReminderBinding
    private lateinit var reminderMessage: String
    private var reminderMillis = 0L
    private var reminderSet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reminder, container, false)

        applyPrimaryThemeColor(binding.toolbar)
        setSimpleBackButton(binding.toolbar)

        reminderSet = AlarmUtil.loadReminderState(requireContext())
        val pair = AlarmUtil.loadReminderMillisAndMessage(requireContext())
        reminderMillis = pair.first
        reminderMessage = pair.second

        binding.switchReminderContainer.setOnClickListener {
            switchReminder.toggle()

            if (switchReminder.isChecked)
                turnOnReminder()
            else
                AlarmUtil.cancelReminder(requireContext())
        }

        binding.reminderTimeContainer.setOnClickListener {
            showTimePickerDialog()
        }

        binding.reminderMessageContainer.setOnClickListener {

        }

        initUI()

        return binding.root
    }

    private fun initUI() {
        binding.switchReminder.isChecked = reminderSet
        binding.reminderTimeContent.text = reminderMillis.toDateFormat("a h:mm")
        binding.reminderMessageContent.text = reminderMessage
    }

    private fun getReminderCalendar(): Calendar {
        val pair = AlarmUtil.loadReminderMillisAndMessage(requireContext())
        val reminderTime = pair.first

        return Calendar.getInstance().apply {
            timeInMillis = reminderTime
            set(Calendar.SECOND, 0)
        }
    }

    private fun turnOnReminder() {
        val calendar = getReminderCalendar()
        val calendarNow = Calendar.getInstance()

        if (calendar.before(calendarNow) || calendarNow.time == calendar.time)
            calendar.add(Calendar.DATE, 1)

        AlarmUtil.setReminder(requireContext(), calendar, reminderMessage)
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminderMillis
        }

        val timePickerDialog = TimePickerDialog(requireContext(), onTimeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        timePickerDialog.show()
        timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE)?.setTextColor(MainActivity.themeColorPrimary)
    }

    private val onTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }

        reminderMillis = calendar.timeInMillis

        binding.reminderTimeContent.text = reminderMillis.toDateFormat("a h:mm")
        AlarmUtil.saveReminderMillis(requireContext(), reminderMillis)
    }
}