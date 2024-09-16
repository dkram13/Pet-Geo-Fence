package com.example.pgfapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.pgfapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import android.graphics.Color
import com.example.pgfapp.databinding.ActivityBoundsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Purpose: Allows user to view maps

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

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
    }

    /*
    Function Name: onMapReady
    Parameters: GoogleMap googleMap
    Description: Creates and displays the map to the user
    */
    override fun onMapReady(googleMap: GoogleMap){
        mMap = googleMap

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

        //when pet gets out of the boundaries, allow the user to scroll through the map
        // ->code for that goes here

        //display the boundary
        
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

}