package com.duke.elliot.kim.kotlin.photodiary.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class DeviceBootReceiver : BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val reminderSet = AlarmUtil.loadReminderState(context)
            val reminderMillisAndMessage = AlarmUtil.loadReminderMillisAndMessage(context)
            val reminderMillis = reminderMillisAndMessage.first
            val reminderMessage = reminderMillisAndMessage.second

            if (reminderSet) {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = reminderMillis
                    set(Calendar.SECOND, 0)
                }

                val calendarNow = Calendar.getInstance()

                if (calendar.before(calendarNow) || calendarNow.time == calendar.time)
                    calendar.add(Calendar.DATE, 1)

                AlarmUtil.setReminder(context, calendar, reminderMessage)
            }
        }
    }
}