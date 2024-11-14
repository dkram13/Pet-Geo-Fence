package com.example.pgfapp.DatabaseStuff.Entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Pets::class,
            parentColumns = ["PetId"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PetLocation(
    @PrimaryKey(autoGenerate = true) val locationId: Int = 0,
    val petId: Int,               // Foreign key to Pets table
    val location: ArrayList<LatLng>,          // JSON string to hold lat-lon coordinates
    val timestamp: String            // Timestamp for the location entry
    )
