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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HubActivity : AppCompatActivity() {

    //PURPOSE: Act as the hub that allows the user to navigate
    //         through the various pages

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHubBinding
    private lateinit var auth: FirebaseAuth

    /*
    Function Name: onCreate
    Parameters: Bundle savedInstanceState
    Purpose: Creates the layout of a page upon start-up
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHubBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get the user currently signed in
        auth = Firebase.auth
        val user = auth.currentUser
        val userID = user?.uid
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

    /*
    Function Name: goToPrivacy
    Parameters: View v
    Description: Sends the user to the privacy policy page
     */
    fun goToPrivacy(v: View?){
        startActivity(Intent(this@HubActivity, PrivacyPolicyActivity::class.java))
    }

    /*
    Function Name: goToTrainGuide
    Parameters: View v
    Description: Sends the user to the Training Guide Page
     */
    fun goToTrainGuide(v: View?){
        startActivity(Intent(this@HubActivity, TrainingGuideActivity::class.java))
    }

    /*
    Function Name: goToHowToUse
    Parameters: View v
    Description: Sends the user to the How To Use Page
     */
    fun goToHowToUse(v: View?){
        startActivity(Intent(this@HubActivity, HowToUseActivity::class.java))
    }

    /*
    Function Name: logout
    Parameters: View v
    Description: Logs out the user and sends them back to the login page
     */
    fun logout(v: View?){
        //log out the user
        auth.signOut()

        //send them back to the sign in page
        startActivity(Intent(this@HubActivity, MainActivity::class.java))
    }

}