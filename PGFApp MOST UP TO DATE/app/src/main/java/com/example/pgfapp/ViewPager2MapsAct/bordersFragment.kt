package com.example.pgfapp.ViewPager2MapsAct

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class bordersFragment : Fragment() {
    private lateinit var databaseViewModel: DatabaseViewModel
    private var activeToggle: Switch? = null
    private lateinit var toggleListener: CompoundButton.OnCheckedChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_borders, container, false)
        val buttonContainer = view.findViewById<LinearLayout>(R.id.button_container)
        val user = Firebase.auth.currentUser
        val uuid = user?.uid.toString()

        // Toggle listener to manage the active switch
        toggleListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                activeToggle?.let { previousSwitch ->
                    if (previousSwitch != buttonView) {
                        previousSwitch.setOnCheckedChangeListener(null)
                        previousSwitch.isChecked = false
                        previousSwitch.setOnCheckedChangeListener(toggleListener)
                    }
                }
                activeToggle = buttonView as Switch
                Toast.makeText(context, "Boundary ${buttonView.text} is now ON", Toast.LENGTH_SHORT).show()
            } else {
                if (activeToggle == buttonView) {
                    activeToggle = null
                }
                Toast.makeText(context, "Boundary ${buttonView.text} is now OFF", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch boarder names from the database and populate the view
        databaseViewModel.grabBorders(uuid).observe(viewLifecycleOwner) { borderButtons ->
            buttonContainer.removeAllViews()

            // For each boarder name, create a row with a toggle and an edit button
            borderButtons.forEach { bounds ->
                // Create a parent LinearLayout for each row
                val parentLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    // Optional: Add padding to give some spacing
                    setPadding(10, 10, 10, 10)
                }

                // Create a button row layout (horizontal LinearLayout)
                val buttonRowLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // Create the "Edit" button with layout weight for even distribution
                val nameButton = Button(requireContext()).apply {
                    text = "${bounds.BoundName}"
                    layoutParams = LinearLayout.LayoutParams(
                        0, // No fixed width
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f // Take equal weight in horizontal space
                    )
                    setOnClickListener {
                        // Handle "Edit" button click
                        Toast.makeText(requireContext(), "Editing: $bounds.BoundName", Toast.LENGTH_SHORT).show()
                    }
                }

                // Create the toggle switch with layout weight for even distribution
                val toggleSwitch = Switch(requireContext()).apply {
                    //text = "Toggle ${bounds.BoundId}"
                    tag = bounds.BoundId
                    layoutParams = LinearLayout.LayoutParams(
                        0, // No fixed width
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f // Take equal weight in horizontal space
                    )
                    setOnCheckedChangeListener(toggleListener)
                }

                // Add the "Edit" button and toggle switch to the horizontal layout
                buttonRowLayout.addView(nameButton)
                buttonRowLayout.addView(toggleSwitch)

                // Add the button row layout to the parent layout
                parentLayout.addView(buttonRowLayout)

                // Add the parent layout to the main container
                buttonContainer.addView(parentLayout)
            }
        }

        return view
    }
}