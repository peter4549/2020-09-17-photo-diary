package com.duke.elliot.kim.kotlin.photodiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.alarm.AlarmUtil
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingFragment
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.theme.loadPrimaryThemeColor
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.theme.loadSecondaryThemeColor
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.lock_screen.LOCK_SCREEN_TAG
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.lock_screen.LockScreenHelper
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.reminder.ReminderFragment
import com.duke.elliot.kim.kotlin.photodiary.export.EXPORT_REQUEST_CODE
import com.duke.elliot.kim.kotlin.photodiary.fluid_keyboard_resize.FluidContentResize
import com.duke.elliot.kim.kotlin.photodiary.folder.FolderModel
import com.duke.elliot.kim.kotlin.photodiary.utility.TypefaceUtil
import com.duke.elliot.kim.kotlin.photodiary.utility.printHashKey
import com.facebook.internal.CallbackManagerImpl
import kotlinx.android.synthetic.main.item_diary.view.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

const val DIARIES_FRAGMENT_HANDLER_COLOR_CHANGED_MESSAGE = 527
const val DIARIES_FRAGMENT_HANDLER_FOLDER_CHANGED_MESSAGE = 528
const val DIARY_WRITING_HANDLER_FOLDER_CHANGED_MESSAGE = 1126

const val PREFERENCES_FIRST_LAUNCH = "zion_preferences_first_launch_2146"

class MainActivity : AppCompatActivity() {

    // TODO must be private, provide fun as interface.
    lateinit var viewModel: MainViewModel
    var diariesFragmentHandler: Handler? = null
    var diaryWritingFragmentHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        printHashKey(this)

        setFontNameIdMap()

        FluidContentResize.listen(this)

        /** 데이터만 관리하는 존재, 각 프래그먼트는 여기의 데이터를 참조한다. ABC, 가 있고, write frag에서 D를 추가.
         * 그 때, 각 탭 맴버 프래그먼트의 리사이클ㄹ러뷰가 올바르게 동작하는가.
         * */
        val viewModelFactory = MainViewModelFactory(requireNotNull(application))
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        themeColorPrimary = loadPrimaryThemeColor(this)
        themeColorSecondary = loadSecondaryThemeColor(this)
        // AppCompatDelegate.setDefaultNightMode(getNightMode(this))

        /** First Launch */
        if (isFirstLaunch()) {
            turnOnReminderFirst()
        }

        // TODO: Implement.
        //AlarmUtilities.setReminder(this, 0L, "")

        // TODO: test. 잘되면 존나 개꿀. 관건은 다이나믹하게 가능한가.. MAINACTIVITY, tab resume에 추가할 거임. 없으면 지운거.
        TypefaceUtil.overrideFont(this, "SERIF", "fonts/nanum_brush_regular.otf")
    }

    inner class DiaryRecyclerViewAdapter(private val diaries: ArrayList<String>): RecyclerView.Adapter<DiaryRecyclerViewAdapter.ViewHolder>() {
        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diary, parent, false)
        )


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val diary = diaries[position]
            holder.view.text_title.text = diary
        }

        override fun getItemCount(): Int = diaries.count()
    }

    override fun onBackPressed() {
        if (isLockScreenOn())
            return

        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (navHostFragment?.childFragmentManager?.primaryNavigationFragment is DiaryWritingFragment)
            MainViewModel.lockScreenException = true

        if (!MainViewModel.lockScreenException) {
            MainViewModel.screenWasOff = true
        }

        MainViewModel.lockScreenException = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("Request Code: $requestCode, Result Code: $resultCode")

        val facebookShareRequestCode = CallbackManagerImpl.RequestCodeOffset.Share.toRequestCode()

        if (requestCode == EXPORT_REQUEST_CODE || requestCode == facebookShareRequestCode)
            MainViewModel.screenWasOff = false
    }

    override fun onResume() {
        super.onResume()

        if (LockScreenHelper.loadLockScreenOnState(this) && MainViewModel.screenWasOff && !isLockScreenOn())
            LockScreenHelper.showAuthLockScreen(this, LockScreenHelper.loadEncodedPinCode(this))

        MainViewModel.screenWasOff = false
    }

    private fun isLockScreenOn(): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(LOCK_SCREEN_TAG)
        return fragment != null && fragment.isVisible
    }

    private fun setFontNameIdMap() {
        for ((i, fontName) in resources.getStringArray(R.array.fonts).withIndex()) {
            fontNameIdMap[fontName] = fontIds[i]
        }
    }

    fun getDiaries() = viewModel.getDiaries()

    fun saveDiary(diary: DiaryModel, folder: FolderModel?) {
        viewModel.insert(diary, folder)
    }

    fun updateDiary(diary: DiaryModel, folder: FolderModel?) {
        viewModel.update(diary, folder)
    }

    fun recreateNoAnimation() {
        val intent = intent
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun isFirstLaunch(): Boolean {
        val preferences = this
            .getSharedPreferences(PREFERENCES_FIRST_LAUNCH, Context.MODE_PRIVATE)

        val firstLaunch = preferences.getBoolean(KEY_FIRST_LAUNCH, true)

        if (firstLaunch) {
            val editor = preferences.edit()
            editor.putBoolean(KEY_FIRST_LAUNCH, false)
            editor.apply()
        }

        return firstLaunch
    }

    private fun turnOnReminderFirst() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val calendarNow = Calendar.getInstance()

        if (calendar.before(calendarNow) || calendarNow.time == calendar.time)
            calendar.add(Calendar.DATE, 1)

        AlarmUtil.setReminder(this, calendar, getString(R.string.reminder_default_message))
    }

    fun getDiaryFolders() = viewModel.getDiaryFolders()
    fun getFolderDao() = viewModel.folderDao
    fun getDiaryDao() = viewModel.database

    fun setFolderId(folderId: Long) {
        viewModel.selectedFolderId.value = folderId
    }

    fun getSelectedFolderId() = viewModel.selectedFolderId

    companion object {
        const val DEFAULT_FONT_ID = R.font.cookie_run_regular
        var fontNameIdMap: MutableMap<String, Int> = mutableMapOf()
        val fontIds = arrayOf(
            R.font.cookie_run_regular,
            R.font.nanum_barun_gothic_regular,
            R.font.nanum_barun_pen_regular,
            R.font.nanum_brush_regular,
            R.font.nanum_gothic_regular,
            R.font.nanum_myeongjo_regular,
            R.font.nanum_pen_regular,
            R.font.nanum_square_regular,
            R.font.nanum_square_round_regular
        )

        @ColorInt
        var themeColorPrimary = 0

        @ColorInt
        var themeColorSecondary = 0

        private const val KEY_FIRST_LAUNCH = "zion_key_first_launch"
    }
}