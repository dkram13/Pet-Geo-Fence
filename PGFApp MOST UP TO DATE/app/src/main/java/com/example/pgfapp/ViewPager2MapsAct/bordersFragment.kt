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
                Toast.makeText(context, "Boundary ${buttonView.text} is now ON", Toast.LENGTH_SHORT).show()
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
                    )
                    setPadding(10, 10, 10, 10)
                }

                val buttonRowLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val nameButton = Button(requireContext()).apply {
                    text = bounds.BoundName
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    setOnClickListener {
                        Toast.makeText(requireContext(), "Editing: ${bounds.BoundName}", Toast.LENGTH_SHORT).show()
                    }
                }

                val deleteButton = Button(requireContext()).apply {
                    text = "delete"
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    setOnClickListener {
                        Toast.makeText(requireContext(), "Deleting: ${bounds}", Toast.LENGTH_SHORT).show()
                        lifecycleScope.launch {
                            databaseViewModel.deleteBoundUsingID(bounds.UUID, bounds.BoundId)
                        }
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

                buttonRowLayout.addView(nameButton)
                buttonRowLayout.addView(deleteButton)
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
                setOnClickListener {
                    Toast.makeText(requireContext(), "Add new boundary", Toast.LENGTH_SHORT).show()
                    // Navigate to boundary creation screen or implement your logic here
                }
            }

            // Add the "+" button to the container
            buttonContainer.addView(addButton)
        }
    }
}