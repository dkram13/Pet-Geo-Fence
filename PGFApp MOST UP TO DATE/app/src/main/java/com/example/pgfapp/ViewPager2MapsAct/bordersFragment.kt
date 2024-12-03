package com.example.pgfapp.ViewPager2MapsAct

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pgfapp.BoundsActivity
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class bordersFragment : Fragment() {
    private lateinit var databaseViewModel: DatabaseViewModel
    private var activeToggle: Switch? = null
    private lateinit var toggleListener: CompoundButton.OnCheckedChangeListener
    private lateinit var buttonContainer: LinearLayout
    private var uuid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
        uuid = Firebase.auth.currentUser?.uid
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_borders, container, false)
        buttonContainer = view.findViewById(R.id.button_container)

        setupToggleListener()
        return view
    }

    override fun onResume() {
        super.onResume()

        // Reset activeToggle to ensure the correct one is active based on the database
        activeToggle = null

        // Repopulate the borders
        populateBorders()
    }

    private fun setupToggleListener() {
        toggleListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            Log.d("ToggleListener", "buttonView is: ${buttonView.javaClass.simpleName}, tag: ${buttonView.tag}")
            val boundId = buttonView.tag as? Int
            Log.d("ToggleListener", "Updating boundId: $boundId, isActive: $isChecked")

            if (boundId != null) {
                lifecycleScope.launch {
                    databaseViewModel.updateIsActive(boundId, isChecked)
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
                                databaseViewModel.updateIsActive(it, false)
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
                //Toast.makeText(context, "Boundary ${buttonView.text} is now ON", Toast.LENGTH_SHORT).show()
            } else {
                // When a switch is turned off, set activeToggle to null and update the database for this switch
                if (activeToggle == buttonView) {
                    activeToggle = null
                }
            }
        }
    }

    private fun populateBorders() {
        val userUuid = uuid ?: return

        // Observe and populate the UI with borders
        databaseViewModel.grabBorders(userUuid).observe(viewLifecycleOwner) { borderButtons ->
            buttonContainer.removeAllViews()

            borderButtons.forEach { bounds ->
                val parentLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 0, 8) // Add vertical spacing between rows
                    }
                    background = ContextCompat.getDrawable(requireContext(), R.drawable.background) // Replace with your color
                    setPadding(4, 4, 4, 4) // Padding for aesthetics
                }

                val buttonRowLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        110//LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val nameButton = Button(requireContext()).apply {
                    text = bounds.BoundName
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        3f
                    )
                    setBackgroundColor(Color.TRANSPARENT) // Makes the button blend in with the bar
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // Adjust text color
                    gravity = Gravity.CENTER
                    //textSize = 16f
                    setOnClickListener {
                        //Toast.makeText(requireContext(), "Editing: ${bounds.BoundName}", Toast.LENGTH_SHORT).show()
                    }
                }
                val deleteButton = ImageView(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    setImageResource(R.drawable.deletebutton) // Replace with your drawable resource
                    setPadding(8, 8, 8, 8) // Optional: Add padding for aesthetics
                    setBackgroundResource(android.R.drawable.btn_default) // Optional: Add button-like background
                    adjustViewBounds = true // Maintain aspect ratio
                    scaleType = ImageView.ScaleType.CENTER_INSIDE // Scale the image inside the bounds
                    isClickable = true // Make it clickable if needed
                    isFocusable = true // Enable focus for accessibility
                    setBackgroundColor(Color.TRANSPARENT) // Blend with the bar
                    //setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // Optional: Use a distinct color
                    //gravity = Gravity.CENTER
                    setOnClickListener {
                        // Show a confirmation dialog before deleting
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Confirmation")
                            .setMessage("Are you sure you want to delete this boundary?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                // Perform the delete operation
                                lifecycleScope.launch {
                                    //Toast.makeText(requireContext(), "Deleting: ${bounds}", Toast.LENGTH_SHORT).show()
                                    databaseViewModel.deleteBoundUsingID(bounds.UUID, bounds.BoundId)
                                }
                                dialog.dismiss() // Close the dialog
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss() // Close the dialog without doing anything
                            }
                            .create()
                            .show()
                    }
                }

                val toggleSwitch = Switch(requireContext()).apply {
                    tag = bounds.BoundId
                    isChecked = bounds.isActive
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    setOnCheckedChangeListener(toggleListener)

                    // Update activeToggle if this is the active toggle from the database
                    if (bounds.isActive) {
                        activeToggle = this
                    }
                }
                val divider = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        3, LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(4, 0, 4, 0) // Optional: Add margins for spacing
                    }
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black)) // Divider color
                }
                val divider2 = View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        3, LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        setMargins(4, 0, 4, 0) // Optional: Add margins for spacing
                    }
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black)) // Divider color
                }

                buttonRowLayout.addView(nameButton)
                buttonRowLayout.addView(divider)
                buttonRowLayout.addView(deleteButton)
                buttonRowLayout.addView(divider2)
                buttonRowLayout.addView(toggleSwitch)

                parentLayout.addView(buttonRowLayout)
                buttonContainer.addView(parentLayout)
            }

            // Add a "+" button at the end of the list
            val addButton = Button(requireContext()).apply {
                text = "+"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                background = ContextCompat.getDrawable(requireContext(), R.drawable.background)
                setOnClickListener {
                    // Navigate to BordersActivity
                    val intent = Intent(requireContext(), BoundsActivity::class.java)
                    startActivity(intent)
                }
            }

            // Add the "+" button to the container
            buttonContainer.addView(addButton)
        }
    }
}