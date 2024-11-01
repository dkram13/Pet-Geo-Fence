package com.example.pgfapp

import CoapUtils
import androidx.lifecycle.lifecycleScope
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.example.pgfapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.eclipse.californium.core.CoapClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.json.JSONObject


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Purpose: Allows user to view maps

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var boundsAct: BoundsActivity
    private var currentMarker: Marker? = null

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


        /*this bit of code here just zooms in on the sample location we're using*/
        /*if you want, you can change it to be your backyard or another area*/
        //sample placement of the yard
        val sampleYard = LatLng(39.7625051, -75.9706618)
        //focuses the camera on a single area
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleYard, 20f))

        //keep map in place when pet is within boundaries by disabling gesture controls
        mMap.getUiSettings().setScrollGesturesEnabled(false)
        mMap.getUiSettings().setZoomGesturesEnabled(false)
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false)
        mMap.getUiSettings().setMapToolbarEnabled(false)

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
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation))
        } catch (e: Exception) {
            Log.e("MapError", "Error creating marker: ${e.message}", e)
        }
    }
}