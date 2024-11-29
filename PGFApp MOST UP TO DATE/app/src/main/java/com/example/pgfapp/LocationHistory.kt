package com.example.pgfapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LocationHistory : AppCompatActivity() {

    //PURPOSE: Allow the user to view their location history

    /*
    Function Name   : onCreate
    Parameters      : Bundle savedInstanceState
    Purpose         : Creates the layout of a page upon start-up
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location_history)
    }

    /*
    Function Name   : backToHubAct
    Parameters      : View v
    Description     : Sends the user back to the hub activity
     */
    fun backToHubAct(v: View?){
        startActivity(Intent(this@LocationHistory, HubActivity::class.java))
    }
}