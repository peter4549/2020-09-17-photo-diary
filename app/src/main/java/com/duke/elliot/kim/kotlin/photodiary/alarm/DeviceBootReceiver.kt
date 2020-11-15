package com.duke.elliot.kim.kotlin.photodiary.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class DeviceBootReceiver : BroadcastReceiver()  {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // TODO, 인텐트로부터 컨텐트, 시간 전달받아야함.
            // 인텐트에 시간, 메시지가 있음 그걸로 알람 재설정. 시간, 메시지 뽑아서 넘길것.
            AlarmUtilities.setReminder(context, 0L, "abc")

            /*
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

             */

        }
    }
}