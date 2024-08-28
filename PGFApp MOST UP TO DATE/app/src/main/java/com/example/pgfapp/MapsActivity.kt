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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Purpose: Allows user to view maps

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    val Loc1 = LatLng(39.7625334, -75.9705679)
    val Loc2 = LatLng(39.7624790, -75.9704892)
    val Loc3 = LatLng(39.7624839, -75.9706199)


    /*
    Function Name: onCreate
    Parameters: Bundle savedInstanceState
    Description: This function makes the page elements viewable
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

    //
    //Function Name: onMapReady
    //Parameters: GoogleMap googleMap
    //Description: This function displays the map and the markers
    //
    override fun onMapReady(googleMap: GoogleMap){
        mMap = googleMap

        mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc1))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))

        //first run
        val handler = Handler()
        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc1).title("Location 1"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc1))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 5000)


        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc2).title("Location 2"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc2))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 10000)


        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc3).title("Location 3"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 15000)


        handler.postDelayed({
            mMap.clear()
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 20000)


        //second run
        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc1).title("Location 1"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc1))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 25000)

        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc2).title("Location 2"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc2))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 30000)

        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc3).title("Location 3"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 35000)

        handler.postDelayed({
            mMap.clear()
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 40000)

        //third run
        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc1).title("Location 1"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc1))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 45000)

        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc2).title("Location 2"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc2))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 50000)

        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc3).title("Location 3"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 55000)

        handler.postDelayed({
            mMap.clear()
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 60000)

        //fourth run
        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc1).title("Location 1"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc1))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 65000)

        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc2).title("Location 2"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc2))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 70000)

        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc3).title("Location 3"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 75000)

        handler.postDelayed({
            mMap.clear()
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 80000)

        //fifth run
        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc1).title("Location 1"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc1))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 85000)

        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc2).title("Location 2"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc2))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 90000)

        handler.postDelayed({
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(Loc3).title("Location 3"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(Loc3))
            mMap.moveCamera(CameraUpdateFactory.zoomTo(100.0f))
        }, 95000)


    }



    /*
    Function Name: gotoHub
    Parameters: View v
    Description: sends the user to the hub activity
     */
    fun gotoHub(v: View?) {
        startActivity(Intent(this@MapsActivity, HubActivity::class.java))
    }
}