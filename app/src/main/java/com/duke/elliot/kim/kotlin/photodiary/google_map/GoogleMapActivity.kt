package com.duke.elliot.kim.kotlin.photodiary.google_map

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.ActivityGoogleMapBinding
import com.duke.elliot.kim.kotlin.photodiary.diary_writing.DiaryModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

const val KEY_GOOGLE_MAP_DIARY = "elliot_google_map_diary_1155"

class GoogleMapActivity: AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGoogleMapBinding
    private lateinit var mGoogleMap: GoogleMap
    private var diary: DiaryModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_google_map)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        diary = intent.getParcelableExtra(KEY_GOOGLE_MAP_DIARY)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap ?: run {
            // TODO: show error,
            return
        }

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mGoogleMap.addMarker(
            MarkerOptions()
            .position(sydney)
            .title("Marker in Sydney"))
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}