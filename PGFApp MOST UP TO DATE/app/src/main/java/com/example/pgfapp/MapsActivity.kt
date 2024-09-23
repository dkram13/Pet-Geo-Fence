package com.example.pgfapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Purpose: Allows user to view maps

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    public var boundaryPts = mutableListOf<LatLng>() //array of boundary points [size 5]
    public var polygonDraw: Polygon? = null //polygon object

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
        //initialize variables
        mMap = googleMap
        var boundsAct = BoundsActivity()

        //populate boundaryPts


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

        //when pet gets out of the boundaries, allow the user to scroll through the map
        // ->code for that goes here

        //display the boundary if it exists
        if(boundaryPts != emptyList<LatLng>()) {
            //Toast.makeText(this, "There are 5 points in this array", Toast.LENGTH_SHORT).show()
        }


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