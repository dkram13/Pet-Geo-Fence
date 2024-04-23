package com.example.pgfapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    //PURPOSE:  Allow the user to sign in and/or sign up.
    //          If the sign in is successful,
    //              ->the user is taken to the maps page
    //          If the sign in is unsuccessful,
    //              ->the user is shown an error message
    //          When the user taps the sign up button,
    //              ->They are taken to an account creation page

    //Global variables
    private lateinit var emailInput: EditText //email input field
    private lateinit var pwdInput: EditText //password input field
    private lateinit var loginButton: Button //login button
    private lateinit var auth: FirebaseAuth //firebase authentication

    /*
    Function: OnCreate
    Parameter: savedInstanceState: Bundle? [value]
    Description: This function sets up the instance of this page.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailInput = findViewById(R.id.email_input) //email input
        pwdInput = findViewById(R.id.password_input) //password input
        loginButton = findViewById(R.id.login_button) //login button
        auth = Firebase.auth //firebase authenticator

        //If the user taps the sign in button
        loginButton.setOnClickListener{
            val email = emailInput.text.toString()
            val pwd = pwdInput.text.toString()

            auth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in is successful
                        Log.d("@id/email" + "@id/pwd", "success")
                        val user = auth.currentUser
                        gotoMaps() //sends the user to the maps page
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("", "failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Sign-in failed. Please Try Again",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

            /* remove this for the final build */
            Log.i("Test Credentials","Username: $email and Password: $pwd")
        }
    }

    /*
    Function Name: onStart
    Parameters: None
    Description: checks if a log-in session is active
     */
    public override fun onStart(){
        super.onStart()
        //checks to see if the user is signed in
        val currentUser = auth.currentUser
        //if(currentUser != null) {
            //recreate() //recreate() RECREATES the instance of this page
        //}
        // /\
        // |
        // |
        // Not sure if this should be removed or not
        // I'll just keep it like this for now

    }

    /*
    Function Name: gotoMaps
    Parameters: None
    Description: Sends the user to the maps page
     */
    fun gotoMaps() {
        startActivity(Intent(this@MainActivity, MapsActivity::class.java))
    }


}