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
    suspend fun DeleteBounds(bounds: Bounds)

    @Query("SELECT COUNT(*) FROM Bounds WHERE UUID = :uuid")
    fun CountBoarders(uuid: String): LiveData<Int>

    @Query("SELECT BoundName FROM Bounds WHERE UUID = :uuid ORDER BY BoundName ASC")//will need to make a value that gets past that gives the
    fun GrabBoarderNames(uuid: String): LiveData<List<String>>        // primary key number

    @Query("SELECT * FROM Bounds WHERE UUID = :uuid ORDER BY BoundName ASC")
    fun GrabBorders(uuid: String): LiveData<List<Bounds>>

    @Query("UPDATE bounds SET isActive = :isActive WHERE boundId = :boundId")
    suspend fun updateIsActive(boundId: Int, isActive: Boolean): Int

    @Query("SELECT * FROM Bounds WHERE UUID = :uuid and isActive = 1")
    fun grabActiveBorder(uuid: String): LiveData<List<Bounds>>

    @Query("DELETE FROM Bounds WHERE UUID = :uuid and BoundId = :boundId")
    suspend fun deleteBoundUsingID(uuid: String, boundId: Int)
}
