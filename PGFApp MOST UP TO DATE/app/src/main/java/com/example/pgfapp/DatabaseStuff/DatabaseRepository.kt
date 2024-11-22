package com.example.pgfapp.DatabaseStuff

import androidx.lifecycle.LiveData
import com.example.pgfapp.DatabaseStuff.DAOs.BoundsDao
import com.example.pgfapp.DatabaseStuff.DAOs.BoundsPetDao
import com.example.pgfapp.DatabaseStuff.DAOs.PetLocationDao
import com.example.pgfapp.DatabaseStuff.DAOs.PetsDao
import com.example.pgfapp.DatabaseStuff.Entities.Bounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseRepository (
    private val boundsDao: BoundsDao,
    private val boundsPetDao: BoundsPetDao,
    private val petsDao: PetsDao,
    private val petLocationDao: PetLocationDao ){

    suspend fun AddBounds(bounds: Bounds){
        withContext(Dispatchers.IO) {
            boundsDao.AddBounds(bounds)
        }
    }

    suspend fun DeleteBounds(bounds: Bounds){
        withContext(Dispatchers.IO) {
            boundsDao.AddBounds(bounds)
        }
    }

    fun countBoarders(uuid: String): LiveData<Int> {
        return boundsDao.CountBoarders(uuid)
    }
    fun grabBoarderNames(uuid: String): LiveData<List<String>> {
        return boundsDao.GrabBoarderNames(uuid)
    }
    fun grabBorders(uuid: String): LiveData<List<Bounds>> {
        return boundsDao.GrabBorders(uuid)
    }
    fun updateBoundaryActiveStatus(boundaryId: Long, isActive: Boolean) {
        // Call the DAO method to update the boundary status
        boundsDao.updateBoundaryActiveStatus(boundaryId, isActive)
    }
    fun deactivateOtherBoundaries(boundaryId: Long) {
        // Call the DAO method to update the boundary status
        boundsDao.deactivateOtherBoundaries(boundaryId)
    }
}
