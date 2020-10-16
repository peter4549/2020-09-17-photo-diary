package com.duke.elliot.kim.kotlin.photodiary

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.diary.media.photo_editor.REQUEST_CODE_WRITE_EXTERNAL_STORAGE
import com.duke.elliot.kim.kotlin.photodiary.fluid_keyboard_resize.FluidContentResize
import com.duke.elliot.kim.kotlin.photodiary.utility.hasPermissions
import kotlinx.android.synthetic.main.item_diary.view.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    // TODO must be private, provide fun as interface.
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupTimber()
        setFontNameIdMap()

        FluidContentResize.listen(this)


        /** 데이터만 관리하는 존재, 각 프래그먼트는 여기의 데이터를 참조한다. ABC, 가 있고, write frag에서 D를 추가.
         * 그 때, 각 탭 맴버 프래그먼트의 리사이클ㄹ러뷰가 올바르게 동작하는가.
         * */
        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        themeColorDark = ContextCompat.getColor(this, R.color.colorDefaultThemeDark)
        themeColorLight = ContextCompat.getColor(this, R.color.colorDefaultThemeLight)
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

    private fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun setFontNameIdMap() {
        for ((i, fontName) in resources.getStringArray(R.array.fonts).withIndex()) {
            fontNameIdMap[fontName] = fontIds[i]
        }
    }

    companion object {
        const val DEFAULT_FONT_ID = R.font.nanum_barun_gothic_regular
        var fontNameIdMap: MutableMap<String, Int> = mutableMapOf()
        val fontIds = arrayOf(
            R.font.nanum_barun_gothic_regular,
            R.font.nanum_barun_pen_regular,
            R.font.nanum_brush_regular,
            R.font.nanum_gothic_regular,
            R.font.nanum_myeongjo_regular,
            R.font.nanum_pen_regular,
            R.font.nanum_square_regular,
            R.font.nanum_square_round_regular
        )
        var themeColorDark = 0
        var themeColorLight = 0
    }
}