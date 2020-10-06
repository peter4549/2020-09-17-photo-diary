package com.duke.elliot.kim.kotlin.photodiary

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.photodiary.fluid_keyboard_resize.FluidContentResize
import kotlinx.android.synthetic.main.item_diary.view.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    // TODO must be private, provide fun as interface.
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupTimber()

        FluidContentResize.listen(this)


        /** 데이터만 관리하는 존재, 각 프래그먼트는 여기의 데이터를 참조한다. ABC, 가 있고, write frag에서 D를 추가.
         * 그 때, 각 탭 맴버 프래그먼트의 리사이클ㄹ러뷰가 올바르게 동작하는가.
         * */
        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        themeColorDark = ContextCompat.getColor(this, R.color.colorDefaultThemeDark)
        themeColorLight = ContextCompat.getColor(this, R.color.colorDefaultThemeLight)

        /*
        viewModel.diaries.observe(this, { diaries ->
            when(viewModel.action) {
                MainViewModel.Action.INITIALIZE -> {
                    main_recycler_view.apply {
                        adapter = DiaryRecyclerViewAdapter(diaries)
                        layoutManager = LinearLayoutManager(this@MainActivity)
                    }
                    main_recycler_view.adapter?.notifyDataSetChanged()
                    viewModel.action = MainViewModel.Action.INITIALIZED
                }
                MainViewModel.Action.ADD -> {
                    Toast.makeText(this@MainActivity, "CALL!", Toast.LENGTH_SHORT).show()
                    main_recycler_view.adapter?.notifyDataSetChanged()
                }
            }
        })

         */
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

    override fun onBackPressed() {
        println("KKKKKKK + ${supportFragmentManager.fragments.map { it.tag }}")
        super.onBackPressed()
    }

    private fun setupTimber() {
        Timber.plant(Timber.DebugTree())
    }

    companion object {
        var themeColorDark = 0
        var themeColorLight = 0
    }
}