package com.example.pgfapp

import CoapUtils
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pgfapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Purpose: Allows user to view maps

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var boundsAct: BoundsActivity
    private var currentMarker: Marker? = null
    private var polygon: Polygon? = null //polygon object

    /*
    Function Name: onCreate
    Parameters: Bundle savedInstanceState
    Description: Creates the page layout on start-up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        /*//coap server stuff
        Log.d("CoapUtils", "Starting Observed Stuff")
        val uri = "coap://californium.eclipseprojects.io/obs-pumping-non"
        CoapUtils.observeCoapResource(uri, lifecycleScope)*/
    }

    override fun onPause() {
        super.onPause()
        // Your custom code here
        // For example, pause a video, save data, or release resource
        Log.d("ActivityLifecycle", "onPause called")
        CoapUtils.cancelObserveCoapResource()
    }

    /*
    Function Name: onMapReady
    Parameters: GoogleMap googleMap
    Description: Creates and displays the map to the user
    */
    override fun onMapReady(googleMap: GoogleMap){
        //initialize the google map
        mMap = googleMap
        //pull boundaries from the database
        // ->code for that goes here
        grabBoarder()

        /*Sample location
        val sampleYard = LatLng(39.7625051, -75.9706618)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleYard, 20f))*/

        //keep map in place when pet is within boundaries by disabling gesture controls
        mMap.getUiSettings().setScrollGesturesEnabled(true) //allows for scrolling
        mMap.getUiSettings().setZoomGesturesEnabled(false) //does not allow for zooming
        mMap.getUiSettings().setMapToolbarEnabled(true) //map toolbar enabled for accessibility

        // Get updated location
        Log.d("CoapUtils", "Starting Observed Stuff")
        val uri = "coap://15.204.232.135:5683/batch"
        CoapUtils.observeCoapResource(uri, lifecycleScope) { newLocation ->
            Log.d("MainActivity", "New Location Observed!")
            Log.d("MainActivity", newLocation)
            // Update the UI or handle the new location as needed

            val jsonObject = JSONObject(newLocation)
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val newLatLng = LatLng(latitude, longitude)

            // Update marker on map
            runOnUiThread {
                updateMarker(newLatLng)
            }
        }

        //when pet gets out of the boundaries, allow the user to scroll through the map
        // ->code for that goes here

        //display the boundary if it exists
        // ->code for that goes here

    }

    /*
    Function Name: gotoHub
    Parameters: View v
    Description: sends the user to the hub activity
     */
    fun gotoHub(v: View?) {
        startActivity(Intent(this@MapsActivity, HubActivity::class.java))
    }

    /*
    Function Name: gotoDrawBounds
    Parameters: View v
    Description: Sends the user to the draw boundaries activity
    */
    fun gotoDrawBounds(v: View?){
        startActivity(Intent(this@MapsActivity, BoundsActivity::class.java))
    }

    /*
    Function Name: testGetCoapReq
    Parameters: View v
    Description: tests getting the coap request
     */
    fun testGetCoapReq(v: View?) {
        //CoapUtils.onSendCoapGetRq(lifecycleScope)
        Log.d("Ignore", "ignore")
    }

    fun grabBoarder() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Boarder").document("5THHdolYzs3uJm1VxVh0")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val boarderName = document.getString("Boarder Name")
                    val geoPoints = document.get("GeoFence Points") as? List<GeoPoint>
                    val uuid = document.getString("UUID")
                    if (geoPoints != null) {
                        // Use map to convert GeoPoints to LatLng and store in ArrayList
                        val latLngList = ArrayList(geoPoints.map { geoPoint ->
                            LatLng(geoPoint.latitude, geoPoint.longitude)
                        })
                        latLngList.forEachIndexed { index, latLng ->
                            Log.d("MapsActivity", "GeoPoint $index: Latitude = ${latLng.latitude}, Longitude = ${latLng.longitude}")
                        }
                    drawPolygon(latLngList)
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
Function Name : drawPolygon
Parameters    : N/A
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
    Function Name : fitBounds
    Parameters    : List<LatLng> (latLngList)
    Purpose       : Adjusts the map view to fit the boundary of the given LatLng points
    */
    fun fitBounds(latLngList: List<LatLng>) {
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
    Function Name: updateMarker
    Parameters: LatLng newLocation
    Description: updates the marker in terms of the current location
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 20f))
        } catch (e: Exception) {
            Log.e("MapError", "Error creating marker: ${e.message}", e)
        }
    }

    fun toEditBounds(v: View?){
        startActivity(Intent(this@MapsActivity, EditBoundsActivity::class.java))
    }
}