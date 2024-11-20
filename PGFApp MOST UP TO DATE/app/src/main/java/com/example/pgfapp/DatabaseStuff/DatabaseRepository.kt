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

}
