package com.example.pgfapp

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth //firebase authentication

    /*
    Function Name: backToLogin
    Parameters: Bundle savedInstanceState
    Description: Creates the page layout upon startup
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /*
    Function Name: backToLogin
    Parameters: View v
    Description: Sends the user back to the login page
     */
    fun backToLogin(v: View?){
        startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
    }


    /*
    Function Name: onNextBtn
    Parameters: View v
    Description: Allows the user to register a new account with a
     */
    fun onNextBtn(v: View?){
        //initialize authenticator
        auth = Firebase.auth

        //get all the user input info
        val emailView = findViewById<View>(R.id.new_email) as EditText
        val pwdInputView = findViewById<View>(R.id.txtpassword) as EditText
        val pwdConfirmView = findViewById<View>(R.id.confirm_pwd) as EditText
        //convert it to readable strings
        val email = emailView.text.toString()
        val pwdInput = pwdInputView.text.toString()
        val pwdConfirm = pwdConfirmView.text.toString()

        //if the email or initial password field are empty
        if(email.isEmpty() || pwdInput.isEmpty()) {
            // Create the alert builder
            val builder = AlertDialog.Builder(this)
            builder.setTitle("You Have Not Entered a Email Address and/or Password")

            // set the custom layout
            val customLayout: View = layoutInflater.inflate(R.layout.custome_layout_2, null)
            builder.setView(customLayout)

            // add a button
            builder.setNegativeButton("Ok") { dialog: DialogInterface?, which: Int ->
                //close the alertdialog
            }
            // create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        }


        val newUser = auth.createUserWithEmailAndPassword(email, pwdInput)
        newUser.addOnCompleteListener { task ->
            if (task.isSuccessful && pwdInput == pwdConfirm) {
                // User registration successful
                val user = auth.currentUser
                val userID = user?.uid
                //send an email for verification
                sendEmailVerification(user)
                //send the user to the sign in page
                startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
            } else {
                // Registration failed
                val errorMessage = task.exception?.message ?: "Failed To Make an Account"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }

    fun sendEmailVerification(user: FirebaseUser?) {
        if (user != null) {
            user.sendEmailVerification()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Email sent successfully
                        Log.d("EmailVerification", "Verification email sent.")
                    } else {
                        // Handle error
                        val errorMessage = "Failed to send verification email: ${task.exception?.message}"
                    }
                }
        } else {
            val errorMessage = "Already Signed In"
        }
    }

}