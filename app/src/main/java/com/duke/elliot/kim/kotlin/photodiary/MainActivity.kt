package com.duke.elliot.kim.kotlin.photodiary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_diary.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        val viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

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
            = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diary, parent, false))


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val diary = diaries[position]
            holder.view.diary_text_title.text = diary
        }

        override fun getItemCount(): Int = diaries.count()
    }
}