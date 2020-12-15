package com.duke.elliot.kim.kotlin.photodiary.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import java.util.*

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // val reminderSet = AlarmUtil.loadReminderState(context)
        val reminderMillisAndMessage = AlarmUtil.loadReminderMillisAndMessage(context)
        val reminderMillis = reminderMillisAndMessage.first
        val reminderMessage = reminderMillisAndMessage.second

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, MainActivity::class.java)

        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            0
        )

        // TODO: 이미 지난 시간이라면, 무시하고 date + 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)

            builder.setSmallIcon(R.drawable.ic_moon_24)
            channel.description = CHANNEL_DESCRIPTION
            notificationManager.createNotificationChannel(channel)
        } else builder.setSmallIcon(R.mipmap.ic_launcher)

        builder.setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(reminderMessage)
            .setContentIntent(pendingIntent)
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminderMillis
            set(Calendar.SECOND, 0)
        }

        calendar.add(Calendar.DATE, 1)
        AlarmUtil.setReminder(context, calendar, reminderMessage)
    }

    companion object {
        private const val CHANNEL_ID = "reminder_notification_channel_id"
        private const val CHANNEL_NAME = "reminder_notification_channel_name"
        private const val CHANNEL_DESCRIPTION = "reminder notification channel"
        private const val NOTIFICATION_ID = 3000
    }
}