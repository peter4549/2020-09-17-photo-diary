package com.duke.elliot.kim.kotlin.photodiary

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.beautycoder.pflockscreen.PFFLockScreenConfiguration
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryWritingFragment
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.getNightMode
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.loadPrimaryThemeColor
import com.duke.elliot.kim.kotlin.photodiary.drawer_items.loadSecondaryThemeColor
import com.duke.elliot.kim.kotlin.photodiary.fluid_keyboard_resize.FluidContentResize
import com.duke.elliot.kim.kotlin.photodiary.utility.TypefaceUtil
import com.duke.elliot.kim.kotlin.photodiary.utility.printHashKey
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import kotlinx.android.synthetic.main.item_diary.view.*


class MainActivity : AppCompatActivity() {

    // TODO must be private, provide fun as interface.
    lateinit var viewModel: MainViewModel


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
        AppCompatDelegate.setDefaultNightMode(getNightMode(this))

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        super.onStop()
        println("THIS CALL WHEN POWER OFF?")
        val f = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (f is DiaryWritingFragment) // do something with f
            showToast(this, "GOOD WORKING")

        val fragments: List<*> = supportFragmentManager.fragments
        val mCurrentFragment = fragments[fragments.size - 1]
        if (mCurrentFragment is DiaryWritingFragment) // do something with f
            showToast(this, "GOOD WORKING 22222")

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val k = navHostFragment!!.childFragmentManager.primaryNavigationFragment
        println("KKKKKK, $k")
        val ss = findNavController(R.id.nav_host_fragment).currentDestination?.getId()
        println("SSSSSSS, $ss")

        println("CHECK BACK: ${isApplicationSentToBackground(this)}")
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        println("HINT CALLLLLL")
    }

    override fun onResume() {
        super.onResume()
    }

    fun isApplicationSentToBackground(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.getRunningTasks(1)
        if (!tasks.isEmpty()) {
            val topActivity = tasks[0].topActivity
            if (topActivity!!.packageName != context.getPackageName()) {
                return true
            }
        }
        return false
    }

    private fun showLockScreen() {
        val pfLockScreenFragment = PFLockScreenFragment()
        val builder = PFFLockScreenConfiguration.Builder(this)
            .setMode(PFFLockScreenConfiguration.MODE_AUTH)
            .setUseFingerprint(true)
        pfLockScreenFragment.setConfiguration(builder.build())
        pfLockScreenFragment.setEncodedPinCode("123123123123")
        pfLockScreenFragment.setLoginListener(object :
            PFLockScreenFragment.OnPFLockScreenLoginListener {
            override fun onCodeInputSuccessful() {

                showToast(this@MainActivity, "코드성공 ")
            }

            override fun onFingerprintSuccessful() {
                showToast(this@MainActivity, "지문로긴성")
            }

            override fun onPinLoginFailed() {
                showToast(this@MainActivity, "핀로긴실패")
            }

            override fun onFingerprintLoginFailed() {
                showToast(this@MainActivity, "지문로긴실패. ")
            }
        })

        pfLockScreenFragment.setOnLeftButtonClickListener {
            showToast(this@MainActivity, "FUCK 777")
        }

        supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setCustomAnimations(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit)
            .replace(R.id.nav_host_fragment, pfLockScreenFragment)
            .commit()

    }

    private fun setFontNameIdMap() {
        for ((i, fontName) in resources.getStringArray(R.array.fonts).withIndex()) {
            fontNameIdMap[fontName] = fontIds[i]
        }
    }

    fun getDiaries() = viewModel.getDiaries()

    fun saveDiary(diary: DiaryModel) {
        viewModel.insert(diary)
    }

    fun updateDiary(diary: DiaryModel) {
        viewModel.update(diary)
    }

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
    }
}