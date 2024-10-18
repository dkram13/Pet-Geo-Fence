package com.example.pgfapp

import CoapUtils
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
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.eclipse.californium.core.CoapClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Purpose: Allows user to view maps

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var boundsAct: BoundsActivity

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
        //initialize the google map
        mMap = googleMap
        //initialize boundaries
        boundsAct = BoundsActivity()
        //initialize the boundaries




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
        /*if(myBounds != emptyList<LatLng>()) {
            //Toast.makeText(this, "There are 5 points in this array", Toast.LENGTH_SHORT).show()

            boundsAct.drawPolygon()
        }*/


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


    //function to test coap get request
    @Composable
    fun testGetCoapReq(v: View?){
        val coap = CoapUtils()
        var send = coap.OnSendCoapGetRq()

        return send
    }

}