package com.example.pgfapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfapp.databinding.ActivityBoundsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import java.security.AccessController.getContext


class BoundsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityBoundsBinding
    private lateinit var opts: GoogleMapOptions
    private lateinit var Bound1: LatLng
    private lateinit var Bound2: LatLng
    private lateinit var Bound3: LatLng
    private lateinit var Bound4: LatLng
    private lateinit var Bound5: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoundsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        //initialize the obj's
        mMap = googleMap

        //disable map controls so that the user can properly input their boundary coordinates
        mMap.getUiSettings().setScrollGesturesEnabled(false)
        mMap.getUiSettings().setZoomGesturesEnabled(false)
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false)

        /*this bit of code here just zooms in on the sample location we're using*/
        /*if you want, you can change it to be your backyard or another area*/
        //sample placement of the yard
        val sampleYard = LatLng(39.7625051, -75.9706618)
        //focuses the camera on a single area
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleYard, 20f))

        //probably need some kind of click listener => look it up in the documentation

    }

}


