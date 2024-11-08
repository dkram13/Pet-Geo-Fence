package com.example.pgfapp

import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pgfapp.databinding.ActivityBoundsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


//PURPOSE: Allows the User to draw a boundary and save it

class BoundsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap //google map object
    private lateinit var binding: ActivityBoundsBinding //view binding object
    private var bounds = ArrayList<LatLng>() //array list of latitude longitude points
    private var polygon: Polygon? = null //polygon object
    private var index: Int = 1

    /*
    Function Name : onCreate
    Parameters    : Bundle savedInstanceState
    Purpose       : Creates the layout of a page upon start-up
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
    Function Name : onMapReady
    Parameters    : GoogleMap googleMap
    Purpose       : Display the map to the user
    */
    override fun onMapReady(googleMap: GoogleMap) {
        //initialize the object
        mMap = googleMap

        //disable gesture controls for a smoother experience
        //->we wouldn't want the user to accidentally move the camera
        mMap.getUiSettings().setScrollGesturesEnabled(false)
        mMap.getUiSettings().setZoomGesturesEnabled(false)
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false)
        mMap.getUiSettings().setMapToolbarEnabled(false)

        /*this bit of code here just zooms in on the sample location we're using*/
        /*if you want, you can change it to be your backyard or another area*/
        //sample placement of the yard
        //just make sure the placement of the camera is consistent with its placement
        //in the maps activity
        val sampleYard = LatLng(39.7625051, -75.9706618)
        //focuses the camera on a single area
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sampleYard, 20f))


        //if there are no bounds present
        if(bounds == emptyList<LatLng>()) {
            //When the user "clicks" or "taps" the screen
            mMap.setOnMapClickListener { latLng ->
                bounds.add(latLng)
                mMap.addMarker(MarkerOptions()
                    .position(latLng).
                    title("Boundary Point: $index").
                    icon(BitmapDescriptorFactory.
                    fromResource(R.mipmap.cust_mark)))
                index += 1
                if (bounds.size >= 3) {
                    drawPolygon()
                }
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
    Function Name : fitBounds
    Parameters    : N/A
    Purpose       : Make sure that the boundary fits properly
     */
    public fun fitBounds() {

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

    /*
    Function Name : saveToDB
    Parameters    : View v - the current activity view
                    EditText name - the name of the boundary provided by the user
    Description   : Saves the latitude and longitude coordinates to the database
    bounds is the array list of the plotted coordinates
    */
    fun saveToDB(v: View?, name: EditText?){
        //save to boundaries
        //bounds is the variable arraylist of latitude and longitude
        val db = Firebase.firestore
        val user = Firebase.auth.currentUser
        //val geoPoints = listOf(GeoPoint(bounds))
        val boarderName = name?.text.toString()
        if (user != null) {

            // Send to CoAP TEST
            val uri = "coap://15.204.232.135:5683/boundary"
            CoapUtils.sendCoordinates(uri, bounds, lifecycleScope)

            val uid = user?.uid
            val geoPoints = bounds.map { latLng ->
                GeoPoint(latLng.latitude, latLng.longitude)}
            
            val boarders = hashMapOf(
                "UUID" to uid,
                "Boarder Name" to boarderName,
                "GeoFence Points" to geoPoints
            )
            db.collection("Boarder")
                .add(boarders)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }// User is signed in
        } else {
            // No user is signed in
        }

        if(bounds.size <= 3){
            Toast.makeText(this, "Boundary Must Be 3 or more Points", Toast.LENGTH_SHORT).show()
        }
        else{
            //go to the maps page
            checkToMaps(v)
        }
    }


    /*
    Function Name : onRedo
    Parameters    : View v
    Description   : Removes all the markers from the map
                    and resets the boundary made by the user
    */
    fun onRedo(v: View?){
        mMap.clear()
        bounds.clear()
        index = 1
    }

    /*
    Function Name : checkToMaps
    Parameters    : View v
    Description   : Sends the user to the maps page after when they're done
    */
    private fun checkToMaps(v: View?){
        //don't forget to pass over the list of coordinates
        startActivity(Intent(this@BoundsActivity, MapsActivity::class.java))
    }

    /*
    Function Name: backToMaps
    Parameters: View v
    Description: Sends the user back to the maps page
    */
    fun backToMaps(v: View?){
        startActivity(Intent(this@BoundsActivity, MapsActivity::class.java))
    }

    /*
    Function Name : onCheck
    Parameters    : View v
    Description   : Prompts the user for a boundary name via an AlertDialog
                    If user selects OK:
                        ->saveToDB()
                    If user selects CANCEL:
                        ->AlertDialog closes and nothing is changed
    */
    fun onCheck(v: View?){
        // Create an alert builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Name The Boundary You Just Created")

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.custom_layout, null)
        builder.setView(customLayout)

        // add a button
        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
            // send data from the AlertDialog to the database
            val editText = customLayout.findViewById<EditText>(R.id.editText)
            saveToDB(v, editText)
        }

        builder.setNegativeButton("Cancel"){ dialog: DialogInterface?, which: Int ->
        //close the alertdialog

        }
        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
        }

    }


