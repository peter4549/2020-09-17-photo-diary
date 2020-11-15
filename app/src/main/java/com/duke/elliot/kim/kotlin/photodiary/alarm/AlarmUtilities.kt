package com.duke.elliot.kim.kotlin.photodiary.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.duke.elliot.kim.kotlin.photodiary.R
import java.util.*

// USE CASE: 첫 실행, load state 함수 실행(디폴트 true) => 결과에 따라 set or pass, (cancel 시, 자동으로 false로 따로 설정할 필요 없음.)
// 그 뒤 스위치 on/off에 따라 set/cancel 수행 후, save state 함수 수행할 것.

object AlarmUtilities {
    const val REMINDER_REQUEST_ID = 1115
    const val REMINDER_SHARED_PREFERENCES = "reminder_shared_preferences"
    const val KEY_REMINDER_STATE = "key_reminder_state"
    const val KEY_REMINDER_CONTENT = "key_reminder_content"
    const val KEY_REMINDER_TIME = "key_reminder_time"

    fun cancelReminder(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).let {
            PendingIntent.getBroadcast(context, REMINDER_REQUEST_ID, it, 0)
        }

        alarmManager?.cancel(intent)
        saveReminderState(context, false)
    }

    private fun saveReminderState(context: Context, state: Boolean) {
        val sharedPreferences =
            context.getSharedPreferences(REMINDER_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_REMINDER_STATE, state)
        editor.apply()
    }

    private fun saveTimeAndContent(context: Context, time: Long, content: String) {
        val sharedPreferences =
            context.getSharedPreferences(REMINDER_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong(KEY_REMINDER_TIME, time)
        editor.putString(KEY_REMINDER_CONTENT, content)
        editor.apply()
    }

    fun loadReminderState(context: Context): Boolean {
        val sharedPreferences =
            context.getSharedPreferences(REMINDER_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_REMINDER_STATE, true)
    }

    fun loadReminderTimeAndContent(context: Context): Pair<Long, String> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 22)
        }
        val defaultTime = calendar.timeInMillis

        val sharedPreferences =
            context.getSharedPreferences(REMINDER_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val time = sharedPreferences.getLong(KEY_REMINDER_TIME, defaultTime)
        val content = sharedPreferences.getString(KEY_REMINDER_CONTENT, context.getString(R.string.default_notification_content))
            ?: context.getString(R.string.default_notification_content)

        return Pair(time, content)
    }

    fun setReminder(context: Context, time: Long, content: String) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 23)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).let {
            PendingIntent.getBroadcast(context, REMINDER_REQUEST_ID, it, 0)
        }
        // Pending 인텐트 말고, 오리진 인텐트에 putInt등으로 데이터 담을 것.

        val packageManager = context.packageManager
        val deviceBootReceiver = ComponentName(context, DeviceBootReceiver::class.java)

        /* 1회성 알람 설정 코드임. 나중에 부정확할 시 아래 코드랑 같이 참ㄴ조.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                intent
            )
        }

         */

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            intent
        )

        packageManager.setComponentEnabledSetting(
            deviceBootReceiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        saveReminderState(context, true)
        // TODO: chooser 구현 후 풀 것.
        // saveTimeAndContent(time, content)
    }

    // TODO setRepeat 부정확할 시, 아래 코드 참고할 것.
    // fun setAlarm(hour: Int, context: Context) { val calendar = Calendar.getInstance() calendar.set(Calendar.HOUR_OF_DAY, hour) calendar.set(Calendar.MINUTE, 0) calendar.set(Calendar.SECOND, 0) var nowCalendar = Calendar.getInstance() if (calendar.before(nowCalendar) || nowCalendar.time == calendar.time) { //이미 지난 시간 일 경우 calendar.add(Calendar.DATE, 1) } var intent = Intent(context, MealsAlarmReceiver::class.java) var pendingIntent = PendingIntent.getBroadcast(context, REQ_MEAL_ALARM, intent, PendingIntent.FLAG_UPDATE_CURRENT) val alarmManaer = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager when { Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> alarmManaer.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent) Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> alarmManaer.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent) else -> alarmManaer.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent) } }

}