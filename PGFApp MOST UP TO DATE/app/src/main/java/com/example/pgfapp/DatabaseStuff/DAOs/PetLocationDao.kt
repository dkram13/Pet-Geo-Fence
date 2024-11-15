package com.example.pgfapp.DatabaseStuff.DAOs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert
import com.example.pgfapp.DatabaseStuff.Entities.PetLocation

@Dao
interface PetLocationDao {
    @Upsert
    fun AddPetLocation(petLocation: PetLocation)

    @Delete
    fun DeletePetLocation(petLocation: PetLocation)
}