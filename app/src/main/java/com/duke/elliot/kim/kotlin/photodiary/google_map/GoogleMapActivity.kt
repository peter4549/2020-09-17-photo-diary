package com.duke.elliot.kim.kotlin.photodiary.google_map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.duke.elliot.kim.kotlin.photodiary.MainActivity
import com.duke.elliot.kim.kotlin.photodiary.R
import com.duke.elliot.kim.kotlin.photodiary.databinding.ActivityGoogleMapBinding
import com.duke.elliot.kim.kotlin.photodiary.utility.OkCancelDialogFragment
import com.duke.elliot.kim.kotlin.photodiary.utility.showToast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.util.*


const val KEY_GOOGLE_MAP_PLACE = "elliot_google_map_place_1155"
private const val UPDATE_INTERVAL = 1000L
private const val FASTEST_UPDATE_INTERVAL = 500L


class GoogleMapActivity: AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityGoogleMapBinding
    private lateinit var mGoogleMap: GoogleMap
    private var currentMarker: Marker? = null
    private lateinit var inputMethodManager: InputMethodManager

    private lateinit var viewModel: GoogleMapViewModel

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var location: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_google_map)

        /** Color */
        applyPrimaryThemeColor(binding.toolbar)
        binding.myLocation.setColorFilter(MainActivity.themeColorPrimary)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        /** View Model */
        val viewModelFactory = GoogleMapViewModelFactory()
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)[GoogleMapViewModel::class.java]

        if (!viewModel.initialized) {
            viewModel.place = intent.getParcelableExtra(KEY_GOOGLE_MAP_PLACE)
            viewModel.initialized = true
        }

        binding.myLocation.setOnClickListener {
            getCurrentLocation()
        }

        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_UPDATE_INTERVAL)

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        binding.placeSearch.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
                searchLocation()
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.google_map, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
            R.id.savePlace -> finishWithSetPlace()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap ?: run {
            showToast(this, getString(R.string.google_map_could_not_be_loaded))
            return
        }

        viewModel.place?.let {
            /** Location already set. */
            setLocation(it)
        } ?: run {
            /** Initial location */
            requestLocationPermissionAndGetCurrentLocation()
        }
    }

    private fun searchLocation() {
        val location = binding.placeSearch.text.toString()
        var addressList: List<Address>? = null
        if (location.isNotBlank()) {
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                addressList = geocoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (addressList == null || addressList.isEmpty())
                return

            val address = addressList[0]
            val name = address.getAddressLine(0)

            binding.locationName.setText(name)

            val latLng = LatLng(address.latitude, address.longitude)

            currentMarker?.remove()
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.title(name)
            markerOptions.draggable(true)
            currentMarker = mGoogleMap.addMarker(markerOptions)

            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
            viewModel.place = createPlace(location, address)
        } else {
            binding.placeSearchContainer.isErrorEnabled = true
            binding.placeSearchContainer.error = getString(R.string.please_enter_a_place_name)
        }
    }

    private fun setLocation(place: PlaceModel) {
        val name = place.name
        binding.locationName.setText(name)

        val latLng = LatLng(place.latitude, place.longitude)
        val markerTitle = place.name

        currentMarker?.remove()
        val markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title(markerTitle)
        markerOptions.draggable(true)
        currentMarker = mGoogleMap.addMarker(markerOptions)

        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15F)
        mGoogleMap.moveCamera(cameraUpdate)

        viewModel.place = place
    }

    private fun requestLocationPermissionAndGetCurrentLocation() {
        val multiplePermissionListener = object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                if (report.deniedPermissionResponses.isEmpty()) {
                    getCurrentLocation()
                    return
                }

                for (response in report.deniedPermissionResponses) {
                    setAsDefaultLocation()
                    showSnackbarOnDenied()
                    break
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.let { showPermissionRationale(it) }
            }
        }

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(multiplePermissionListener)
            .check()
    }

    fun showPermissionRationale(token: PermissionToken) {
        val requestPermissionDialog = OkCancelDialogFragment().apply {
            setDialogParameters(
                binding.root.context.getString(R.string.permission_request),
                binding.root.context.getString(R.string.location_permission_request_message)
            ) {
                token.continuePermissionRequest()
            }

            setCancelClickEvent {
                token.cancelPermissionRequest()
            }

            setOnDismissListener {
                token.cancelPermissionRequest()
            }
        }

        requestPermissionDialog.show(
            supportFragmentManager,
            requestPermissionDialog.tag
        )
    }

    private fun showSnackbarOnDenied() {
        val snackbar = Snackbar
            .make(
                binding.root,
                getString(R.string.snackbar_on_denied_message_location),
                Snackbar.LENGTH_LONG
            )
            .setAction(getString(R.string.settings)) {
                openApplicationSettings()
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.colorPositiveButton))

        snackbar.show()
    }

    private fun openApplicationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", this.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    /** Initial location. */
    private fun getCurrentLocation() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            setAsDefaultLocation()
            showSnackbarOnDenied()
            return
        }

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null)
                    showToast(this, getString(R.string.failed_to_get_current_location))
                else {
                    val addresses = Geocoder(this, Locale.getDefault()).getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    ) ?: run {
                        showToast(this, getString(R.string.failed_to_get_current_location))
                        setAsDefaultLocation()
                        return@addOnSuccessListener
                    }

                    if (addresses.isEmpty()) {
                        showToast(this, getString(R.string.failed_to_get_current_location))
                        setAsDefaultLocation()
                        return@addOnSuccessListener
                    }

                    val address = addresses[0]
                    val place = createPlace(address.getAddressLine(0), address)
                    setLocation(place)
                }
            }
    }

    private fun setAsDefaultLocation() {
        val defaultLatLng = LatLng(37.56, 126.97)
        val markerTitle = getString(R.string.could_not_get_location_information_title)
        val markerSnippet = getString(R.string.could_not_get_location_information_snippet)

        currentMarker?.remove()
        val markerOptions = MarkerOptions()
        markerOptions.position(defaultLatLng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        currentMarker = mGoogleMap.addMarker(markerOptions)
        val cameraUpdate: CameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLatLng, 15F)
        mGoogleMap.animateCamera(cameraUpdate)
    }

    private fun createPlace(name: String, address: Address) = PlaceModel(
        true, name, address.longitude, address.latitude
    )

    private fun applyPrimaryThemeColor(vararg views: View) {
        for (view in views) {
            view.setBackgroundColor(MainActivity.themeColorPrimary)
            view.invalidate()
        }
    }

    private fun finishWithSetPlace() {
        val intent = Intent()
        val enteredPlaceName = binding.locationName.text.toString()

        if (enteredPlaceName.isNotBlank()) {
            viewModel.place?.let {
                it.name = enteredPlaceName
                intent.putExtra(KEY_GOOGLE_MAP_PLACE, it)
                setResult(RESULT_OK, intent)
                finish()
            }
        } else {
            binding.locationNameContainer.isErrorEnabled = true
            binding.locationNameContainer.error = getString(R.string.please_enter_a_place_name)
        }
    }
}