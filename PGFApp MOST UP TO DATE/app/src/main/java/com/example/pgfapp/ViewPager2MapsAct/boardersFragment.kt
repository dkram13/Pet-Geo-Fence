package com.example.pgfapp.ViewPager2MapsAct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class boardersFragment : Fragment() {
    private lateinit var databaseViewModel: DatabaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_boarders, container, false)

        // Find the container where buttons will be dynamically added
        val buttonContainer = view.findViewById<LinearLayout>(R.id.button_container)
        val user = Firebase.auth.currentUser
        val uuid = user?.uid.toString()
        // Fetch UUID (you'll need to provide a UUID to filter the boarders)
        //val uuid = "your-uuid-value" // Replace this with the actual UUID logic

        // Observe the LiveData from ViewModel
        databaseViewModel.grabBoarderNames(uuid).observe(viewLifecycleOwner) { boarderNames ->
            // Clear existing buttons
            buttonContainer.removeAllViews()

            // Dynamically create buttons for each boarder name
            boarderNames.forEach { name ->
                val button = Button(requireContext()).apply {
                    text = name // Set button text to the boarder name
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        // Handle button click
                        Toast.makeText(
                            context,
                            "Clicked on: $name",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                buttonContainer.addView(button)
            }
        }

        return view
    }
}