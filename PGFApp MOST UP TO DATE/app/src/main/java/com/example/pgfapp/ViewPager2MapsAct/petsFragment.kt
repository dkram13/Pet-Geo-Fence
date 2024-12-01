package com.example.pgfapp.ViewPager2MapsAct

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.DatabaseStuff.Entities.Pets
import com.example.pgfapp.R
import com.example.pgfapp.viewmodels.PetDataViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class petsFragment : Fragment() {
    private lateinit var databaseViewModel: DatabaseViewModel
    private lateinit var petDataViewModel: PetDataViewModel
    private lateinit var petsContainer: LinearLayout
    private var uuid: String? = null

    private val batteryUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.example.pgfapp.BATTERY_UPDATE") {
                val petIMEI = intent.getStringExtra("petIMEI")
                val batteryLevel = intent.getIntExtra("batteryLevel", -1)

                // Call the ViewModel to update the battery level
                if (!petIMEI.isNullOrEmpty()) {
                    petDataViewModel.updateBatteryLevel(petIMEI, batteryLevel)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        petDataViewModel = ViewModelProvider(this)[PetDataViewModel::class.java]
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
        uuid = Firebase.auth.currentUser?.uid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pets, container, false)
        petsContainer = view.findViewById(R.id.pets_container)

        populatePets()

        return view
    }

    override fun onResume() {
        super.onResume()
        //populatePets()


        val filter = IntentFilter("com.example.pgfapp.BATTERY_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12 (API 31) and higher, specify the receiver's export status
            requireContext().registerReceiver(batteryUpdateReceiver, filter, RECEIVER_EXPORTED)
        } else {
            // For older versions, just register the receiver as usual
            requireContext().registerReceiver(batteryUpdateReceiver, filter)
        }

        petDataViewModel.batteryLevels.observe(viewLifecycleOwner) { batteryLevels ->
            // Iterate through the batteryLevels list and update the UI
            batteryLevels.forEach { petBatteryLevel ->
                val batteryLevel = petBatteryLevel.batteryLevel
                val imei = petBatteryLevel.imei
                updateBatteryLevelUI(imei, batteryLevel)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister the battery update receiver to prevent leaks
        requireContext().unregisterReceiver(batteryUpdateReceiver)
    }

    private fun populatePets() {
        val userUuid = uuid ?: return

        // Observe LiveData from the ViewModel
        databaseViewModel.grabPets(userUuid).observe(viewLifecycleOwner) { pets ->
            petsContainer.removeAllViews() // Clear the container before adding new buttons

            pets.forEach { pet ->
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

                // Add a button for the pet name
                val nameButton = Button(requireContext()).apply {
                    text = pet.PetName
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        3f
                    )
                    setBackgroundColor(Color.TRANSPARENT) // Makes the button blend in with the bar
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // Adjust text color
                    gravity = Gravity.CENTER
                    setOnClickListener {
                    }
                }

                // Add a delete button
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
                    setOnClickListener {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Confirmation")
                            .setMessage("Are you sure you want to delete this Collar?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                lifecycleScope.launch {
                                    databaseViewModel.deletePetUsingID(pet.UUID, pet.PetId)
                                }
                                dialog.dismiss()
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                }

                val batteryButton = TextView(requireContext()).apply {
                    text = "N/A"
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    gravity = Gravity.CENTER
                    setPadding(8, 8, 8, 8) // Add some padding to make it look like a button
                    setBackgroundColor(Color.TRANSPARENT)
                    //setBackgroundResource(android.R.drawable.btn_default) // Use button-like background
                    setTextColor(
                       ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    ) // Set text color
                    maxLines = 1 // Limit to one line
                    ellipsize = TextUtils.TruncateAt.END
                    isClickable = false // Disable clicks
                    isFocusable = false // Disable focus
                    tag = "battery-${pet.IMEI}" // Tag to identify this battery button

                }
                /*val dataLeftButton = TextView(requireContext()).apply {
                    text = "5GB/60GB"
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    gravity = Gravity.CENTER
                    setPadding(16, 8, 16, 8) // Add some padding to make it look like a button
                    setBackgroundResource(android.R.drawable.btn_default) // Use button-like background
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    ) // Set text color
                    maxLines = 1 // Limit to one line
                    ellipsize = TextUtils.TruncateAt.END
                    isClickable = false // Disable clicks
                    isFocusable = false // Disable focus
                }*/
                // Add buttons to the row layout
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
                buttonRowLayout.addView(batteryButton)

                //buttonRowLayout.addView(dataLeftButton)
                // Add the row to the container
                parentLayout.addView(buttonRowLayout)
                petsContainer.addView(parentLayout)
            }

            // Add a "+" button at the end
            val addButton = Button(requireContext()).apply {
                text = "+"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                background = ContextCompat.getDrawable(requireContext(), R.drawable.background)
                setOnClickListener {
                    showAddPetDialog()
                }
            }

            // Add the "+" button to the container
            petsContainer.addView(addButton)
        }
    }

    // Function to show the dialog when "+" button is clicked
    private fun showAddPetDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add a New Pet")

        // Create an input layout with two EditTexts (IMEI and Pet Name)
        val inputLayout = LinearLayout(requireContext())
        inputLayout.orientation = LinearLayout.VERTICAL

        val imeiInput = EditText(requireContext()).apply {
            hint = "Enter IMEI"
            //inputType = InputType.TYPE_CLASS_PHONE // Ensures only numeric input for IMEI
        }

        val petNameInput = EditText(requireContext()).apply {
            hint = "Enter Pet Name"
        }

        inputLayout.addView(imeiInput)
        inputLayout.addView(petNameInput)

        builder.setView(inputLayout)

        builder.setPositiveButton("Add") { dialog, _ ->
            val imei = imeiInput.text.toString()
            val petName = petNameInput.text.toString()

            if (imei.isNotEmpty() && petName.isNotEmpty()) {
                // Save the pet to the database
                savePet(imei, petName)
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // Function to save the pet (you can replace this with your database logic)
    private fun savePet(imei: String, petName: String) {
        // Add the logic to save the pet in your database or ViewModel
        val userUuid = uuid ?: return
        val pet = Pets(
            IMEI = imei,
            PetName = petName,
            UUID = userUuid
        ) // Assuming you have a Pet data class with IMEI
        databaseViewModel.AddPet(pet) // Assuming addPet is implemented in your ViewModel
    }

    private fun updateBatteryLevelUI(petIMEI: String?, batteryLevel: Int) {
        try {
            if (petIMEI != null) {
                // Find the battery TextView by its tag
                val batteryTextView = petsContainer.findViewWithTag<TextView>("battery-$petIMEI")
                batteryTextView?.text = "$batteryLevel%"
            }
        } catch (e: Exception) {
            // Handle the exception (e.g., log it)
            Log.e("PETF", "Error updating battery level UI", e)
        }
    }
}