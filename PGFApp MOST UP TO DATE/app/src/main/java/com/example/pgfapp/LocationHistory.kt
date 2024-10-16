package com.example.pgfapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LocationHistory : AppCompatActivity() {

    //PURPOSE: Allow the user to view their location history

    /*
    Function Name: onCreate
    Parameters: Bundle savedInstanceState
    Purpose: Creates the layout of a page upon start-up
    */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location_history)
    }
}