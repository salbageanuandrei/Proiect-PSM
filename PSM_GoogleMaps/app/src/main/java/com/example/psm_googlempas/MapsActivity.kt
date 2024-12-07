package com.example.psm_googlempas

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.psm_googlempas.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import java.io.IOException
import java.util.Locale


private const val KEY_CAMERA_POSITION = "camera_position"
private const val KEY_LOCATION = "location"

private const val DEFAULT_ZOOM = 15
private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1


// milliseconds for double-click detection
private const val doubleClickInterval: Long = 3000
private var lastClickTime: Long = 0

private lateinit var map: GoogleMap


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var binding: ActivityMapsBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private var cameraPosition: CameraPosition? = null
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-34.0, 151.0)
    private var currentMarker: Marker? = null
    lateinit var searchView : SearchView

    private lateinit var locationManager: LocationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set de action bar color and Title
        supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#fcfcff")))
        supportActionBar?.title = Html.fromHtml("<font color='#0e0a1b'>Hartă Interactivă</font>")

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Here we are adding functionality to the button
        val btn = findViewById<Button>(R.id.currentLoc)
        btn.setBackgroundColor(getResources().getColor(R.color.Transparent))
        btn.setOnClickListener {
            getLastLocation()
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager


        // Initialize the Places client
        Places.initialize(applicationContext, getString(R.string.maps_api_key))
        placesClient = Places.createClient(this)

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // we retrieve saved instance state
        savedInstanceState?.let {
            lastKnownLocation = it.getParcelable(KEY_LOCATION)
            cameraPosition = it.getParcelable(KEY_CAMERA_POSITION)
        }
        // initializing the search view.
        searchView = findViewById(R.id.idSearchView)


        // adding on query listener for our search view.
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // here we are getting the
                // location name from search view.
                val location = searchView.query.toString()
                var returnBool  = false

                // here we create a list of address
                // and we will store the list of all address.
                var addressList: List<Address>? = null

                    // on below line we are creating and initializing a geo coder.
                    val geocoder = Geocoder(this@MapsActivity)
                    try {
                        // here we are getting location from the
                        // location name and adding that location to address list.
                        addressList = geocoder.getFromLocationName(
                            location,
                            10,
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (!addressList.isNullOrEmpty()) {
                        val Address = addressList[0]

                        // here we are creating a variable for the location
                        // here we will add our locations latitude and longitude.
                        val latLng = LatLng(Address.latitude, Address.longitude)

                        returnBool = true
                        // here  we are adding marker to that position.
                        map.addMarker(MarkerOptions().position(latLng).title(location))


                        // here we animate the camera to that position.
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                    }
                    else
                    {
                        //do nothing
                    }

                if(!returnBool)
                {
                    Toast.makeText(this@MapsActivity, "Adresa gresita!", Toast.LENGTH_SHORT).show()
                }
                return returnBool
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })


        // we will Obtain the SupportMapFragment and we will be
        // notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    // Get current location
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            val locationByGps:Location = lastKnownLocationByGps

            val currentLocationLatLng = LatLng(locationByGps.latitude, locationByGps.longitude)

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 16F))

                }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Enable zoom controls and zoom  gestures
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isZoomGesturesEnabled = true

        // Add markers on click
        addMarkerbyclick(map)

        // Add a marker in Arad and move the camera
        val varArad = LatLng(46.19, 21.32)

        map.addMarker(MarkerOptions().position(varArad).title("Marker in Arad"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(varArad,DEFAULT_ZOOM.toFloat()))

        // Add a marker in Timisoara and move the camera
        val varTimisoara = LatLng(45.76, 21.22)

        map.addMarker(MarkerOptions().position(varTimisoara).title("Marker in Timisoara"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(varTimisoara,DEFAULT_ZOOM.toFloat()))



        // Enable My Location if permission is granted
        getLocationPermission()

        // Update location
        updateLocationUI()

        //Get device location
        getDeviceLocation()

        // Set Custom window
        setCustomInfoWindow()

        //remove marker on double click
        setDoubleClickToRemoveMarker(map)


    }

    private fun setCustomInfoWindow() {
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(arg0: Marker): View? = null

            override fun getInfoContents(marker: Marker): View {
                val infoWindow = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    findViewById<FrameLayout>(R.id.map),
                    false
                )
                infoWindow.findViewById<TextView>(R.id.title).text = marker.title
                infoWindow.findViewById<TextView>(R.id.snippet).text = marker.snippet
                return infoWindow
            }
        })
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionGranted = false
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            locationPermissionGranted = grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
            updateLocationUI()
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true

            } else {
                map.isMyLocationEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        lastKnownLocation?.let {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }




    private fun addMarkerbyclick(map: GoogleMap)
    {
        // Set a click listener for taps on the map
        map.setOnMapClickListener { latLng ->
            // Remove the previous marker if it exists
            currentMarker?.remove()

            // Add a new marker at the tapped location
            currentMarker = map.addMarker(
                MarkerOptions().position(latLng).title("Locația selectată mai jos")
            )
            currentMarker?.setIcon(BitmapDescriptorFactory.defaultMarker( 23F))

            // Optionally, move the camera to the new marker
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
            getAddressFromLocation(latLng)

        }


    }

    private fun setDoubleClickToRemoveMarker(map: GoogleMap) {
        map.setOnMarkerClickListener { clickedMarker ->

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < doubleClickInterval) {
                    // Double-click detected
                    clickedMarker.remove() // Remove the marker
                    lastClickTime = 0 // Reset click time
                    true
                } else {
                    lastClickTime = currentTime // Update last click time
                    false // return false to allow default behavior if needed
                }

        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        map.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }
    private fun getAddressFromLocation(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            // Fetch address data from the Geocoder
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 3)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = address.getAddressLine(0) ?: "Adresa nu a fost găsită"

                // Display the address information in a Toast
                Toast.makeText(this, "Adresa: $addressText", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Nici o adresă nu a fost găsită", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Geocoder serviciu indisponibil", Toast.LENGTH_SHORT).show()
        }
    }

}
