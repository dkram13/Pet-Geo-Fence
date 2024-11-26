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
import androidx.lifecycle.lifecycleScope
import com.example.pgfapp.DatabaseStuff.DatabaseViewModel
import com.example.pgfapp.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class petsFragment : Fragment() {
    private lateinit var databaseViewModel: DatabaseViewModel
    private lateinit var petsContainer: LinearLayout
    private var uuid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseViewModel = ViewModelProvider(this)[DatabaseViewModel::class.java]
        uuid = Firebase.auth.currentUser?.uid
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pets, container, false)
        petsContainer = view.findViewById(R.id.pets_container)
        return view
    }

    override fun onResume() {
        super.onResume()
        populatePets()
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
                        1f
                    )
                    setOnClickListener {
                        Toast.makeText(requireContext(), "Editing: ${pet.PetName}", Toast.LENGTH_SHORT).show()
                    }
                }



                // Add buttons to the row layout
                buttonRowLayout.addView(nameButton)


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
                    Toast.makeText(requireContext(), "Add a new pet", Toast.LENGTH_SHORT).show()
                    // Implement navigation to add pet screen
                }
            }

            // Add the "+" button to the container
            petsContainer.addView(addButton)
        }
    }
}