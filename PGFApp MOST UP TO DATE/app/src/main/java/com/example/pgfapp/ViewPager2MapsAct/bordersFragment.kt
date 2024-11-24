package com.example.pgfapp.ViewPager2MapsAct

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

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
            Log.d("ToggleListener", "buttonView is: ${buttonView.javaClass.simpleName}, tag: ${buttonView.tag}")
            val boundId = buttonView.tag as? Int
            Log.d("ToggleListener", "Updating boundId: $boundId, isActive: $isChecked")

            if (boundId != null) {
                lifecycleScope.launch {
                    // Update the database for the current toggle
                    databaseViewModel.updateIsActive(boundId, isChecked) // Set isActive for the current boundId
                }
            }

            if (isChecked) {
                // If a switch is turned on, deactivate the previous active toggle
                activeToggle?.let { previousSwitch ->
                    if (previousSwitch != buttonView) {
                        Log.d("ToggleListener", "Turning off the previous switch: ${previousSwitch.tag}")
                        // Update the database to set isActive to false for the previously active bound
                        val previousBoundId = previousSwitch.tag as? Int
                        previousBoundId?.let {
                            lifecycleScope.launch {
                                databaseViewModel.updateIsActive(it, false) // Set isActive = false for previous bound
                            }
                        }
                        // Turn off the previous switch
                        previousSwitch.setOnCheckedChangeListener(null)
                        previousSwitch.isChecked = false
                        previousSwitch.setOnCheckedChangeListener(toggleListener)
                    }
                }

                // Set the current switch as active and update the database for its boundId
                activeToggle = buttonView as Switch
                Toast.makeText(context, "Boundary ${buttonView.text} is now ON", Toast.LENGTH_SHORT).show()
            } else {
                // When a switch is turned off, set activeToggle to null and update the database for this switch
                if (activeToggle == buttonView) {
                    Log.d("ToggleListener", "Turning off the current active switch")
                    activeToggle = null  // Reset active toggle
                    // Update the database to set isActive to false for the current boundId
                    val currentBoundId = buttonView.tag as? Int
                    currentBoundId?.let {
                        lifecycleScope.launch {
                            databaseViewModel.updateIsActive(it, false) // Set isActive = false for this bound
                        }
                    }
                }
                Toast.makeText(context, "Boundary ${buttonView.text} is now OFF", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch boarder names from the database and populate the view
        databaseViewModel.grabBorders(uuid).observe(viewLifecycleOwner) { borderButtons ->
            buttonContainer.removeAllViews()

            borderButtons.forEach { bounds ->
                // Create a parent LinearLayout for each row
                val parentLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
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

                // Create the "Edit" button
                val nameButton = Button(requireContext()).apply {
                    text = "${bounds.BoundName}"
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f // Take equal weight in horizontal space
                    )
                    setOnClickListener {
                        Toast.makeText(requireContext(), "Editing: ${bounds.BoundName}", Toast.LENGTH_SHORT).show()
                    }
                }

                // Create the toggle switch and set its initial state based on the database
                val toggleSwitch = Switch(requireContext()).apply {
                    tag = bounds.BoundId
                    isChecked = bounds.isActive // Set the initial state of the switch based on the database value
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
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