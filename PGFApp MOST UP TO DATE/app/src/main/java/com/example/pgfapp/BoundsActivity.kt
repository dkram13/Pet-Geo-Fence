package com.example.pgfapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.firestore.ktx.*
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
                if (bounds.size < 4) {
                    bounds.add(latLng)
                    mMap.addMarker(MarkerOptions()
                        .position(latLng).
                        title("Boundary Point: $index").
                        icon(BitmapDescriptorFactory.
                        fromResource(R.drawable.custom_marker)))
                    index += 1
                    if (bounds.size == 4) {
                        drawPolygon()
                    }
                } else {
                    Toast.makeText(this, "Only 4 Points Allowed", Toast.LENGTH_SHORT).show()
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
    Parameters    : View v
    Description   : Saves the latitude and longitude coordinates to the database
    bounds is the list of coordiants
    */
    fun saveToDB(v: View?){
        //save to boundaries
        //bounds is the varial list of latitude and longitude
        val db = Firebase.firestore
        val user = Firebase.auth.currentUser
        //val geoPoints = listOf(GeoPoint(bounds))

        if (user != null) {
            val uid = user?.uid
            val geoPoints = bounds.map { latLng ->
                GeoPoint(latLng.latitude, latLng.longitude)}
            
            val usertg = hashMapOf(
                "UUID" to uid,
                "last" to "this is test 2 of getting uid",
                "GeoFence Points" to geoPoints
            )
            db.collection("userstg")
                .add(usertg)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }// User is signed in
        } else {
            // No user is signed in
        }

        /*
        val saveBoarder = hashMapOf(
            "Name" to       // need to find a way to change name based on the boarders a user has
            "point 1" to bounds[0]      // need to find a way to iterate over a list of tuples of geopoint locations
            "point 2" to bounds[1]
            "point 3" to bounds[2]
            "point 4" to bounds[3]
            "point 5" to bounds[4]
            "UUID" to                   // need to find a way to get uuid for user based on who is logged in.
        )
        db.collection("Boarder")
            .add(saveBoarder)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
        */

        if(bounds.size != 4){
            Toast.makeText(this, "Boundary Must Be 4 Points", Toast.LENGTH_SHORT).show()
        }

        if(bounds.size == 4) {
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
    fun checkToMaps(v: View?){
        //don't forget to pass over the list of coordinates
        startActivity(Intent(this@BoundsActivity, MapsActivity::class.java))
    }

}


