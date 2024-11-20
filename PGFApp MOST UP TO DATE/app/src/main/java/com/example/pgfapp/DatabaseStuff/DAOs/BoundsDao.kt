package com.example.pgfapp.DatabaseStuff.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.pgfapp.DatabaseStuff.Entities.Bounds

@Dao
interface BoundsDao {
    @Upsert
    fun AddBounds(bounds: Bounds)

    @Delete
    fun DeleteBounds(bounds: Bounds)

    @Query("SELECT COUNT(*) FROM Bounds WHERE UUID = :uuid")
    fun CountBoarders(uuid: String): LiveData<Int>

    @Query("SELECT BoundName FROM Bounds WHERE UUID = :uuid ORDER BY BoundName ASC")//will need to make a value that gets past that gives the
    fun GrabBoarderNames(uuid: String): LiveData<List<String>>        // primary key number

}
