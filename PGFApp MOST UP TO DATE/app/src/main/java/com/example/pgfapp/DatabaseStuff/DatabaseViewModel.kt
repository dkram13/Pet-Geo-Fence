package com.example.pgfapp.DatabaseStuff


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.pgfapp.DatabaseStuff.Entities.Bounds
import kotlinx.coroutines.launch

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DatabaseRepository

    init {
        // Get all the DAOs from the database
        val boundsDao = UserDatabase.getDatabase(application).BoundsDao()
        val boundsPetDao = UserDatabase.getDatabase(application).BoundsPetDao()
        val petsDao = UserDatabase.getDatabase(application).PetsDao()
        val petLocationDao = UserDatabase.getDatabase(application).PetLocationDao()

        // Initialize the repository with all the DAOs
        repository = DatabaseRepository(boundsDao, boundsPetDao, petsDao, petLocationDao)
    }

    // Method to call AddBounds from the repository
    fun addBounds(bounds: Bounds) {
        // Using viewModelScope to launch a coroutine to perform the database operation
        viewModelScope.launch {
            repository.AddBounds(bounds)
        }
    }

    fun deleteBounds(bounds: Bounds) {
        // Using viewModelScope to launch a coroutine to perform the database operation
        viewModelScope.launch {
            repository.DeleteBounds(bounds)
        }
    }

    fun countBoarders(uuid: String): LiveData<Int> {
        return repository.countBoarders(uuid)
    }

    fun grabBoarderNames(uuid: String): LiveData<List<String>> {
        return repository.grabBoarderNames(uuid)
    }
    fun grabBorders(uuid: String): LiveData<List<Bounds>> {
        return repository.grabBorders(uuid)
    }
    suspend fun updateIsActive(boundId: Int, isActive: Boolean) {
        repository.updateIsActive(boundId, isActive)
    }
    fun grabActiveBorder(uuid: String): LiveData<List<Bounds>> {
        return repository.grabActiveBorder(uuid)
    }
}