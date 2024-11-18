package com.example.pgfapp.ViewPager2MapsAct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.R


class boardersFragment : Fragment() {
    private lateinit var databaseViewModel: DatabaseViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_boarders, container, false)
/*
        // Find the container where buttons will be dynamically added
        val buttonContainer = view.findViewById<LinearLayout>(R.id.button_container)

        // Observe the LiveData from ViewModel
        databaseViewModel.countBounds.observe(viewLifecycleOwner) { boardersList ->
            // Clear existing buttons
            buttonContainer.removeAllViews()

            // Dynamically create buttons for each boarder
            boardersList.forEach { boarder ->
                val button = Button(requireContext()).apply {
                    text = boarder.BoundsName
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setOnClickListener {
                        // Handle button click
                        Toast.makeText(
                            context,
                            "Clicked on: ${boarder.BoundsName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                buttonContainer.addView(button)
            }
        }*/

        return view
    }
}