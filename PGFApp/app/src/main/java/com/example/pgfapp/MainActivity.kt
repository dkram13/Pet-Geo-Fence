package com.example.pgfapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {


    private lateinit var emailInput: EditText
    private lateinit var pwdInput: EditText
    private lateinit var loginButton: Button
    private lateinit var auth: FirebaseAuth
    //init FBA



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailInput = findViewById(R.id.username_input)
        pwdInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        auth = Firebase.auth

        loginButton.setOnClickListener{
            val email = emailInput.text.toString()
            val pwd = pwdInput.text.toString()

            Log.i("Test Credents","Username: $email and Password: $pwd")
        }
    }

    public override fun onStart(){
        super.onStart()
        //checks to see if the user is signed in
        val currentUser = auth.currentUser
        if(currentUser != null){
            recreate()
        }
    }


}