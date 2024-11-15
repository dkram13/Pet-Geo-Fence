package com.example.pgfapp

import CoapUtils
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.example.pgfapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.json.JSONObject
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build



class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Purpose: Allows user to view maps

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var currentMarker: Marker? = null
    private var bounds = ArrayList<LatLng>()
    private var markers = mutableListOf<Marker?>()
    private lateinit var boundAct: BoundsActivity
    private var locInaccRadius: Double? = null
    private var marker: Marker? = null
    private var polygon: Polygon? = null //polygon object


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

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
        //pull boundaries from the database
        grabBorder()

        /*Sample location
        val sampleYard = LatLng(39.7625051, -75.9706618)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleYard, 20f))*/

        //keep map in place when pet is within boundaries by disabling gesture controls
        mMap.getUiSettings().setScrollGesturesEnabled(true) //allows for scrolling
        mMap.getUiSettings().setMapToolbarEnabled(true) //map toolbar enabled for accessibility

        //when pet gets out of the boundaries, allow the user to scroll through the map
        // ->code for that goes here

    }



    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val newLocation = it.getParcelableExtra<LatLng>("newLocation")
                newLocation?.let { location ->
                    // Update the marker on the UI thread
                    runOnUiThread {
                        updateMarker(location)
                    }
                }
            }
        }
    }


    /*
    Function Name : grabBorder
    Description   : Grabs the boundary from the database
     */
    private fun grabBorder() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Boarder").document("9bJOQc0cPMfoqfuioXRr")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val boarderName = document.getString("Boarder Name")
                    val geoPoints = document.get("GeoFence Points") as? List<GeoPoint>
                    val uuid = document.getString("UUID")
                    if (geoPoints != null) {
                        // Use map to convert GeoPoints to LatLng and store in ArrayList
                        bounds = ArrayList(geoPoints.map { geoPoint ->
                            LatLng(geoPoint.latitude, geoPoint.longitude)
                        })
                        bounds.forEachIndexed { index, latLng ->
                            Log.d("MapsActivity", "GeoPoint $index: Latitude = ${latLng.latitude}, Longitude = ${latLng.longitude}")
                        }
                    drawPolygon(bounds)
                    }
                    /*if (geoPoints != null) {
                        // Loop through each GeoPoint and print its details
                        geoPoints.forEachIndexed { index, geoPoint ->
                            Log.d("FirestoreData", "GeoPoint $index: Latitude = ${geoPoint.latitude}, Longitude = ${geoPoint.longitude}")
                        }
                    } else {
                        Log.d("FirestoreData", "No Geo Points found.")
                    }*/
                }
                else {
                    println("Document does not exist") }
                }
            .addOnFailureListener { exception ->
                // Log the exception for debugging
                println("Error fetching document: ${exception.message}")
            }

    }

    /*
    Function Name : updateMarker
    Parameters    : LatLng newLocation
    Description   : Updates the marker in terms of the current location
     */
    private fun updateMarker(newLocation: LatLng) {
        try {
            // Remove the previous marker, if it exists
            currentMarker?.remove()

            locInaccRadius = 0.00

            // Add a new marker at the updated location
            currentMarker = mMap.addMarker(
                MarkerOptions()
                    .position(newLocation)
                    .title("Observed Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.cust_mark))
            )

            // Move the camera to the new marker location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 20f))
        } catch (e: Exception) {
            Log.e("MapError", "Error creating marker: ${e.message}", e)
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

    /*
    Function Name : updateMarker
    Parameters    : LatLng newLocation
    Description   : Updates the marker in terms of the current location
     */
    private fun updateMarker(newLocation: LatLng) {
        try {
            // Remove the previous marker, if it exists
            currentMarker?.remove()

            // Add a new marker at the updated location
            currentMarker = mMap.addMarker(
                MarkerOptions()
                    .position(newLocation)
                    .title("Observed Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.cust_mark))
            )

            // Move the camera to the new marker location
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 20f))
        } catch (e: Exception) {
            Log.e("MapError", "Error creating marker: ${e.message}", e)
        }
    }

    //METHODS THAT DEAL WITH HIDING/REVEALING UI ELEMENTS
    /*
    Function Name : hideButtons
    Description   : Hides the all the buttons and UI elements belonging to the main maps page
     */
    @SuppressLint("UseSwitchCompatOrMaterialCode")
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
    fun editBounds(v: View?){
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


    // METHODS THAT SEND THE USER TO A DIFFERENT ACTIVITY
    /*
    Function Name : gotoHub
    Parameters    : View v
    Description   : sends the user to the settings
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