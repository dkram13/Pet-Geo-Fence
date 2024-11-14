package com.example.pgfapp.DatabaseStuff.DAOs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert
import com.example.pgfapp.DatabaseStuff.Entities.BoundsPet

@Dao
interface BoundsPetDao {
    @Upsert
    fun AddBoundsPet(boundsPet: BoundsPet)

    @Delete
    fun DeleteBoundsPet(boundsPet: BoundsPet)
}