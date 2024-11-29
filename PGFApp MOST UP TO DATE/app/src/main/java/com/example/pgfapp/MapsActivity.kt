package com.example.pgfapp


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.DatabaseStuff.UserDatabase
import com.example.pgfapp.ViewPager2MapsAct.PagerAdapter
import com.example.pgfapp.databinding.ActivityMapsBinding
import com.example.pgfapp.utilities.CoapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Purpose: Allows user to view maps

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var currentMarker: Marker? = null
    private var bounds = ArrayList<LatLng>()
    private var markers = mutableListOf<Marker?>()
    private lateinit var boundAct: BoundsActivity
    private var locInaccRadius: Double = 0.0
    private var marker: Marker? = null
    private val petMarkers = mutableMapOf<String, Marker?>()
    private val petCircles = mutableMapOf<String, Circle?>()
    private lateinit var switch: Switch
    private var polygon: Polygon? = null //polygon object
    private lateinit var adapter: PagerAdapter
    //private var circle: Circle? = null
    private lateinit var databaseViewModel: DatabaseViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Getting event from location foreground service
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("com.example.pgfapp.LOCATION_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 (API 31) and higher, specify the receiver's export status
            registerReceiver(locationReceiver, filter, RECEIVER_EXPORTED)
        } else {
            // For older versions, just register the receiver as usual
            registerReceiver(locationReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(locationReceiver)
    }

    /*
    Function Name : onCreate
    Parameters    : Bundle savedInstanceState
    Description   : Creates the page layout on start-up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = PagerAdapter(supportFragmentManager,lifecycle)
        binding.viewPager2.adapter = adapter
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.viewPager2.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    // Function to check if location permissions are granted
    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Get the users fine (exact) location using FusedLocationProvider
    private fun getCurrentLocation() {
        // Check for permission again in case it was granted after the initial check
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Request current location
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                if (location != null) {
                    // Use the location data here
                    val latLng = LatLng(location.latitude, location.longitude)

                    // Move the camera to the user's location and add a marker
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20f))
                }
            })
    }

    // Handle permission request result (for runtime permissions)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, get location
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
    Function Name : onMapReady
    Parameters    : GoogleMap googleMap
    Description   : Creates and displays the map to the user,
                    Grabs and displays the border the user currently has selected,
                    Gets the updated location of the pet from the CoAP server
    */
    override fun onMapReady(googleMap: GoogleMap){


        //initialize the google map
        mMap = googleMap
        // Check for permission again in case it was granted after the initial check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }


        val userTheme = getCurrentThemeMode()
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this,
                    getMapStyleResource(userTheme)
                )
            )
            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapsActivity", "Can't find style. Error: ", e)
        }


        //pull boundaries from the database
        grabBorder()

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //check if the user has their location services enabled
        if (hasLocationPermissions()) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, locationPermissions, 1)
        }

        //keep map in place when pet is within boundaries by disabling gesture controls
        mMap.getUiSettings().setScrollGesturesEnabled(true) //allows for scrolling
        mMap.getUiSettings().setMapToolbarEnabled(true) //map toolbar enabled for accessibility

        //when pet gets out of the boundaries, allow the user to scroll through the map
        // ->code for that goes here

    }

    /*
    Function Name   : getCurrentThemeMode
    Description     : Checks the users current phone theme and returns a string indicating either dark or light mode
    Return Value    : if dark mode is active it returns "dark"
                      if light mode is active it returns "light"
     */
    private fun getCurrentThemeMode(): String {
        // Check if the system is using dark or light mode
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> "dark" // Dark mode is active
            Configuration.UI_MODE_NIGHT_NO -> "light" // Light mode is active
            else -> "light" // Default to light mode
        }
    }

    private fun getMapStyleResource(mode: String): Int {
        // Return the corresponding map style based on the theme mode
        if (mode == "dark") {
             return R.raw.dark_mode_maps // Reference to the dark map style
        } else {
            return R.raw.light_mode_maps // Reference to the light map style
        }
    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val newLocation = it.getParcelableExtra<LatLng>("newLocation")
                val accuracy = it.getStringExtra("accuracy")
                val petIMEI = it.getStringExtra("petIMEI")
                newLocation?.let { location ->
                    accuracy?.let { accuracy ->
                        petIMEI?.let { imei ->
                            runOnUiThread {
                                updateMarkerForPet(location, accuracy, imei)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateMarkerForPet(location: LatLng, accuracy: String, petIMEI: String) {
        try {
            // Remove the previous marker and circle if they exist
            petMarkers[petIMEI]?.remove()
            petCircles[petIMEI]?.remove()

            // Add a new marker at the updated location
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Pet $petIMEI Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.cust_mark))
            )
            petMarkers[petIMEI] = marker

            val circle = mMap.addCircle(
                CircleOptions()
                    .center(location)
                    .radius(accuracy.toDouble())
                    .strokeColor(android.graphics.Color.RED)
                    .fillColor(android.graphics.Color.argb(50, 255, 0, 0)) // Semi-transparent red
            )
            petCircles[petIMEI] = circle

            // Move the camera to the new marker location (zoom as needed)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        } catch (e: Exception) {
            Log.e("MapError", "Error updating marker and circle: ${e.message}", e)
        }
    }

    /*
    Function Name : grabBorder
    Description   : Grabs the boundary from the database
     */
    private fun grabBorder() {
        val user = Firebase.auth.currentUser
        val uid = user?.uid
        if (uid != null) {

            val petsDao = UserDatabase.getDatabase(application).PetsDao() // Access the DAO

            databaseViewModel.grabActiveBorder(uid).observe(this, Observer { activeBorders ->
                if (activeBorders.isNotEmpty()) {
                    // Access the points from the first active border (assuming only one active border)
                    val activeBorderPoints = activeBorders[0].boarder

                    // Draw the polygon if there are points
                    if (activeBorderPoints.isNotEmpty()) {
                        drawPolygon(activeBorderPoints)

                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // Fetch all pets for the given UUID
                                val pets = petsDao.grabPetsSync(uid)

                                pets.forEach { pet ->
                                    val petIMEI = pet.IMEI
                                    Log.i("MapsActivity", "SENDING NEW BORDER TO: $petIMEI")

                                    // Construct the CoAP URI dynamically for the device
                                    val uri = "coap://15.204.232.135:5683/${petIMEI}/boundary"

                                    // Send the coordinates to the CoAP server
                                    CoapUtils.sendCoordinates(uri, activeBorderPoints, this)
                                }
                            } catch (e: Exception) {
                                Log.e("MapsActivity", "Error while sending borders: ${e.message}")
                            }
                        }
                    }
                } else {
                    polygon?.remove()
                    // Handle case when there is no active border
                    //Toast.makeText(this, "No active border found", Toast.LENGTH_SHORT).show()

                    val emptyBounds = ArrayList<LatLng>()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Fetch all pets for the given UUID
                            val pets = petsDao.grabPetsSync(uid)

                            pets.forEach { pet ->
                                val petIMEI = pet.IMEI
                                Log.i("MapsActivity", "SENDING NEW BORDER TO: $petIMEI")

                                // Construct the CoAP URI dynamically for the device
                                val uri = "coap://15.204.232.135:5683/${petIMEI}/boundary"

                                // Send the coordinates to the CoAP server
                                CoapUtils.sendCoordinates(uri, emptyBounds, this)
                            }
                        } catch (e: Exception) {
                            Log.e("MapsActivity", "Error while sending borders: ${e.message}")
                        }
                    }
                }
            })
        }
    }


    //METHODS THAT DEAL WITH THE POLYGON DRAWING
    /*
    Function Name : drawPolygon
    Parameters    : List<LatLng> latLngList
    Purpose       : Draw the boundary based on the user-input
    */
    fun drawPolygon(latLngList: List<LatLng>) {
        // If a polygon already exists, remove it
        polygon?.remove()

        // Set up polygon options
        val polygonOptions = PolygonOptions()
            .addAll(latLngList)
            .strokeColor(android.graphics.Color.RED)
            .fillColor(android.graphics.Color.argb(50, 255, 0, 0))

        // Add the polygon to the map
        polygon = mMap.addPolygon(polygonOptions)

        // Ensure the map view fits within the bounds of the polygon
        fitBounds(latLngList)
    }

    /*
    Function Name : updatePolygon
    Description   : Clears the boundary point arraylist and then
                repopulates it with the updated boundary points.
    */
    fun updatePolygon(){
        // Update polygon points with current marker positions
        bounds.clear()
        for(marker in markers){
            if(marker != null){
                bounds.add(marker.position)
            }
        }
    }

    /*
    Function Name : fitBounds
    Parameters    : List<LatLng> latLngList
    Purpose       : Adjusts the map view to fit the boundary of the given LatLng points
    */
    private fun fitBounds(latLngList: List<LatLng>) {
        if (latLngList.isNotEmpty()) {
            // Set up the boundary builder
            val boundsBuilder = LatLngBounds.Builder()

            // Include each point in the boundary
            latLngList.forEach { point ->
                boundsBuilder.include(point)
            }

            // Build the boundary
            val bounds = boundsBuilder.build()

            // Adjust the map view with a bit of padding around the polygon
            val padding = 100 // Adjust as necessary
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    }

    //METHODS THAT DEAL WITH HIDING/REVEALING UI ELEMENTS
    /*
    Function Name : hideButtons
    Description   : Hides the all the buttons and UI elements belonging to the main maps page
     */
    /*@SuppressLint("UseSwitchCompatOrMaterialCode")
    fun hideButtons(){
        //hide all the buttons and switches
        val settingsBtn = findViewById<View>(R.id.settings_button) as ImageButton
        settingsBtn.visibility = View.GONE
        val addBtn = findViewById<View>(R.id.add_button) as ImageButton
        addBtn.visibility = View.GONE
        val petsBtn = findViewById<View>(R.id.pets) as ImageButton
        petsBtn.visibility = View.GONE
        val editBoundsBtn = findViewById<View>(R.id.edit_bounds_button) as ImageButton
        editBoundsBtn.visibility = View.GONE
        val boundaryToggle = this.findViewById<View>(R.id.simpleSwitch) as Switch
        boundaryToggle.visibility = View.GONE

        //hide all the images
        val infoTab = findViewById<View>(R.id.boundary_info_tab) as ImageView
        infoTab.visibility = View.GONE

        //hide all the text
        val boundsTxt = findViewById<View>(R.id.boundsTxt) as TextView
        boundsTxt.visibility = View.GONE
        val dashLine = findViewById<View>(R.id.dashLine) as TextView
        dashLine.visibility = View.GONE
        val nameOfBound = findViewById<View>(R.id.boundsName) as TextView
        nameOfBound.visibility = View.GONE
        val dividerBar = findViewById<View>(R.id.bar) as TextView
        dividerBar.visibility = View.GONE
        val dividerBar2 = findViewById<View>(R.id.bar2) as TextView
        dividerBar2.visibility = View.GONE
    }

    /*
    Function Name : unHideButtons
    Description   : un-hides the all the buttons and UI elements belonging to the main maps page
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun unHideButtons(v: View?){
        //un-hide all the buttons and switches
        val settingsBtn = findViewById<View>(R.id.settings_button) as ImageButton
        settingsBtn.visibility = View.VISIBLE
        val addBtn = findViewById<View>(R.id.add_button) as ImageButton
        addBtn.visibility = View.VISIBLE
        val petsBtn = findViewById<View>(R.id.pets) as ImageButton
        petsBtn.visibility = View.VISIBLE
        val editBoundsBtn = findViewById<View>(R.id.edit_bounds_button) as ImageButton
        editBoundsBtn.visibility = View.VISIBLE
        val boundaryToggle = this.findViewById<View>(R.id.simpleSwitch) as Switch
        boundaryToggle.visibility = View.VISIBLE

        //un-hide all the images
        val infoTab = findViewById<View>(R.id.boundary_info_tab) as ImageView
        infoTab.visibility = View.VISIBLE

        //un-hide all the text
        val boundsTxt = findViewById<View>(R.id.boundsTxt) as TextView
        boundsTxt.visibility = View.VISIBLE
        val dashLine = findViewById<View>(R.id.dashLine) as TextView
        dashLine.visibility = View.VISIBLE
        val nameOfBound = findViewById<View>(R.id.boundsName) as TextView
        nameOfBound.visibility = View.VISIBLE
        val dividerBar = findViewById<View>(R.id.bar) as TextView
        dividerBar.visibility = View.VISIBLE
        val dividerBar2 = findViewById<View>(R.id.bar2) as TextView
        dividerBar2.visibility = View.VISIBLE

        //hide the buttons we don't need
        val backArrow = findViewById<View>(R.id.back) as ImageButton
        backArrow.visibility = View.GONE
        val checkMark = findViewById<View>(R.id.check) as ImageButton
        checkMark.visibility = View.GONE

        onBackArrow(v)
    }

*/

    //METHODS THAT CONTROL WHAT A BUTTON DOES
    /*
    Function Name   : onBackArrow
    Parameters      : View v
    Description     : Clears the map of all markers/polygons/things we don't need,
                      Clears the markers mutable List,
                      and grabs the most up to date border
     */
    private fun onBackArrow(v: View?){
        mMap.clear()
        markers.clear()
        grabBorder()
    }



    /*
    Function Name : editBounds
    Parameters    : View v - the current activity view
    Description   : Hides the UI elements we do not currently need,
                    Makes necessary UI elements visible,
                    and then allows the user to edit the selected boundary.
     */
/*    fun editBounds(v: View?){
        hideButtons() //hide all the buttons

        //make the needed buttons visible
        val backBtn = findViewById<View>(R.id.back) as ImageButton
        backBtn.visibility = View.VISIBLE
        val checkBtn = findViewById<View>(R.id.check) as ImageButton
        checkBtn.visibility = View.VISIBLE

        //loop to get each marker
        var count = 1
        for(point in bounds){
            marker = mMap.addMarker(MarkerOptions().position(point).title("Point $count").icon(BitmapDescriptorFactory.fromResource(R.mipmap.cust_mark)).draggable(true))
            markers.add(marker)
            count += 1
        }

        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
            }

            override fun onMarkerDrag(marker: Marker) {
                updatePolygon()
            }

            override fun onMarkerDragEnd(marker: Marker) {
                // Update location when drag ends
                updatePolygon()
                drawPolygon(bounds)
            }
        })
    }
*/
    /*
    Function Name : switchFunctionality
    Parameters    : View v
    Description   : The Switch Functionality
     */
/*    fun switchFunctionality(v: View?){
        switch = findViewById<View>(R.id.simpleSwitch) as Switch

        if(switch.isChecked){
            //make the polygon visible
            polygon?.isVisible = true
        }
        else{
            //make the polygon invisible
            polygon?.isVisible = false
        }

        // on below line we are adding check change listener for our switch.
        switch.setOnCheckedChangeListener { Switch, isChecked ->
            // on below line we are checking
            // if switch is checked or not.
            if (isChecked) {
                //make the polygon visible
                polygon?.isVisible = true
            } else {
                //make the polygon invisible
                polygon?.isVisible = false
            }
        }
    }
*/

    // METHODS THAT SEND THE USER TO A DIFFERENT ACTIVITY
    /*
    Function Name : gotoHub
    Parameters    : View v
    Description   : sends the user to the settings hub
    */
    fun gotoHub(v: View?) {
        startActivity(Intent(this@MapsActivity, HubActivity::class.java))
    }

    /*
    Function Name : gotoDrawBounds
    Parameters    : View v
    Description   : Pauses the CoAP server and
                    Sends the user to the draw boundaries activity
    */
    fun gotoDrawBounds(v: View?){
        onPause()
        startActivity(Intent(this@MapsActivity, BoundsActivity::class.java))
    }
}