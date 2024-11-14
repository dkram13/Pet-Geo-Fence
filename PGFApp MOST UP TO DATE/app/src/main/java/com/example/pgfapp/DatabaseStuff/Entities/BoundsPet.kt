package com.example.pgfapp.DatabaseStuff.Entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["petId", "boundId"],  // Composite primary key
    foreignKeys = [
        ForeignKey(
            entity = Pets::class,
            parentColumns = ["PetId"],       // Primary key in Pets table
            childColumns = ["petId"],        // Corresponding foreign key in this table
            onDelete = ForeignKey.CASCADE    // Cascade delete option
        ),
        ForeignKey(
            entity = Bounds::class,
            parentColumns = ["BoundId"],     // Primary key in Bounds table
            childColumns = ["boundId"],      // Corresponding foreign key in this table
            onDelete = ForeignKey.CASCADE    // Cascade delete option
        )
    ]
)
data class BoundsPet(
    val petId: Int,        // Foreign key referencing Pets table
    val boundId: Int       // Foreign key referencing Bounds table
)