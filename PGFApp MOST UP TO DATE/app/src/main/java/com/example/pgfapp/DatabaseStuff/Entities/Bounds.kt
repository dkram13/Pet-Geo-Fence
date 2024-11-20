package com.example.pgfapp.DatabaseStuff.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.pgfapp.DatabaseStuff.GeoPointsConverter
import com.google.android.gms.maps.model.LatLng

@Entity
data class Bounds(
    @PrimaryKey(autoGenerate = true) val BoundId: Int = 0,
    val UUID: String,             // Unique Identifier (UUID)
    val BoundName: String,           // Name of the boarder
    @TypeConverters(GeoPointsConverter::class) val boarder: ArrayList<LatLng>

    )
