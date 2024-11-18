package com.example.pgfapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.DatabaseStuff.Entities.Bounds
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
import com.google.firebase.ktx.Firebase
import kotlin.math.atan2


//PURPOSE: Allows the User to draw a boundary and save it

class BoundsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap //google map object
    private lateinit var binding: ActivityBoundsBinding //view binding object
    private var bounds = ArrayList<LatLng>() //array list of latitude longitude points
    private var polygon: Polygon? = null //polygon object
    private var index: Int = 1
    private lateinit var databaseViewModel: DatabaseViewModel
    /*
    Function Name : onCreate
    Parameters    : Bundle savedInstanceState
    Purpose       : Creates the layout of a page upon start-up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoundsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
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
    // Function to calculate the centroid of the boundary points
    private fun calculateCentroid(points: ArrayList<LatLng>): LatLng {
        val latitude = points.sumOf { it.latitude } / points.size
        val longitude = points.sumOf { it.longitude } / points.size
        return LatLng(latitude, longitude)
    }

    // Function to sort points in clockwise order around the centroid
    private fun sortPointsClockwise(points: ArrayList<LatLng>): ArrayList<LatLng> {
        val centroid = calculateCentroid(points)
        return ArrayList(points.sortedBy { point ->
            atan2(point.latitude - centroid.latitude, point.longitude - centroid.longitude)
        })
    }
    /*
    Function Name : drawPolygon
    Parameters    : N/A
    Purpose       : Draw the boundary based on the user-input
     */
    fun drawPolygon() {
        // Sort points in a clockwise order
        val sortedBounds = sortPointsClockwise(bounds)

        bounds.clear()
        bounds.addAll(sortedBounds)
        // If a polygon already exists, remove it
        polygon?.remove()

        // Set up the polygon options with sorted points
        val polygonOptions = PolygonOptions()
            .addAll(sortedBounds)
            .strokeColor(android.graphics.Color.RED)
            .fillColor(android.graphics.Color.argb(50, 255, 0, 0))

        // Add the polygon to the map
        polygon = mMap.addPolygon(polygonOptions)

        // Fit the polygon within view
        fitBounds()
    }

    /*
    Function Name : fitBounds
    Parameters    : N/A
    Purpose       : Make sure that the boundary fits properly
     */
    fun fitBounds() {

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
    /*fun saveToDB(v: View?, name: EditText?){
        //save to boundaries
        //bounds is the variable arraylist of latitude and longitude
        val db = Firebase.firestore
        val user = Firebase.auth.currentUser
        //val geoPoints = listOf(GeoPoint(bounds))
        val boarderName = name?.text.toString()
        if (user != null) {

            // Send to CoAP TEST
            val uri = "coap://15.204.232.135:5683/boundary"
            com.example.pgfapp.utilities.CoapUtils.sendCoordinates(uri, bounds, lifecycleScope)

            val uid = user?.uid
            val geoPoints = bounds.map { latLng ->
                GeoPoint(latLng.latitude, latLng.longitude)
            }
            
            val boarders = hashMapOf(
                "UUID" to uid,
                "Boarder Name" to boarderName,
                "GeoFence Points" to geoPoints
            )
                .add(boarders)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
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
*/
    fun saveToDB(v: View?, name: EditText?) {
        val boarderName = name?.text.toString()

        // Ensure that the bounds have at least 3 points
        if (bounds.size < 3) {
            Toast.makeText(this, "Boundary Must Be 3 or more Points", Toast.LENGTH_SHORT).show()
            return
        }
        // Convert the boundary points to a list of LatLng (or GeoPoint, depending on how your Room DB is set up)
        /*val geoPoints = bounds.map { latLng ->
            GeoPoint(latLng.latitude, latLng.longitude)
        }*/
        val user = Firebase.auth.currentUser
        val uid = user?.uid
        // Create a Boundary object with the name and points
        val boundary = Bounds(
            UUID = uid ?: "",  // You can generate a UUID or use any unique identifier for this boundary
            BoundsName = boarderName,
            boarder = bounds // Store the list of LatLng points as the boundary
        )

        // Save the boundary to the database via the ViewModel
        databaseViewModel.addBounds(boundary)

        // Go to the maps page after saving the boundary
        checkToMaps(v)
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


