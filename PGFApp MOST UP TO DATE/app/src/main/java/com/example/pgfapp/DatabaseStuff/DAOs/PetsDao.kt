package com.example.pgfapp.DatabaseStuff.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.pgfapp.DatabaseStuff.Entities.Pets

@Dao
interface PetsDao {
    @Upsert
    fun AddPet(pets: Pets)

    @Delete
    fun DeletePet(pets: Pets)

    @Query("SELECT * FROM Pets WHERE UUID = :uuid ORDER BY PetName ASC")
    fun grabPets(uuid: String): LiveData<List<Pets>>

    @Query("SELECT * FROM Pets WHERE UUID = :uuid ORDER BY PetName ASC")
    fun grabPetsSync(uuid: String): List<Pets> // New synchronous method
    @Query("DELETE FROM Pets WHERE UUID = :uuid and PetId = :petId")
    suspend fun deletePetUsingID(uuid: String, petId: Int)
}