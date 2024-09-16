package com.example.pgfapp

import android.graphics.Color
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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import org.checkerframework.common.returnsreceiver.qual.This
import java.security.AccessController.getContext

    //PURPOSE: Allows the User to draw a boundary and save it

class BoundsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap //google map object
    private lateinit var binding: ActivityBoundsBinding //view binding object
    public val bounds = mutableListOf<LatLng>() //array of boundary points [size 5]
    public var polygon: Polygon? = null //polygon object

    /*
    Function Name: onCreate
    Parameters: Bundle savedInstanceState
    Purpose: Creates the layout of a page upon start-up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoundsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    /*
    Function Name: onMapReady
    Parameters: GoogleMap googleMap
    Purpose: Display the map to the user
    */
    override fun onMapReady(googleMap: GoogleMap) {
        //initialize the object
        mMap = googleMap

        //disable gesture controls for a smoother experience
        //->we wouldn't want the user to accidentally move the camera
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);

        /*this bit of code here just zooms in on the sample location we're using*/
        /*if you want, you can change it to be your backyard or another area*/
        //sample placement of the yard
        //just make sure the placement of the camera is consistent with its placement
        //in the maps activity
        val sampleYard = LatLng(39.7625051, -75.9706618)
        //focuses the camera on a single area
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleYard, 20f))


        //When the user "clicks" or "taps" the screen
        mMap.setOnMapClickListener { latLng ->
            if (bounds.size < 5) {
                bounds.add(latLng)
                mMap.addMarker(MarkerOptions().position(latLng))
                if (bounds.size == 5) {
                    drawPolygon()
                }
            } else {
                Toast.makeText(this, "Only 5 Points Allowed", Toast.LENGTH_SHORT).show()
            }
        }

    }


    /*
    Function Name : drawPolygon
    Parameters    : N/A
    Purpose       : Draw the boundary based on the user-input
     */
    public fun drawPolygon() {

        //if polygon is not null
        if (polygon != null) {
            //remove it
            polygon?.remove()
        }

        //set up the polygon drawing
        val polygonOptions = PolygonOptions()
            .addAll(bounds)
            .strokeColor(android.graphics.Color.RED)
            .fillColor(android.graphics.Color.argb(50, 255, 0, 0))

        //add the polygon to the map
        polygon = mMap.addPolygon(polygonOptions)

        //ensure it fits within the points of the boundary
        fitBounds()
    }

    /*
    Function Name: fitBounds
    Parameters: N/A
    Purpose: Make sure that the boundary fits properly
     */
    private fun fitBounds() {

        //if the bounds array is not empty
        if (bounds.isNotEmpty()) {
            //set up the boundary builder
            val boundsBuilder = LatLngBounds.Builder()
            //for every latLng point in the bounds array
            for (point in bounds) {
                //build the boundary using that point
                boundsBuilder.include(point)
            }
            //build the entire boundary
            val bounds = boundsBuilder.build()
        }
    }


}


