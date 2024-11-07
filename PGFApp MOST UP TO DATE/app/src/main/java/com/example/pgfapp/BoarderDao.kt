package com.example.pgfapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert

@Dao
interface BoarderDao {

    @Upsert
    fun AddBoarder(bounds: Bounds)

    @Delete
    fun DeleteBoarder(bounds: Bounds)

}