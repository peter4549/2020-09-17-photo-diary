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

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationIntent = Intent(context, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            0
        )

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
            .setContentText(intent.getStringExtra("aa")
                ?: context.getString(R.string.default_notification_content))
            .setContentIntent(pendingIntent)
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        // TODO: Repeat 테스트 후 부정확시 여기에 알람 등록코드 추가필요.
    }

    companion object {
        private const val CHANNEL_ID = "reminder_notification_channel_id"
        private const val CHANNEL_NAME = "reminder_notification_channel_name"
        private const val CHANNEL_DESCRIPTION = "reminder notification channel"
        private const val NOTIFICATION_ID = 3000
    }
}