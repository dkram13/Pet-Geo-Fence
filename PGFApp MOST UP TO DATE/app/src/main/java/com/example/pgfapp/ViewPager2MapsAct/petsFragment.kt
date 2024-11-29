package com.example.pgfapp.ViewPager2MapsAct

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
                // Create a row for each pet
                val buttonRowLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                }

                // Add a button for the pet name
                val nameButton = Button(requireContext()).apply {
                    text = pet.PetName
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        2f
                    )
                    maxLines = 1 // Limit to one line
                    ellipsize = TextUtils.TruncateAt.END
                    setOnClickListener {
                        //Toast.makeText(requireContext(), "Editing: ${pet.PetName}", Toast.LENGTH_SHORT).show()
                    }
                }
                val deleteButton = Button(requireContext()).apply {
                    text = "delete"
                    layoutParams = LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                    setOnClickListener {
                        Toast.makeText(requireContext(), "Deleting: ${pet.PetName}", Toast.LENGTH_SHORT).show()
                        lifecycleScope.launch {
                            databaseViewModel.deletePetUsingID(pet.UUID, pet.PetId)
                        }
                    }
                }

                val batteryButton = TextView(requireContext()).apply {
                    text = "N/A"
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

                buttonRowLayout.addView(nameButton)
                buttonRowLayout.addView(deleteButton)
                buttonRowLayout.addView(batteryButton)
                //buttonRowLayout.addView(dataLeftButton)
                // Add the row to the container
                petsContainer.addView(buttonRowLayout)
            }

            // Add a "+" button at the end
            val addButton = Button(requireContext()).apply {
                text = "+"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
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