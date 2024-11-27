package com.example.pgfapp.DatabaseStuff

import androidx.lifecycle.LiveData
import com.example.pgfapp.DatabaseStuff.DAOs.BoundsDao
import com.example.pgfapp.DatabaseStuff.DAOs.BoundsPetDao
import com.example.pgfapp.DatabaseStuff.DAOs.PetLocationDao
import com.example.pgfapp.DatabaseStuff.DAOs.PetsDao
import com.example.pgfapp.DatabaseStuff.Entities.Bounds
import com.example.pgfapp.DatabaseStuff.Entities.Pets
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

    fun grabBorders(uuid: String): LiveData<List<Bounds>> {
        return boundsDao.grabBorders(uuid)
    }
    suspend fun updateIsActive(boundId: Int, isActive: Boolean) {
        boundsDao.updateIsActive(boundId, isActive)
    }
    fun grabActiveBorder(uuid: String): LiveData<List<Bounds>> {
        return boundsDao.grabActiveBorder(uuid)
    }
    suspend fun deleteBoundUsingID(uuid: String, boundId: Int) {
        boundsDao.deleteBoundUsingID(uuid, boundId)
    }

    suspend fun AddPet(pets: Pets) {
        withContext(Dispatchers.IO) {
            petsDao.AddPet(pets)
        }
    }

    fun grabPets(uuid: String): LiveData<List<Pets>> {
        return petsDao.grabPets(uuid)
    }

    suspend fun deletePetUsingID(uuid: String, petId: Int) {
        petsDao.deletePetUsingID(uuid, petId)
    }

}
