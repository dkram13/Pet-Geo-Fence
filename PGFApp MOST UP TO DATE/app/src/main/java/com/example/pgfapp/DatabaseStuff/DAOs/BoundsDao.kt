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

    @Query("SELECT * FROM Bounds WHERE UUID = :uuid ORDER BY BoundName ASC")
    fun GrabBorders(uuid: String): LiveData<List<Bounds>>

    @Query("UPDATE Bounds SET isActive = :isActive WHERE BoundId = :boundaryId")
    fun updateBoundaryActiveStatus(boundaryId: Long, isActive: Boolean)

    @Query("UPDATE Bounds SET isActive = 0 WHERE BoundId != :boundaryId")
    fun deactivateOtherBoundaries(boundaryId: Long)
}
