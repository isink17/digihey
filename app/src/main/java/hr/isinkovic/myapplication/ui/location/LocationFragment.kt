package hr.isinkovic.myapplication.ui.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import hr.isinkovic.myapplication.R
import hr.isinkovic.myapplication.databinding.FragmentLocationBinding
import hr.isinkovic.myapplication.db.CardDatabase

class LocationFragment : Fragment(), OnMapReadyCallback, LocationListener, View.OnClickListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 126
        private const val LATITUDE_KEY = "sp/homeLatitude"
        private const val LONGITUDE_KEY = "sp/homeLongitude"
        internal const val CARDS_ADDED = "cardsAdded"
        internal const val NOT_SET = "NOT_SET"
        const val LOCATION = "location"
        const val FUEL = "fuel"
        const val REGULAR = 1
        const val PREMIUM = 2
    }

    private lateinit var _binding: FragmentLocationBinding
    private lateinit var viewModel: LocationViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var mLocationManager: LocationManager
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var marker: Marker? = null
    private var myLocationMarker: Marker? = null
    private var selectedFuel: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(this) {}
        val mActivity = activity
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        val viewModelFactory = LocationViewModelFactory(
            PreferenceManager.getDefaultSharedPreferences(activity!!),
            CardDatabase.getDatabase(context!!.applicationContext).cardDao()
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[LocationViewModel::class.java]

        if (mActivity != null) {
            mLocationManager =
                mActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity)
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
            selectedFuel = null

            _binding.leftMapBtn.setOnClickListener(this)
            _binding.rightMapBtn.setOnClickListener(this)
            _binding.proceedBtn.setOnClickListener(this)
            _binding.rightBtn.setOnClickListener(this)
            _binding.leftBtn.setOnClickListener(this)

            when (viewModel.selectedFuel) {
                PREMIUM -> _binding.rightBtn.isSelected = true
                REGULAR -> _binding.leftBtn.isSelected = true
            }

        } else {
            throw IllegalArgumentException("No activity")
        }
        return _binding.root
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.clear()
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity!!)

        val myDefaultLocation = viewModel.position ?: LatLng(
            preferences.getString(LATITUDE_KEY, getString(R.string.default_lat))!!.toDouble(),
            preferences.getString(LONGITUDE_KEY, getString(R.string.default_lon))!!.toDouble()
        )
        marker = mMap.addMarker(
            MarkerOptions().position(myDefaultLocation)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.img_marker))
                .title(getString(R.string.lbl_marker_car_location))
        )

        if (hasLocationPermission()) {
            myLocationMarker = mMap.addMarker(
                MarkerOptions().position(myDefaultLocation)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_marker))
                    .title(getString(R.string.lbl_marker_my_location))
            )
        }


        if (viewModel.position == null) {
            focusDefaultLocation(myDefaultLocation)
            getFirstLocation()
        } else {
            focusDefaultLocation(myDefaultLocation)
            getLocation(false)
        }
    }

    private fun focusDefaultLocation(defaultLocation: LatLng) {
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 19f))
        mMap.setOnCameraMoveListener {
            marker?.let {
                viewModel.position = mMap.cameraPosition.target
                marker!!.position = viewModel.position!!
            }
        }
    }

    override fun onClick(p0: View?) {
        when {
            p0!!.id == R.id.rightMapBtn -> {
                if (hasLocationPermission()) {
                    getLocation(true)
                } else {
                    Toast.makeText(
                        context!!,
                        getString(R.string.lbl_request_location),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    ActivityCompat.requestPermissions(
                        activity!!,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
            p0.id == R.id.leftMapBtn -> {
                val preferences = PreferenceManager.getDefaultSharedPreferences(activity!!)
                if (preferences.contains(LONGITUDE_KEY) && preferences.contains(LATITUDE_KEY)) {
                    val homeLocation = LatLng(
                        preferences.getString(LATITUDE_KEY, NOT_SET)!!.toDouble(),
                        preferences.getString(LONGITUDE_KEY, NOT_SET)!!.toDouble()
                    )
                    mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    moveCamera(homeLocation)
                } else {
                    context?.let { it1 ->
                        AlertDialog.Builder(it1)
                            .setTitle(getString(R.string.lbl_title_home_location))
                            .setMessage(getString(R.string.lbl_text_home_location))
                            .setPositiveButton(R.string.common_lbl_yes) { _, _ ->
                                marker?.let { it2 ->
                                    preferences.edit()
                                        .putString(LATITUDE_KEY, it2.position.latitude.toString())
                                        .commit()
                                    preferences.edit()
                                        .putString(LONGITUDE_KEY, it2.position.longitude.toString())
                                        .commit()
                                    it2.position
                                }
                                Toast.makeText(
                                    context!!,
                                    getString(R.string.lbl_added_home_location), Toast.LENGTH_SHORT
                                ).show()
                            }
                            .setNegativeButton(R.string.common_lbl_no) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }
            p0.id == R.id.proceedBtn -> {
                if (viewModel.selectedFuel != -1) {
                    val bundle = Bundle()
                    bundle.putString(
                        LOCATION,
                        marker.let {
                            viewModel.position?.run { "$latitude , $longitude" } })
                    bundle.putInt(FUEL, viewModel.selectedFuel)
                    Navigation.findNavController(p0)
                        .navigate(R.id.action_locationFragment_to_paymentFragment, bundle)

                } else {
                    Toast.makeText(
                        context!!,
                        getString(R.string.lbl_select_fuel_type),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
            p0.id == R.id.leftBtn -> {
                _binding.leftBtn.isSelected = true
                _binding.rightBtn.isSelected = false
                viewModel.selectedFuel = REGULAR
            }
            p0.id == R.id.rightBtn -> {
                _binding.leftBtn.isSelected = false
                _binding.rightBtn.isSelected = true
                viewModel.selectedFuel = PREMIUM
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getFirstLocation() {
        if (activity != null) {
            if (hasLocationPermission()) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
                mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    this
                )
            } else {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            throw IllegalArgumentException("No context")
        }
    }

    override fun onLocationChanged(p0: Location) {
        val latLng = LatLng(p0.latitude, p0.longitude)
        moveCamera(latLng)
        if (myLocationMarker == null) {
            myLocationMarker = mMap.addMarker(
                MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_marker))
                    .title(getString(R.string.lbl_marker_my_location))
            )
        } else {
            myLocationMarker!!.position = latLng
        }
        mLocationManager.removeUpdates(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)
            || isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            getFirstLocation()
        }
    }

    private fun isPermissionGranted(
        grantPermissions: Array<String>,
        grantResults: IntArray,
        permission: String
    ): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation(shouldMoveCamera: Boolean) {
        if (isLocationEnabled()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener {
                val location: Location? = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    if (myLocationMarker == null) {
                        myLocationMarker = mMap.addMarker(
                            MarkerOptions().position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_marker))
                                .title(getString(R.string.lbl_marker_my_location))
                        )
                    } else {
                        myLocationMarker!!.position = latLng
                    }
                    if (shouldMoveCamera) {
                        moveCamera(latLng)
                    }
                }
            }
        } else {
            Toast.makeText(context, getString(R.string.lbl_turn_on_location), Toast.LENGTH_LONG)
                .show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

    }

    private fun moveCamera(location: LatLng) {
        viewModel.position = location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 19f))
        marker?.let { it.position = location }
    }
}