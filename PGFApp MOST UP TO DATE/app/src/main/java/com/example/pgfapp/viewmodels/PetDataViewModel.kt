package com.example.pgfapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PetDataViewModel : ViewModel() {
    private val _batteryLevels = MutableLiveData<List<PetBatteryLevel>>()  // Store battery levels as a list of PetBatteryLevel
    val batteryLevels: LiveData<List<PetBatteryLevel>> = _batteryLevels

    // Update the battery level for a specific pet IMEI
    fun updateBatteryLevel(petIMEI: String, batteryLevel: Int) {
        val currentLevels = _batteryLevels.value ?: emptyList()

        // Update or add the new battery level
        val updatedList = currentLevels.map {
            if (it.imei == petIMEI) {
                it.copy(batteryLevel = batteryLevel)  // Update existing pet's battery level
            } else {
                it  // Keep other pets unchanged
            }
        }.toMutableList()

        // If the pet IMEI is not found, add a new entry
        if (currentLevels.none { it.imei == petIMEI }) {
            updatedList.add(PetBatteryLevel(petIMEI, batteryLevel))
        }

        _batteryLevels.value = updatedList
    }
}

data class PetBatteryLevel(
    val imei: String,
    val batteryLevel: Int
)