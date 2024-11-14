package com.example.pgfapp.DatabaseStuff.DAOs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert
import com.example.pgfapp.DatabaseStuff.Entities.Pets

@Dao
interface PetsDao {
    @Upsert
    fun AddPet(pets: Pets)

    @Delete
    fun DeletePet(pets: Pets)

}