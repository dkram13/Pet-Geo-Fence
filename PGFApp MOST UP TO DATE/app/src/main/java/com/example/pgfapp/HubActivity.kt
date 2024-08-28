package com.example.pgfapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pgfapp.databinding.ActivityHubBinding

class HubActivity : AppCompatActivity() {

    //PURPOSE: Act as the hub that allows the user to navigate
    //         through the various pages

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHubBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHubBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    /*
    Function Name: backToMaps
    Parameters: View v
    Description: Sends the user back to the maps page
     */
    fun backToMaps(v: View?){
        startActivity(Intent(this@HubActivity, MapsActivity::class.java))
    }

    /*
    Function Name: goToLocHist
    Parameters: View v
    Description: Sends the user to the location history page
     */
    fun goToLocHist(v: View?){
        startActivity(Intent(this@HubActivity, LocationHistory::class.java))
    }


    //logs out the user
    fun logout(v: View?){
        startActivity(Intent(this@HubActivity, MainActivity::class.java))
    }

}