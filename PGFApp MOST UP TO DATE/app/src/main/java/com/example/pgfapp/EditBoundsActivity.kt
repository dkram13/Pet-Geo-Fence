package com.example.pgfapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pgfapp.databinding.ActivityEditBoundsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.firestore.ktx.*

class EditBoundsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityEditBoundsBinding
    private var bounds = ArrayList<LatLng>()
    private var markers = mutableListOf<Marker?>()
    private var polygon: Polygon? = null

    /*
    Function Name: onCreate
    Parameters: Bundle savedInstanceState
    Description: Creates the page layout on start-up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBoundsBinding.inflate(layoutInflater)
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
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //disable the map toolbar
        mMap.getUiSettings().setMapToolbarEnabled(false)

        /*this bit of code here just zooms in on the sample location we're using*/
        /*if you want, you can change it to be your backyard or another area*/
        //sample placement of the yard
        val sampleYard = LatLng(39.7625051, -75.9706618)
        //focuses the camera on a single area
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleYard, 18f))

        //Pull the Boundary Points from firebase
        //boundaries = pullBoundsFromDB()

        //TEMPORARY DATA FOR THE PURPOSES OF ENSURING WE CAN GET SOME LEVEL OF FUNCTIONALITY
        var p1 = LatLng(39.76275438963148, -75.97075134515762)
        val mark1 = mMap.addMarker(MarkerOptions().position(p1).title("Point 1").icon(BitmapDescriptorFactory.fromResource(R.mipmap.cust_mark)).draggable(true))

        var p2 = LatLng(39.76253522168883,-75.97083818167448)
        val mark2 = mMap.addMarker(MarkerOptions().position(p2).title("Point 2").icon(BitmapDescriptorFactory.fromResource(R.mipmap.cust_mark)).draggable(true))

        var p3 = LatLng(39.76261253986582, -75.97046703100204)
        val mark3 = mMap.addMarker(MarkerOptions().position(p3).title("Point 3").icon(BitmapDescriptorFactory.fromResource(R.mipmap.cust_mark)).draggable(true))

        var p4 = LatLng(39.76278934043762, -75.970548838377)
        val mark4 = mMap.addMarker(MarkerOptions().position(p4).title("Point 4").icon(BitmapDescriptorFactory.fromResource(R.mipmap.cust_mark)).draggable(true))

        //ANYTHING BELOW THIS LINE IS TEMPORARY
        bounds = ArrayList<LatLng>()

        bounds.add(0, p1)
        bounds.add(1, p2)
        bounds.add(2, p3)
        bounds.add(3, p4)

        markers.add(0, mark1)
        markers.add(1, mark2)
        markers.add(2, mark3)
        markers.add(3, mark4)
        //ANYTHING BEYOND THIS LINE IS NOT TEMPORARY

        drawBounds()

        mMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
            }

            override fun onMarkerDrag(marker: Marker) {
                updatePolygon()
            }

            override fun onMarkerDragEnd(marker: Marker) {
                // Update location when drag ends
                updatePolygon()
                drawBounds()
            }
        })

    }


    /*
    Function Name: drawBounds
    Description: Draws the boundary
    Parameters: LatLng p1, LatLng p2, LatLng p3, LatLng p4
     */
    private fun drawBounds(){

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
    }

    private fun updatePolygon(){
        // Update polygon points with current marker positions
        bounds.clear()
        for(marker in markers){
            if(marker != null){
                bounds.add(marker.position)
            }
        }

    }

    /*
    Function Name: pullBoundsFromDB
    Description: Pulls the boundary points from firebase
    Parameters:
    Return Value: bounds - the boundary points
     */
    private fun pullBoundsFromDB(bounds: ArrayList<LatLng>): ArrayList<LatLng>{
        //pull from the boundaries
        return bounds
    }


    /*
    Function Name: updateDB
    Description: Sends the updated boundary coordinates to the database
     */
    private fun updateDB(){

    }


}


